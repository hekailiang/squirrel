package org.squirrelframework.foundation.fsm.threadsafe;

import java.util.Map;

import org.squirrelframework.foundation.fsm.ImmutableUntypedState;
import org.squirrelframework.foundation.fsm.annotation.AnsyncExecute;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

@ContextInsensitive
@StateMachineParameters(stateType=String.class, eventType=String.class, contextType=Void.class)
public class ConcurrentSimpleStateMachine extends AbstractUntypedStateMachine {
    
    StringBuilder logger = new StringBuilder();
    
    Thread methodCallThread = null;

    protected ConcurrentSimpleStateMachine(ImmutableUntypedState initialState, 
            Map<Object, ImmutableUntypedState> states) {
        super(initialState, states);
    }
    
    @AnsyncExecute
    protected void fromAToB(String from, String to, String event) {
        logger.append("fromAToB");
        methodCallThread = Thread.currentThread();
    }
}
