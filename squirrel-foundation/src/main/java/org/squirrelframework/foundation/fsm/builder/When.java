package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;

import java.util.List;

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
     * Define actions to be performed during transition. When used in multiple transition builder,
     * the actions will be sequentially assigned to each transition. If actions size is less than
     * transitions size, the last action will be assigned to the rest of transitions. null value in
     * actions will be skipped which means no action will be assigned to corresponding transition.
     * @param actions performed actions
     */
    void perform(List<? extends Action<T, S, E, C>> actions);
    
    /**
     * Define mvel action to be performed during transition
     * @param expression mvel expression
     */
    void evalMvel(String expression);
    
    /**
     * Define action method to be called during transition. When used in multiple transition builder,
     * the method name can be joined by '|'. Each method action will be sequentially assigned to each
     * transition. If actions size is less than transitions size, the last method action  will be assigned
     * to the rest of transitions. '_' represent a place holder which means no method action will be
     * assigned to corresponding transition.
     * @param methodName method name
     */
    void callMethod(String methodName);
}
