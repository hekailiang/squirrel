package org.squirrel.foundation.fsm.builder;

import org.squirrel.foundation.fsm.StateMachine;

public interface TransitionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {
    From<T, S, E, C> from(S stateId);
    To<T, S, E, C> within(S stateId);
}
