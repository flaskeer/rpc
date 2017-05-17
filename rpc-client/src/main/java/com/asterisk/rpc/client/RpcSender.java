package com.asterisk.rpc.client;

import com.asterisk.rpc.common.bean.RpcRequest;
import com.asterisk.rpc.common.bean.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client channel is ready...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        responseMap.put(msg.getRequestId(), msg);
        Thread thread = waiterMap.remove(msg.getRequestId());
        synchronized (thread) {
            thread.notify();
        }
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
}
