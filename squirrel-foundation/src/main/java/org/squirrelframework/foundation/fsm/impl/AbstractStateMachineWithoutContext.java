package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;

import org.squirrelframework.foundation.fsm.StateMachineWithoutContext;
import org.squirrelframework.foundation.fsm.ImmutableState;

public abstract class AbstractStateMachineWithoutContext<T extends StateMachineWithoutContext<T, S, E>, S, E> 
    extends AbstractStateMachine<T, S, E, Void> implements StateMachineWithoutContext<T, S, E> {

    protected AbstractStateMachineWithoutContext(ImmutableState<T, S, E, Void> initialState, 
            Map<S, ImmutableState<T, S, E, Void>> states) {
        super(initialState, states);
    }
    
    @Override
    public void fire(E event) {
        super.fire(event, null);
    }
    
    @Override
    public S test(E event) {
        return super.test(event, null);
    }
    
    @Override
    public void start() {
        super.start(null);
    }
    
    @Override
    public void terminate() {
        super.terminate(null);
    }
    
    @Override
    public boolean isContextSensitive() {
        return false;
    }
}
