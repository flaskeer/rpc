package com.asterisk.rpc.akka.listener;

import akka.actor.AbstractLoggingActor;
import akka.actor.ReceiveTimeout;
import akka.event.LoggingReceive;
import scala.concurrent.duration.Duration;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class LoggingListener extends AbstractLoggingActor {

    @Override
    public void preStart() throws Exception {
        getContext().setReceiveTimeout(Duration.create("15 seconds"));
    }

    @Override
    public Receive createReceive() {
        return LoggingReceive.create(receiveBuilder()
                .match(WorkerApi.Progress.class, progress -> {
                    log().info("Current progress:{} %s", progress.percent);
                    if (progress.percent >= 100.0) {
                        getContext().system().terminate();
                    }
                })
                .matchEquals(ReceiveTimeout.getInstance(), x -> {
                    log().error("Shutting down due to unavailable service");
                    getContext().system().terminate();
                })
                .build(), getContext());
    }
}
