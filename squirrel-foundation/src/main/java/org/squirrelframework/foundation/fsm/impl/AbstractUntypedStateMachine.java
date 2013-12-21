package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;

import org.squirrelframework.foundation.fsm.ImmutableUntypedState;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;

public abstract class AbstractUntypedStateMachine 
    extends AbstractStateMachine<UntypedStateMachine, Object, Object, Object> 
    implements UntypedStateMachine {

    protected AbstractUntypedStateMachine(ImmutableUntypedState initialState,
                Map<Object, ImmutableUntypedState> states) {
        super(initialState, states);
    }
}
