package org.squirrelframework.foundation.fsm.builder;

import org.squirrelframework.foundation.fsm.StateMachine;

public interface DeferBoundActionTo<T extends StateMachine<T, S, E, C>, S, E, C> {

    /**
     * Build transition event
     * @param event transition event
     * @return On clause builder
     */
    When<T, S, E, C> on(E event);
    
    When<T, S, E, C> onAny(E event);
    
}
