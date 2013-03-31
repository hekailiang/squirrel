package org.squirrel.foundation.fsm;

public interface StateContext<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    T getStateMachine();
    
    ImmutableState<T, S, E, C> getSourceState();
    
    C getContext();
    
    E getEvent();
}
