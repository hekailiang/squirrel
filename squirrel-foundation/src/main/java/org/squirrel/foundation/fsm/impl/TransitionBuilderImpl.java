package org.squirrel.foundation.fsm.impl;

import java.util.List;
import java.util.Map;

import org.squirrel.foundation.component.SquirrelComponent;
import org.squirrel.foundation.fsm.Action;
import org.squirrel.foundation.fsm.Condition;
import org.squirrel.foundation.fsm.MutableState;
import org.squirrel.foundation.fsm.MutableTransition;
import org.squirrel.foundation.fsm.StateMachine;
import org.squirrel.foundation.fsm.TransitionType;
import org.squirrel.foundation.fsm.builder.From;
import org.squirrel.foundation.fsm.builder.On;
import org.squirrel.foundation.fsm.builder.To;
import org.squirrel.foundation.fsm.builder.TransitionBuilder;
import org.squirrel.foundation.fsm.builder.When;

class TransitionBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements 
    TransitionBuilder<T, S, E, C>, From<T, S, E, C>, To<T, S, E, C>, On<T, S, E, C>, SquirrelComponent {

    private final Map<S, MutableState<T, S, E, C>> states;
    
    private MutableState<T, S, E, C> sourceState;
    
    private MutableState<T, S, E, C> targetState;
    
    private MutableTransition<T, S, E, C> transition;
    
    private TransitionType transitionType = TransitionType.EXTERNAL;
    
    TransitionBuilderImpl(Map<S, MutableState<T, S, E, C>> states) {
        this.states = states;
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
        return this;
    }

    @Override
    public To<T, S, E, C> to(S stateId) {
        targetState = FSM.getState(states, stateId);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public To<T, S, E, C> toFinal() {
        targetState = (MutableState<T, S, E, C>) FSM.FINAL_STATE;
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
        transitionType = TransitionType.INTERNAL;
        return this;
    }
}
