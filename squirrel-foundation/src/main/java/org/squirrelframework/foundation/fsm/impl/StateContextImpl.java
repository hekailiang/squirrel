package org.squirrelframework.foundation.fsm.impl;

import java.util.List;

import org.squirrelframework.foundation.fsm.ActionExecutor;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineData;
import org.squirrelframework.foundation.fsm.TransitionResult;

import com.google.common.collect.Lists;

class StateContextImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements StateContext<T, S, E, C> {
    private final StateMachine<T, S, E, C> stateMachine;
    private final StateMachineData<T, S, E, C> stateMachineData;
    private final ImmutableState<T, S, E, C> sourceState;
    private final C context;
    private final E event;
    private final TransitionResult<T, S, E, C> result;
    private final ActionExecutor<T, S, E, C> executor;
    
    StateContextImpl(StateMachine<T, S, E, C> stateMachine, StateMachineData<T, S, E, C> stateMachineData,
            ImmutableState<T, S, E, C> sourceState, E event, C context, 
    		TransitionResult<T, S, E, C> result, ActionExecutor<T, S, E, C> executor) {
        this.stateMachine = stateMachine;
        this.stateMachineData = stateMachineData;
        this.sourceState = sourceState;
        this.event = event;
        this.context = context;
        this.result = result;
        this.executor = executor;
    }
    
    @Override
    public StateMachine<T, S, E, C> getStateMachine() {
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
    	S childStateId = stateMachineData.read().lastActiveChildStateOf(parentState.getStateId());
    	if(childStateId!=null) {
    		return stateMachineData.read().rawStateFrom(childStateId);
    	} else {
    		return parentState.getInitialState();
    	}
    }
    
    @Override
    public void setLastActiveChildState(ImmutableState<T, S, E, C> parentState, ImmutableState<T, S, E, C> childState) {
    	stateMachineData.write().lastActiveChildStateFor(parentState.getStateId(), childState.getStateId());
    }

	@Override
    public List<ImmutableState<T, S, E, C>> getSubStatesOn(ImmutableState<T, S, E, C> parentState) {
		List<ImmutableState<T, S, E, C>> subStates = Lists.newArrayList();
		for(S stateId : stateMachine.getSubStatesOn(parentState.getStateId())) {
			subStates.add(stateMachineData.read().rawStateFrom(stateId));
		}
	    return subStates;
    }

	@Override
    public TransitionResult<T, S, E, C> getResult() {
	    return result;
    }

	@Override
    public ActionExecutor<T, S, E, C> getExecutor() {
	    return executor;
    }

    @Override
    public StateMachineData<T, S, E, C> getStateMachineData() {
        return stateMachineData;
    }
}
