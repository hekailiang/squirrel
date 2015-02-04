package org.squirrelframework.foundation.issues;

/**
 * Created by kailianghe on 2/4/15.
 */
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;


public class Issue40 {

    private static final StateMachine.StartListener startListener =
            new StateMachine.StartListener() {
                @Override
                public void started(StateMachine.StartEvent event) {
                    Issue40.started(event);
                }
            };
    private static final StateMachine.StateMachineExceptionListener stateMachineExceptionListener =
            new StateMachine.StateMachineExceptionListener() {
                @Override
                public void stateMachineException(StateMachine.StateMachineExceptionEvent event) {
                    Issue40.stateMachineException(event);
                }
            };
    private static final StateMachine.StateMachineListener stateMachineListener =
            new StateMachine.StateMachineListener() {

                @Override
                public void stateMachineEvent(StateMachine.StateMachineEvent event) {
                    Issue40.stateMachineEvent(event);
                }
            };
    private static final StateMachine.TransitionBeginListener transitionBeginListener =
            new StateMachine.TransitionBeginListener() {
                @Override
                public void transitionBegin(StateMachine.TransitionBeginEvent event) {
                    Issue40.transitionBegin(event);
                }
            };
    private static final StateMachine.TransitionCompleteListener transitionCompleteListener =
            new StateMachine.TransitionCompleteListener() {
                @Override
                public void transitionComplete(StateMachine.TransitionCompleteEvent event) {
                    Issue40.transitionComplete(event);
                }
            };
    private static final StateMachine.TransitionDeclinedListener transitionDeclinedListener =
            new StateMachine.TransitionDeclinedListener() {
                @Override
                public void transitionDeclined(StateMachine.TransitionDeclinedEvent event) {
                    transitionDeclined(event);
                }
            };
    private static final StateMachine.TransitionEndListener transitionEndListener =
            new StateMachine.TransitionEndListener() {
                @Override
                public void transitionEnd(StateMachine.TransitionEndEvent event) {
                    Issue40.transitionEnd(event);
                }
            };
    private static final StateMachine.TransitionExceptionListener transitionExceptionListener =
            new StateMachine.TransitionExceptionListener() {
                @Override
                public void transitionException(StateMachine.TransitionExceptionEvent event) {
                    Issue40.transitionException(event);
                }
            };

    @Test
    public void testRemoveEvents () {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        builder.externalTransition().from("A").to("B").on(FSMEvent.ToB).callMethod("fromAToB");
        builder.onEntry("B").callMethod("ontoB");

        UntypedStateMachine fsm = builder.newStateMachine("A");
        attachListenersToFSM(fsm);
        fsm.fire(FSMEvent.ToB, 10);
        detachListenersFromFSM(fsm);
        System.out.println("Current state is " + fsm.getCurrentState());
    }

    public static void attachListenersToFSM ( UntypedStateMachine fsm ) {
        fsm.addStartListener(startListener);
        fsm.addStateMachineExceptionListener(stateMachineExceptionListener);
        fsm.addStateMachineListener(stateMachineListener);
        fsm.addTransitionBeginListener(transitionBeginListener);
        fsm.addTransitionCompleteListener(transitionCompleteListener);
        fsm.addTransitionDeclinedListener(transitionDeclinedListener);
        fsm.addTransitionEndListener(transitionEndListener);
        fsm.addTransitionExceptionListener(transitionExceptionListener);
    }

    public static void detachListenersFromFSM ( UntypedStateMachine fsm ) {
        fsm.removeTransitionExceptionListener(transitionExceptionListener);
        fsm.removeTransitionEndListener(transitionEndListener);
        fsm.removeTransitionDeclinedListener(transitionDeclinedListener);
        fsm.removeTransitionCompleteListener(transitionCompleteListener);
        fsm.removeTransitionBeginListener(transitionBeginListener);
        fsm.removeStateMachineListener(stateMachineListener);
        fsm.removeStateMachineExceptionListener(stateMachineExceptionListener);
        fsm.removeStartListener(startListener);
    }

    private static void started ( StateMachine.StartEvent e) {
    }

    private static void stateMachineEvent ( StateMachine.StateMachineEvent e) {
    }

    private static void stateMachineException ( StateMachine.StateMachineExceptionEvent e) {
    }

    private static void transitionBegin ( StateMachine.TransitionBeginEvent e) {
    }

    private static void transitionComplete ( StateMachine.TransitionCompleteEvent e) {
    }

    private static void transitionDeclined ( StateMachine.TransitionDeclinedEvent e) {
    }


    private static void transitionEnd (StateMachine.TransitionEndEvent e) {
    }

    private static void transitionException (StateMachine.TransitionExceptionEvent e) {
    }

    @StateMachineParameters( stateType = String.class, eventType = FSMEvent.class, contextType = Integer.class )
    static class StateMachineSample extends AbstractUntypedStateMachine {
        protected void fromAToB ( String from, String to, FSMEvent event, Integer context ) {
            System.out.println("Transition from '" + from + "' to '" + to + "' on event '" + event + "' with context '" + context + "'.");
        }

        protected void ontoB ( String from, String to, FSMEvent event, Integer context ) {
            System.out.println("Entry State \'" + to + "\'.");
        }
    }

    enum FSMEvent {
        ToA, ToB, ToC, ToD
    }

}