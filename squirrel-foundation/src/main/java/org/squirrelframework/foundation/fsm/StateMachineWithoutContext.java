package org.squirrelframework.foundation.fsm;

/**
 * State machine which does not include environmental context during state transition.
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 */
public interface StateMachineWithoutContext<T extends StateMachineWithoutContext<T, S, E>, S, E> extends StateMachine<T, S, E, Void> {
    
    /**
     * Fire event
     * @param event
     */
    void fire(E event);
    
    /**
     * Test event
     * @param event
     */
    S test(E event);
    
    /**
     * Start state machine
     */
    void start();
    
    /**
     * Terminate state machine
     */
    void terminate();
}
