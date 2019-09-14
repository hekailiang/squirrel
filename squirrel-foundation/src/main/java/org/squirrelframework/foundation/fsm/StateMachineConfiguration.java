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
    
    private boolean isDebugModeEnabled = false;
    
    private boolean isDelegatorModeEnabled = false;

    private IdProvider idProvider = IdProvider.Default.getInstance();
    
    public boolean isAutoStartEnabled() {
        return isAutoStartEnabled;
    }

    public StateMachineConfiguration enableAutoStart(boolean isAutoStartEnabled) {
        this.isAutoStartEnabled = isAutoStartEnabled;
        return this;
    }

    public boolean isAutoTerminateEnabled() {
        return isAutoTerminateEnabled;
    }

    public StateMachineConfiguration enableAutoTerminate(boolean isAutoTerminateEnabled) {
        this.isAutoTerminateEnabled = isAutoTerminateEnabled;
        return this;
    }

    public boolean isDataIsolateEnabled() {
        return isDataIsolateEnabled;
    }

    public StateMachineConfiguration enableDataIsolate(boolean isDataIsolateEnabled) {
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
    
    public boolean isDebugModeEnabled() {
        return isDebugModeEnabled;
    }
    
    public StateMachineConfiguration enableDebugMode(boolean isDebugModeEnabled) {
        this.isDebugModeEnabled = isDebugModeEnabled;
        return this;
    }
    
    public boolean isDelegatorModeEnabled() {
        return isDelegatorModeEnabled;
    }

    public StateMachineConfiguration enableDelegatorMode(boolean isDelegatorModeEnabled) {
        this.isDelegatorModeEnabled = isDelegatorModeEnabled;
        return this;
    }
}
