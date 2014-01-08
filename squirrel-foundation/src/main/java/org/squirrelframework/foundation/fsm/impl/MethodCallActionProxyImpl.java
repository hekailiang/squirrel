package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.base.Preconditions;

public class MethodCallActionProxyImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodCallActionProxyImpl.class);
    
    private final String methodName;
    
    private final ExecutionContext executionContext;
    
    private Action<T, S, E, C> delegator;
    
    private final int weight;
    
    MethodCallActionProxyImpl(String methodName, ExecutionContext executionContext) {
        String[] arrays = StringUtils.split(methodName, ':');
        this.methodName = arrays[0];
        if(arrays.length>1 && arrays[1].matches("[\\+-]?\\d+")) {
            if(arrays[1].startsWith("+"))
                arrays[1] = arrays[1].substring(1);
            this.weight = Integer.valueOf(arrays[1]);
        } else if(methodName.startsWith("before")) {
            this.weight = Action.BEFORE_WEIGHT;
        } else if(methodName.startsWith("after")) {
            this.weight = Action.AFTER_WEIGHT;
        } else {
            this.weight = Action.NORMAL_WEIGHT;
        }
        this.executionContext = executionContext;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        Preconditions.checkNotNull(stateMachine);
        
        if(delegator==null) {
            Class<?> stateMachineClazz = stateMachine.getClass();
            Method method = StateMachineBuilderImpl.findMethodCallActionInternal( 
                    stateMachineClazz, methodName, executionContext.getMethodCallParamTypes() );
            if(method!=null) {
                delegator = FSM.newMethodCallAction(method, weight, executionContext);
            } else {
                if(logger.isInfoEnabled()){
                    logger.warn("Cannot find method '"+methodName+"' with parameters '"+
                            executionContext.getMethodCallParamTypes()+"' in class "+stateMachineClazz+".");
                }
                delegator = (Action<T, S, E, C>)Action.DUMMY_ACTION;
            }
        }
        delegator.execute(from, to, event, context, stateMachine);
    }

    @Override
    public String name() {
        return methodName;
    }

    @Override
    public int weight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return methodName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if(obj == null)
            return false;
        if(obj instanceof MethodCallActionImpl && 
                MethodCallActionImpl.class.cast(obj).name().equals(methodName)) 
            return true;
        if(getClass() != obj.getClass() || 
                !methodName.equals(MethodCallActionProxyImpl.class.cast(obj).methodName))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "method#"+methodName;
    }
}
