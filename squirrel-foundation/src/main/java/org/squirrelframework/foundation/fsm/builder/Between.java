package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * Created by kailianghe on 7/12/14.
 */
public interface Between<T extends StateMachine<T, S, E, C>, S, E, C> {
    /**
     * Build mutual transitions between state
     * @param toStateId to state id
     * @return and clause builder
     */
    And<T, S, E, C> and(S toStateId);
}
