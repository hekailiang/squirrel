package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

public interface EntryExitActionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> extends When<T, S, E, C> {
}
