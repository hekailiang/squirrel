package org.squirrelframework.foundation.issues;

import org.junit.*;
import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
import org.squirrelframework.foundation.fsm.*;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class Issue33 {

    public enum HState {
        A, A1, A2, A2a, A2a1, A2a2, B
    }

    public enum HEvent {
        A2B, B2A, A12A2, A2a12A2a2
    }

    @States({
            @State(name="A", entryCallMethod="enterA", exitCallMethod="leftA", historyType = HistoryType.DEEP),
            @State(parent="A", name="A1", entryCallMethod="enterA1", exitCallMethod="leftA1", initialState = true),
            @State(parent="A", name="A2", entryCallMethod="enterA2", exitCallMethod="leftA2"),
            @State(parent="A2", name="A2a", entryCallMethod="enterA2a", exitCallMethod="leftA2a", initialState = true),
            @State(parent="A2a", name="A2a1", entryCallMethod="enterA2a1", exitCallMethod="leftA2a1", initialState = true),
            @State(parent = "A2a", name="A2a2", entryCallMethod="enterA2a2", exitCallMethod="leftA2a2"),
            @State(name="B", entryCallMethod="enterB", exitCallMethod="leftB")
    })
    @Transitions({
            @Transit(from="A1", to="A2", on="A12A2", callMethod="transitA12A2"),
            @Transit(from="A2a1", to="A2a2", on="A2a12A2a2", callMethod="transitA2a12A2a2"),
            @Transit(from="A", to="B", on="A2B", callMethod="transitA2B"),
            @Transit(from="B", to="A", on="B2A", callMethod="transitB2A")
    })
    static class HierarchicalStateMachine extends AbstractStateMachine<HierarchicalStateMachine, HState, HEvent, Integer> {
        private StringBuilder logger = new StringBuilder();

        public void entryA(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryA");
        }
        public void exitA(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitA");
        }

        public void transitA2a12A2a2(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA2a12A2a2");
        }
        public void transitA12A2(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA12A2");
        }

        public void entryA1(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryA1");
        }
        public void exitA1(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitA1");
        }
        public void entryA2(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryA2");
        }
        public void exitA2(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitA2");
        }
        public void entryA2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryA2a");
        }
        public void exitA2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitA2a");
        }

        public void entryB(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryB");
        }

        public void exitB(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitB");
        }

        public void enterA2a1(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterA2a1");
        }

        public void leftA2a1(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftA2a1");
        }
        public void enterA2a2(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterA2a2");
        }

        public void leftA2a2(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftA2a2");
        }

        public void transitA2B(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA2B");
        }

        public void transitB2A(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitB2A");
        }

        @Override
        protected void beforeActionInvoked(HState from, HState to, HEvent event, Integer context) {
            addOptionalDot();
        }

        private void addOptionalDot() {
            if (logger.length() > 0) {
                logger.append('.');
            }
        }

        public String consumeLog() {
            final String result = logger.toString();
            logger = new StringBuilder();
            return result;
        }

    }

    HierarchicalStateMachine stateMachine;

    StateMachineLogger fsmLogger;

    @BeforeClass
    public static void beforeTest() {
    }

    @AfterClass
    public static void afterTest() {
        ConverterProvider.INSTANCE.clearRegistry();
        SquirrelPostProcessorProvider.getInstance().clearRegistry();
    }

    @After
    public void teardown() {
        if(stateMachine.getStatus()!= StateMachineStatus.TERMINATED)
            stateMachine.terminate(null);
        System.out.println("-------------------------------------------------");
    }

    @Before
    public void setup() {
        StateMachineBuilder<HierarchicalStateMachine, HState, HEvent, Integer> builder =
                StateMachineBuilderFactory.create(HierarchicalStateMachine.class,
                        HState.class, HEvent.class, Integer.class, new Class<?>[0]);

        stateMachine = builder.newStateMachine(HState.A,
                StateMachineConfiguration.create().enableDebugMode(true),
                new Object[0]);
    }

    @Test
    public void testBasicHierarchicalState() {
        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("entryA..entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));

        stateMachine.fire(HEvent.A12A2);

        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.transitA12A2..entryA2..entryA2a.enterA2a1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2a1)));

        stateMachine.fire(HEvent.A2a12A2a2);

        assertThat(stateMachine.consumeLog(), is(equalTo("leftA2a1.transitA2a12A2a2.enterA2a2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2a2)));

        stateMachine.fire(HEvent.A2B);

        assertThat(stateMachine.consumeLog(), is(equalTo("leftA2a2..exitA2a..exitA2..exitA.transitA2B..entryB")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B)));

        stateMachine.fire(HEvent.B2A);

        assertThat(stateMachine.consumeLog(), is(equalTo("exitB.transitB2A..entryA..entryA2..entryA2a.enterA2a2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2a2)));
    }

}