package org.squirrelframework.foundation.fsm;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * This class is used to hold all the internal data of state machine. User can
 * dump a state machine data through {@link StateMachineData#dump(Reader)} which
 * means take a snapshot of state machine or save the current state machine
 * execution state.
 * 
 * @author Henry.He
 * 
 * @param <T>
 *            type of State Machine
 * @param <S>
 *            type of State
 * @param <E>
 *            type of Event
 * @param <C>
 *            type of Context
 */
public interface StateMachineData<T extends StateMachine<T, S, E, C>, S, E, C>
        extends Serializable {
    /**
     * Dump source state machine data (expect transient data, such as states)
     * into current state machine data
     * 
     * @param src
     *            source state machine data
     */
    void dump(StateMachineData.Reader<T, S, E, C> src);

    /**
     * @return state machine data reader
     */
    Reader<T, S, E, C> read();

    /**
     * @return state machine data writer
     */
    Writer<T, S, E, C> write();

    public interface Reader<T extends StateMachine<T, S, E, C>, S, E, C> {
        /**
         * @return current state id of state machine
         */
        S currentState();

        /**
         * @return last active state id of state machine
         */
        S lastState();

        /**
         * @return id of state machine initial state
         */
        S initialState();

        /**
         * @param parentStateId
         *            id of parent state
         * @return last active child state of the parent state
         */
        S lastActiveChildStateOf(S parentStateId);

        /**
         * @return all the active parent states
         */
        Collection<S> activeParentStates();

        /**
         * @param parentStateId
         * @return sub state of parallel state
         */
        List<S> subStatesOn(S parentStateId);

        /**
         * @return current raw state of state machine
         */
        ImmutableState<T, S, E, C> currentRawState();

        /**
         * @return last active raw state of state machine
         */
        ImmutableState<T, S, E, C> lastRawState();

        /**
         * @return initial raw state of state machine
         */
        ImmutableState<T, S, E, C> initialRawState();

        /**
         * @param stateId
         *            the identify of state
         * @return raw state of the same state identify
         */
        ImmutableState<T, S, E, C> rawStateFrom(S stateId);

        /**
         * @return all the parallel states
         */
        Collection<S> parallelStates();

        /**
         * @return type of state machine
         */
        Class<? extends T> typeOfStateMachine();

        /**
         * @return type of state
         */
        Class<S> typeOfState();

        /**
         * @return type of event
         */
        Class<E> typeOfEvent();

        /**
         * @return type of context
         */
        Class<C> typeOfContext();

        /**
         * @return all the raw states defined in the state machine
         */
        Collection<ImmutableState<T, S, E, C>> rawStates();

        /**
         * @return all the states defined in the state machine
         */
        Collection<S> states();

        /**
         * @return all linked states
         */
        Collection<S> linkedStates();

        Reader<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateDataOf(
                S linkedState);
    }

    public interface Writer<T extends StateMachine<T, S, E, C>, S, E, C> {

        /**
         * Write current state of state machine data to provided state id
         * 
         * @param currentStateId
         */
        void currentState(S currentStateId);

        /**
         * Write last state of state machine data to provided state id
         * 
         * @param lastStateId
         */
        void lastState(S lastStateId);

        /**
         * Write initial state of state machine data to provided state id
         * 
         * @param initialStateId
         */
        void initalState(S initialStateId);

        /**
         * Set last active child state of parent state
         * 
         * @param parentStateId
         *            id of parent state
         * @param childStateId
         *            id of child state
         */
        void lastActiveChildStateFor(S parentStateId, S childStateId);

        /**
         * Write provided sub state for provided parent state
         * 
         * @param parentStateId
         * @param subStateId
         */
        void subStateFor(S parentStateId, S subStateId);

        /**
         * Remove provide sub state under provided parent state
         * 
         * @param parentStateId
         * @param subStateId
         */
        void removeSubState(S parentStateId, S subStateId);

        /**
         * Remove all sub states under provider parent state
         * 
         * @param parentStateId
         */
        void removeSubStatesOn(S parentStateId);

        /**
         * Write type of state machine
         * 
         * @param stateMachineType
         */
        void typeOfStateMachine(Class<? extends T> stateMachineType);

        /**
         * Write type of state
         * 
         * @param stateClass
         */
        void typeOfState(Class<S> stateClass);

        /**
         * Write type of event
         * 
         * @param eventClass
         */
        void typeOfEvent(Class<E> eventClass);

        /**
         * Write type of context
         * 
         * @param contextClass
         */
        void typeOfContext(Class<C> contextClass);

        /**
         * Write linked state data on specified linked state
         * 
         * @param linkedState
         *            specified linked state
         * @param linkStateData
         *            linked state data
         */
        void linkedStateDataOn(
                S linkedState,
                StateMachineData.Reader<? extends StateMachine<?, S, E, C>, S, E, C> linkStateData);
    }
}
