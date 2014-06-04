package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.annotation.AsyncExecute;
import org.squirrelframework.foundation.fsm.annotation.ExecuteWhen;
import org.squirrelframework.foundation.fsm.annotation.LogExecTime;
import org.squirrelframework.foundation.util.ReflectUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

public class MethodCallActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    
    final static Logger logger = LoggerFactory.getLogger(MethodCallActionImpl.class);

    private final Method method;
    
    private boolean logExecTime;
    
    private final String executeWhenExpr;
    
    private final String methodDesc;
    
    private final ExecutionContext executionContext;
    
    private final int weight;
    
    private final boolean isAsync;
    
    private final long timeout;
    
    MethodCallActionImpl(Method method, int weight, ExecutionContext executionContext) {
        Preconditions.checkNotNull(method, "Method of the action cannot be null.");
        this.method = method;
        this.weight = weight;
        this.executionContext = executionContext;
        
        AsyncExecute asyncAnnotation = method.getAnnotation(AsyncExecute.class);
        this.isAsync = asyncAnnotation!=null;
        this.timeout = asyncAnnotation!=null ? asyncAnnotation.timeout() : -1;
        
        logExecTime = ReflectUtils.isAnnotatedWith(method, LogExecTime.class);
        if(!logExecTime) {
            logExecTime = method.getDeclaringClass().getAnnotation(LogExecTime.class) != null;
        }
        
        ExecuteWhen executeWhen = method.getAnnotation(ExecuteWhen.class);
        if(executeWhen!=null) {
            executeWhenExpr = executeWhen.value();
            executionContext.getScriptManager().compile(executeWhenExpr);
        } else {
            executeWhenExpr = null;
        }
        
        methodDesc = ReflectUtils.logMethod(method);
    }
    
    @Override
    public void execute(final S from, final S to, 
            final E event, final C context, final T stateMachine) {
        invokeMethod(from, to, event, context, stateMachine);
    }
        
    private void invokeMethod(S from, S to, E event, C context, T stateMachine) {
        if(executeWhenExpr!=null) {
            Map<String, Object> variables = new HashMap<String, Object>();
//            variables.put("from", from);
//            variables.put("to", to);
//            variables.put("event", event);
            variables.put("context", context);
//            variables.put("stateMachine", stateMachine);
            boolean isAllowed = executionContext.getScriptManager().evalBoolean(executeWhenExpr, variables);
            if(!isAllowed) return;
        }
        
        Object[] paramValues = Lists.newArrayList(from, to, event, context).
                subList(0, executionContext.getMethodCallParamTypes().length).toArray();
        if(logExecTime && logger.isDebugEnabled()) {
            Stopwatch sw = Stopwatch.createStarted();
            ReflectUtils.invoke(method, stateMachine, paramValues);
            logger.debug("Execute Method \""+methodDesc+"\" tooks "+sw+".");
        } else {
            ReflectUtils.invoke(method, stateMachine, paramValues);
        }
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
        if(obj == null)
            return false;
        if(obj instanceof MethodCallActionProxyImpl && obj.equals(this))
            return true;
        if (getClass() != obj.getClass() || 
                !method.equals(MethodCallActionImpl.class.cast(obj).method))
            return false;
        return true;
    }

    @Override
    public int weight() {
        return weight;
    }
    
    @Override
    final public String toString() {
        return "method#"+method.getName()+":"+weight;
    }

    @Override
    public boolean isAsync() {
        return isAsync;
    }

    @Override
    public long timeout() {
        return timeout;
    }
}
