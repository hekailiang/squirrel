package org.squirrelframework.foundation.fsm;

/**
 * Then context of state machine when processing any events
 * 
 * @author Henry.He
 *
 * @param <T> state machine type
 * @param <S> state type
 * @param <E> event type
 * @param <C> context type
 */
public interface StateContext<T extends StateMachine<T, S, E, C>, S, E, C> {
    
	/**
	 * @return current state machine object
	 */
    StateMachine<T, S, E, C> getStateMachine();
    
    /**
     * @return state machine data
     */
    StateMachineData<T, S, E, C> getStateMachineData();
    
    /**
     * @return source state of state machine
     */
    ImmutableState<T, S, E, C> getSourceState();
    
    /**
     * @return external context object
     */
    C getContext();
    
    /**
     * @return event 
     */
    E getEvent();
    
    /**
     * @return transition result
     */
    TransitionResult<T, S, E, C> getResult();
    
    /**
     * @return action executor
     */
    ActionExecutionService<T, S, E, C> getExecutor();
}
