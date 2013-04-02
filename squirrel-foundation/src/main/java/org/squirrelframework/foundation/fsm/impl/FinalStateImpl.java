package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.Visitor;


final class FinalStateImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements MutableState<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(FinalStateImpl.class);
    
    @Override
    public S getStateId() {
        return null;
    }

    @Override
    public List<Action<T, S, E, C>> getEntryActions() {
        return Collections.emptyList();
    }

    @Override
    public List<Action<T, S, E, C>> getExitActions() {
        return Collections.emptyList();
    }

    @Override
    public List<ImmutableTransition<T, S, E, C>> getAllTransitions() {
        return Collections.emptyList();
    }

    @Override
    public List<ImmutableTransition<T, S, E, C>> getTransitions(E event) {
        return Collections.emptyList();
    }

    @Override
    public void entry(StateContext<T, S, E, C> stateContext) {
        logger.debug("Final state entry");
    }

    @Override
    public void exit(StateContext<T, S, E, C> stateContext) {
        throw new UnsupportedOperationException("The final state should never be exited.");
    }

    @Override
    public MutableTransition<T, S, E, C> addTransitionOn(E event) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

    @Override
    public void addEntryAction(Action<T, S, E, C> action) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

    @Override
    public void addEntryActions(List<Action<T, S, E, C>> actions) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

    @Override
    public void addExitAction(Action<T, S, E, C> action) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

    @Override
    public void addExitActions(List<Action<T, S, E, C>> actions) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        visitor.visitOnExit(this);
    }
    
    @Override
    public boolean isFinal() {
        return true;
    }
    
    @Override
    public String toString() {
        return "Final";
    }
}
