package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.MvelScriptManager;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.base.Preconditions;

public class MethodCallActionProxyImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodCallActionProxyImpl.class);
    
    private final String methodName;
    
    private final MvelScriptManager scriptManager;
    
    private Action<T, S, E, C> delegator;
    
    MethodCallActionProxyImpl(String methodName, MvelScriptManager scriptManager) {
        this.methodName = methodName;
        this.scriptManager = scriptManager;
    }

    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        Preconditions.checkNotNull(stateMachine);
        
        if(delegator==null) {
            boolean isContextSensitive = stateMachine.isContextSensitive();
            Class<?> stateMachineClazz = stateMachine.getClass();
            
            Class<S> stateClazz = stateMachine.typeOfState();
            Class<E> eventClazz = stateMachine.typeOfEvent();
            Class<C> contextClazz = stateMachine.typeOfContext();
            
            final Class<?>[] methodCallParamTypes = isContextSensitive ? 
                    new Class<?>[]{stateClazz, stateClazz, eventClazz, contextClazz} : 
                    new Class<?>[]{stateClazz, stateClazz, eventClazz};
                    
            Method method = StateMachineBuilderImpl.findMethodCallActionInternal( 
                    stateMachineClazz, methodName, methodCallParamTypes );
            if(method!=null) {
                delegator = FSM.newMethodCallAction(method, scriptManager);
            } else {
                if(logger.isInfoEnabled()){
                    logger.warn("Cannot find method '"+methodName+"' with parameters '"+
                        methodCallParamTypes+"' in class "+stateMachineClazz+".");
                }
                delegator = new Action<T, S, E, C>() {
                    @Override
                    public void execute(S from, S to, E event, C context, T stateMachine) {
                        // do nothing, dummy action
                    }
                };
            }
        }
        delegator.execute(from, to, event, context, stateMachine);
    }

}
