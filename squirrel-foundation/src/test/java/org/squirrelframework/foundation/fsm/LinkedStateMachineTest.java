package org.squirrelframework.foundation.fsm;

import java.util.Map;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;

public class LinkedStateMachineTest {
    
    enum LState {
        A, B, C, D, A1, A2, A3
    }
    
    enum LEvent {
        A2B, B2C, C2D, D2A, A12A2, A22A3, A32A1
    }
    
    @States({
        @State(name="A", entryCallMethod="enterA", exitCallMethod="leftA"), 
        @State(name="B", entryCallMethod="enterB", exitCallMethod="leftB"), 
        @State(name="C", entryCallMethod="enterC", exitCallMethod="leftC"),
        @State(name="C", entryCallMethod="enterD", exitCallMethod="leftD")
    })
    @Transitions({
        @Transit(from="A", to="B", on="A2B", callMethod="transitA2B"),
        @Transit(from="B", to="C", on="B2C", callMethod="transitB2C"), 
        @Transit(from="C", to="D", on="C2D", callMethod="transitC2D"),
        @Transit(from="D", to="A", on="D2A", callMethod="transitD2A")
    })
    static class TestStateMachine extends AbstractStateMachine<TestStateMachine, LState, LEvent, Integer> {

        private StringBuilder logger;
        
        protected TestStateMachine(
                ImmutableState<TestStateMachine, LState, LEvent, Integer> initialState,
                Map<LState, ImmutableState<TestStateMachine, LState, LEvent, Integer>> states, StringBuilder logger) {
            super(initialState, states);
            this.logger = logger;
        }
        
        public void transitA2B(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("transitA2B");
        }
        
        public void transitB2C(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("transitB2C");
        }
        
        public void transitC2D(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("transitC2D");
        }
        
        public void transitD2A(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("transitD2A");
        }
        
        public void enterA(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("enterA");
        }
        
        public void leftA(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("leftA");
        }
        
        public void enterB(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("enterB");
        }
        
        public void leftB(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("leftB");
        }
        
        public void enterC(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("enterC");
        }
        
        public void leftC(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("leftC");
        }
        
        public void enterD(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("enterA");
        }
        
        public void leftD(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("leftA");
        }
        
        @Override
        public void start(Integer context) {
            logger.append("start1");
            super.start(context);
        }
        
        @Override
        public void terminate(Integer context) {
            addOptionalDot();
            logger.append("terminate1");
            super.terminate(context);
        }
        
        private void addOptionalDot() {
            if (logger.length() > 0) {
                logger.append('.');
            }
        }
    }
    
    @States({
        @State(name="A1", entryCallMethod="enterA1", exitCallMethod="leftA1"), 
        @State(name="A2", entryCallMethod="enterA2", exitCallMethod="leftA2"), 
        @State(name="A3", entryCallMethod="enterA3", exitCallMethod="leftA3")
    })
    @Transitions({
        @Transit(from="A1", to="A2", on="A12A2", callMethod="transitA12A2"),
        @Transit(from="A2", to="A3", on="A22A3", callMethod="transitA22A3"), 
        @Transit(from="A3", to="A1", on="A32A1", callMethod="transitA32A1")
    })
    static class LinkedStateMachine extends AbstractStateMachine<LinkedStateMachine, LState, LEvent, Integer> {

        private StringBuilder logger;
        
        protected LinkedStateMachine(ImmutableState<LinkedStateMachine, LState, LEvent, Integer> initialState,
                Map<LState, ImmutableState<LinkedStateMachine, LState, LEvent, Integer>> states, StringBuilder logger) {
            super(initialState, states);
            this.logger = logger;
        }
        
        public void transitA12A2(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("transitA12A2");
        }
        
        public void transitA22A3(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("transitA22A3");
        }
        
        public void transitA32A1(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("transitA32A1");
        }
        
        public void enterA1(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("enterA1");
        }
        
        public void leftA1(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("leftA1");
        }
        
        public void enterA2(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("enterA2");
        }
        
        public void leftA2(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("leftA2");
        }
        
        public void enterA3(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("enterA3");
        }
        
