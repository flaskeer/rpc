package com.asterisk.rpc.sample.server;

import com.asterisk.rpc.sample.api.HelloService;
import com.asterisk.rpc.sample.api.Person;
import com.asterisk.rpc.server.RpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@RpcService(value = HelloService.class, version = "sample.hello2")
public class HelloServiceImpl2 implements HelloService {

    @Autowired
    private Environment environment;

    @Override
    public String hello(String name) {
        return "你好! " + name + " " + Arrays.toString(environment.getActiveProfiles());
    }

    @Override
    public String hello(Person person) {
        return "你好! " + person.getFirstName() + " " + person.getLastName();
    }
}
