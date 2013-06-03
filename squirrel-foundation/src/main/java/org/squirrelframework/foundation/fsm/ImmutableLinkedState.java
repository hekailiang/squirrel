package org.squirrelframework.foundation.fsm;

/**
 * Immutable linked state.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface ImmutableLinkedState<T extends StateMachine<T, S, E, C>, S, E, C> extends ImmutableState<T, S, E, C> {
    
    /**
     * @return linked state machine
     */
    StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> getLinkedStateMachine();
}
