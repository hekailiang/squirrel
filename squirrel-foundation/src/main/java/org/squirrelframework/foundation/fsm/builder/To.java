package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

public interface To<T extends StateMachine<T, S, E, C>, S, E, C> {
    On<T, S, E, C> on(E event);
}
