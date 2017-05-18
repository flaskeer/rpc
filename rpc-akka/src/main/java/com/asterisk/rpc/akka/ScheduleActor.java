package com.asterisk.rpc.akka;

import akka.actor.AbstractActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class ScheduleActor extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        getContext().getSystem().scheduler().scheduleOnce(Duration.create(500, TimeUnit.MILLISECONDS),
                getSelf(),"tick",getContext().dispatcher(),null);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("tick",message -> {
                    System.out.println("receive message :" + message);
                    getContext().getSystem().scheduler().scheduleOnce(
                            Duration.create(1,TimeUnit.SECONDS),
                            getSelf(),"tick",getContext().dispatcher(),null);
                })
                .matchEquals("restart",message -> {
                    throw new ArithmeticException();
                })
                .build();
    }
}
