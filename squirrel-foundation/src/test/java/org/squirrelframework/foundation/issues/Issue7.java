package org.squirrelframework.foundation.issues;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachineWithoutContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Issue7 {
    @Test public void performsTransactionOnEventFromAction() {
        FSM fsm = createFSM();
        fsm.fire(Event.GoWild);
        assertEquals(State.GoneWild, fsm.getCurrentState());
        fsm.fire(Event.CalmDown);
        assertEquals(State.Calm, fsm.getCurrentState());
    }

    private static FSM createFSM() {
        StateMachineBuilder<FSM, State, Event, Void> builder =
                StateMachineBuilderFactory.create(FSM.class, State.class, Event.class);

        builder.externalTransition().from(State.Calm).to(State.GoingWild).on(Event.GoWild);
        builder.externalTransition().from(State.GoingWild).to(State.GoneWild).on(Event.GoneWild);
        builder.externalTransition().from(State.GoneWild).to(State.Calm).on(Event.CalmDown);

        builder.onEntry(State.GoingWild).perform(new Action<FSM, State, Event, Void>() {
            @Override
            public void execute(State from, State to, Event event, Void context, FSM stateMachine) {
                // let's assume we are ready to go wild already and just fire event
                stateMachine.fire(Event.GoneWild);
            }
        });
        builder.onEntry(State.GoneWild).perform(new Action<FSM, State, Event, Void>() {
            @Override
            public void execute(State from, State to, Event event, Void context, FSM stateMachine) {
                throw new RuntimeException("Oops!");
            }
        });
        return builder.newStateMachine(State.Calm);
    }

    private static class FSM extends AbstractStateMachineWithoutContext<FSM, State, Event> {
        protected FSM(ImmutableState<FSM, State, Event, Void> initialState,
                      Map<State, ImmutableState<FSM, State, Event, Void>> states) {
            super(initialState, states);
        }
    }

    private static enum State {
        GoneWild,
        GoingWild,
        Calm
    }

    private static enum Event {
        GoneWild,
        GoWild,
        CalmDown
    }
}
