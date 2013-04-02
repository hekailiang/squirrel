package org.squirrelframework.foundation.fsm.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.Visitor;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;

final class StateImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements MutableState<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(StateImpl.class);
    
    private final S stateId;
    
    private final Actions<T, S, E, C> entryActions = FSM.newActions();
    
    private final Actions<T, S, E, C> exitActions  = FSM.newActions();
    
    private final LinkedListMultimap<E, ImmutableTransition<T, S, E, C>> transitions = LinkedListMultimap.create();
    
    StateImpl(S stateId) {
        this.stateId = stateId;
    }
    
    @Override
    public S getStateId() {
        return stateId;
    }

    @Override
    public List<Action<T, S, E, C>> getEntryActions() {
        return entryActions.getAll();
    }

    @Override
    public List<Action<T, S, E, C>> getExitActions() {
        return exitActions.getAll();
    }

    @Override
    public List<ImmutableTransition<T, S, E, C>> getAllTransitions() {
        return Lists.newArrayList(transitions.values());
    }

    @Override
    public List<ImmutableTransition<T, S, E, C>> getTransitions(E event) {
        return Lists.newArrayList(transitions.get(event));
    }

    @Override
    public void entry(StateContext<T, S, E, C> stateContext) {
        for(Action<T, S, E, C> entryAction : getEntryActions()) {
            entryAction.execute(null, stateId, stateContext.getEvent(), 
                    stateContext.getContext(), stateContext.getStateMachine());
        }
        logger.debug("State \""+stateId+"\" entry.");
    }

    @Override
    public void exit(StateContext<T, S, E, C> stateContext) {
        for(Action<T, S, E, C> exitAction : getExitActions()) {
            exitAction.execute(stateId, null, stateContext.getEvent(), 
                    stateContext.getContext(), stateContext.getStateMachine());
        }
        logger.debug("State \""+stateId+"\" exit.");
    }

    @Override
    public MutableTransition<T, S, E, C> addTransitionOn(E event) {
        MutableTransition<T, S, E, C> newTransition = FSM.newTransition();
        newTransition.setSourceState(this);
        newTransition.setEvent(event);
        transitions.put(event, newTransition);
        return newTransition;
    }

    @Override
    public void addEntryAction(Action<T, S, E, C> newAction) {
        entryActions.add(newAction);
    }
    
    @Override
    public void addEntryActions(List<Action<T, S, E, C>> newActions) {
        entryActions.addAll(newActions);
    }

    @Override
    public void addExitAction(Action<T, S, E, C> newAction) {
        exitActions.add(newAction);
    }

    @Override
    public void addExitActions(List<Action<T, S, E, C>> newActions) {
        exitActions.addAll(newActions);
    }
    
    @Override
    public boolean isFinal() {
        return false;
    }
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        for(ImmutableTransition<T, S, E, C> transition : getAllTransitions()) {
            transition.accept(visitor);
        }
        visitor.visitOnExit(this);
    }
    
    @Override
    public String toString() {
        return stateId.toString();
    }
}
