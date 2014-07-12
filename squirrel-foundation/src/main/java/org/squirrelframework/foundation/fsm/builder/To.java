package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * On clause builder which is used to build transition event
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface To<T extends StateMachine<T, S, E, C>, S, E, C> {
    /**
     * Build transition event
     * @param event transition event
     * @return On clause builder
     */
    On<T, S, E, C> on(E event);
}
