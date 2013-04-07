package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

public interface ExternalTransitionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {
    From<T, S, E, C> from(S stateId);
}
