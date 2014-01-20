package org.squirrelframework.foundation.fsm;

public class StateMachineProvider<T extends StateMachine<T, S, E, C>, S, E, C> {
    
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
    
    public StateMachine<T, S, E, C> get() {
        return stateMachineBuilder.newStateMachine(initialState, extraParams);
    }
}
