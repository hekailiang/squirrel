package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.component.SquirrelPostProcessor;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineIntercepter;
import org.squirrelframework.foundation.fsm.StateMachine.StateMachineEvent;

public abstract class AbstractStateMachineIntercepter<T extends StateMachine<T, S, E, C>, S, E, C> 
    implements StateMachineIntercepter<T, S, E, C>, SquirrelPostProcessor<T> {

    @Override
    public void postProcess(T component) {
        component.addListener(StateMachine.StateMachineEvent.class, new StateMachine.StateMachineListener<T, S, E, C>() {
            @Override
            public void stateMachineEvent(final StateMachineEvent<T, S, E, C> event) {
                if(event instanceof StateMachine.StartEvent) {
                    onStart(event.getStateMachine());
                } else if (event instanceof StateMachine.TerminateEvent) {
                    onTerminate(event.getStateMachine());
                } else if(event instanceof StateMachine.TransitionEvent) {
                    StateMachine.TransitionEvent<T, S, E, C> transitionEvent = (StateMachine.TransitionEvent<T, S, E, C>)event;
                    beforeOnTransition(transitionEvent.getStateMachine(), 
                                transitionEvent.getSourceState(), transitionEvent.getCause(), transitionEvent.getContext());
                    if (event instanceof StateMachine.TransitionBeginEvent) {
                        onTransitionBegin(transitionEvent.getStateMachine(), 
                                transitionEvent.getSourceState(), transitionEvent.getCause(), transitionEvent.getContext());
                    } else if (event instanceof StateMachine.TransitionCompleteEvent) {
                        StateMachine.TransitionCompleteEvent<T, S, E, C> transitionCompleteEvent = (StateMachine.TransitionCompleteEvent<T, S, E, C>)event;
                        onTransitionComplete(transitionCompleteEvent.getStateMachine(), transitionCompleteEvent.getSourceState(), 
                                transitionCompleteEvent.getTargetState(), transitionCompleteEvent.getCause(), transitionCompleteEvent.getContext());
                    } else if (event instanceof StateMachine.TransitionDeclinedEvent) {
                        onTransitionDeclined(transitionEvent.getStateMachine(), 
                                transitionEvent.getSourceState(), transitionEvent.getCause(), transitionEvent.getContext());
                    } else if (event instanceof StateMachine.TransitionExceptionEvent) {
                        StateMachine.TransitionExceptionEvent<T, S, E, C> transitionExceptionEvent = (StateMachine.TransitionExceptionEvent<T, S, E, C>)event;
                        onTransitionCausedException(transitionExceptionEvent.getException(),  
                                transitionExceptionEvent.getStateMachine(), transitionExceptionEvent.getSourceState(), 
                                transitionExceptionEvent.getCause(), transitionExceptionEvent.getContext());
                    }
                    afterOnTransition(transitionEvent.getStateMachine(), 
                            transitionEvent.getSourceState(), transitionEvent.getCause(), transitionEvent.getContext());
                }
            }
        }, "stateMachineEvent");
    }
}
