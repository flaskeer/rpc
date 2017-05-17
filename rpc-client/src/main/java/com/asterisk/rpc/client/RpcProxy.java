package com.asterisk.rpc.client;

import com.asterisk.rpc.common.bean.RpcRequest;
import com.asterisk.rpc.common.bean.RpcResponse;
import com.asterisk.rpc.common.util.StringUtil;
import com.asterisk.rpc.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;


public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serviceAddress;

    private ServiceDiscovery serviceDiscovery;


    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest request = request(serviceVersion, method, args);
                    String[] array = discovery(interfaceClass, serviceVersion);

                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    long time = System.currentTimeMillis();
                    ClientInitializer clientInitializer = new ClientInitializer(host,port);
                    RpcResponse response = null;
                    try {
                       response = clientInitializer.send(request);
                    } catch (Exception e) {
                        clientInitializer.close();
                    }
                    LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
                    if (response == null) {
                        throw new RuntimeException("response is null");
                    }
                    // 返回 RPC 响应结果
                    if (response.hasException()) {
                        throw response.getException();
                    } else {
                        return response.getResult();
                    }
                }
        );
    }

    private String[] discovery(Class<?> interfaceClass, String serviceVersion) {
        // 获取 RPC 服务地址
        if (serviceDiscovery != null) {
            String serviceName = interfaceClass.getName();
            if (StringUtil.isNotEmpty(serviceVersion)) {
                serviceName += "-" + serviceVersion;
            }
            serviceAddress = serviceDiscovery.discover(serviceName);
            LOGGER.debug("discover service: {} => {}", serviceName, serviceAddress);
        }
        if (StringUtil.isEmpty(serviceAddress)) {
            throw new RuntimeException("server address is empty");
        }
        // 从 RPC 服务地址中解析主机名与端口号
        return StringUtil.split(serviceAddress, ":");
    }

    private RpcRequest request(String serviceVersion, Method method, Object[] args) {
        // 创建 RPC 请求对象并设置请求属性
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setServiceVersion(serviceVersion);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        return request;
    }
}
