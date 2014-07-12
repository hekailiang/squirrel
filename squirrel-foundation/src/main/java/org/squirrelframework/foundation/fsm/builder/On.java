package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * On clause builder which is used to build transition condition
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface On<T extends StateMachine<T, S, E, C>, S, E, C> extends When<T, S, E, C> {
    /**
     * Add condition for the transition
     * @param condition transition condition
     * @return When clause builder
     */
    When<T, S, E, C> when(Condition<C> condition);
    
    /**
     * Add condition for the transition
     * @param expression mvel expression
     * @return When clause builder
     */
    When<T, S, E, C> whenMvel(String expression);
}
