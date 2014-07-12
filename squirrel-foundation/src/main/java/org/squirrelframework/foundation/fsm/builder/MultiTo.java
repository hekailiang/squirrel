package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * Created by kailianghe on 7/12/14.
 */
public interface MultiTo<T extends StateMachine<T, S, E, C>, S, E, C> {
    /**
     * Build transition event
     * @param events transition event
     * @return On clause builder
     */
    On<T, S, E, C> onEach(E... events);
}
