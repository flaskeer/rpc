package com.asterisk.rpc.client;

import com.asterisk.rpc.common.bean.RpcRequest;
import com.asterisk.rpc.common.bean.RpcResponse;
import com.asterisk.rpc.common.codec.RpcDecoder;
import com.asterisk.rpc.common.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
@Slf4j
public class ClientInitializer {

    private Bootstrap bootstrap;

    private ChannelFuture future;

    private boolean isInit = false;

    private boolean isClosed = false;

    private RpcSender rpcSender = new RpcSender();


    private void init(String host, int port) {
        if (isInit) {
            throw new RuntimeException("client is already started ...");
        }
        EventLoopGroup group = new NioEventLoopGroup(2);
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcEncoder(RpcRequest.class))
                                         .addLast(new RpcDecoder(RpcResponse.class))
                                         .addLast(rpcSender);
                        }
                    });
            future = bootstrap.connect(host, port).sync();
            isInit = true;
        } catch (Exception e) {
            isClosed = true;
            log.error("start error",e);
        } finally {
            if (isClosed) {
                group.shutdownGracefully();
            }
        }
    }


    public ClientInitializer(String host, int port) {
        init(host, port);
    }

    public void close() {
        if (isClosed) {
            return;
        }
        try {
            future.channel().close();
        } finally {
            bootstrap.group().shutdownGracefully();
        }
        isClosed = true;
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException {
        if (isClosed || !isInit) {
            throw new RuntimeException("client has been closed");
        }
        return rpcSender.send(request,future.channel());
    }


}
