package org.squirrel.foundation.fsm.builder;

import org.squirrel.foundation.fsm.StateMachine;

public interface From<T extends StateMachine<T, S, E, C>, S, E, C> {
    To<T, S, E, C> to(S stateId);
    To<T, S, E, C> toFinal();
}
