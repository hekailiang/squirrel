package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.StateMachine;

public class StateMachineIntercepterStub<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractStateMachineIntercepter<T, S, E, C> {

	@Override
    public void onStart(T stateMachine) {
    }

	@Override
    public void onTerminate(T stateMachine) {
    }

	@Override
    public void beforeOnTransition(T stateMachine, S sourceState, E event, C context) {
    }

	@Override
    public void onTransitionBegin(T stateMachine, S sourceState, E event, C context) {
    }

	@Override
    public void onTransitionComplete(T stateMachine, S sourceState, S targetState, E event, C context) {
    }

	@Override
    public void onTransitionDeclined(T stateMachine, S sourceState, E event, C context) {
    }

	@Override
    public void onTransitionCausedException(Exception e, T stateMachine, S sourceState, E event, C context) {
    }

	@Override
    public void afterOnTransition(T stateMachine, S sourceState, E event, C context) {
    }

}
