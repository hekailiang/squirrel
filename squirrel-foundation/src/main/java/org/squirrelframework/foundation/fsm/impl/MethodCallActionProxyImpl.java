package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;

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
    
    MethodCallActionProxyImpl(String methodName, ExecutionContext executionContext) {
        this.methodName = methodName;
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
                delegator = FSM.newMethodCallAction(method, executionContext);
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
        if(delegator==null) {
            return "method-proxy-"+methodName;
        } else {
            return delegator.name();
        }
    }

}
