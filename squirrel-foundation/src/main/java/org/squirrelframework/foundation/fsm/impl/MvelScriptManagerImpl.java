package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mvel2.MVEL;
import org.squirrelframework.foundation.fsm.MvelScriptManager;

public class MvelScriptManagerImpl implements MvelScriptManager {
    
    private Map<String, Object> compiledExpressions = 
            new ConcurrentHashMap<String, Object>();

    @Override
    public <T> T eval(String script, Object context, Class<T> returnType) {
        Object evaluateResult = null;
        if(compiledExpressions.containsKey(script)) {
            Object exp = compiledExpressions.get(script);
            evaluateResult = MVEL.executeExpression(exp, context);
        } else {
            evaluateResult = MVEL.eval(script, context);
        }
        return returnType.cast(evaluateResult);
    }

    @Override
    public void compile(String script) {
        if(!compiledExpressions.containsKey(script)) {
            Object compiled = MVEL.compileExpression(script);
            if(compiled!=null) {
                compiledExpressions.put(script, compiled);
            }
        }
    }

    @Override
    public boolean evalBoolean(String script, Object context) {
        return eval(script, context, Boolean.class);
    }

}
