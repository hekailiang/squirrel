package org.squirrelframework.foundation.fsm;

/**
 * Indicate state machine running status
 * 
 * @author Henry.He
 *
 */
public enum StateMachineStatus {
	
	/**
	 * State machine is instantiated but not started
	 */
    INITIALIZED, 
    
    /**
     * State machine is started but not processing any event
     */
    IDLE, 
    
    /**
     * State machine is processing events
     */
    BUSY, 
    
    /**
     * State machine is terminated
     */
    TERMINATED, 
    
    /**
     * State machine is in error state
     */
    ERROR
}
