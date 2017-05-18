package com.asterisk.rpc.akka;

import akka.actor.AbstractActor;
import akka.actor.Inbox;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.Future;

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
        Future<String> future = Futures.successful("foo");
        return receiveBuilder()
                .matchEquals(Msg.GREET, m -> {
                    System.out.println("Hello world");
                    LOGGER.info("sender is:{} ,receiver is:{}",self(),sender());
                    sender().tell("hehe",self());
//                    sender().tell(Msg.DONE, self());
                })
                .matchEquals("hello,I'm inbox", msg -> {
                    System.out.println("ok I have received .." + msg);
                    sender().tell("hehe",self());

                }).matchAny(msg -> System.out.println("unknown type message? who is you?" + msg)
        )
                .build();
    }
}
