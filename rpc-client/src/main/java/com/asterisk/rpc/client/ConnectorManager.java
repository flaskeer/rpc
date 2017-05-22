package com.asterisk.rpc.client;

import com.google.common.collect.Sets;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class ConnectorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorManager.class);

    private ReentrantLock lock = new ReentrantLock();

    private Condition connected = lock.newCondition();

    private long connectTimeout = 6000;

    private AtomicInteger roundRobin = new AtomicInteger(0);

    private boolean isRunning = true;

    private static ConnectorManager instance;

    private Executor executor = Executors.newFixedThreadPool(16);

    private EventLoopGroup group = new NioEventLoopGroup(4);

    private CopyOnWriteArrayList<RpcSender> rpcSenders = new CopyOnWriteArrayList<>();

    private Map<InetSocketAddress,RpcSender> connectServerNodes = new ConcurrentHashMap<>();

    private ConnectorManager() {
    }

    public static ConnectorManager getInstance() {
        if (instance == null) {
            synchronized (ConnectorManager.class) {
                if (instance == null) {
                    instance = new ConnectorManager();
                }
            }
        }
        return instance;
    }

    public void reConnect(RpcSender rpcSender, SocketAddress socketAddress) {
        if (rpcSender != null) {
            rpcSenders.remove(rpcSender);
            connectServerNodes.remove(rpcSender.getRemoterPeer());
        }
        connectServerNode((InetSocketAddress) socketAddress);
    }

    public void updateConnectedServer(List<String> allServerAddress) {
        if (allServerAddress != null) {
            if (allServerAddress.size() > 0) {
                HashSet<InetSocketAddress> newAllServerAddress = Sets.newHashSet();
                allServerAddress.forEach(serviceAddress -> {
                    String[] array = serviceAddress.split(":");
                    if (array.length == 2) {
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        InetSocketAddress inetSocketAddress = new InetSocketAddress(host,port);
                        newAllServerAddress.add(inetSocketAddress);
                    }
                });
                newAllServerAddress.forEach(newServiceAddress -> {
                    if (!connectServerNodes.keySet().contains(newServiceAddress)) {
                        connectServerNode(newServiceAddress);
                    }
                });
            }

        } else {
            LOGGER.error("no available server node.All server nodes are down..");
            rpcSenders.forEach(rpcSender -> {
                SocketAddress remoterPeer = rpcSender.getRemoterPeer();
                RpcSender sender = connectServerNodes.get(remoterPeer);
                sender.close();
                connectServerNodes.remove(rpcSender);
            });
            rpcSenders.clear();
        }
    }

    private void connectServerNode(InetSocketAddress socketAddress) {
        executor.execute(() -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcSender());
            ChannelFuture channelFuture = bootstrap.connect(socketAddress);
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (channelFuture.isSuccess()) {
                    LOGGER.debug("Successfully connect to remote server. remote peer = " + socketAddress);
                    RpcSender rpcSender = channelFuture.channel().pipeline().get(RpcSender.class);
                    addHandler(rpcSender);
                }
            });
        });
    }

    private void addHandler(RpcSender rpcSender) {
        rpcSenders.add(rpcSender);
        InetSocketAddress remoteAddress = (InetSocketAddress) rpcSender.getChannel().remoteAddress();
        connectServerNodes.put(remoteAddress,rpcSender);
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(this.connectTimeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public RpcSender chooseHandler() {
        CopyOnWriteArrayList<RpcSender> handlers = (CopyOnWriteArrayList<RpcSender>) this.rpcSenders.clone();
        int size = handlers.size();
        while (isRunning && size <= 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    handlers = (CopyOnWriteArrayList<RpcSender>) this.rpcSenders.clone();
                    size = handlers.size();
                }
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(),e);
                throw new RuntimeException("can not connect any servers",e);
            }
        }
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return handlers.get(index);
    }

}
