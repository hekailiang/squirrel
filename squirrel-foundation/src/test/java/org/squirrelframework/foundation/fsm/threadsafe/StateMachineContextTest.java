package org.squirrelframework.foundation.fsm.threadsafe;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.ImmutableUntypedState;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineContext;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.AsyncExecute;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionBegin;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class StateMachineContextTest {
    
    enum FSMEvent {
        ToA, ToB, ToC, ToD
    }

    @StateMachineParameters(stateType = String.class, eventType = FSMEvent.class, contextType = Integer.class)
    static class StateMachineSample extends AbstractUntypedStateMachine {
        
        StateMachineSample currentInstance;
        
        protected StateMachineSample(ImmutableUntypedState initialState, Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
        
        @AsyncExecute
        public void onAToB(String from, String to, FSMEvent event, Integer context) {
            currentInstance = StateMachineContext.currentInstance();
        }
    }
    
    static class TestListener {
        @OnTransitionBegin(when="from.equals(\"D\")&&context>10")
        public void onTransitionBeginWithD(StateMachineSample fsm) {
            fsm.fire(FSMEvent.ToB, 10);
        }
        
        @OnTransitionBegin(when="from.equals(\"D\")&&context>15")
        public void onTransitionBeginWithB(StateMachineSample fsm) {
            fsm.fire(FSMEvent.ToC, 10);
        }
    }
    
    @Test
    public void testMultipleEvents() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        builder.externalTransition().from("D").to("A").on(FSMEvent.ToA);
        builder.externalTransition().from("A").to("B").on(FSMEvent.ToB);
        builder.externalTransition().from("B").to("C").on(FSMEvent.ToC);
        builder.externalTransition().from("C").to("D").on(FSMEvent.ToD);
        
        final StateMachineSample fsm = builder.newUntypedStateMachine("D");
        fsm.addDeclarativeListener(new TestListener());
        String expected1 = (String) fsm.test(FSMEvent.ToA, 5);
        assertThat(expected1, equalTo("A"));
        String expected2 = (String) fsm.test(FSMEvent.ToA, 11);
        assertThat(expected2, equalTo("B"));
        String expected3 = (String) fsm.test(FSMEvent.ToA, 21);
        assertThat(expected3, equalTo("C"));
    }
    
    @Test
    public void testThreadLocal() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        builder.externalTransition().from("A").to("B").on(FSMEvent.ToB).callMethod("onAToB");
        final StateMachineSample fsm = builder.newUntypedStateMachine("A");
        assertTrue(StateMachineContext.currentInstance()==null);
        fsm.fire(FSMEvent.ToB, 10);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        assertTrue(fsm==fsm.currentInstance);
        assertTrue(StateMachineContext.currentInstance()==null);
    }

}
