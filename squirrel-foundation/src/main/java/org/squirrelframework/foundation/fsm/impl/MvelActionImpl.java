package org.squirrelframework.foundation.fsm.impl;

import java.util.HashMap;
import java.util.Map;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.MvelScriptManager;
import org.squirrelframework.foundation.fsm.StateMachine;

class MvelActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    
    private final String expression;
    
    private final MvelScriptManager scriptManager;
    
    MvelActionImpl(String expression, MvelScriptManager scriptManager) {
        this.expression = expression;
        this.scriptManager = scriptManager;
        
        scriptManager.compile(expression);
    }
    
    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        try {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("from", from);
            variables.put("to", to);
            variables.put("event", event);
            variables.put("context", context);
            variables.put("stateMachine", stateMachine);
            scriptManager.eval(expression, variables, Void.class);
        } catch (Exception e) {
            System.err.println("Evaluate \""+expression+"\" failed, which caused by "+e.getCause().getMessage());
        }
    }
}
