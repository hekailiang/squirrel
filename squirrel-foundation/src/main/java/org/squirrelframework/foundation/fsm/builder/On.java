package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.StateMachine;

public interface On<T extends StateMachine<T, S, E, C>, S, E, C> extends When<T, S, E, C> {
    When<T, S, E, C> when(Condition<C> condition);
}
