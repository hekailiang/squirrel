package org.squirrelframework.foundation.fsm.monitor;

import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.impl.StateMachineIntercepterStub;

import com.google.common.base.Stopwatch;

public class TransitionExecTimeMonitor<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineIntercepterStub<T, S, E, C> {
	
	private Stopwatch sw;

    @Override
    public void onTransitionBegin(T stateMachine, S sourceState, E event, C context) {
        sw = new Stopwatch();
        sw.start();
    }

    @Override
    public void onTransitionComplete(T stateMachine, S sourceState, S targetState, E event, C context) {
        System.err.println(stateMachine.getClass().getCanonicalName() + ": Transition from \""
                + sourceState + "\" to \"" + targetState + "\" on \"" + event
                + "\" complete which took " + sw.elapsedMillis() + "ms.");
    }
}
