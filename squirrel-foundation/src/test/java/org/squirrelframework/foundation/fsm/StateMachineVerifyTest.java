package org.squirrelframework.foundation.fsm;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;

public class StateMachineVerifyTest extends AbstractStateMachineTest {
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="test", type=TransitionType.INTERNAL)
    })
    private static class InvalidInternalTransitionStateMachine extends AbstractStateMachine<InvalidInternalTransitionStateMachine, TestState, TestEvent, Integer> {
        protected InvalidInternalTransitionStateMachine(
                ImmutableState<InvalidInternalTransitionStateMachine, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<InvalidInternalTransitionStateMachine, TestState, TestEvent, Integer>> states) {
            super(initialState, states);
        }
        @SuppressWarnings("unused")
        public void test(TestState from, TestState to, TestEvent event, Integer context) {
        }
    }
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="test"),
        @Transit(from="A", to="B", on="ToB", callMethod="test", type=TransitionType.LOCAL),
    })
    private static class ConflictTransitionStateMachine extends AbstractStateMachine<ConflictTransitionStateMachine, TestState, TestEvent, Integer> {
        protected ConflictTransitionStateMachine(
                ImmutableState<ConflictTransitionStateMachine, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<ConflictTransitionStateMachine, TestState, TestEvent, Integer>> states) {
            super(initialState, states);
        }
        @SuppressWarnings("unused")
        public void test(TestState from, TestState to, TestEvent event, Integer context) {
        }
    }
    
    @States({@State(name="A", isFinal=true),
            @State(name="B", parent="A")
    })
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="test")
    })
    private static class InvalidFinalStateMachine extends AbstractStateMachine<InvalidFinalStateMachine, TestState, TestEvent, Integer> {
        protected InvalidFinalStateMachine(
                ImmutableState<InvalidFinalStateMachine, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<InvalidFinalStateMachine, TestState, TestEvent, Integer>> states) {
            super(initialState, states);
        }
        @SuppressWarnings("unused")
        public void test(TestState from, TestState to, TestEvent event, Integer context) {
        }
    }
    
    @Test
    public void testInvalidFinalState() {
        StateMachineBuilder<InvalidFinalStateMachine, TestState, TestEvent, Integer> builder = 
                StateMachineBuilderImpl.newStateMachineBuilder(InvalidFinalStateMachine.class, TestState.class, 
                TestEvent.class, Integer.class);
        try {
            builder.newStateMachine(TestState.A);
        } catch(RuntimeException e) {
            System.out.println("Expected error: "+e.getMessage());
            return;
        }
        Assert.fail();
    }
    
    @Test
    public void testInvalidInternalTransition() {
        StateMachineBuilder<InvalidInternalTransitionStateMachine, TestState, TestEvent, Integer> builder = 
                StateMachineBuilderImpl.newStateMachineBuilder(InvalidInternalTransitionStateMachine.class, TestState.class, 
                TestEvent.class, Integer.class);
        try {
            builder.newStateMachine(TestState.A);
        } catch(RuntimeException e) {
            System.out.println("Expected error: "+e.getMessage());
            return;
        }
        Assert.fail();
    }
    
    @Test
    public void testVerifyConflictTransition() {
        StateMachineBuilder<ConflictTransitionStateMachine, TestState, TestEvent, Integer> builder = 
                StateMachineBuilderImpl.newStateMachineBuilder(ConflictTransitionStateMachine.class, TestState.class, 
                TestEvent.class, Integer.class);
        try {
            builder.newStateMachine(TestState.A);
        } catch(RuntimeException e) {
            System.out.println("Expected error: "+e.getMessage());
            return;
        }
        Assert.fail();
    }
}
