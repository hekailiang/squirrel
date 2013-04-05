package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.TransitionResult;
import org.squirrelframework.foundation.util.TypeReference;

class TransitionResultImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements TransitionResult<T, S, E, C> {
	
	private final  boolean accepted;
	
	private final ImmutableState<T, S, E, C> targetState;
	
	@SuppressWarnings("rawtypes")
    private static final TransitionResult NOT_ACCEPTED = SquirrelProvider.getInstance().newInstance(
			TransitionResult.class, new Class<?>[]{boolean.class, ImmutableState.class}, new Object[]{false, null});
	
	TransitionResultImpl(boolean accepted, ImmutableState<T, S, E, C> targetState) {
		this.accepted = accepted;
		this.targetState = targetState;
	}
	
	@SuppressWarnings("unchecked")
    public static <T extends StateMachine<T, S, E, C>, S, E, C> TransitionResult<T, S, E, C> notAccepted() {
		return NOT_ACCEPTED;
	}
	
	public static <T extends StateMachine<T, S, E, C>, S, E, C> TransitionResult<T, S, E, C> newResult(
			boolean accepted, ImmutableState<T, S, E, C> targetState) {
		return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionResult<T, S, E, C>>() {
		}, new Class<?>[]{boolean.class, ImmutableState.class}, new Object[]{accepted, targetState});
	}

	@Override
    public boolean isAccepted() {
	    return accepted;
    }

	@Override
    public ImmutableState<T, S, E, C> getTargetState() {
	    return targetState;
    }
}
