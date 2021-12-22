package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.MethodReference;
import org.squirrelframework.foundation.fsm.StateMachine;

public class MethodReferenceActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    private final MethodReference<T, S, E, C> methodReference;

    public MethodReferenceActionImpl(MethodReference<T, S, E, C> methodReference) {
        this.methodReference = methodReference;
    }

    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        this.methodReference.invoke(stateMachine, from, to, event, context);
    }

    @Override
    public String name() {
        // useless as not meaningful
        return methodReference.getClass().getSimpleName();
    }

    @Override
    public int weight() {
        return NORMAL_WEIGHT;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public long timeout() {
        return -1;
    }
}
