package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.ActionExecutionService;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineData;
import org.squirrelframework.foundation.fsm.TransitionResult;

class StateContextImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements StateContext<T, S, E, C> {
    private final StateMachine<T, S, E, C> stateMachine;
    private final StateMachineData<T, S, E, C> stateMachineData;
    private final ImmutableState<T, S, E, C> sourceState;
    private final C context;
    private final E event;
    private final TransitionResult<T, S, E, C> result;
    private final ActionExecutionService<T, S, E, C> executor;
    
    StateContextImpl(StateMachine<T, S, E, C> stateMachine, StateMachineData<T, S, E, C> stateMachineData,
            ImmutableState<T, S, E, C> sourceState, E event, C context, 
    		TransitionResult<T, S, E, C> result, ActionExecutionService<T, S, E, C> executor) {
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
    public TransitionResult<T, S, E, C> getResult() {
	    return result;
    }

	@Override
    public ActionExecutionService<T, S, E, C> getExecutor() {
	    return executor;
    }

    @Override
    public StateMachineData<T, S, E, C> getStateMachineData() {
        return stateMachineData;
    }
}
