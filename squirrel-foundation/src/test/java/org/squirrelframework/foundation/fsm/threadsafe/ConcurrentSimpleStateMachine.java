package org.squirrelframework.foundation.fsm.threadsafe;

import java.util.Map;

import org.squirrelframework.foundation.fsm.ImmutableUntypedState;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.annotation.AsyncExecute;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

@ContextInsensitive
@StateMachineParameters(stateType=String.class, eventType=String.class, contextType=UntypedStateMachine.class)
public class ConcurrentSimpleStateMachine extends AbstractUntypedStateMachine {
    
    StringBuilder logger = new StringBuilder();
    
    Thread fromAToBCallThread = null;
    
    Thread fromBToCCallThread = null;

    protected ConcurrentSimpleStateMachine(ImmutableUntypedState initialState, 
            Map<Object, ImmutableUntypedState> states) {
        super(initialState, states);
    }
    
    @AsyncExecute
    protected void fromAToB(String from, String to, String event) {
        logger.append("fromAToB");
        fromAToBCallThread = Thread.currentThread();
        fire("SECOND");
    }
    
    protected void fromBToC(String from, String to, String event) {
        logger.append("fromBToC");
        fromBToCCallThread = Thread.currentThread();
    }
    
    @Override
    protected void beforeActionInvoked(Object fromState, Object toState, Object event, Object context) {
        if (logger.length() > 0) {
            logger.append('.');
        }
    }
}
