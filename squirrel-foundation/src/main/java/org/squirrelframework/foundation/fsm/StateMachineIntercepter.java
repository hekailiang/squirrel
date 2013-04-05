package org.squirrelframework.foundation.fsm;

public interface StateMachineIntercepter<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    void onStart(T stateMachine);
    
    void onTerminate(T stateMachine);
    
    void beforeOnTransition(T stateMachine, S sourceState, E event, C context);
    
    void onTransitionBegin(T stateMachine, S sourceState, E event, C context);
    
    void onTransitionComplete(T stateMachine, S sourceState, S targetState, E event, C context);
    
    void onTransitionDeclined(T stateMachine, S sourceState, E event, C context);
    
    void onTransitionCausedException(Exception e, T stateMachine, S sourceState, E event, C context);
    
    void afterOnTransition(T stateMachine, S sourceState, E event, C context);
}
