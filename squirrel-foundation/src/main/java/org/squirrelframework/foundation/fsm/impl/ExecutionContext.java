package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.MvelScriptManager;

class ExecutionContext {
    
    private final MvelScriptManager scriptManager;
    
    private final Class<?> executionTargetType;
    
    private final Class<?>[] methodCallParamTypes;
    
    ExecutionContext(MvelScriptManager scriptManager, Class<?> executionTargetType, Class<?>[] methodCallParamTypes) {
        this.scriptManager = scriptManager;
        this.executionTargetType = executionTargetType;
        this.methodCallParamTypes = methodCallParamTypes;
    }

    public MvelScriptManager getScriptManager() {
        return scriptManager;
    }
    
    public Class<?> getExecutionTargetType() {
        return executionTargetType;
    }

    public Class<?>[] getMethodCallParamTypes() {
        return methodCallParamTypes;
    }

}
