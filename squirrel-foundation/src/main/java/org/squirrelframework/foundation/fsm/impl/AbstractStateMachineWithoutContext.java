package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;

import org.squirrelframework.foundation.fsm.StateMachineWithoutContext;
import org.squirrelframework.foundation.fsm.ImmutableState;

public abstract class AbstractStateMachineWithoutContext<T extends StateMachineWithoutContext<T, S, E>, S, E> 
    extends AbstractStateMachine<T, S, E, T> implements StateMachineWithoutContext<T, S, E> {

    protected AbstractStateMachineWithoutContext(ImmutableState<T, S, E, T> initialState, 
            Map<S, ImmutableState<T, S, E, T>> states) {
        super(initialState, states);
    }
    
    @Override
    public void fire(E event) {
        super.fire(event, getThis());
    }
    
    @Override
    public S test(E event) {
        return super.test(event, getThis());
    }
    
    @Override
    public void start() {
        super.start(getThis());
    }
    
    @Override
    public void terminate() {
        super.terminate(getThis());
    }
    
    @Override
    public boolean isContextSensitive() {
        return false;
    }
}
