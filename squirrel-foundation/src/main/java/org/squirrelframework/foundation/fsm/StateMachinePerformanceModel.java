package org.squirrelframework.foundation.fsm;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

public class StateMachinePerformanceModel {
    
    private String name;

    private long totalTransitionInvokedTimes = 0;
    private long totalTransitionFailedTimes = 0;
    private long totalTransitionDeclinedTimes = 0;
    private float averageTransitionConsumedTime = 0.0f;
    private final Map<String, Long> transitionInvokeTimes = Maps.newHashMap();
    private final Map<String, Float> averTransitionConsumedTime = Maps.newHashMap();
    private final Map<String, Long> maxTransitionConsumedTime = Maps.newHashMap();
    private final Map<String, Long> minTransitionConsumedTime = Maps.newHashMap();
    
    private long totalActionInvokedTimes = 0;
    private long totalActionFailedTimes = 0;
    private float averageActionConsumedTime = 0.0f;
    private final Map<String, Long> actionInvokeTimes = Maps.newHashMap();
    private final Map<String, Float> averActionConsumedTime = Maps.newHashMap();
    private final Map<String, Long> maxActionConsumedTime = Maps.newHashMap();
    private final Map<String, Long> minActionConsumedTime = Maps.newHashMap();
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    public long getTotalTransitionInvokedTimes() {
        return totalTransitionInvokedTimes;
    }
    
    void setTotalTransitionInvokedTimes(long totalTransitionInvokedTimes) {
        this.totalTransitionInvokedTimes = totalTransitionInvokedTimes;
    }
    
    public long getTotalTransitionFailedTimes() {
        return totalTransitionFailedTimes;
    }
    
    void setTotalTransitionFailedTimes(long totalTransitionFailedTimes) {
        this.totalTransitionFailedTimes = totalTransitionFailedTimes;
    }
    
    public long getTotalTransitionDeclinedTimes() {
        return totalTransitionDeclinedTimes;
    }
    
    void setTotalTransitionDeclinedTimes(long totalTransitionDeclinedTimes) {
        this.totalTransitionDeclinedTimes = totalTransitionDeclinedTimes;
    }
    
    public float getAverageTransitionConsumedTime() {
        return averageTransitionConsumedTime;
    }
    
    void setAverageTransitionConsumedTime(float averageTransitionConsumedTime) {
        this.averageTransitionConsumedTime = averageTransitionConsumedTime;
    }
    
    public long getTotalActionInvokedTimes() {
        return totalActionInvokedTimes;
    }
    
    void setTotalActionInvokedTimes(long totalActionInvokedTimes) {
        this.totalActionInvokedTimes = totalActionInvokedTimes;
    }
    
    public long getTotalActionFailedTimes() {
        return totalActionFailedTimes;
    }
    
    void setTotalActionFailedTimes(long totalActionFailedTimes) {
        this.totalActionFailedTimes = totalActionFailedTimes;
    }
    
    public float getAverageActionConsumedTime() {
        return averageActionConsumedTime;
    }
    
    void setAverageActionConsumedTime(float averageActionConsumedTime) {
        this.averageActionConsumedTime = averageActionConsumedTime;
    }
    
    public Map<String, Long> getTransitionInvokeTimes() {
        return Collections.unmodifiableMap(transitionInvokeTimes);
    }
    
    void addTransitionInvokeTime(String key, Long value) {
        transitionInvokeTimes.put(key, value);
    }
    
    public Map<String, Float> getAverTransitionConsumedTime() {
        return Collections.unmodifiableMap(averTransitionConsumedTime);
    }
    
    void addAverTransitionConsumedTime(String key, float value) {
        averTransitionConsumedTime.put(key, value);
    }
    
    public Map<String, Long> getMaxTransitionConsumedTime() {
        return Collections.unmodifiableMap(maxTransitionConsumedTime);
    }
    
    void addMaxTransitionConsumedTime(String key, Long value) {
        maxTransitionConsumedTime.put(key, value);
    }
    
    public Map<String, Long> getMinTransitionConsumedTime() {
        return Collections.unmodifiableMap(minTransitionConsumedTime);
    }
    
    void addMinTransitionConsumedTime(String key, Long value) {
        minTransitionConsumedTime.put(key, value);
    }
    
    public Map<String, Long> getActionInvokeTimes() {
        return Collections.unmodifiableMap(actionInvokeTimes);
    }
    
    void addActionInvokeTime(String key, Long value) {
        actionInvokeTimes.put(key, value);
    }
    
    public Map<String, Float> getAverActionConsumedTime() {
        return Collections.unmodifiableMap(averActionConsumedTime);
    }
    
    void addAverActionConsumedTime(String key, float value) {
        averActionConsumedTime.put(key, value);
    }
    
    public Map<String, Long> getMaxActionConsumedTime() {
        return Collections.unmodifiableMap(maxActionConsumedTime);
    }
    
    void addMaxActionConsumedTime(String key, Long value) {
        maxActionConsumedTime.put(key, value);
    }
    
    public Map<String, Long> getMinActionConsumedTime() {
        return Collections.unmodifiableMap(minActionConsumedTime);
    }
    
    void addMinActionConsumedTime(String key, Long value) {
        minActionConsumedTime.put(key, value);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("========================== ");
        builder.append(name);
        builder.append(" ==========================\n");
        
        builder.append("Total Transition Invoked: ").append(totalTransitionInvokedTimes).append("\n");
        builder.append("Total Transition Failed: ").append(totalTransitionFailedTimes).append("\n");
        builder.append("Total Transition Declained: ").append(totalTransitionDeclinedTimes).append("\n");
        builder.append("Average Transition Comsumed: ").append(String.format("%.4fms", averageTransitionConsumedTime)).append("\n");
        
        builder.append("\t").append("Transition Key").append("\t\tInvoked Times\tAverage Time\t\tMax Time\tMin Time\n");
        for(String tKey : transitionInvokeTimes.keySet()) {
            builder.append("\t").append(StringUtils.abbreviateMiddle(tKey, "...", 15)).append("\t\t").
                append(transitionInvokeTimes.get(tKey)).append("\t\t").
                append(String.format("%.4fms", averTransitionConsumedTime.get(tKey))).append("\t\t").
                append(maxTransitionConsumedTime.get(tKey)).append("ms\t\t").
                append(minTransitionConsumedTime.get(tKey)).append("ms\t\t");
            builder.append("\n");
        }
        builder.append("\n");
        
        builder.append("Total Action Invoked: ").append(totalActionInvokedTimes).append("\n");
        builder.append("Total Action Failed: ").append(totalActionFailedTimes).append("\n");
        builder.append("Average Action Execution Comsumed: ").append(String.format("%.4fms", averageActionConsumedTime)).append("\n");
        
        builder.append("\t").append("Action Key").append("\t\tInvoked Times\tAverage Time\t\tMax Time\tMin Time\n");
        for(String aKey : actionInvokeTimes.keySet()) {
            builder.append("\t").append(StringUtils.abbreviateMiddle(aKey, "...", 15)).append("\t\t").
                append(actionInvokeTimes.get(aKey)).append("\t\t").
                append(String.format("%.4fms", averActionConsumedTime.get(aKey))).append("\t\t").
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
