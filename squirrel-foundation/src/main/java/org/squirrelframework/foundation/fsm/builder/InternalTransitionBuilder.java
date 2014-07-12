package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * Internal transition builder which is used to build a internal transition
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface InternalTransitionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {
    /**
     * Build a internal transition
     * @param stateId id of transition
     * @return To clause builder
     */
    To<T, S, E, C> within(S stateId);
}
