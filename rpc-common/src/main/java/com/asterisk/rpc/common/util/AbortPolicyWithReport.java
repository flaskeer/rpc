package com.asterisk.rpc.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {

    private Logger LOGGER = LoggerFactory.getLogger(AbortPolicyWithReport.class);

    private final String threadName;

    public AbortPolicyWithReport(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Thread pool is EXHAUSTED !"
                                  + " Thread Name %s,PoolSize :%d (active: %d, max: %d,largest :%d),Task: %d (completed: %d),Executor status:(isShutdown:%s,isTerminated:%s,isTerminating:%s) !",
                        threadName,e.getPoolSize(),e.getActiveCount(),e.getCorePoolSize(),e.getMaximumPoolSize(),
                     e.getLargestPoolSize(),e.getTaskCount(),e.getCompletedTaskCount(),e.isShutdown(),
                   e.isTerminated(),e.isTerminating());
        LOGGER.warn(msg);
        throw new RejectedExecutionException(msg);
    }
}
