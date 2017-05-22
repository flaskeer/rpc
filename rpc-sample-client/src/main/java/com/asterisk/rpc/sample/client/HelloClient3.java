package com.asterisk.rpc.sample.client;

import com.asterisk.rpc.client.RpcClient;
import com.asterisk.rpc.sample.api.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient3 {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcClient rpcProxy = context.getBean(RpcClient.class);

        int loopCount = 100;

        long start = System.currentTimeMillis();

        HelloService helloService = rpcProxy.create(HelloService.class);
        for (int i = 0; i < loopCount; i++) {
            String result = helloService.hello("World");
            System.out.println(result);
        }

        long time = System.currentTimeMillis() - start;
        System.out.println("loop: " + loopCount);
        System.out.println("time: " + time + "ms");
        System.out.println("tps: " + (double) loopCount / ((double) time / 1000));

        System.exit(0);
    }
}
