package com.asterisk.rpc.sample.client;

import com.asterisk.rpc.client.RpcClient;
import com.asterisk.rpc.sample.api.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcClient rpcProxy = context.getBean(RpcClient.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("World");
        System.out.println(result);

        HelloService helloService2 = rpcProxy.create(HelloService.class, "sample.hello2");
        String result2 = helloService2.hello("世界");
        System.out.println(result2);

        System.exit(0);
    }
}
