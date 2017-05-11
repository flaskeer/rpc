package com.asterisk.rpc.server;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

    private AtomicInteger mThreadNum;

    private String mPrefix;

    private boolean mDamon;

    private ThreadGroup mGroup;

    public NamedThreadFactory(String prefix,boolean daemon) {
        this.mThreadNum = new AtomicInteger(1);
        this.mPrefix = prefix + "-thread-";
        this.mDamon = daemon;
        SecurityManager manager = System.getSecurityManager();
        this.mGroup = manager == null ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup();
    }

    public NamedThreadFactory(String mPrefix) {
        this(mPrefix,false);
    }

    public ThreadGroup getThreadGroup() {
        return mGroup;
    }

    public NamedThreadFactory() {
        this("pool-" + POOL_SEQ.getAndIncrement(),true);
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = this.mPrefix + this.mThreadNum.getAndIncrement();
        Thread ret = new Thread(this.mGroup,r,name,0L);
        ret.setDaemon(mDamon);
        return ret;
    }
}
