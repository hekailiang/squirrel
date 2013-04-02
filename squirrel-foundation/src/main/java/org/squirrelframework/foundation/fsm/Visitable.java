package org.squirrelframework.foundation.fsm;

public interface Visitable<T extends StateMachine<T, S, E, C>, S, E, C> {
    void accept(final Visitor<T, S, E, C> visitor);
}
