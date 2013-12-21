package org.squirrelframework.foundation.fsm;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParamters;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class UntypedStateMachineTest {
    
    enum TestEvent {
        toA, toB, toC, toD
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB", callMethod="fromAToB"),
        @Transit(from="b", to="c", on="toC"),
        @Transit(from="c", to="d", on="toD")
    })
    @StateMachineParamters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    static class UntypedStateMachineSample extends AbstractUntypedStateMachine {
        protected UntypedStateMachineSample(ImmutableUntypedState initialState, Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
        
        protected void fromAToB(String from, String to, TestEvent event, Integer context) {
            Assert.assertTrue(context.equals(1));
            System.out.println("Must be called!");
        }
    }
    
    @Test
    public void testUntypedStateMachine() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineSample.class);
        builder.externalTransition().from("d").to("a").on(TestEvent.toA);
        UntypedStateMachine fsm = builder.newStateMachine("a");
        Assert.assertTrue(fsm.getCurrentState().equals("a"));
        fsm.fire(TestEvent.toB, 1);
        Assert.assertTrue(fsm.getCurrentState().equals("b"));
        fsm.fire(TestEvent.toC, 2);
        Assert.assertTrue(fsm.getCurrentState().equals("c"));
        fsm.fire(TestEvent.toD, 3);
        Assert.assertTrue(fsm.getCurrentState().equals("d"));
        fsm.fire(TestEvent.toA, 4);
        Assert.assertTrue(fsm.getCurrentState().equals("a"));
    }

}
