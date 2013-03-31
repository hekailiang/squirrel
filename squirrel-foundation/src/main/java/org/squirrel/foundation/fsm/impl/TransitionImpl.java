package org.squirrel.foundation.fsm.impl;

import java.util.List;

import org.squirrel.foundation.fsm.Action;
import org.squirrel.foundation.fsm.Actions;
import org.squirrel.foundation.fsm.Condition;
import org.squirrel.foundation.fsm.Conditions;
import org.squirrel.foundation.fsm.ImmutableState;
import org.squirrel.foundation.fsm.MutableTransition;
import org.squirrel.foundation.fsm.StateContext;
import org.squirrel.foundation.fsm.StateMachine;
import org.squirrel.foundation.fsm.TransitionType;
import org.squirrel.foundation.fsm.Visitor;

class TransitionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements MutableTransition<T, S, E, C> {
    
    private ImmutableState<T, S, E, C> sourceState;
    
    private ImmutableState<T, S, E, C> targetState;
    
    private E event;
    
    private Actions<T, S, E, C> actions = FSM.newActions();
    
    private Condition<C> condition = Conditions.always();
    
    private TransitionType type = TransitionType.EXTERNAL;

    @Override
    public ImmutableState<T, S, E, C> getSourceState() {
        return sourceState;
    }

    @Override
    public ImmutableState<T, S, E, C> getTargetState() {
        return targetState;
    }

    @Override
    public List<Action<T, S, E, C>> getActions() {
        return actions.getAll();
    }

    @Override
    public ImmutableState<T, S, E, C> transit(StateContext<T, S, E, C> stateContext) {
        T stateMachine = stateContext.getStateMachine();
        for(Action<T, S, E, C> action : getActions()) {
            action.execute(sourceState.getStateId(), targetState.getStateId(), stateContext.getEvent(), 
                    stateContext.getContext(), stateMachine);
        }
        return targetState;
    }

    @Override
    public void setSourceState(ImmutableState<T, S, E, C> state) {
        this.sourceState = state;
    }

    @Override
    public void setTargetState(ImmutableState<T, S, E, C> state) {
        this.targetState = state;
    }

    @Override
    public void addAction(Action<T, S, E, C> newAction) {
        actions.add(newAction);
    }

    @Override
    public void addActions(List<Action<T, S, E, C>> newActions) {
        actions.addAll(newActions);
    }

    @Override
    public Condition<C> getCondition() {
        return condition;
    }

    @Override
    public void setCondition(Condition<C> condition) {
        this.condition = condition;
    }

    @Override
    public E getEvent() {
        return event;
    }

    @Override
    public void setEvent(E event) {
        this.event = event;
    }
    
    @Override
    public TransitionType getType() {
        return type;
    }
    
    @Override
    public void setType(TransitionType type) {
        this.type = type;
    }
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        visitor.visitOnExit(this);
    }
    
    @Override
    public boolean isMatch(S fromState, S toState, E event, Class<?> condClazz, TransitionType type) {
        if(toState==null && !getTargetState().isFinal())
            return false;
        if(toState!=null && !getTargetState().isFinal() && !getTargetState().getStateId().equals(toState))
            return false;
        if(!getEvent().equals(event)) 
            return false;
        if(getCondition().getClass()!=condClazz)
            return false;
        if(!getType().equals(type))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return sourceState + "-[" + event.toString() +", "+
                condition.getClass().getSimpleName()+ "]->" + targetState;
    }
}
