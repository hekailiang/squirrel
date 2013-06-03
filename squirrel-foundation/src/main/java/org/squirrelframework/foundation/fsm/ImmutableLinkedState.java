package org.squirrelframework.foundation.fsm;

public interface ImmutableLinkedState<T extends StateMachine<T, S, E, C>, S, E, C> extends ImmutableState<T, S, E, C> {
    StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> getLinkedStateMachine();
}
