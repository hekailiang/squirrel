package org.squirrel.foundation.fsm.builder;

import org.squirrel.foundation.fsm.StateMachine;

public interface To<T extends StateMachine<T, S, E, C>, S, E, C> {
    On<T, S, E, C> on(E event);
}
