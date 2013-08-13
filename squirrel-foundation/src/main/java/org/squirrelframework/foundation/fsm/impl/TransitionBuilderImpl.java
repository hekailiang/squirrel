package org.squirrelframework.foundation.fsm.impl;

import java.util.List;
import java.util.Map;

import org.squirrelframework.foundation.component.SquirrelComponent;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.TransitionType;
import org.squirrelframework.foundation.fsm.builder.ExternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.From;
import org.squirrelframework.foundation.fsm.builder.InternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.LocalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.On;
import org.squirrelframework.foundation.fsm.builder.To;
import org.squirrelframework.foundation.fsm.builder.When;

class TransitionBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements 
    ExternalTransitionBuilder<T, S, E, C>, InternalTransitionBuilder<T, S, E, C>, LocalTransitionBuilder<T, S, E, C>, 
    From<T, S, E, C>, To<T, S, E, C>, On<T, S, E, C>, SquirrelComponent {

    private final Map<S, MutableState<T, S, E, C>> states;
    
    private MutableState<T, S, E, C> sourceState;
    
    private MutableState<T, S, E, C> targetState;
    
    private MutableTransition<T, S, E, C> transition;
    
    private final TransitionType transitionType;
    
    private final int priority;
    
    TransitionBuilderImpl(Map<S, MutableState<T, S, E, C>> states, TransitionType transitionType, int priority) {
        this.states = states;
        this.transitionType = transitionType;
        this.priority = priority;
    }
    
    @Override
    public void perform(Action<T, S, E, C> action) {
        transition.addAction(action);
    }

    @Override
    public void perform(List<Action<T, S, E, C>> actions) {
        transition.addActions(actions);
    }

    @Override
    public On<T, S, E, C> on(E event) {
        transition = sourceState.addTransitionOn(event);
        transition.setTargetState(targetState);
        transition.setType(transitionType);
        transition.setPriority(priority);
        return this;
    }

    @Override
    public To<T, S, E, C> to(S stateId) {
        targetState = FSM.getState(states, stateId);
        return this;
    }
    
    @Override
    public To<T, S, E, C> toFinal(S stateId) {
        targetState = (MutableState<T, S, E, C>) FSM.getState(states, stateId);
        if(!targetState.isFinalState()) {
        	targetState.setFinal(true);
        }
        return this;
    }

    @Override
    public From<T, S, E, C> from(S stateId) {
        sourceState = FSM.getState(states, stateId);
        return this;
    }

    @Override
    public When<T, S, E, C> when(Condition<C> condition) {
        transition.setCondition(condition);
        return this;
    }
    
    @Override
    public To<T, S, E, C> within(S stateId) {
        sourceState = targetState = FSM.getState(states, stateId);
        return this;
    }
}
