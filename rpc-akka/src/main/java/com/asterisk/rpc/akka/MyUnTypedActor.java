package com.asterisk.rpc.akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class MyUnTypedActor extends AbstractActor {

    private final LoggingAdapter LOGGER = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(Greeter.Msg.DONE, s -> {
                    LOGGER.info("Received String message:{}", s);
                    getContext().stop(self());
                })
                .matchEquals("hehe", s -> {
                    System.out.println("hello greeter, your message is:" + s);
                })
                .build();
    }

    @Override
    public void preStart() throws Exception {
        ActorSystem system = ActorSystem.create("asterisk-rpc");
        ActorRef greeter = getContext().actorOf(Props.create(Greeter.class), "greeter");
        LOGGER.info("greeter path:{}, sender is:{}", greeter.path(), self());
        Inbox inbox = Inbox.create(system);
        inbox.send(greeter, "hello,I'm inbox");
        Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(greeter, Greeter.Msg.GREET, timeout);
    }
}
