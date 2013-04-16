package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.List;

import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.TransitionResult;

import com.google.common.collect.Lists;

class TransitionResultImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements TransitionResult<T, S, E, C> {
	
	private  boolean accepted;
	
	private ImmutableState<T, S, E, C> targetState;
	
	private TransitionResult<T, S, E, C> parent;
	
	private List<TransitionResult<T, S, E, C>> subResults;
	
	private void addSubResult(TransitionResult<T, S, E, C> subResult) {
		if(subResults==null) 
			subResults=Lists.newArrayList();
		subResults.add(subResult);
	}

	@Override
    public boolean isAccepted() {
		if(accepted) {
			return true;
		} else if(subResults!=null) {
			for(TransitionResult<T, S, E, C> subResult : getSubResults()) {
				if(subResult.isAccepted()) return true;
			}
		}
	    return false;
    }
	
	private TransitionResult<T, S, E, C> getRootResult() {
		if(parent==null) 
			return this;
		return ((TransitionResultImpl<T, S, E, C>)parent).getRootResult();
	}

	@Override
    public ImmutableState<T, S, E, C> getTargetState() {
	    return targetState;
    }

	@Override
    public List<TransitionResult<T, S, E, C>> getSubResults() {
	    return subResults!=null ? Lists.newArrayList(subResults) : 
	    	Collections.<TransitionResult<T, S, E, C>>emptyList();
    }

	@Override
    public List<TransitionResult<T, S, E, C>> getAcceptedResults() {
		List<TransitionResult<T, S, E, C>> acceptedResults = Lists.newArrayList();
		if(subResults!=null) {
			for(TransitionResult<T, S, E, C> subResult : getSubResults()) {
				acceptedResults.addAll(subResult.getAcceptedResults());
			}
		}
		if(accepted) 
			acceptedResults.add(this);
	    return acceptedResults;
    }

	@Override
    public TransitionResult<T, S, E, C> getParentResut() {
	    return parent;
    }

	@Override
    public TransitionResult<T, S, E, C> setAccepted(boolean accepted) {
	    this.accepted = accepted;
	    return this;
    }

	@Override
    public TransitionResult<T, S, E, C> setTargetState(ImmutableState<T, S, E, C> targetState) {
	    this.targetState = targetState;
	    return this;
    }

	@Override
    public TransitionResult<T, S, E, C> setParent(TransitionResult<T, S, E, C> parent) {
		this.parent = parent;
		if(parent!=null && parent instanceof TransitionResultImpl) {
			((TransitionResultImpl<T, S, E, C>)parent).addSubResult(this);
		}
		return this;
    }

	@Override
    public boolean isDeclined() {
	    return !isAccepted();
    }
}
