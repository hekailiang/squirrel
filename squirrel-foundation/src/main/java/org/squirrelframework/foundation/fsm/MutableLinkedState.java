package org.squirrelframework.foundation.fsm;

/**
 * Mutable linked state.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface MutableLinkedState<T extends StateMachine<T, S, E, C>, S, E, C> extends MutableState<T, S, E, C> {
    
    /**
     * Set linked state machine
     * @param linkedStateMachine linked state machine
     */
    void setLinkedStateMachine(StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachine);
}
