package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.fsm.builder.*;

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
     * Create multiple external transitions builder with default priority
     * @return multiple external transitions builder
     */
    MultiTransitionBuilder<T, S, E, C> externalTransitions();
    
    /**
     * Start to build external transition, same as externalTransition
     * @return External transition builder
     */
    ExternalTransitionBuilder<T, S, E, C> transition();

    /**
     * The same as <code>externalTransitions</code>
     * @return multiple external transitions builder
     */
    MultiTransitionBuilder<T, S, E, C> transitions();

    /**
     * Create defer bound action builder
     * @return defer bound action builder
     */
    DeferBoundActionBuilder<T, S, E, C> transit();
    
    /**
     * Start to build local transition
     * @return Local transition builder
     */
    LocalTransitionBuilder<T, S, E, C> localTransition();

    /**
     * Create multiple local transitions builder with default priority
     * @return multiple local transitions builder
     */
    MultiTransitionBuilder<T, S, E, C> localTransitions();
    
    /**
     * Start to build internal transition
     * @return Internal transition builder
     */
    InternalTransitionBuilder<T, S, E, C> internalTransition();

    /**
     * Create external transition builder with priority
     * @param priority external transition priority
     * @return external transition builder with priority
     */
    ExternalTransitionBuilder<T, S, E, C> externalTransition(int priority);

    /**
     * Create multiple external transitions builder with priority
     * @param priority external transitions priority
     * @return multiple external transitions builder
     */
    MultiTransitionBuilder<T, S, E, C> externalTransitions(int priority);
    
    /**
     * Same as externalTransition
     * @param priority transition priority
     * @return External transition builder
     */
    ExternalTransitionBuilder<T, S, E, C> transition(int priority);

    /**
     * the same as <code>externalTransitions<code/>
     * @param priority external transitions priority
     * @return multiple external transitions builder
     */
    MultiTransitionBuilder<T, S, E, C> transitions(int priority);

    /**
     * Create local transition builder with priority
     * @param priority local transition priority
     * @return local transition builder
     */
    LocalTransitionBuilder<T, S, E, C> localTransition(int priority);

    /**
     * Create multiple local transitions builder with priority
     * @param priority local transition priority
     * @return local transition builder
     */
    MultiTransitionBuilder<T, S, E, C> localTransitions(int priority);

    /**
     * Create internal transition builder with priority
     * @param priority internal transition priority
     * @return internal transition
     */
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
    MutableState<T, S, E, C> defineLinkedState(S stateId, 
            StateMachineBuilder<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachineBuilder, 
            S initialLinkedState, Object... extraParams);
    
    /**
     * Define a timed state
     * @param stateId state id
     * @param initialDelay initial delay ms
     * @param timeInterval time period if null not repeat
     * @param autoEvent
     * @param autoContext
     * @return timed state
     */
    MutableState<T, S, E, C> defineTimedState(S stateId, long initialDelay, 
            long timeInterval, E autoEvent, C autoContext);
    
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
     * Define sequential child states on parent state without initial state
     * @param parentStateId id of parent state
     * @param childStateIds child states id of parent state
     */
    void defineNoInitSequentialStatesOn(S parentStateId, S... childStateIds);

    /**
     * Define sequential child states on parent state without initial state
     * @param parentStateId id of parent state
     * @param historyType history type of parent state
     * @param childStateIds child states id of parent state
     */
    void defineNoInitSequentialStatesOn(S parentStateId, HistoryType historyType, S... childStateIds);
    
    /**
     * Define sequential child states on parent state. For parallel state the history type always be none.
     * 
     * @param parentStateId id of parent state
     * @param childStateIds child states id of parent state. The first child state will be used as initial child state of parent state.
     */
    void defineParallelStatesOn(S parentStateId, S... childStateIds);
    
    /**
     * Define event for parallel transition finished
     * @param finishEvent
     */
    void defineFinishEvent(E finishEvent);
    
    /**
     * Define event for state machine started
     * @param startEvent
     */
    void defineStartEvent(E startEvent);
    
    /**
     * Define event for state machine terminated
     * @param terminateEvent
     */
    void defineTerminateEvent(E terminateEvent);
    
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
    
    /**
     * Create new state machine instance according to state machine definition
     * @param initialStateId the id of state machine initial state
     * @param configuration configuration for state machine
     * @param extraParams other parameters for instantiate state machine
     * @return new state machine
     */
    T newStateMachine(S initialStateId, StateMachineConfiguration configuration, Object... extraParams);
    
    /**
     * Set default state machine configuration for state machine instance created by this builder 
     * @param configure state machine default configuration
     */
    void setStateMachineConfiguration(StateMachineConfiguration configure);
}
