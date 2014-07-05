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
    
    enum Issue17Event {SAME, NEXT}
    
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
        builder.localTransition().from(Issue17State.A1).to(Issue17State.A2).on(Issue17Event.NEXT).callMethod("onA1ToA2");
        
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
        fsm.fire(Issue17Event.SAME);
        Assert.assertTrue(fsm.consumeLog().equals("onA1ToA2"));
    }

}
