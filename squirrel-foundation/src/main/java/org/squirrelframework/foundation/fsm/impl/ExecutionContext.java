package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.MvelScriptManager;

class ExecutionContext {
    
    private final MvelScriptManager scriptManager;
    
    private final Class<?>[] methodCallParamTypes;
    
    ExecutionContext(MvelScriptManager scriptManager, Class<?>[] methodCallParamTypes) {
        this.scriptManager = scriptManager;
        this.methodCallParamTypes = methodCallParamTypes;
    }

    public MvelScriptManager getScriptManager() {
        return scriptManager;
    }

    public Class<?>[] getMethodCallParamTypes() {
        return methodCallParamTypes;
    }

}
