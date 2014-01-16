package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.squirrelframework.foundation.component.SquirrelConfiguration;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.AnonymousAction;
import org.squirrelframework.foundation.fsm.ImmutableTimedState;
import org.squirrelframework.foundation.fsm.MutableTimedState;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.collect.Maps;

public class TimedStateImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends StateImpl<T, S, E, C> 
    implements ImmutableTimedState<T, S, E, C>, MutableTimedState<T, S, E, C> {
    
    private Integer timeInterval;
    
    private Integer initialDelay;
    
    private E autoFireEvent;
    
    private C autoFireContext;
    
    private final ScheduledExecutorService scheduler = SquirrelConfiguration.getScheduler();
    
    // TODO-hhe: not a good way to store runtime data in state definition
    private transient final Map<String, Future<?>> futures = Maps.newConcurrentMap();
    
    private Action<T, S, E, C> lastEntryAction = new AnonymousAction<T, S, E, C>() {
        @Override
        public void execute(S from, S to, E event, C context, final T stateMachine) {
            Runnable scheduledTask = new Runnable() {
                @Override
                public void run() {
                    stateMachine.fire(autoFireEvent, autoFireContext);
                }
            };
            if(timeInterval==null) {
                scheduler.schedule(scheduledTask, initialDelay, TimeUnit.MILLISECONDS);
            } else {
                Future<?> future = scheduler.scheduleAtFixedRate(scheduledTask, 
                        initialDelay, timeInterval, TimeUnit.MILLISECONDS);
                String key = stateMachine.getIdentifier()+'@'+getStateId();
                futures.put(key, future);
            }
        }
        
        @Override
        public int weight() {
            return Action.LAST_WEIGHT;
        }

        @Override
        public String name() {
            return "_TIMED_STATE_ENTRY_ACTION";
        }
    };
    
    private Action<T, S, E, C> firstExitAction = new AnonymousAction<T, S, E, C>() {
        @Override
        public void execute(S from, S to, E event, C context, T stateMachine) {
            if(timeInterval==null) return;
            String key = stateMachine.getIdentifier()+'@'+getStateId();
            Future<?> future = futures.remove(key);
            if(future!=null) {
                future.cancel(false);
            }
        }
        
        @Override
        public int weight() {
            return Action.FIRST_WEIGHT;
        }

        @Override
        public String name() {
            return "_TIMED_STATE_EXIT_ACTION";
        }
    };

    TimedStateImpl(S stateId) {
        super(stateId);
        entryActions.add(lastEntryAction);
        exitActions.add(firstExitAction);
    }

    @Override
    public Integer getTimeInterval() {
        return timeInterval;
    }

    @Override
    public E getAutoFireEvent() {
        return autoFireEvent;
    }

    @Override
    public void setTimeInterval(Integer timeInterval) {
        this.timeInterval = timeInterval;
    }

    @Override
    public void setAutoFireEvent(E event) {
        this.autoFireEvent = event;
    }

    @Override
    public void setInitialDelay(Integer initialDelay) {
        this.initialDelay = initialDelay;
    }

    @Override
    public Integer getInitialDelay() {
        return initialDelay;
    }

    @Override
    public void setAutoFireContext(C context) {
        this.autoFireContext = context;
    }

    @Override
    public C getAutoFireContext() {
        return autoFireContext;
    }

}
