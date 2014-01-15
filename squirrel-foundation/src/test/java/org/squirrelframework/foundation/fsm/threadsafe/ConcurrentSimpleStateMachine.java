package org.squirrelframework.foundation.fsm.threadsafe;

import java.util.Map;

import org.squirrelframework.foundation.fsm.ImmutableUntypedState;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParamters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

@ContextInsensitive
@StateMachineParamters(stateType=String.class, eventType=String.class, contextType=Void.class)
public class ConcurrentSimpleStateMachine extends AbstractUntypedStateMachine {

    protected ConcurrentSimpleStateMachine(ImmutableUntypedState initialState, 
            Map<Object, ImmutableUntypedState> states) {
        super(initialState, states);
    }

}
