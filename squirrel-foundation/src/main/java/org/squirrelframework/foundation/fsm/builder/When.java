package org.squirrelframework.foundation.fsm.builder;

import java.util.List;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;


public interface When<T extends StateMachine<T, S, E, C>, S, E, C> {
    void perform(Action<T, S, E, C> action);
    void perform(List<Action<T, S, E, C>> actions);
}
