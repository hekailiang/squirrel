package org.squirrelframework.foundation.fsm;

public interface TransitionResult<T extends StateMachine<T, S, E, C>, S, E, C> {
	boolean isAccepted();
	
	ImmutableState<T, S, E, C> getTargetState();
}
