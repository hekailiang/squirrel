package org.squirrelframework.foundation.fsm;

import java.io.Serializable;

/**
 * method reference for reference transaction action using method reference starts from jdk8
 * 
 * @param <T> type of statemachine
 * @param <S> type of state
 * @param <E> type of event
 * @param <C> type of context
 */
@FunctionalInterface
public interface MethodReference<T extends StateMachine<T, S, E, C>, S, E, C> extends Serializable {
    void invoke(T instance, S from, S to, E event, C context);
}
