package com.asterisk.rpc.client;

import com.asterisk.rpc.client.proxy.RpcProxy;
import com.asterisk.rpc.common.bean.RpcRequest;
import com.asterisk.rpc.common.util.StringUtil;
import com.asterisk.rpc.registry.ServiceDiscovery;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String serviceAddress;

    private ServiceDiscovery serviceDiscovery;

    private static ExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10));


    public RpcClient(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
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
                new RpcProxy<>(interfaceClass));
    }

    public static void submit(Runnable task) {
        executor.submit(task);
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
