package com.asterisk.rpc.common.flow;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 *  流控module
 */
public interface FlowManager {

    void acquire() throws InterruptedException;

    void acquire(int permits) throws InterruptedException;

    boolean acquire(int permits,int timeout);

    void release();

    void release(int permits);

    int getAvailable();

    void setThreshold(int newThreshold);

    int getThreshold();

}
