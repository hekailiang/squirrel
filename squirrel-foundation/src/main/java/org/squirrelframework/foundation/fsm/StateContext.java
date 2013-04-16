package org.squirrelframework.foundation.fsm;

import java.util.List;

public interface StateContext<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    T getStateMachine();
    
    ImmutableState<T, S, E, C> getSourceState();
    
    C getContext();
    
    E getEvent();
    
    ImmutableState<T, S, E, C> getLastActiveChildStateOf(ImmutableState<T, S, E, C> parentState);
    
    void setLastActiveChildState(ImmutableState<T, S, E, C> parentState, ImmutableState<T, S, E, C> childState);
    
    List<ImmutableState<T, S, E, C>> getSubStatesOn(ImmutableState<T, S, E, C> parentState);
    
    TransitionResult<T, S, E, C> getResult();
    
    ActionExecutor<T, S, E, C> getExecutor();
}
