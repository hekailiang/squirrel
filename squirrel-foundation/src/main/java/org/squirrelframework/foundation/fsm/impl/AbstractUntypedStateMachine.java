package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;

import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;

public abstract class AbstractUntypedStateMachine extends AbstractStateMachine<UntypedStateMachine, Object, Object, Object> 
    implements UntypedStateMachine {

    protected AbstractUntypedStateMachine(
            ImmutableState<UntypedStateMachine, Object, Object, Object> initialState,
            Map<Object, ImmutableState<UntypedStateMachine, Object, Object, Object>> states) {
        super(initialState, states);
    }
}
