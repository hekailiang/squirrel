package org.squirrelframework.foundation.fsm.samples;

import junit.framework.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

/**
 * Created by kailianghe on 7/12/14.
 */
public class DecisionStateSampleTest {

    enum DecisionState {
        A, _A, B, C, D
    }

    enum DecisionEvent {
        A2ANY, A2B, A2C, A2D, ANY2A
    }

    @States({
            @State(name="A", entryCallMethod = "enterA", exitCallMethod = "leftA"),
            @State(parent="A", name="_A", entryCallMethod = "enterMakeDecision", exitCallMethod = "leftMakeDecision") // _A is decision state for A
    })
    @StateMachineParameters(stateType = DecisionState.class, eventType = DecisionEvent.class, contextType = Integer.class)
    static class DecisionStateMachine extends AbstractUntypedStateMachine {

        StringBuilder logger = new StringBuilder();

        public void enterA(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("enterA");
        }

        public void leftA(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("leftA");
        }

        public void enterMakeDecision(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("enterMakeDecision");
        }

        public void leftMakeDecision(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("leftMakeDecision");
        }

        public void makeDecision(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            if(context < 10) {
                fire(DecisionEvent.A2B, context);
            } else if(context < 20) {
                fire(DecisionEvent.A2C, context);
            } else if(context < 40) {
                fire(DecisionEvent.A2D, context);
            } else {
                fire(DecisionEvent.ANY2A, context);
            }
        }

        public void a2b(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("a2b");
        }

        public void a2c(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("a2c");
        }

        public void a2d(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("a2d");
        }

        String consumeLog() {
            final String result = logger.toString();
            logger = new StringBuilder();
            return result;
        }

        @Override
        protected void beforeActionInvoked(Object fromState, Object toState, Object event, Object context) {
            if (logger.length() > 0) {
                logger.append('.');
            }
        }

    }

    DecisionStateMachine buildStateMachine() {
        DecisionStateMachine fsm;
        final UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(DecisionStateMachine.class);

        builder.transitions().from(DecisionState._A).toSome(DecisionState.B, DecisionState.C, DecisionState.D).
                onSome(DecisionEvent.A2B, DecisionEvent.A2C, DecisionEvent.A2D).callMethod("a2b|a2c");

        builder.transitions().fromSome(DecisionState.B, DecisionState.C, DecisionState.D).
                to(DecisionState.A).on(DecisionEvent.ANY2A);

        // use local transition avoid invoking state A exit functions
        builder.localTransition().from(DecisionState.A).to(DecisionState._A).on(DecisionEvent.A2ANY).callMethod("makeDecision");
        builder.localTransition().from(DecisionState._A).to(DecisionState.A).on(DecisionEvent.ANY2A);

        fsm = builder.newUntypedStateMachine(DecisionState.A);
        return fsm;
    }

    @Test
    public void testDecisionStateMachine() {
        DecisionStateMachine fsm = buildStateMachine();

        fsm.start();
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.A));
        Assert.assertTrue("enterA".equals(fsm.consumeLog()));

        fsm.fire(DecisionEvent.A2B, 30);
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.A));
        Assert.assertTrue("".equals(fsm.consumeLog()));

        fsm.fire(DecisionEvent.A2ANY, 100);
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.A));
        Assert.assertTrue("enterMakeDecision.leftMakeDecision".equals(fsm.consumeLog()));


        fsm.fire(DecisionEvent.A2ANY, 5);
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.B));
        Assert.assertTrue("enterMakeDecision.leftMakeDecision.leftA.a2b".equals(fsm.consumeLog()));


        fsm.fire(DecisionEvent.ANY2A, 1000);
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.A));
        Assert.assertTrue("enterA".equals(fsm.consumeLog()));


        fsm.fire(DecisionEvent.A2ANY, 15);
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.C));
        Assert.assertTrue("enterMakeDecision.leftMakeDecision.leftA.a2c".equals(fsm.consumeLog()));

        fsm.fire(DecisionEvent.ANY2A, 1000);
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.A));
        Assert.assertTrue("enterA".equals(fsm.consumeLog()));

        fsm.fire(DecisionEvent.A2ANY, 30);
        Assert.assertTrue(fsm.getCurrentState().equals(DecisionState.D));
        Assert.assertTrue("enterMakeDecision.leftMakeDecision.leftA.a2c".equals(fsm.consumeLog()));

        fsm.terminate();
//        System.out.println(fsm.exportXMLDefinition(true));
    }

}

