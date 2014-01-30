package org.squirrelframework.foundation.fsm;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.squirrelframework.foundation.fsm.annotation.OnActionExecException;
import org.squirrelframework.foundation.fsm.annotation.OnAfterActionExecuted;
import org.squirrelframework.foundation.fsm.annotation.OnBeforeActionExecuted;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionBegin;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionDecline;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionEnd;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

public class StateMachineStatModel {
    
    private final String name;
    
    private final ConcurrentMap<String, Stopwatch> transitionWatches = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> transitionInvokeTimes = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> transitionFailedTimes = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> transitionDeclinedTimes = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> transitionElapsedMillis = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> maxTransitionConsumedTime = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> minTransitionConsumedTime = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, Stopwatch> actionWatches = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> actionInvokeTimes = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> actionFailedTimes = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> actionElapsedMillis = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> maxActionConsumedTime = Maps.newConcurrentMap();
    
    private final ConcurrentMap<String, AtomicLong> minActionConsumedTime = Maps.newConcurrentMap();
    
    public StateMachineStatModel(String name) {
        this.name = name;
    }
    
    public String getTransitionKey(Object sourceState, Object targetState, Object event, Object context) {
        return sourceState+ "--{"+event+", "+context+"}->"+targetState;
    }
    
    @OnTransitionBegin
    public void onTransitionBegin(StateMachine<?,?,?,?> fsm) {
        transitionWatches.put(fsm.getIdentifier(), new Stopwatch().start());
    }
    
    @OnTransitionEnd
    public void onTransitionEnd(Object sourceState, Object targetState, 
            Object event, Object context, StateMachine<?,?,?,?> fsm) {
        String tKey = getTransitionKey(sourceState, targetState, event, context);
        long delta = transitionWatches.get(fsm.getIdentifier()).elapsedMillis();
        transitionElapsedMillis.putIfAbsent(tKey, new AtomicLong(0));
        transitionElapsedMillis.get(tKey).addAndGet(delta);
        transitionInvokeTimes.putIfAbsent(tKey, new AtomicLong(0));
        transitionInvokeTimes.get(tKey).incrementAndGet();
        synchronized (this) {
            if(maxTransitionConsumedTime.get(tKey)==null || 
                    delta>maxTransitionConsumedTime.get(tKey).get()) {
                maxTransitionConsumedTime.put(tKey, new AtomicLong(delta));
            }
            if(minTransitionConsumedTime.get(tKey)==null || 
                    delta<minTransitionConsumedTime.get(tKey).get()) {
                minTransitionConsumedTime.put(tKey, new AtomicLong(delta));
            }
        }
    }
    
    @OnTransitionException
    public void onTransitionException(Object sourceState, Object targetState, Object event, Object context) {
        String tKey = getTransitionKey(sourceState, targetState, event, context);
        transitionFailedTimes.putIfAbsent(tKey, new AtomicLong(0));
        transitionFailedTimes.get(tKey).incrementAndGet();
    }
    
    @OnTransitionDecline
    public void onTransitionDeclined(Object sourceState, Object event, Object context) {
        String tKey = getTransitionKey(sourceState, null, event, context);
        transitionDeclinedTimes.putIfAbsent(tKey, new AtomicLong(0));
        transitionDeclinedTimes.get(tKey).incrementAndGet();
    }
    
    @OnBeforeActionExecuted
    public void onBeforeActionExecuted(StateMachine<?,?,?,?> fsm, Action<?, ?, ?,?> action) {
        actionWatches.put(fsm.getIdentifier(), new Stopwatch().start());
    }
    
    @OnAfterActionExecuted
    public void onAfterActionExecuted(StateMachine<?,?,?,?> fsm, Action<?, ?, ?,?> action) {
        String aKey = action.toString();
        long delta = actionWatches.get(fsm.getIdentifier()).elapsedMillis();
        actionElapsedMillis.putIfAbsent(aKey, new AtomicLong(0));
        actionElapsedMillis.get(aKey).addAndGet(delta);
        actionInvokeTimes.putIfAbsent(aKey, new AtomicLong(0));
        actionInvokeTimes.get(aKey).incrementAndGet();
        synchronized (this) {
            if(maxActionConsumedTime.get(aKey)==null || 
                    delta>maxActionConsumedTime.get(aKey).get()) {
                maxActionConsumedTime.put(aKey, new AtomicLong(delta));
            }
            if(minActionConsumedTime.get(aKey)==null || 
                    delta<minActionConsumedTime.get(aKey).get()) {
                minActionConsumedTime.put(aKey, new AtomicLong(delta));
            }
        }
    }
    
