package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.TransitionResult;
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
    public ImmutableState<T, S, E, C> getParentState() {
	    return null;
    }
    
    @Override
    public ImmutableState<T, S, E, C> getChildInitialState() {
	    return null;
    }

    @Override
    public void entry(StateContext<T, S, E, C> stateContext) {
        logger.debug("Final state entry");
        stateContext.getStateMachine().terminate();
    }
    
    @Override
    public ImmutableState<T, S, E, C> enterShallow(StateContext<T, S, E, C> stateContext) {
    	return this;
    }

    @Override
    public void exit(StateContext<T, S, E, C> stateContext) {
        throw new UnsupportedOperationException("The final state should never be exited.");
    }

    @Override
    public TransitionResult<T, S, E, C> internalFire(StateContext<T, S, E, C> stateContext) {
    	throw new UnsupportedOperationException("The final state should never process any event.");
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

	@Override
    public MutableTransition<T, S, E, C> addTransitionOn(E event) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public void addEntryAction(Action<T, S, E, C> newAction) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public void addEntryActions(List<Action<T, S, E, C>> newActions) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public void addExitAction(Action<T, S, E, C> newAction) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public void addExitActions(List<Action<T, S, E, C>> newActions) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public void setParentState(MutableState<T, S, E, C> parent) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public void setChildInitialState(MutableState<T, S, E, C> childInitialState) {
        throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public int getLevel() {
	    return 0;
    }

	@Override
    public void setLevel(int level) {
		throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public void addChildState(MutableState<T, S, E, C> childState) {
		throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public ImmutableState<T, S, E, C> enterByHistory(StateContext<T, S, E, C> stateContext) {
	    return this;
    }

	@Override
    public HistoryType getHistoryType() {
	    return HistoryType.NONE;
    }

	@Override
    public void setHistoryType(HistoryType historyType) {
		throw new UnsupportedOperationException("The final state should never be modified.");
    }

	@Override
    public ImmutableState<T, S, E, C> enterDeep(StateContext<T, S, E, C> stateContext) {
	    return this;
    }
}
