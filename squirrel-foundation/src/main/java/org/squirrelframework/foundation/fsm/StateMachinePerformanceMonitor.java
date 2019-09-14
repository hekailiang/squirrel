package org.squirrelframework.foundation.fsm;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.squirrelframework.foundation.fsm.annotation.OnActionExecException;
import org.squirrelframework.foundation.fsm.annotation.OnAfterActionExecuted;
import org.squirrelframework.foundation.fsm.annotation.OnBeforeActionExecuted;
import org.squirrelframework.foundation.fsm.annotation.OnStateMachineStart;
import org.squirrelframework.foundation.fsm.annotation.OnStateMachineTerminate;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionBegin;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionDecline;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionEnd;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

public class StateMachinePerformanceMonitor {
    
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
    
    private final Object waitLock = new Object();
    
    private volatile boolean isBusyStat = false;
    
    public StateMachinePerformanceMonitor(String name) {
        this.name = name;
    }
    
    private String getTransitionKey(Object sourceState, Object targetState, Object event, Object context) {
        return sourceState+ "--{"+event+", "+context+"}->"+targetState;
    }
    
    private void clearCache() {
        transitionInvokeTimes.clear();
        transitionFailedTimes.clear();
        transitionDeclinedTimes.clear();
        transitionElapsedMillis.clear();
        maxTransitionConsumedTime.clear();
        minTransitionConsumedTime.clear();
        actionInvokeTimes.clear();
        actionFailedTimes.clear();
        actionElapsedMillis.clear();
        maxActionConsumedTime.clear();
        minActionConsumedTime.clear();
    }
    
    private void waitIfBusyStat() {
        if (isBusyStat) {
            synchronized (waitLock) {
                while (isBusyStat) {
                    try {
                        waitLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    
    private void notifyAllAfterBusyStat() {
        synchronized (waitLock) {
            isBusyStat = false;
            waitLock.notifyAll();
        }
    }
    
    @OnStateMachineStart
    public void onStateMachineStart(StateMachine<?,?,?,?> fsm) {
        transitionWatches.put(fsm.getIdentifier(), Stopwatch.createUnstarted());
        actionWatches.put(fsm.getIdentifier(), Stopwatch.createUnstarted());
    }
    
    @OnStateMachineTerminate
    public void onStateMachineTerminate(StateMachine<?,?,?,?> fsm) {
        transitionWatches.remove(fsm.getIdentifier());
        actionWatches.remove(fsm.getIdentifier());
    }
    
    @OnTransitionBegin
    public void onTransitionBegin(StateMachine<?,?,?,?> fsm) {
        waitIfBusyStat();
        transitionWatches.get(fsm.getIdentifier()).reset().start();
    }
    
    @OnTransitionEnd
    public void onTransitionEnd(Object sourceState, Object targetState, 
            Object event, Object context, StateMachine<?,?,?,?> fsm) {
        waitIfBusyStat();
        String tKey = getTransitionKey(sourceState, targetState, event, context);
        long delta = transitionWatches.get(fsm.getIdentifier()).stop().elapsed(TimeUnit.MILLISECONDS);
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
        waitIfBusyStat();
        String tKey = getTransitionKey(sourceState, targetState, event, context);
        transitionFailedTimes.putIfAbsent(tKey, new AtomicLong(0));
        transitionFailedTimes.get(tKey).incrementAndGet();
    }
    
    @OnTransitionDecline
    public void onTransitionDeclined(Object sourceState, Object event, Object context) {
        waitIfBusyStat();
        String tKey = getTransitionKey(sourceState, null, event, context);
        transitionDeclinedTimes.putIfAbsent(tKey, new AtomicLong(0));
        transitionDeclinedTimes.get(tKey).incrementAndGet();
    }
    
    @OnBeforeActionExecuted
    public void onBeforeActionExecuted(StateMachine<?,?,?,?> fsm, Action<?, ?, ?,?> action) {
        waitIfBusyStat();
        actionWatches.get(fsm.getIdentifier()).reset().start();
    }
    
    @OnAfterActionExecuted
    public void onAfterActionExecuted(StateMachine<?,?,?,?> fsm, Action<?, ?, ?,?> action) {
        waitIfBusyStat();
        String aKey = action.toString();
        long delta = actionWatches.get(fsm.getIdentifier()).stop().elapsed(TimeUnit.MILLISECONDS);
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
        waitIfBusyStat();
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
    
    public synchronized StateMachinePerformanceModel getPerfModel() {
        isBusyStat = true;
        
        StateMachinePerformanceModel perfModel = new StateMachinePerformanceModel();
        perfModel.setName(name);
        
        long totalTransitionInvokedTimes = getTotal(transitionInvokeTimes);
        perfModel.setTotalTransitionInvokedTimes(totalTransitionInvokedTimes);
        
        long totalTransitionFailedTimes = getTotal(transitionFailedTimes);
        perfModel.setTotalTransitionFailedTimes(totalTransitionFailedTimes);
        
        long totalTransitionDeclinedTimes = getTotal(transitionDeclinedTimes);
        perfModel.setTotalTransitionDeclinedTimes(totalTransitionDeclinedTimes);
        
        long totalTransitionConsumedTime = getTotal(transitionElapsedMillis);
        float averageTranstionConsumedTime = 
                totalTransitionConsumedTime / (totalTransitionInvokedTimes+Float.MIN_VALUE);
        perfModel.setAverageTransitionConsumedTime(averageTranstionConsumedTime);
        
        long totalActionInvokedTimes = getTotal(actionInvokeTimes);
        perfModel.setTotalActionInvokedTimes(totalActionInvokedTimes);
        
        long totalActionFailedTimes = getTotal(actionFailedTimes);
        perfModel.setTotalActionFailedTimes(totalActionFailedTimes);
        
        long totalActionConsumedTime = getTotal(actionElapsedMillis);
        float averageActionConsumedTime = 
                totalActionConsumedTime / (totalActionInvokedTimes+Float.MIN_VALUE);
        perfModel.setAverageActionConsumedTime(averageActionConsumedTime);
        
        
        for(String tKey : transitionInvokeTimes.keySet()) {
            float averageTime = transitionElapsedMillis.get(tKey).get() / 
                    (transitionInvokeTimes.get(tKey).get()+Float.MIN_VALUE);
            perfModel.addAverTransitionConsumedTime(tKey, averageTime);
            perfModel.addTransitionInvokeTime(tKey, transitionInvokeTimes.get(tKey).get());
            perfModel.addMaxTransitionConsumedTime(tKey, maxTransitionConsumedTime.get(tKey).get());
            perfModel.addMinTransitionConsumedTime(tKey, minTransitionConsumedTime.get(tKey).get());
        }
        
        
        for(String aKey : actionInvokeTimes.keySet()) {
            float averageTime = actionElapsedMillis.get(aKey).get() / 
                    (actionInvokeTimes.get(aKey).get()+Float.MIN_VALUE);
            perfModel.addAverActionConsumedTime(aKey, averageTime);
            perfModel.addActionInvokeTime(aKey, actionInvokeTimes.get(aKey).get());
            perfModel.addMaxActionConsumedTime(aKey, maxActionConsumedTime.get(aKey).get());
            perfModel.addMinActionConsumedTime(aKey, minActionConsumedTime.get(aKey).get());
        }
        
        clearCache();
        notifyAllAfterBusyStat();
        return perfModel;
    }
}
