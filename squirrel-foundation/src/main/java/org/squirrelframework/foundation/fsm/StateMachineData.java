package org.squirrelframework.foundation.fsm;

import java.util.Collection;
import java.util.List;

public interface StateMachineData<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    void dump(StateMachineData<T, S, E, C> src);
    
    /**
     * @return type of state machine
     */
    Class<? extends T> getTypeOfStateMachine();
    
    void setTypeOfStateMachine(Class<? extends T> stateMachineType);
    
    /**
     * @return type of state
     */
    Class<S> getTypeOfState();
    
    void setTypeOfState(Class<S> stateClass);
    
    /**
     * @return type of event
     */
    Class<E> getTypeOfEvent();
    
    void setTypeOfEvent(Class<E> eventClass);
    
    /**
     * @return type of context
     */
    Class<C> getTypeOfContext();
    
    void setTypeOfContext(Class<C> contextClass);
    
    Collection<ImmutableState<T, S, E, C>> getRawStates();
    
    Collection<S> getStates();
    
    /**
     * @param stateId the identify of state 
     * @return raw state of the same state identify
     */
    ImmutableState<T, S, E, C> getRawStateFrom(S stateId);
    
    Reader<T, S, E, C> read();
    
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
         * @param parentStateId id of parent state
         * @return last active child state of the parent state
         */
        S lastActiveChildStateOf(S parentStateId);
        
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
        
        Collection<S> parallelStates();
    }
    
    Writer<T, S, E, C> write();
    
    public interface Writer<T extends StateMachine<T, S, E, C>, S, E, C> {
        
        void currentState(S currentStateId);
        
        void lastState(S lastStateId);
        
        void initalState(S initialStateId);
        
        /**
         * Set last active child state of parent state
         * @param parentStateId id of parent state
         * @param childStateId id of child state
         */
        void lastActiveChildStateFor(S parentStateId, S childStateId);
        
        void subStateFor(S parentStateId, S subStateId);
        
        void removeSubState(S parentStateId, S subStateId);
        
        void removeSubStatesOn(S parentStateId);
    }
}
