package org.squirrelframework.foundation.fsm;

public interface MutableLinkedState<T extends StateMachine<T, S, E, C>, S, E, C> extends MutableState<T, S, E, C> {
    void setLinkedStateMachine(StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachine);
}
