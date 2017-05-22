package com.asterisk.rpc.akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
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

    private final ActorRef child = getContext().actorOf(Props.empty(),"target");

    private ActorRef lastSender = getContext().system().deadLetters();

    public MyUnTypedActor() {
        getContext().watch(child);
    }

    @Override
    public Receive createReceive() {


        return receiveBuilder()
                .matchEquals(Greeter.Msg.DONE, s -> {
                    LOGGER.info("Received String message:{}", s);
                    getContext().stop(self());
                })
                .matchEquals("hehe", s -> {
                    System.out.println("hello greeter, your message is:" + s);
                    context().become(receiveBuilder().build().onMessage(),true);
                    System.out.println("view is: " + context().children());
                })
                .matchEquals("kill",s -> {
                    getContext().stop(child);
                    lastSender = getSender();
                })
                .match(Terminated.class, t -> t.actor().equals(child),t -> {
                    lastSender.tell("finished",getSelf());
                })
                .build();
    }

    @Override
    public void preStart() throws Exception {
        ActorRef greeter = getContext().actorOf(Props.create(Greeter.class), "greeter");
        LOGGER.info("greeter path:{}, sender is:{}", greeter.path(), self());
        Inbox inbox = Inbox.create(getContext().system());
        inbox.send(greeter, "hello,I'm inbox");
        inbox.send(greeter, Greeter.Msg.GREET);
    }
}
