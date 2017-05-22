package com.asterisk.rpc.client;

import com.asterisk.rpc.common.bean.RpcRequest;
import com.asterisk.rpc.common.bean.RpcResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
@Slf4j
public class RpcSender extends SimpleChannelInboundHandler<RpcResponse> {


    private Map<String, RpcResponse> responseMap = new ConcurrentHashMap<>();

    private Map<String, Thread> waiterMap = new ConcurrentHashMap<>();

    private SocketAddress remoterPeer;

    private Channel channel;


    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemoterPeer() {
        return remoterPeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client channel is ready...");
        super.channelActive(ctx);
        this.remoterPeer = channel.remoteAddress();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        responseMap.put(msg.getRequestId(), msg);
        Thread thread = waiterMap.remove(msg.getRequestId());
        synchronized (thread) {
            thread.notify();
        }
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RpcResponse send(RpcRequest request, Channel channel) throws InterruptedException {
        String id = request.getRequestId();
        Thread thread = Thread.currentThread();
        waiterMap.put(id, thread);
        channel.writeAndFlush(request);
        while (!responseMap.containsKey(id)) {
            synchronized (thread) {
                thread.wait();
            }
        }
        waiterMap.remove(id);
        return responseMap.remove(id);
    }

//    public ListenableFuture asyncSend(RpcRequest request) {
//        RpcClient.submit(() -> {
//            channel.writeAndFlush(request);
//        });
//    }
}
