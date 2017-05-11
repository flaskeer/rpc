package com.asterisk.rpc.sample.server;

import com.asterisk.rpc.sample.api.HelloService;
import com.asterisk.rpc.sample.api.Person;
import com.asterisk.rpc.server.RpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Autowired
    public Environment environment;

    @Override
    public String hello(String name) {
        return "Hello! " + name + " " + Arrays.toString(environment.getDefaultProfiles());
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
