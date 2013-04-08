package org.squirrelframework.foundation.fsm;

public interface StateContext<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    T getStateMachine();
    
    ImmutableState<T, S, E, C> getSourceState();
    
    C getContext();
    
    E getEvent();
    
    ImmutableState<T, S, E, C> getLastActiveChildStateOf(ImmutableState<T, S, E, C> parentState);
    
    void setLastActiveChildState(ImmutableState<T, S, E, C> parentState, ImmutableState<T, S, E, C> childState);
}
