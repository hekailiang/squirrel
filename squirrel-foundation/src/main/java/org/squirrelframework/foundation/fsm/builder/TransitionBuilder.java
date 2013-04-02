package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

public interface TransitionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {
    From<T, S, E, C> from(S stateId);
    To<T, S, E, C> within(S stateId);
}
