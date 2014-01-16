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
            executorService = Executors.newFixedThreadPool(1);
            MoreExecutors.addDelayedShutdownHook(executorService, 120, TimeUnit.SECONDS);
            SquirrelSingletonProvider.getInstance().register(ExecutorService.class, executorService);
        }
        return executorService;
    }
    
    public static ScheduledExecutorService getScheduler() {
        ScheduledExecutorService scheduler = SquirrelSingletonProvider.getInstance().get(ScheduledExecutorService.class);
        if(scheduler==null) {
            // create default scheduler
            scheduler = Executors.newScheduledThreadPool(1);
            MoreExecutors.addDelayedShutdownHook(scheduler, 120, TimeUnit.SECONDS);
            SquirrelSingletonProvider.getInstance().register(ScheduledExecutorService.class, scheduler);
        }
        return scheduler;
    }
    
}
