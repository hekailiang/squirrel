package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * Created by kailianghe on 7/12/14.
 */
public interface MultiTransitionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {

    /**
     * Build transition source state.
     * @param stateId id of state
     * @return multi from clause builder
     */
    MultiFrom<T, S, E, C> from(S stateId);

    /**
     * Build transition source states.
     * @param stateIds id of states
     * @return single from clause builder
     */
    From<T, S, E, C> fromAmong(S... stateIds);

    /**
     * Build mutual transitions between two state
     * @param fromStateId from state id
     * @return between clause builder
     */
    Between<T, S, E, C> between(S fromStateId);
}
