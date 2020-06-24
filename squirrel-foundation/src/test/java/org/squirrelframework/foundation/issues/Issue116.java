/**
 * Alipay.com Inc. Copyright (c) 2004-2020 All Rights Reserved.
 */
package org.squirrelframework.foundation.issues;

import junit.framework.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineConfiguration;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class Issue116 {

    enum FSMEvent {
        SELF_TRANSITION
    }

    @Test
    public void testSelfTransit() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(MyStateMachine.class);
        builder.defineState("S");
        builder.transition().from("S").to("S").on(FSMEvent.SELF_TRANSITION).callMethod("transit");
        UntypedStateMachine fsm =
                builder.newStateMachine("S", StateMachineConfiguration.create().enableDebugMode(true));
        fsm.start();
        String startLog = ((MyStateMachine)fsm).consumeLog();
        Assert.assertTrue(startLog.equals("entryS"));
        fsm.fire(FSMEvent.SELF_TRANSITION);
        String execLog = ((MyStateMachine)fsm).consumeLog();
        Assert.assertTrue(execLog.equals("exitS-transit-entryS"));
    }

    @StateMachineParameters(
            stateType = String.class,
            eventType = FSMEvent.class,
            contextType = String.class)
    static class MyStateMachine extends AbstractUntypedStateMachine {

        StringBuilder logger = new StringBuilder();

        protected void entryS(String from, String to, FSMEvent event, String ctx) {
            logger.append("entryS");
        }

        protected void exitS(String from, String to, FSMEvent event, String ctx) {
            logger.append("exitS");
        }

        protected void transit(String from, String to, FSMEvent event, String ctx) {
            logger.append("-transit-");
        }

        String consumeLog() {
            final String result = logger.toString();
            logger = new StringBuilder();
            return result;
        }
    }
}