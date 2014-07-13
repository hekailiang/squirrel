package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * Created by kailianghe on 7/12/14.
 */
public interface And<T extends StateMachine<T, S, E, C>, S, E, C> {
    /**
     * Specify mutual transition events
     * @param fromEvent cause transition from fromState to toState
     * @param toEvent cause transition from toState to fromState
     * @return on clause builder
     */
    On<T, S, E, C> onMutual(E fromEvent, E toEvent);
}
