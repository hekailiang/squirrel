package org.squirrelframework.foundation.fsm.impl;

import java.util.List;

import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.TransitionResult;

import com.google.common.collect.Lists;

class StateContextImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements StateContext<T, S, E, C> {
    private final T stateMachine;
    private final ImmutableState<T, S, E, C> sourceState;
    private final C context;
    private final E event;
    private final TransitionResult<T, S, E, C> result;
    
    StateContextImpl(T stateMachine, ImmutableState<T, S, E, C> sourceState, E event, C context, TransitionResult<T, S, E, C> result) {
        this.stateMachine = stateMachine;
        this.sourceState = sourceState;
        this.event = event;
        this.context = context;
        this.result = result;
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

	@Override
    public List<ImmutableState<T, S, E, C>> getSubStatesOn(ImmutableState<T, S, E, C> parentState) {
		List<ImmutableState<T, S, E, C>> subStates = Lists.newArrayList();
		for(S stateId : stateMachine.getSubStatesOn(parentState.getStateId())) {
			subStates.add(stateMachine.getRawStateFrom(stateId));
		}
	    return subStates;
    }

	@Override
    public TransitionResult<T, S, E, C> getResult() {
	    return result;
    }
}
