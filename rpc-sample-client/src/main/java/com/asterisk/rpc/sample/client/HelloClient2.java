package com.asterisk.rpc.sample.client;

import com.asterisk.rpc.client.RpcClient;
import com.asterisk.rpc.sample.api.HelloService;
import com.asterisk.rpc.sample.api.Person;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient2 {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcClient rpcProxy = context.getBean(RpcClient.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello(new Person("hello", "world"));
        System.out.println(result);

        System.exit(0);
    }
}