        public void leftA3(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
            logger.append("leftA3");
        }
        
        @Override
        public void start(Integer context) {
            addOptionalDot();
            logger.append("start2");
            super.start(context);
        }
        
        @Override
        public void terminate(Integer context) {
            addOptionalDot();
            logger.append("terminate2");
            super.terminate(context);
        }
        
        private void addOptionalDot() {
            if (logger.length() > 0) {
                logger.append('.');
            }
        }
    }
    
    TestStateMachine stateMachine;
    
    StringBuilder logger;
    
    @Before
    public void setup() {
        logger = new StringBuilder();
        StateMachineBuilder<LinkedStateMachine, LState, LEvent, Integer> builderOfLinkedStateMachine = 
                StateMachineBuilderImpl.newStateMachineBuilder(
                        LinkedStateMachine.class, LState.class, LEvent.class, Integer.class, new Class<?>[]{StringBuilder.class});
        
        StateMachineBuilder<TestStateMachine, LState, LEvent, Integer> builderOfTestStateMachine = 
                StateMachineBuilderImpl.newStateMachineBuilder(
                        TestStateMachine.class, LState.class, LEvent.class, Integer.class, new Class<?>[]{StringBuilder.class});
        
        // defined linked state
        builderOfTestStateMachine.definedLinkedState(LState.A, builderOfLinkedStateMachine, LState.A1, logger);
        builderOfTestStateMachine.definedLinkedState(LState.C, builderOfLinkedStateMachine, LState.A2, logger);
        
        stateMachine = builderOfTestStateMachine.newStateMachine(LState.A, logger);
    }
    
    @Test
    public void testInitialLinkedState() {
        stateMachine.start(0);
        assertThat(stateMachine.getCurrentState(), equalTo(LState.A));
        assertThat(stateMachine.getCurrentRawState(), instanceOf(ImmutableLinkedState.class));
        ImmutableLinkedState<TestStateMachine, LState, LEvent, Integer> linkedState = 
                (ImmutableLinkedState<TestStateMachine, LState, LEvent, Integer>)stateMachine.getCurrentRawState();
        assertThat(linkedState.getLinkedStateMachine(), instanceOf(LinkedStateMachine.class));
        LinkedStateMachine linkedStateMachine = (LinkedStateMachine)linkedState.getLinkedStateMachine();
        LState linkedStateId = linkedStateMachine.getCurrentState();
        assertThat(linkedStateId, equalTo(LState.A1));
        
        assertThat(logger.toString(), equalTo("start1.start2.enterA1.enterA"));
    }
    
    @Test
    public void testLinkedStateMachineProcessEvent() {
        stateMachine.fire(LEvent.A12A2, 0);
        assertThat(logger.toString(), equalTo("start1.start2.enterA1.enterA.leftA1.transitA12A2.enterA2"));
    }
    
    @Test
    public void testTestStateMachineProcessEvent() {
        stateMachine.fire(LEvent.A2B, 0);
        assertThat(logger.toString(), equalTo("start1.start2.enterA1.enterA.leftA.terminate2.leftA1.transitA2B.enterB"));
    }
    
    @Test
    public void testInitialLinkedState2() {
        stateMachine.fire(LEvent.A2B, 0);
        stateMachine.fire(LEvent.B2C, 0);
        stateMachine.fire(LEvent.A22A3, 0);
        assertThat(stateMachine.getCurrentState(), equalTo(LState.C));
        assertThat(stateMachine.getCurrentRawState(), instanceOf(ImmutableLinkedState.class));
        ImmutableLinkedState<TestStateMachine, LState, LEvent, Integer> linkedState = 
                (ImmutableLinkedState<TestStateMachine, LState, LEvent, Integer>)stateMachine.getCurrentRawState();
        assertThat(linkedState.getLinkedStateMachine(), instanceOf(LinkedStateMachine.class));
        LinkedStateMachine linkedStateMachine = (LinkedStateMachine)linkedState.getLinkedStateMachine();
        LState linkedStateId = linkedStateMachine.getCurrentState();
        assertThat(linkedStateId, equalTo(LState.A3));
    }
}
