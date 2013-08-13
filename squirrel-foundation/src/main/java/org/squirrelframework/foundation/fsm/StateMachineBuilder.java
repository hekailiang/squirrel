package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.fsm.builder.EntryExitActionBuilder;
import org.squirrelframework.foundation.fsm.builder.ExternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.InternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.LocalTransitionBuilder;

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
    
    ExternalTransitionBuilder<T, S, E, C> externalTransition(int priority);
    
    LocalTransitionBuilder<T, S, E, C> localTransition(int priority);
    
    InternalTransitionBuilder<T, S, E, C> internalTransition(int priority);
    
    /**
     * Define a new state in state machine model
     * @param stateId id of new state
     * @return defined new mutable state
     */
    MutableState<T, S, E, C> defineState(S stateId);
    
    /**
     * Define a final state in state machine model
     * @param stateId id of final state
     * @return defined final state
     */
    MutableState<T, S, E, C> defineFinalState(S stateId);
    
    /**
     * Define a linked state
     * @param stateId id of linked state
     * @param linkedStateMachineBuilder linked state machine builder
     * @param initialLinkedState initial linked state
     * @param extraParams additional parameters used to create linked state machine
     * @return linked state
     */
    MutableState<T, S, E, C> definedLinkedState(S stateId, 
            StateMachineBuilder<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachineBuilder, 
            S initialLinkedState, Object... extraParams);
    
    /**
     * Define sequential child states whose hierarchy type is default set to NONE on parent state
     * @param parentStateId id of parent state
     * @param childStateIds child states id of parent state. The first child state will be used as initial child state of parent state.
     */
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
