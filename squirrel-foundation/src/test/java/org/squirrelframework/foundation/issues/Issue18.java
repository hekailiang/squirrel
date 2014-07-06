package org.squirrelframework.foundation.issues;

import junit.framework.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

/**
 * Created by kailianghe on 7/5/14.
 */
public class Issue18 {

    enum Issue18State {A, B}

    enum Issue18Event {GO}

    @ContextInsensitive
    @StateMachineParameters(stateType=Issue18State.class, eventType=Issue18Event.class, contextType=Void.class)
    static class Issue18StateMachine extends AbstractUntypedStateMachine {
        StringBuilder logger = new StringBuilder();
        void onA2A(Issue18State from, Issue18State to, Issue18Event cause) {
            logger.append("onA2A");
        }
        void onA2B(Issue18State from, Issue18State to, Issue18Event cause) {
            logger.append("onA2B");
        }
        void onB2B(Issue18State from, Issue18State to, Issue18Event cause) {
            logger.append("onB2B");
        }
        void onB2A(Issue18State from, Issue18State to, Issue18Event cause) {
            logger.append("onB2A");
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

    @Test
    public void testIssue18() {
        final UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(Issue18StateMachine.class);
        builder.externalTransition(10).from(Issue18State.A).to(Issue18State.B).on(Issue18Event.GO).
                when(new CounterCondition(5, "Count_5_A")).callMethod("onA2B");
        builder.internalTransition(1).within(Issue18State.A).on(Issue18Event.GO).callMethod("onA2A");

        builder.externalTransition(10).from(Issue18State.B).to(Issue18State.A).on(Issue18Event.GO).
                when(new CounterCondition(5, "Count_5_B")).callMethod("onB2A");
        builder.internalTransition(1).within(Issue18State.B).on(Issue18Event.GO).callMethod("onB2B");

        Issue18StateMachine fsm = builder.newUntypedStateMachine(Issue18State.A);
        fsm.start();
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        Assert.assertTrue("onA2A.onA2A.onA2A.onA2A.onA2B".equals(fsm.consumeLog()));
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        fsm.fire(Issue18Event.GO);
        Assert.assertTrue("onB2B.onB2B.onB2B.onB2B.onB2A".equals(fsm.consumeLog()));
    }

}

class CounterCondition implements Condition<Object> {
    int counter;
    final String name;

    CounterCondition(int init, String name) {
        this.counter = init;
        this.name = name;
    }

    @Override
    public boolean isSatisfied(Object context) {
        return --counter==0;
    }

    @Override
    public String name() {
        return name;
    }
}