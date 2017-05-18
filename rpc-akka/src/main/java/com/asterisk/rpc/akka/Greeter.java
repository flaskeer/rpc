package com.asterisk.rpc.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Inbox;
import akka.actor.Props;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.Futures;
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
public class Greeter extends AbstractActor {

    private final LoggingAdapter LOGGER = Logging.getLogger(getContext().getSystem(),this);
    enum Msg {
        GREET, DONE
    }

    @Override
    public Receive createReceive() {
        ActorRef myUnTypedActor = getContext().actorOf(Props.create(MyUnTypedActor.class));
        return receiveBuilder()
                .matchEquals(Msg.GREET, m -> {
                    System.out.println("Hello world");
                    LOGGER.info("sender is:{} ,receiver is:{}",self(),sender());
                    myUnTypedActor.tell("hehe",self());
//                    sender().tell(Msg.DONE, self());
                })
                .matchEquals("hello,I'm inbox", msg -> {
                    System.out.println("ok I have received .." + msg);
                    myUnTypedActor.tell("hehe",self());

                }).matchAny(msg -> System.out.println("unknown type message? who is you?" + msg)
        )
                .build();
    }
}
