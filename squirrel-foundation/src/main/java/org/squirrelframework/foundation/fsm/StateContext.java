package org.squirrelframework.foundation.fsm;

import java.util.List;

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
    T getStateMachine();
    
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
     * Search state machine active child state store and return last active child state of parent state
     * @param parentState parent state
     * @return last active child state of parent state
     */
    ImmutableState<T, S, E, C> getLastActiveChildStateOf(ImmutableState<T, S, E, C> parentState);
    
    /**
     * Update state machine active child state store
     * @param parentState parent state
     * @param childState last active child state
     */
    void setLastActiveChildState(ImmutableState<T, S, E, C> parentState, ImmutableState<T, S, E, C> childState);
    
    /**
     * @param parentState parent state
     * @return sub-states of parent parallel stae
     */
    List<ImmutableState<T, S, E, C>> getSubStatesOn(ImmutableState<T, S, E, C> parentState);
    
    /**
     * @return transition result
     */
    TransitionResult<T, S, E, C> getResult();
    
    /**
     * @return action executor
     */
    ActionExecutor<T, S, E, C> getExecutor();
}
