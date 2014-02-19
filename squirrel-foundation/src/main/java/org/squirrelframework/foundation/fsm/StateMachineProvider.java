package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.SquirrelInstanceProvider;

@Deprecated
public class StateMachineProvider<T extends StateMachine<T, S, E, C>, S, E, C> implements SquirrelInstanceProvider<T> {
    
    private final StateMachineBuilder<T, S, E, C> stateMachineBuilder;
    
    private final S initialState;
    
    private final Object[] extraParams;
    
    public StateMachineProvider(
            StateMachineBuilder<T, S, E, C> stateMachineBuilder,
            S initialState, Object[] extraParams) {
        this.stateMachineBuilder = stateMachineBuilder;
        this.initialState = initialState;
        this.extraParams = extraParams;
    }
    
    public T get() {
        return stateMachineBuilder.newStateMachine(initialState, extraParams);
    }
}
