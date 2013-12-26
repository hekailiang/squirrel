package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.MvelScriptManager;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.annotation.ExecuteWhen;
import org.squirrelframework.foundation.fsm.annotation.LogExecTime;
import org.squirrelframework.foundation.util.ReflectUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

public class MethodCallActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    
    final static Logger logger = LoggerFactory.getLogger(MethodCallActionImpl.class);

    private final Method method;
    
    private boolean logExecTime;
    
    private final String executeWhenExpr;
    
    private final String methodDesc;
    
    private final MvelScriptManager scriptManager;
    
    MethodCallActionImpl(Method method, MvelScriptManager scriptManager) {
        Preconditions.checkNotNull(method, "Method of the action cannot be null.");
        this.method = method;
        this.scriptManager = scriptManager;
        
        logExecTime = ReflectUtils.isAnnotatedWith(method, LogExecTime.class);
        if(!logExecTime) {
            logExecTime = method.getDeclaringClass().getAnnotation(LogExecTime.class) != null;
        }
        
        ExecuteWhen executeWhen = method.getAnnotation(ExecuteWhen.class);
        if(executeWhen!=null) {
            executeWhenExpr = executeWhen.value();
            scriptManager.compile(executeWhenExpr);
        } else {
            executeWhenExpr = null;
        }
        
        methodDesc = ReflectUtils.logMethod(method);
    }
    
    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        if(executeWhenExpr!=null) {
            Map<String, Object> variables = new HashMap<String, Object>();
//            variables.put("from", from);
//            variables.put("to", to);
//            variables.put("event", event);
            variables.put("context", context);
//            variables.put("stateMachine", stateMachine);
            boolean isAllowed = scriptManager.evalBoolean(executeWhenExpr, variables);
            if(!isAllowed) return;
        }
        
        Object[] params = stateMachine.isContextSensitive() ? 
                new Object[]{from, to, event, context} : new Object[]{from, to, event};
        if(logExecTime && logger.isDebugEnabled()) {
            Stopwatch sw = new Stopwatch().start();
            ReflectUtils.invoke(method, stateMachine, params);
            logger.debug("Execute Method \""+methodDesc+"\" tooks "+sw.stop().elapsedMillis()+"ms.");
        } else {
            ReflectUtils.invoke(method, stateMachine, params);
        }
    }
    
    @Override
    public String toString() {
        return "callMethod("+methodDesc+")";
    }
    
    @Override
    public String name() {
        return method.getName();
    }
    
    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass() || 
                !method.equals(MethodCallActionImpl.class.cast(obj).method))
            return false;
        return true;
    }
}
