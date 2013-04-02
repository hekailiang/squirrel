package org.squirrelframework.foundation.fsm;

import java.util.List;

import org.squirrelframework.foundation.component.SquirrelComponent;

public interface Actions<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelComponent {
    void add(Action<T, S, E, C> newAction);
    
    void addAll(List<Action<T, S, E, C>> newActions);
    
    List<Action<T, S, E, C>> getAll();
}
