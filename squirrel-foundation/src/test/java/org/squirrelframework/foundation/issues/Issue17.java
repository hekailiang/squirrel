package org.squirrelframework.foundation.issues;

import junit.framework.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionDeclinedEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionDeclinedListener;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class Issue17 {
    
    enum Issue17State {A, A1, A2}
    
    enum Issue17Event {SAME, NEXT, SAME_A2}
    
    @ContextInsensitive
    @StateMachineParameters(stateType=Issue17State.class, eventType=Issue17Event.class, contextType=Void.class)
    static class Issue17StateMachine extends AbstractUntypedStateMachine {
        StringBuilder logger = new StringBuilder();
        void onA1ToA2(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("onA1ToA2");
        }
        void onSameWithinA(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("onSameWithinA");
        }
        void onSameWithinA2(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("onSameWithinA2");
        }
        void onExitA2(Issue17State from, Issue17State to, Issue17Event cause) {
            logger.append("onExitA2");
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
        builder.defineSequentialStatesOn(Issue17State.A, Issue17State.A1, Issue17State.A2);
        builder.internalTransition().within(Issue17State.A).on(Issue17Event.SAME).callMethod("onSameWithinA");
        builder.internalTransition().within(Issue17State.A2).on(Issue17Event.SAME_A2).callMethod("onSameWithinA2");
        builder.localTransition().from(Issue17State.A1).to(Issue17State.A2).on(Issue17Event.NEXT).callMethod("onA1ToA2");
        builder.onExit(Issue17State.A2).callMethod("onExitA2");
        
        Issue17StateMachine fsm = builder.newUntypedStateMachine(Issue17State.A);
        fsm.addTransitionDeclinedListener(new TransitionDeclinedListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionDeclined(
                    TransitionDeclinedEvent<UntypedStateMachine, Object, Object, Object> event) {
                System.out.println("Transition declined from state "+event.getSourceState()+" on event "+event.getCause());
            }
        });
        fsm.start();
        fsm.fire(Issue17Event.NEXT);
        Assert.assertEquals("onA1ToA2", fsm.consumeLog());
        fsm.fire(Issue17Event.SAME);
        Assert.assertEquals(Issue17State.A2, fsm.getCurrentState());
        Assert.assertEquals("onSameWithinA",fsm.consumeLog());               
        fsm.fire(Issue17Event.SAME_A2);
        Assert.assertEquals(Issue17State.A2, fsm.getCurrentState());
        Assert.assertEquals("onSameWithinA2",fsm.consumeLog());        
    }

}
