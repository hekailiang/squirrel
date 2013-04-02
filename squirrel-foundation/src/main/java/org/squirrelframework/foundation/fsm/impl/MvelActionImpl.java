package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;

public class MvelActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {

    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        // TODO-hhe: support action defined in mvel scripts
    }
}
