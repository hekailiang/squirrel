package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.SquirrelComponent;

/**
 * An activity that is executed during transition happening.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface Action<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelComponent {
    /**
     * Execute the activity.
     * 
     * @param from transition source state
     * @param to transition target state
     * @param event event that trigger the transition
     * @param context context object
     * @param stateMachine the state machine
     */
    void execute(S from, S to, E event, C context, T stateMachine);
}
