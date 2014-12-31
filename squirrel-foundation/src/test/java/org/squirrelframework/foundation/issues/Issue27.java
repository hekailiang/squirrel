package org.squirrelframework.foundation.issues;

import junit.framework.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

/**
 * Created by kailianghe on 12/1/14.
 */
public class Issue27 {

    enum Issue17State {Off, Operating, Moving}

    enum Issue17Event {Move}

    @ContextInsensitive
    @StateMachineParameters(stateType=Issue17State.class, eventType=Issue17Event.class, contextType=Void.class)
    static class Issue17StateMachine extends AbstractUntypedStateMachine {
        StringBuilder logger = new StringBuilder();
        void exitOff(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("exitOff-");
        }
        void entryOff(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("entryOff-");
        }
        void entryMoving(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("entryMoving-");
        }
        void entryOperating(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("entryOperating-");
        }

        String consumeLog() {
            final String result = logger.toString();
            logger = new StringBuilder();
            return result;
        }
    }

    @Test
    public void testIssue17() {
        final UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(Issue17StateMachine.class);
        builder.defineSequentialStatesOn(Issue17State.Operating, Issue17State.Moving);
        builder.externalTransition().from(Issue17State.Off).to(Issue17State.Moving).on(Issue17Event.Move);

        Issue17StateMachine fsm = builder.newUntypedStateMachine(Issue17State.Off);
        fsm.start();
        fsm.fire(Issue17Event.Move);
//        System.out.println(fsm.consumeLog());
        Assert.assertTrue(fsm.consumeLog().equals("entryOff-exitOff-entryOperating-entryMoving-"));
    }
}
