package org.squirrelframework.foundation.issues;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.AnonymousAction;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import static org.junit.Assert.assertEquals;

public class Issue31 {
    @Test
    public void performsTransactionOnEventFromAction() {
        FSM fsm = createFSM();
        fsm.start();
        fsm.fire(Event.Stop);
        assertEquals(State.Working, fsm.getCurrentState());
    }

    private static FSM createFSM() {
        StateMachineBuilder<FSM, State, Event, Void> builder =
                StateMachineBuilderFactory.create(FSM.class, State.class, Event.class, Void.class);

        builder.externalTransition().from(State.Idle).to(State.Working).on(Event.Start);
        builder.externalTransition().from(State.Working).to(State.Idle).on(Event.Stop);


        builder.onEntry(State.Idle).perform(new LoggerAction("Idle entry") {
            @Override
            public void execute(State from, State to, Event event, Void context, FSM stateMachine) {
                super.execute(from, to, event, context, stateMachine);
                stateMachine.fire(Event.Start);
            }
        });
        builder.onExit(State.Idle).perform(new LoggerAction("Idle exit"));
        builder.onEntry(State.Working).perform(new LoggerAction("Working entry"));
        builder.onExit(State.Working).perform(new LoggerAction("Working exit"));

        return builder.newStateMachine(State.Idle);
    }

    private static class LoggerAction extends AnonymousAction<FSM, State, Event, Void> {
        private final String str;

        public LoggerAction(String str) {
            this.str = str;
        }

        @Override
        public void execute(State from, State to, Event event, Void context, FSM stateMachine) {
            System.err.println(str);
        }
    }

    @ContextInsensitive
    private static class FSM extends AbstractStateMachine<FSM, State, Event, Void> {
    }

    private static enum State {
        Idle,
        Working
    }

    private static enum Event {
        Start,
        Stop
    }
}

