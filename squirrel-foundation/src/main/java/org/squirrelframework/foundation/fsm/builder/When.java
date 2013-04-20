package org.squirrelframework.foundation.fsm.builder;

import java.util.List;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * When clause builder which is used to install actions during transition
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface When<T extends StateMachine<T, S, E, C>, S, E, C> {
	/**
	 * Define action to be performed during transition
	 * @param action performed action
	 */
    void perform(Action<T, S, E, C> action);
    /**
     * Define actions to be performed during transition
     * @param actions performed actions
     */
    void perform(List<Action<T, S, E, C>> actions);
}
