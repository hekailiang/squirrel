package org.squirrelframework.foundation.component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.MoreExecutors;

public class SquirrelConfiguration {
    
    public static ExecutorService getExecutor() {
        ExecutorService executorService = SquirrelSingletonProvider.getInstance().get(ExecutorService.class);
        if(executorService==null) {
            // create default executor
            executorService = registerNewExecutorService(1, 120, TimeUnit.SECONDS);
        }
        return executorService;
    }
    
    public static ExecutorService registerNewExecutorService(final int threadNum, 
            final long terminationTimeout, final TimeUnit timeUnit) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        MoreExecutors.addDelayedShutdownHook(executorService, terminationTimeout, timeUnit);
        SquirrelSingletonProvider.getInstance().register(ExecutorService.class, executorService);
        return executorService;
    }
    
    public static ScheduledExecutorService getScheduler() {
        ScheduledExecutorService scheduler = SquirrelSingletonProvider.getInstance().get(ScheduledExecutorService.class);
        if(scheduler==null) {
            // create default scheduler
            scheduler = registerNewSchedulerService(1, 120, TimeUnit.SECONDS);
        }
        return scheduler;
    }
    
    public static ScheduledExecutorService registerNewSchedulerService(final int threadNum, 
            final long terminationTimeout, final TimeUnit timeUnit) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(threadNum);
        MoreExecutors.addDelayedShutdownHook(scheduler, terminationTimeout, timeUnit);
        SquirrelSingletonProvider.getInstance().register(ScheduledExecutorService.class, scheduler);
        return scheduler;
    }
    
}
