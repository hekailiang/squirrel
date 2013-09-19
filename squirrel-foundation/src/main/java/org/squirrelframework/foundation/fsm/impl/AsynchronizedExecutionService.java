package org.squirrelframework.foundation.fsm.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.util.concurrent.MoreExecutors;

public class AsynchronizedExecutionService<T extends StateMachine<T, S, E, C>, S, E, C>
        extends AbstractExecutionService<T, S, E, C> {

    private ExecutorService executorService;

    public AsynchronizedExecutionService() {
        executorService = Executors.newFixedThreadPool(1);
        MoreExecutors.addDelayedShutdownHook(executorService, 120,
                TimeUnit.SECONDS);
    }

    @Override
    public void execute() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AsynchronizedExecutionService.super.execute();
            }
        });
    }

}
