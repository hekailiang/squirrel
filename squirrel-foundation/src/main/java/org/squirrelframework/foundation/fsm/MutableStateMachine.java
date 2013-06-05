package org.squirrelframework.foundation.fsm;

public interface MutableStateMachine<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachine<T, S, E, C> {
    
    /**
     * Set last active child state of parent state
     * @param parentStateId id of parent state
     * @param childStateId id of child state
     */
    void setLastActiveChildState(S parentStateId, S childStateId);
    
    void setSubState(S parentState, S subState);
    
    void removeSubState(S parentState, S subState);
    
    void removeSubStatesOn(S parentState);

}
