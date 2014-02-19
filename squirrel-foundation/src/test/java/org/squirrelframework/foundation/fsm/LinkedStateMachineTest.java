package org.squirrelframework.foundation.fsm;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

public class LinkedStateMachineTest {

    enum LState {
        A, B, C, D, A1, A2, A3
    }

    enum LEvent {
        A2B, B2C, C2D, D2A, A12A2, A22A3, A32A1
    }

    @States({
            @State(name = "A", entryCallMethod = "enterA", exitCallMethod = "leftA"),
            @State(name = "B", entryCallMethod = "enterB", exitCallMethod = "leftB"),
            @State(name = "C", entryCallMethod = "enterC", exitCallMethod = "leftC"),
            @State(name = "C", entryCallMethod = "enterD", exitCallMethod = "leftD") })
    @Transitions({
            @Transit(from = "A", to = "B", on = "A2B", callMethod = "transitA2B"),
            @Transit(from = "B", to = "C", on = "B2C", callMethod = "transitB2C"),
            @Transit(from = "C", to = "D", on = "C2D", callMethod = "transitC2D"),
            @Transit(from = "D", to = "A", on = "D2A", callMethod = "transitD2A") })
    static class TestStateMachine extends
            AbstractStateMachine<TestStateMachine, LState, LEvent, Integer> {

        private StringBuilder logger;

        protected TestStateMachine(StringBuilder logger) {
            this.logger = logger;
        }

        public void transitA2B(LState from, LState to, LEvent event,
                Integer context) {
            addOptionalDot();
            logger.append("transitA2B");
        }

        public void transitB2C(LState from, LState to, LEvent event,
                Integer context) {
            addOptionalDot();
            logger.append("transitB2C");
        }

        public void transitC2D(LState from, LState to, LEvent event,
                Integer context) {
            addOptionalDot();
            logger.append("transitC2D");
        }

        public void transitD2A(LState from, LState to, LEvent event,
                Integer context) {
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
            @State(name = "A1", entryCallMethod = "enterA1", exitCallMethod = "leftA1"),
            @State(name = "A2", entryCallMethod = "enterA2", exitCallMethod = "leftA2"),
            @State(name = "A3", entryCallMethod = "enterA3", exitCallMethod = "leftA3") })
    @Transitions({
            @Transit(from = "A1", to = "A2", on = "A12A2", callMethod = "transitA12A2"),
            @Transit(from = "A2", to = "A3", on = "A22A3", callMethod = "transitA22A3"),
            @Transit(from = "A3", to = "A1", on = "A32A1", callMethod = "transitA32A1") })
    static class LinkedStateMachine extends
            AbstractStateMachine<LinkedStateMachine, LState, LEvent, Integer> {

        private StringBuilder logger;

        protected LinkedStateMachine(StringBuilder logger) {
            this.logger = logger;
        }

        public void transitA12A2(LState from, LState to, LEvent event,
                Integer context) {
            logger.append("transitA12A2");
        }

        public void transitA22A3(LState from, LState to, LEvent event,
                Integer context) {
            logger.append("transitA22A3");
        }

        public void transitA32A1(LState from, LState to, LEvent event,
                Integer context) {
            logger.append("transitA32A1");
        }

        public void enterA1(LState from, LState to, LEvent event,
                Integer context) {
            logger.append("enterA1");
        }

        public void leftA1(LState from, LState to, LEvent event, Integer context) {
            logger.append("leftA1");
        }

        public void enterA2(LState from, LState to, LEvent event,
                Integer context) {
            logger.append("enterA2");
        }

        public void leftA2(LState from, LState to, LEvent event, Integer context) {
            logger.append("leftA2");
        }

        public void enterA3(LState from, LState to, LEvent event,
                Integer context) {
            logger.append("enterA3");
        }

        public void leftA3(LState from, LState to, LEvent event, Integer context) {
            logger.append("leftA3");
        }
        
        @Override
        protected void beforeActionInvoked(LState from, LState to, LEvent event, Integer context) {
            addOptionalDot();
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
    
    TestStateMachine stateMachine2;

    StringBuilder logger;
    
    @Before
    public void setup() {
        logger = new StringBuilder();
        StateMachineBuilder<LinkedStateMachine, LState, LEvent, Integer> builderOfLinkedStateMachine = 
                StateMachineBuilderFactory.create(LinkedStateMachine.class, LState.class, LEvent.class,
                        Integer.class, new Class<?>[] { StringBuilder.class });

        StateMachineBuilder<TestStateMachine, LState, LEvent, Integer> builderOfTestStateMachine = 
                StateMachineBuilderFactory.create(TestStateMachine.class, LState.class, LEvent.class,
                        Integer.class, new Class<?>[] { StringBuilder.class });
        
        // defined linked state
        builderOfTestStateMachine.defineLinkedState(LState.A, builderOfLinkedStateMachine, LState.A1, logger);
        builderOfTestStateMachine.defineLinkedState(LState.C, builderOfLinkedStateMachine, LState.A2, logger);
        stateMachine = builderOfTestStateMachine.newStateMachine(LState.A, logger);
        stateMachine2 = builderOfTestStateMachine.newStateMachine(LState.A, logger);
    }

    @Test
    public void testInitialLinkedState() {
        doTestInitialLinkedState(stateMachine, logger);
        logger.append("|");
        doTestInitialLinkedState(stateMachine2, logger);
        assertThat(logger.toString(), equalTo("start1.enterA.start2.enterA1|start1.enterA.start2.enterA1"));
    }
    
    private void doTestInitialLinkedState(TestStateMachine stateMachine, StringBuilder logger) {
        stateMachine.start(0);
        assertThat(stateMachine.getCurrentState(), equalTo(LState.A1));
        assertThat(stateMachine.getCurrentRawState().getStateId(), equalTo(LState.A1));
    }

    @Test
    public void testLinkedStateMachineProcessEvent() {
        stateMachine.fire(LEvent.A12A2, 0);
        assertThat(
                logger.toString(),
                equalTo("start1.enterA.start2.enterA1.leftA1.transitA12A2.enterA2"));
    }

    @Test
    public void testTestStateMachineProcessEvent() {
        stateMachine.fire(LEvent.A2B, 0);
        assertThat(
                logger.toString(),
                equalTo("start1.enterA.start2.enterA1.terminate2.leftA1.leftA.transitA2B.enterB"));
    }

    @Test
    public void testInitialLinkedState2() {
        stateMachine.fire(LEvent.A2B, 0);
        stateMachine.fire(LEvent.B2C, 0);
        stateMachine.fire(LEvent.A22A3, 0);
        assertThat(stateMachine.getCurrentState(), equalTo(LState.A3));
        assertThat(stateMachine.getCurrentRawState().getStateId(), equalTo(LState.A3));
    }
    
    @Test
    public void testSavedData() {
        stateMachine.fire(LEvent.A12A2, 0);
        assertThat(stateMachine.getCurrentState(), equalTo(LState.A2));
        StateMachineData.Reader<TestStateMachine, LState, LEvent, Integer> savedData = 
                stateMachine.dumpSavedData();
        assertThat(savedData.linkedStates(), contains(LState.A));
        stateMachine.terminate(null);
        
        try {
            // use buffering
            OutputStream file = new FileOutputStream("data.sqr");
            OutputStream buffer = new BufferedOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(buffer, "UTF-8"); 
            osw.write(ObjectSerializableSupport.serialize(savedData));
            osw.flush();
        } catch (IOException ex) {
            Assert.fail();
        }

        setup();

        try {
            // use buffering
            InputStream file = new FileInputStream("data.sqr");
            InputStream buffer = new BufferedInputStream(file);
            InputStreamReader isr = new InputStreamReader(buffer, "UTF-8"); 
            BufferedReader br = new BufferedReader(isr); 
            String fileContent = br.readLine(); 
            
            // deserialize the List
            StateMachineData.Reader<TestStateMachine, LState, LEvent, Integer> loadedSavedData = 
                    ObjectSerializableSupport.deserialize(fileContent);
            stateMachine.loadSavedData(loadedSavedData);
            stateMachine.fire(LEvent.A22A3, 0);
            assertThat(stateMachine.getCurrentState(), equalTo(LState.A3));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        } 

    }
}
