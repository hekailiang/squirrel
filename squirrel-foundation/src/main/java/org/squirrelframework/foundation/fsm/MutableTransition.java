package org.squirrelframework.foundation.fsm;

import java.util.List;

public interface MutableTransition<T extends StateMachine<T, S, E, C>, S, E, C> extends ImmutableTransition<T, S, E, C> {
    
    void setSourceState(ImmutableState<T, S, E, C> state);
    
    void setTargetState(ImmutableState<T, S, E, C> state);
    
    void addAction(Action<T, S, E, C> newAction);
    
    void addActions(List<Action<T, S, E, C>> newActions);
    
    void setCondition(Condition<C> condition);
    
    void setEvent(E event);
    
    void setType(TransitionType type);
    
    void setPriority(int priority);
}
