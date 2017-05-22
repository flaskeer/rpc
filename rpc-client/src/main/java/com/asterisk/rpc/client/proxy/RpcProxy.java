package com.asterisk.rpc.client.proxy;

import com.asterisk.rpc.client.ConnectorManager;
import com.asterisk.rpc.client.RpcSender;
import com.asterisk.rpc.common.bean.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class RpcProxy<T> implements InvocationHandler{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private Class<T> clazz;

    public RpcProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ",with InvocationHandler " + this;
            }
        } else {
            throw new IllegalStateException(String.valueOf(method));
        }
        RpcRequest request = request(method, args);
        RpcSender rpcSender = ConnectorManager.getInstance().chooseHandler();
        return rpcSender.send(request,rpcSender.getChannel());
    }


    private RpcRequest request(Method method, Object[] args) {
        // 创建 RPC 请求对象并设置请求属性
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        return request;
    }
}