    @OnActionExecException
    public void onActionExecException(Action<?, ?, ?,?> action) {
        String aKey = action.toString();
        actionFailedTimes.putIfAbsent(aKey, new AtomicLong(0));
        actionFailedTimes.get(aKey).incrementAndGet();
    }
    
    private long getTotal(ConcurrentMap<String, AtomicLong> data) {
        long result = 0;
        for(AtomicLong value : data.values()) {
            result += value.get();
        }
        return result;
    }
    
    public String getStatInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("========================== ");
        builder.append(name);
        builder.append(" ==========================\n");
        
        long totalTransitionInvokedTimes = getTotal(transitionInvokeTimes);
        long totalTransitionFailedTimes = getTotal(transitionFailedTimes);
        long totalTransitionDeclinedTimes = getTotal(transitionDeclinedTimes);
        long totalTransitionConsumedTime = getTotal(transitionElapsedMillis);
        float averageTranstionConsumedTime = 
                totalTransitionConsumedTime / (totalTransitionInvokedTimes+Float.MIN_VALUE);
        
        long totalActionInvokedTimes = getTotal(actionInvokeTimes);
        long totalActionFailedTimes = getTotal(actionFailedTimes);
        long totalActionConsumedTime = getTotal(actionElapsedMillis);
        float averageActionConsumedTime = 
                totalActionConsumedTime / (totalActionInvokedTimes+Float.MIN_VALUE);
        
        builder.append("Total Transition Invoked: ").append(totalTransitionInvokedTimes).append("\n");
        builder.append("Total Transition Failed: ").append(totalTransitionFailedTimes).append("\n");
        builder.append("Total Transition Declained: ").append(totalTransitionDeclinedTimes).append("\n");
        builder.append("Average Transition Comsumed: ").append(String.format("%.2fms", averageTranstionConsumedTime)).append("\n");
        
        builder.append("\t").append("Transition Key").append("\t\tInvoked Times\tAverage Time\tMax Time\tMin Time\n");
        for(String tKey : transitionInvokeTimes.keySet()) {
            float averageTime = transitionElapsedMillis.get(tKey).get() / (transitionInvokeTimes.get(tKey).get()+Float.MIN_VALUE);
            builder.append("\t").append(StringUtils.abbreviateMiddle(tKey, "...", 15)).append("\t\t").
                append(transitionInvokeTimes.get(tKey)).append("\t\t").
                append(String.format("%.2fms", averageTime)).append("\t\t").
                append(maxTransitionConsumedTime.get(tKey)).append("ms\t\t").
                append(minTransitionConsumedTime.get(tKey)).append("ms\t\t");
            builder.append("\n");
        }
        
        builder.append("Total Action Invoked: ").append(totalActionInvokedTimes).append("\n");
        builder.append("Total Action Failed: ").append(totalActionFailedTimes).append("\n");
        builder.append("Average Action Execution Comsumed: ").append(String.format("%.2fms", averageActionConsumedTime)).append("\n");
        
        builder.append("\t").append("Action Key").append("\t\tInvoked Times\tAverage Time\tMax Time\tMin Time\n");
        for(String aKey : actionInvokeTimes.keySet()) {
            float averageTime = actionElapsedMillis.get(aKey).get() / (actionInvokeTimes.get(aKey).get()+Float.MIN_VALUE);
            builder.append("\t").append(StringUtils.abbreviateMiddle(aKey, "...", 15)).append("\t\t").
                append(actionInvokeTimes.get(aKey)).append("\t\t").
                append(String.format("%.2fms", averageTime)).append("\t\t").
                append(maxActionConsumedTime.get(aKey)).append("ms\t\t").
                append(minActionConsumedTime.get(aKey)).append("ms\t\t");
            builder.append("\n");
        }
        
        builder.append("========================== ");
        builder.append(name);
        builder.append(" ==========================").append("\n");
        return builder.toString();
        
    }
}
