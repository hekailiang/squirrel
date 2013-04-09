package org.squirrelframework.foundation.fsm;

/**
 * Define the relationship between child states of current state.
 * 
 * @author Henry.He
 */
public enum StateType {
	/**
	 * The child states are mutually exclusive and an initial state must 
	 * be set by calling MutableState.setInitialState()
	 */
	ExclusiveStates, 
	
	/**
	 * The child states are parallel. When the parent state is entered, 
	 * all its child states are entered in parallel.
	 */
	ParallelStates
}
