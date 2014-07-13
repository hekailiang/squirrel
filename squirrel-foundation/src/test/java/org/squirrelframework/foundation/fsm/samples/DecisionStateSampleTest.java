package org.squirrelframework.foundation.fsm.samples;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedAnonymousAction;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
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

    static StringBuilder logger;

    @Before
    public void setUp() {
        logger = new StringBuilder();
    }

    @StateMachineParameters(stateType = DecisionState.class, eventType = DecisionEvent.class, contextType = Integer.class)
    static class DecisionStateMachine extends AbstractUntypedStateMachine {

        public void enterA(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("enterA");
        }

        public void leftA(DecisionState from, DecisionState to, DecisionEvent event, Integer context) {
            logger.append("leftA");
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

    static class DecisionMaker extends UntypedAnonymousAction {
        // wrap any local state in action
        int transitionCount = 0;
        final String name;

        DecisionMaker(String name) {
            this.name = name;
        }

        @Override
        public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
            DecisionState typedFrom = (DecisionState)from;
            DecisionState typedTo = (DecisionState)to;
            DecisionEvent typedEvent = (DecisionEvent)event;
            if(typedFrom!=null && typedTo==null) {
                logger.append("leftMakeDecision");
                // local state clean up at some situation, e.g. at some criteria restore transitionCount
            } else if(typedFrom==null && typedTo!=null) {
                logger.append("enterMakeDecision");
                // local state initialize at some situation
            } else if(typedFrom!=null && typedFrom!=null && typedEvent==DecisionEvent.A2ANY) {
                // perform dynamic transitions
                Integer typedContext = (Integer)context;
                if(typedContext < 10) {
                    stateMachine.fire(DecisionEvent.A2B, context);
                    transitionCount++;
                } else if(typedContext < 20) {
                    stateMachine.fire(DecisionEvent.A2C, context);
                    transitionCount++;
                } else if(typedContext < 40) {
                    stateMachine.fire(DecisionEvent.A2D, context);
                    transitionCount++;
                } else {
                    stateMachine.fire(DecisionEvent.ANY2A, context);
                }
                // e.g. if transitionCount>10 then change transition router rules
            }
        }

        @Override
        public String name() {
            return name;
        }
    }

    DecisionStateMachine buildStateMachine() {
        DecisionStateMachine fsm;
        final UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(DecisionStateMachine.class);
        final DecisionMaker decisionMaker = new DecisionMaker("DecisionMaker");

        // _A is decision state for A and it is invisible to user
        builder.defineNoInitSequentialStatesOn(DecisionState.A, DecisionState._A);
        builder.onEntry(DecisionState.A).callMethod("enterA");
        builder.onExit(DecisionState.A).callMethod("leftA");
        builder.onEntry(DecisionState._A).perform(decisionMaker);
        builder.onExit(DecisionState._A).perform(decisionMaker);

        // transition to left state A are all started with _A which means all transition cause exit state A must be router by _A
        builder.transitions().from(DecisionState._A).toAmong(DecisionState.B, DecisionState.C, DecisionState.D).
                onEach(DecisionEvent.A2B, DecisionEvent.A2C, DecisionEvent.A2D).callMethod("a2b|a2c|_");

        builder.transitions().fromAmong(DecisionState.B, DecisionState.C, DecisionState.D).
                to(DecisionState.A).on(DecisionEvent.ANY2A);

        // use local transition avoid invoking state A exit functions when entering its decision state
        builder.localTransitions().between(DecisionState.A).and(DecisionState._A).
                onMutual(DecisionEvent.A2ANY, DecisionEvent.ANY2A).
                perform(Lists.newArrayList(decisionMaker, null));

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
        Assert.assertTrue("enterMakeDecision.leftMakeDecision.leftA".equals(fsm.consumeLog()));

        fsm.terminate();
//        System.out.println(fsm.exportXMLDefinition(true));
    }

}

