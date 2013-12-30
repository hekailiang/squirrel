package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;

import org.squirrelframework.foundation.fsm.StateMachineWithoutContext;
import org.squirrelframework.foundation.fsm.ImmutableState;

@Deprecated
public abstract class AbstractStateMachineWithoutContext<T extends StateMachineWithoutContext<T, S, E>, S, E> 
    extends AbstractStateMachine<T, S, E, Void> implements StateMachineWithoutContext<T, S, E> {

    protected AbstractStateMachineWithoutContext(ImmutableState<T, S, E, Void> initialState, 
            Map<S, ImmutableState<T, S, E, Void>> states) {
        super(initialState, states);
    }
    
    @Override
    public boolean isContextSensitive() {
        return false;
    }
}
