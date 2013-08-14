package org.squirrelframework.foundation.fsm;

import java.util.List;

public interface MutableState<T extends StateMachine<T, S, E, C>, S, E, C> extends ImmutableState<T, S, E, C> {
    
    MutableTransition<T, S, E, C> addTransitionOn(E event);
    
    void addEntryAction(Action<T, S, E, C> newAction);
    
    void addEntryActions(List<Action<T, S, E, C>> newActions);
    
    void addExitAction(Action<T, S, E, C> newAction);
    
    void addExitActions(List<Action<T, S, E, C>> newActions);
    
    void setParentState(MutableState<T, S, E, C> parent);
    
    void addChildState(MutableState<T, S, E, C> childState);
    
    void setInitialState(MutableState<T, S, E, C> childInitialState);
    
    void setLevel(int level);
    
    void setHistoryType(HistoryType historyType);
    
    void setFinal(boolean isFinal);
    
    void setCompositeType(StateCompositeType compositeType);
    
    void prioritizeTransitions();
}
