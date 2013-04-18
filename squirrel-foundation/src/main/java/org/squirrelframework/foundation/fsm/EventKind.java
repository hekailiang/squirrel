package org.squirrelframework.foundation.fsm;

/**
 * Predefined event kind
 * 
 * @author Henry.He
 */
public enum EventKind {
	
	/**
	 * Indicate the event is fired when starting state machine
	 */
	START, 
	
	/**
	 * Indicate the event is fired when child states reached final state
	 */
	FINISH, 
	
	/**
	 * Indicate the event is fired when terminating state machine
	 */
	TERMINATE, 
	
	/**
	 * Indicate the event is defined by user
	 */
	CUSTOM
}
