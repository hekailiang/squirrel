package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.IdProvider;
import org.squirrelframework.foundation.component.SquirrelSingleton;

/**
 * This class is used to configure state machine runtime behavior
 * @author Henry.He
 */
public class StateMachineConfiguration implements SquirrelSingleton {
    
    private static StateMachineConfiguration instance = StateMachineConfiguration.create();

    public static StateMachineConfiguration getInstance() {
        return instance;
    }

    public static void setInstance(StateMachineConfiguration instance) {
        StateMachineConfiguration.instance = instance;
    }
    
    public static StateMachineConfiguration create() {
        return new StateMachineConfiguration();
    }
    
    private StateMachineConfiguration() {}
    
    private boolean isAutoStartEnabled = true;
    
    private boolean isAutoTerminateEnabled = true;
    
    private boolean isDataIsolateEnabled = false;
    
    private boolean isDebugEnabled= false;
    
    private IdProvider idProvider = IdProvider.Default.getInstance();
    
    public boolean isAutoStartEnabled() {
        return isAutoStartEnabled;
    }

    public StateMachineConfiguration setAutoStartEnabled(boolean isAutoStartEnabled) {
        this.isAutoStartEnabled = isAutoStartEnabled;
        return this;
    }

    public boolean isAutoTerminateEnabled() {
        return isAutoTerminateEnabled;
    }

    public StateMachineConfiguration setAutoTerminateEnabled(boolean isAutoTerminateEnabled) {
        this.isAutoTerminateEnabled = isAutoTerminateEnabled;
        return this;
    }

    public boolean isDataIsolateEnabled() {
        return isDataIsolateEnabled;
    }

    public StateMachineConfiguration setDataIsolateEnabled(boolean isDataIsolateEnabled) {
        this.isDataIsolateEnabled = isDataIsolateEnabled;
        return this;
    }

    public IdProvider getIdProvider() {
        return idProvider;
    }

    public StateMachineConfiguration setIdProvider(IdProvider idProvider) {
        this.idProvider = idProvider;
        return this;
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public StateMachineConfiguration setDebugEnabled(boolean isDebugEnabled) {
        this.isDebugEnabled = isDebugEnabled;
        return this;
    }
}
