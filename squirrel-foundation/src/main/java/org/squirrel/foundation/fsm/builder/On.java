package org.squirrel.foundation.fsm.builder;

import org.squirrel.foundation.fsm.Condition;
import org.squirrel.foundation.fsm.StateMachine;

public interface On<T extends StateMachine<T, S, E, C>, S, E, C> extends When<T, S, E, C> {
    When<T, S, E, C> when(Condition<C> condition);
}
