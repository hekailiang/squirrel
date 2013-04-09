package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;

class StateContextImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements StateContext<T, S, E, C> {
    final private T stateMachine;
    final private ImmutableState<T, S, E, C> sourceState;
    final private C context;
    final private E event;
    
    StateContextImpl(T stateMachine, ImmutableState<T, S, E, C> sourceState, E event, C context) {
        this.stateMachine = stateMachine;
        this.sourceState = sourceState;
        this.event = event;
        this.context = context;
    }
    
    @Override
    public T getStateMachine() {
        return stateMachine;
    }

    @Override
    public ImmutableState<T, S, E, C> getSourceState() {
        return sourceState;
    }

    @Override
    public C getContext() {
        return context;
    }

    @Override
    public E getEvent() {
        return event;
    }
    
    @Override
    public ImmutableState<T, S, E, C> getLastActiveChildStateOf(ImmutableState<T, S, E, C> parentState) {
    	S childStateId = stateMachine.getLastActiveChildStateOf(parentState.getStateId());
    	if(childStateId!=null) {
    		return stateMachine.getRawStateFrom(childStateId);
    	} else {
    		return parentState.getInitialState();
    	}
    }
    
    @Override
    public void setLastActiveChildState(ImmutableState<T, S, E, C> parentState, ImmutableState<T, S, E, C> childState) {
    	stateMachine.setLastActiveChildState(parentState.getStateId(), childState.getStateId());
    }
}
