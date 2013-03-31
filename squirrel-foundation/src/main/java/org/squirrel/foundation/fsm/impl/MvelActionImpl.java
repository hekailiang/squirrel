package org.squirrel.foundation.fsm.impl;

import org.squirrel.foundation.fsm.Action;
import org.squirrel.foundation.fsm.StateMachine;

public class MvelActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {

    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        // TODO-hhe: support action defined in mvel scripts
    }
}
