package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

public interface LocalTransitionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> extends ExternalTransitionBuilder<T, S, E, C> {
}
