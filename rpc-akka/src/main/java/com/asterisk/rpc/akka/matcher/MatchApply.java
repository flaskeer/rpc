package com.asterisk.rpc.akka.matcher;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
@FunctionalInterface
public interface MatchApply<T> {

    void apply(T t);

}
