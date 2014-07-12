package org.squirrelframework.foundation.fsm;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionDeclinedEvent;
import org.squirrelframework.foundation.fsm.annotation.*;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ParallelStateMachineTest {

    enum PState {
        Total, A, A1, A1a, A1b, A1c, A2, A2a, A2b, A2c, B, C
    }

    enum PEvent {
        A1a2A1b, A1a2B, A1b2A1a, A1b2A1c, A2a2A2b, A2b2A2a, A2b2A2c, A2B, B2A, Finish
    }

    @States({
        @State(name="Total", entryCallMethod="enterTotal", exitCallMethod="exitTotal"),
        @State(parent="Total", name="A", entryCallMethod="enterA", exitCallMethod="exitA",
            compositeType=StateCompositeType.PARALLEL, historyType=HistoryType.DEEP),

        @State(parent="A", name="A1", entryCallMethod="enterA1", exitCallMethod="exitA1", historyType=HistoryType.DEEP),
        @State(parent="A1", name="A1a", entryCallMethod="enterA1a", exitCallMethod="exitA1a", initialState=true),
        @State(parent="A1", name="A1b", entryCallMethod="enterA1b", exitCallMethod="exitA1b"),
        @State(parent="A1", name="A1c", entryCallMethod="enterA1c", exitCallMethod="exitA1c", isFinal=true),

        @State(parent="A", name="A2", entryCallMethod="enterA2", exitCallMethod="exitA2", historyType=HistoryType.DEEP),
        @State(parent="A2", name="A2a", entryCallMethod="enterA2a", exitCallMethod="exitA2a"),
        @State(parent="A2", name="A2b", entryCallMethod="enterA2b", exitCallMethod="exitA2b", initialState=true),
        @State(parent="A2", name="A2c", entryCallMethod="enterA2c", exitCallMethod="exitA2c", isFinal=true),

        @State(parent="Total", name="B", entryCallMethod="enterB", exitCallMethod="exitB"),
        @State(parent="Total", name="C", entryCallMethod="enterC", exitCallMethod="exitC"),
    })
    @Transitions({
        @Transit(from="A", to="B", on="A2B", callMethod="transitA2B"),
        @Transit(from="B", to="A", on="B2A", callMethod="transitB2A"),
        @Transit(from="A1a", to="A1b", on="A1a2A1b", callMethod="transitA1a2A1b"),
        @Transit(from="A1a", to="B", on="A1a2B", callMethod="transitA1a2B"),
        @Transit(from="A1b", to="A1a", on="A1b2A1a", callMethod="transitA1b2A1a"),
        @Transit(from="A1b", to="A1c", on="A1b2A1c", callMethod="transitA1b2A1c"),
        @Transit(from="A2a", to="A2b", on="A2a2A2b", callMethod="transitA2a2A2b"),
        @Transit(from="A2b", to="A2a", on="A2b2A2a", callMethod="transitA2b2A2a"),
        @Transit(from="A2b", to="A2c", on="A2b2A2c", callMethod="transitA2b2A2c"),
        @Transit(from="A", to="C", on="Finish", callMethod="transitA2C"),
    })
    @ContextEvent(finishEvent="Finish")
    static class ParallelStateMachine extends AbstractStateMachine<ParallelStateMachine, PState, PEvent, Integer> {
        private StringBuilder logger = new StringBuilder();

        public void transitA1a2B(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA1a2B");
        }

        public void transitA2C(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA2C");
        }

        public void transitA1b2A1c(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA1b2A1c");
        }

        public void transitA2b2A2c(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA2b2A2c");
        }

        public void transitA2a2A2b(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA2a2A2b");
        }

        public void transitA2b2A2a(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA2b2A2a");
        }

        public void transitA1b2A1a(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA1b2A1a");
        }

        public void transitA1a2A1b(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA1a2A1b");
        }

        public void transitB2A(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitB2A");
        }

        public void transitA2B(PState from, PState to, PEvent event, Integer context) {
            logger.append("transitA2B");
        }

        public void enterTotal(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterTotal");
        }

        public void exitTotal(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitTotal");
        }

        public void enterA(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA");
        }

        public void exitA(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA");
        }

        public void enterA1(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA1");
        }

        public void exitA1(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA1");
        }

        public void enterA1a(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA1a");
        }

        public void exitA1a(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA1a");
        }

        public void enterA1b(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA1b");
        }

        public void exitA1b(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA1b");
        }

        public void enterA1c(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA1c");
        }

        public void exitA1c(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA1c");
        }

        public void enterA2(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA2");
        }

        public void exitA2(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA2");
        }

        public void enterA2a(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA2a");
        }

        public void exitA2a(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA2a");
        }

        public void enterA2b(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA2b");
        }

        public void exitA2b(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA2b");
        }

        public void enterA2c(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterA2c");
        }

        public void exitA2c(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitA2c");
        }

        public void enterB(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterB");
        }

        public void exitB(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitB");
        }

        public void enterC(PState from, PState to, PEvent event, Integer context) {
            logger.append("enterC");
        }

        public void exitC(PState from, PState to, PEvent event, Integer context) {
            logger.append("exitC");
        }

        @Override
        protected void beforeActionInvoked(PState from, PState to, PEvent event, Integer context) {
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

    private ParallelStateMachine stateMachine;

    @AfterClass
    public static void afterTest() {
        ConverterProvider.INSTANCE.clearRegistry();
    }

    @After
    public void teardown() {
        stateMachine.terminate(null);
    }

    @Before
    public void setup() {
        StateMachineBuilder<ParallelStateMachine, PState, PEvent, Integer> builder = StateMachineBuilderFactory.create
                (ParallelStateMachine.class, PState.class, PEvent.class, Integer.class);
        stateMachine = builder.newStateMachine(PState.A);
    }

    @Test
    public void testInitialParallelStates() {
        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("enterTotal.enterA.enterA1.enterA1a.enterA2.enterA2b")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A1a, PState.A2b));
    }

    @Test
    public void testReceiveSubStateEvent() {
        stateMachine.addTransitionDeclinedListener(new StateMachine.TransitionDeclinedListener<ParallelStateMachine, PState, PEvent, Integer>() {
            @Override
            public void transitionDeclined(TransitionDeclinedEvent<ParallelStateMachine, PState, PEvent, Integer> event) {
                stateMachine.consumeLog();
            }
        });
        stateMachine.start();
        stateMachine.consumeLog();
        stateMachine.fire(PEvent.A1a2A1b, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1a.transitA1a2A1b.enterA1b")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A2b, PState.A1b));

        stateMachine.terminate(null);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA2b.exitA2.exitA1b.exitA1.exitA.exitTotal")));
    }

    @Test
    public void testEnterSubFinalState() {
        stateMachine.start();
        stateMachine.consumeLog();
        stateMachine.fire(PEvent.A1a2A1b, 1);
        stateMachine.consumeLog();
        stateMachine.fire(PEvent.A1b2A1c, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1b.transitA1b2A1c.enterA1c")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A2b, PState.A1c));

        stateMachine.fire(PEvent.A2b2A2c, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA2b.transitA2b2A2c.enterA2c.exitA1.exitA2.exitA.transitA2C.enterC")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.C)));

        stateMachine.terminate(null);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitC.exitTotal")));
    }

    @Test
    public void testSavedData() {
        stateMachine.start();
        stateMachine.fire(PEvent.A1a2A1b, 1);
        stateMachine.fire(PEvent.A1b2A1c, 1);
        List<PState> subStates = stateMachine.getSubStatesOn(stateMachine.getCurrentState());
        assertEquals("Total/A/A2/A2b", stateMachine.getRawStateFrom(subStates.get(0)).getPath());
        assertEquals("Total/A/A1/A1c", stateMachine.getRawStateFrom(subStates.get(1)).getPath());
        assertEquals("Total/A", stateMachine.getCurrentRawState().getPath());
        StateMachineData.Reader<ParallelStateMachine, PState, PEvent, Integer> savedData = 
                stateMachine.dumpSavedData();
        stateMachine.terminate(null);
        assertThat(savedData.currentState(), is(equalTo(PState.A)));
        assertThat(savedData.lastActiveChildStateOf(PState.A1), is(equalTo(PState.A1b)));
        
        List<PState> expectedResult = Lists.newArrayList(PState.A2b, PState.A1c);
        assertThat(savedData.subStatesOn(PState.A), is(equalTo(expectedResult)));
        
        stateMachine = StateMachineBuilderFactory.create(savedData.typeOfStateMachine(), savedData.typeOfState(), 
                savedData.typeOfEvent(), savedData.typeOfContext()).newStateMachine(PState.A);
        stateMachine.loadSavedData(savedData);
        
        stateMachine.fire(PEvent.A2b2A2c, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA2b.transitA2b2A2c.enterA2c.exitA1.exitA2.exitA.transitA2C.enterC")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.C)));
        
        stateMachine.terminate();
        assertThat(stateMachine.consumeLog(), is(equalTo("exitC.exitTotal")));
    }

    @Test
    public void testParallelSubStateExit() {
        stateMachine.start();
        stateMachine.consumeLog();
        stateMachine.fire(PEvent.A1a2B, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1a.exitA1.exitA2b.exitA2.exitA.transitA1a2B.enterB")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.B)));
    }

    @Test
    public void testHistoricalState() {
        stateMachine.start();
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A1a, PState.A2b));

        stateMachine.fire(PEvent.A1a2A1b, 1);
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A2b, PState.A1b));

        stateMachine.fire(PEvent.A2b2A2a, 1);
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A1b, PState.A2a));

        stateMachine.fire(PEvent.A2B, 1);
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.B)));
        assertThat(stateMachine.getSubStatesOn(PState.A), is(empty()));
        assertThat(stateMachine.getSubStatesOn(PState.B), is(empty()));

        stateMachine.fire(PEvent.B2A, 1);
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A1b, PState.A2a));
    }

    @Test
    public void testExportAndImportParallelState() {
        SCXMLVisitor visitor = SquirrelProvider.getInstance().newInstance(SCXMLVisitor.class);
        stateMachine.accept(visitor);
//        visitor.convertSCXMLFile("ParallelStateMachine", true);
        
        String xmlDef = visitor.getScxml(false);
        
        UntypedStateMachineBuilder builder = new UntypedStateMachineImporter().importDefinition(xmlDef);
        stateMachine = builder.newAnyStateMachine(PState.A);
        
        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("enterTotal.enterA.enterA1.enterA1a.enterA2.enterA2b")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A1a, PState.A2b));
        
        stateMachine.fire(PEvent.A1a2A1b, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1a.transitA1a2A1b.enterA1b")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
        assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A2b, PState.A1b));
    }

}
