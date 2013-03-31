package org.squirrel.foundation.fsm.builder;

import org.squirrel.foundation.fsm.StateMachine;

public interface EntryExitActionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> extends When<T, S, E, C> {
}
