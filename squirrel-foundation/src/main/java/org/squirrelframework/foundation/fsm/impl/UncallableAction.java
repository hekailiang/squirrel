package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;

final class UncallableActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    
    private final Action<T, S, E, C> action;
    
    UncallableActionImpl(Action<T, S, E, C> action) {
        this.action = action;
    }

    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        throw new UnsupportedOperationException("Cannot invoke uncallable action.");
    }

    @Override
    public String name() {
        return action.name();
    }

    @Override
    public int weight() {
        return action.weight();
    }
    
    @Override
    public final String toString() {
        return action.toString();
    }

    @Override
    public boolean isAsync() {
        return action.isAsync();
    }

    @Override
    public long timeout() {
        return action.timeout();
    }
}
