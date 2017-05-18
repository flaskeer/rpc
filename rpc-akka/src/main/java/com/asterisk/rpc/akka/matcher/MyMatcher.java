package com.asterisk.rpc.akka.matcher;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class MyMatcher {

    public <P> MyMatcher matchEquals(final P object,MatchApply<P> matchApply) {
        matchApply.apply(object);
        return this;
    }



}
