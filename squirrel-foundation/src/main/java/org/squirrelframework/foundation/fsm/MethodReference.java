package org.squirrelframework.foundation.fsm;

@FunctionalInterface
public interface MethodReference<T extends StateMachine<T, S, E, C>, S, E, C> {
    void invoke(T instance, S from, S to, E event, C context);
}
