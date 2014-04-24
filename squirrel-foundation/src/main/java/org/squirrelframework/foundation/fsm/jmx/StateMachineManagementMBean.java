package org.squirrelframework.foundation.fsm.jmx;

public interface StateMachineManagementMBean {
    // Attributes
    String getIdentifier();
    
    String getCurrentState();
    
    String getPerfStatDetails();
    
    long getTotalTransitionInvokedTimes();
    
    long getTotalTransitionFailedTimes();
    
    long getTotalTransitionDeclinedTimes();
    
    float getAverageTranstionConsumedTime();
    
    String getLastErrorMessage();
    
    // Operations
    String toggleLogging();
    
    String togglePerfMon();
    
    void fireEvent(String event, String context);
}
