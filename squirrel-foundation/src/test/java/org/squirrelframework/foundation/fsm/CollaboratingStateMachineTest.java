package org.squirrelframework.foundation.fsm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedAnonymousAction;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

/**
 * Tests two interacting FSMs.
 * <p>
 * A state change action running on the first FSM fires an event against the
 * second FSM. The second FSM's state should change accordingly.
 * 
 * @author Jeremy.Stone
 */
public class CollaboratingStateMachineTest {
    /*
     * Pair of FSMs. Two states each (A and B) with transitions allowed in
     * either direction.
     * 
     * One FSM starts at A; the other at B.
     */
    UntypedStateMachine fsmAtoB;

    UntypedStateMachine fsmBtoA;

    static class Context {
        FSMEvent lastEvent;
    }

    final Context ctxAtoB = new Context();

    final Context ctxBtoA = new Context();

    enum FSMEvent {
        ToA, ToB
    }

    static class StateMachineSample extends AbstractUntypedStateMachine {
    }

    /** */
    @Test
    public void transitionInOneFSMHasActionThatCausesTransitionInAnother() {
        createFsmBtoA();
        createFsmAtoB();

        fsmAtoB.fire(FSMEvent.ToB, ctxAtoB);

        // Check final states as well as last events seen by each FSM...
        assertEquals(FSMEvent.ToB, ctxAtoB.lastEvent);
        assertEquals("B", fsmAtoB.getCurrentState());

        assertEquals(FSMEvent.ToA, ctxBtoA.lastEvent);
        assertEquals("A", fsmBtoA.getCurrentState());
    }

    private void createFsmAtoB() {
        UntypedStateMachineBuilder fsmAtoB_builder = StateMachineBuilderFactory.create(StateMachineSample.class);

        // In transitioning from A to B trigger other FSM (fsmBtoA) to
        // transition
        // to A...
        fsmAtoB_builder.externalTransition().from("A").to("B").on(FSMEvent.ToB)
                .perform(storeEventAndFire(fsmBtoA, ctxBtoA, FSMEvent.ToA));
        fsmAtoB_builder.externalTransition().from("B").to("A").on(FSMEvent.ToA).perform(storeState());

        fsmAtoB = fsmAtoB_builder.newStateMachine("A");
    }

    private void createFsmBtoA() {
        UntypedStateMachineBuilder fsmBtoA_builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        fsmBtoA_builder.externalTransition().from("A").to("B").on(FSMEvent.ToB).perform(storeState());
        fsmBtoA_builder.externalTransition().from("B").to("A").on(FSMEvent.ToA).perform(storeState());

        fsmBtoA = fsmBtoA_builder.newStateMachine("B");
    }

    private static UntypedAnonymousAction storeEventAndFire(final UntypedStateMachine otherFsm, final Context otherCtx,
            final FSMEvent otherEvent) {
        return new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                ((Context) context).lastEvent = (FSMEvent) event;
                otherFsm.fire(otherEvent, otherCtx);
            }
        };
    }

    private static UntypedAnonymousAction storeState() {
        return new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                ((Context) context).lastEvent = (FSMEvent) event;
            }
        };
    }
}