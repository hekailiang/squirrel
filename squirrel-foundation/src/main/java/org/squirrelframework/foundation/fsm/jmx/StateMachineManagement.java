package org.squirrelframework.foundation.fsm.jmx;

import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineLogger;
import org.squirrelframework.foundation.fsm.StateMachinePerformanceModel;
import org.squirrelframework.foundation.fsm.StateMachinePerformanceMonitor;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import com.google.common.base.Preconditions;

public class StateMachineManagement implements StateMachineManagementMBean {
    
    private StateMachine<?, ?, ?, ?> stateMachine;
    
    private StateMachineLogger fsmLogger;
    
    private StateMachinePerformanceMonitor performanceMonitor;
    
    private long totalTransitionInvokedTimes = 0;
    
    private long totalTransitionFailedTimes = 0;
    
    private long totalTransitionDeclinedTimes = 0;
    
    private float averageTransitionConsumedTime = 0.0f;
    
    private String perfStatDetails = "[Empty]";
    
    public StateMachineManagement(StateMachine<?, ?, ?, ?> stateMachine) {
        Preconditions.checkNotNull(stateMachine);
        this.stateMachine = stateMachine;
    }
    
    @Override
    public String getIdentifier() {
        return stateMachine.getIdentifier();
    }

    @Override
    public String getCurrentState() {
        return stateMachine.getCurrentState()!=null ? 
                stateMachine.getCurrentState().toString() : "[NULL]";
    }
    
    @Override
    public String getPerfStatDetails() {
        return perfStatDetails;
    }
    
    public long getTotalTransitionInvokedTimes() {
        return totalTransitionInvokedTimes;
    }
    
    public long getTotalTransitionFailedTimes() {
        return totalTransitionFailedTimes;
    }
    
    public long getTotalTransitionDeclinedTimes() {
        return totalTransitionDeclinedTimes;
    }
    
    public float getAverageTransitionConsumedTime() {
        return averageTransitionConsumedTime;
    }
    
    @Override
    public String getLastErrorMessage() {
        return stateMachine.getLastException()!=null ? 
                stateMachine.getLastException().getMessage() : "[NoException]";
    }

    @Override
    public String toggleLogging() {
        if(fsmLogger==null) {
            fsmLogger = new StateMachineLogger(stateMachine);
            fsmLogger.startLogging();
            return "Logging Started";
        } else {
            fsmLogger.stopLogging();
            fsmLogger = null;
            return "Logging Ended";
        }
    }
    
    @Override
    public String togglePerfMon() {
        if(performanceMonitor==null) {
            perfStatDetails = "[Empty]";
            performanceMonitor = new StateMachinePerformanceMonitor("Performance-of-"+stateMachine.getIdentifier());
            stateMachine.addDeclarativeListener(performanceMonitor);
            if(stateMachine.isStarted()) {
                performanceMonitor.onStateMachineStart(stateMachine);
            }
            return "Performance Monitor Start";
        } else {
            StateMachinePerformanceModel perfModel = performanceMonitor.getPerfModel();
            this.totalTransitionInvokedTimes = perfModel.getTotalTransitionInvokedTimes();
            this.totalTransitionDeclinedTimes = perfModel.getTotalTransitionDeclinedTimes();
            this.totalTransitionFailedTimes = perfModel.getTotalTransitionFailedTimes();
            this.averageTransitionConsumedTime = perfModel.getAverageTransitionConsumedTime();
            
            perfStatDetails = perfModel.toString();
            stateMachine.removeDeclarativeListener(performanceMonitor);
            performanceMonitor = null;
            return "Performance Monitor End";
        }
    }

    @Override
    public void fireEvent(String event, String context) {
        Converter<?> eventConverter = ConverterProvider.INSTANCE.getConverter(stateMachine.typeOfEvent());
        Converter<?> contextConverter = ConverterProvider.INSTANCE.getConverter(stateMachine.typeOfContext());
        Object e = eventConverter.convertFromString(event);
        Object c = context!=null && !context.isEmpty() ? contextConverter.convertFromString(context) : null;
        ((AbstractStateMachine<?,?,?,?>)stateMachine).untypedFire(e, c);
    }
}
