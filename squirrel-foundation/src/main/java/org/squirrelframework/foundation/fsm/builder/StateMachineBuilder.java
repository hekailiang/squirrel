package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * State machine builder API.
 * 
 * @author Henry.He
 *
 * @param <T> The type of implemented state machine
 * @param <S> The type of implemented state
 * @param <E> The type of implemented event
 * @param <C> The type of implemented context
 */
public interface StateMachineBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {
	
	/**
	 * Start to build external transition
	 * @return External transition builder
	 */
    ExternalTransitionBuilder<T, S, E, C> externalTransition();
    
    /**
     * Start to build local transition
     * @return Local transition builder
     */
    LocalTransitionBuilder<T, S, E, C> localTransition();
    
    /**
     * Start to build internal transition
     * @return Internal transition builder
     */
    InternalTransitionBuilder<T, S, E, C> internalTransition();
    
    /**
     * Define a new state in state machine model
     * @param stateId id of new state
     * @return defined new immutable state
     */
    MutableState<T, S, E, C> defineState(S stateId);
    
    void defineSequentialStatesOn(S parentStateId, S... childStateIds);
    
    /**
     * Define sequential child states on parent state
     * 
     * @param parentStateId id of parent state
     * @param historyType history type of parent state
     * @param childStateIds child states id of parent state. The first child state will be used as initial child state of parent state.
     */
    void defineSequentialStatesOn(S parentStateId, HistoryType historyType, S... childStateIds);
    
    /**
     * Define sequential child states on parent state. For parallel state the history type always be none.
     * 
     * @param parentStateId id of parent state
     * @param childStateIds child states id of parent state. The first child state will be used as initial child state of parent state.
     */
    void defineParallelStatesOn(S parentStateId, S... childStateIds);
    
    /**
     * Define on entry actions for state
     * @param stateId the id of state
     * @return the builder to build state on entry actions
     */
    EntryExitActionBuilder<T, S, E, C> onEntry(S stateId);
    
    /**
     * Define on exit actions for state
     * @param stateId the id of state
     * @return the builder to build state on exit actions
     */
    EntryExitActionBuilder<T, S, E, C> onExit(S stateId);
    
    /**
     * Create a new state machine instance
     * @param initialStateId initial state id
     * @return new state machine instance
     */
    T newStateMachine(S initialStateId);
    
    /**
     * Create new state machine instance according to state machine definition 
     * @param initialStateId the id of state machine initial state
     * @param extraParams other parameters for instantiate state machine
     * @return new state machine
     */
    T newStateMachine(S initialStateId, Object... extraParams);
}
