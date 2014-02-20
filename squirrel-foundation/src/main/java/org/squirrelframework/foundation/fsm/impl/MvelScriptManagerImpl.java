package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mvel2.MVEL;
import org.squirrelframework.foundation.fsm.MvelScriptManager;

public class MvelScriptManagerImpl implements MvelScriptManager {
    
    private Map<String, Object> compiledExpressions;

    @Override
    public <T> T eval(String script, Object context, Class<T> returnType) {
        Object evaluateResult = null;
        if(getCompiledExpression().containsKey(script)) {
            Object exp = getCompiledExpression().get(script);
            evaluateResult = MVEL.executeExpression(exp, context);
        } else {
            evaluateResult = MVEL.eval(script, context);
        }
        return returnType.cast(evaluateResult);
    }

    @Override
    public void compile(String script) {
        if(!getCompiledExpression().containsKey(script)) {
            Object compiled = MVEL.compileExpression(script);
            if(compiled!=null) {
                getCompiledExpression().put(script, compiled);
            }
        }
    }
    
    private Map<String, Object> getCompiledExpression() {
        if(compiledExpressions==null) 
            compiledExpressions = new ConcurrentHashMap<String, Object>();
        return compiledExpressions;
    }

    @Override
    public boolean evalBoolean(String script, Object context) {
        return eval(script, context, Boolean.class);
    }

}
