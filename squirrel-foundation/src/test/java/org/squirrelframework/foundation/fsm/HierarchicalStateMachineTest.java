package org.squirrelframework.foundation.fsm;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HierarchicalStateMachineTest {

    public enum HState {
        A("this is A"), A1, A1a, A1a1, A2, A2a, A3, A4, B, B1, B2, B2a, B3, D, E, E1, C;
        private String desc;

        HState() {
        }

        HState(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            if(StringUtils.isNotBlank(desc)){
                return this.name() + '(' + desc + ')';
            }else{
                return this.name();
            }
        }
    }

    public enum HEvent {
        A2B("this is A2B"), B2A, Finish,
        A12A2, A12A3, A12A4, A12A1a, A12A1a1, A1a12A1, A1a2A1a1, A1a12A1a, A32A1, A12B3, A22A2a,
        B12B2, B22B2a, B22A, A2D, D2E1;
        private String desc;

        HEvent() {
        }
        HEvent(String desc) {
            this.desc = desc;
        }
        @Override
        public String toString() {
            if(StringUtils.isNotBlank(desc)){
                return this.name() + '(' + desc + ')';
            }else{
                return this.name();
            }
        }
    }

    @States({
        @State(parent="A", name="A3", entryCallMethod="enterA3", exitCallMethod="leftA3"),
        @State(parent="A", name="A4", entryCallMethod="enterA4", exitCallMethod="leftA4", isFinal=true),
        @State(parent="B", name="B3", entryCallMethod="enterB3", exitCallMethod="leftB3"),
        @State(name="C", entryCallMethod="enterC", exitCallMethod="leftC"),
        @State(parent="A1", name="A1a", entryCallMethod="enterA1a", exitCallMethod="leftA1a"),
        @State(parent="A1a", name="A1a1", entryCallMethod="enterA1a1", exitCallMethod="leftA1a1"),
        @State(name="A2", historyType=HistoryType.DEEP),
        @State(parent="A2", name="A2a", entryCallMethod="enterA2a", exitCallMethod="leftA2a"),
        @State(name="B2", historyType=HistoryType.DEEP),
        @State(parent="B2", name="B2a", entryCallMethod="enterB2a", exitCallMethod="leftB2a"),
        @State(name="D", entryCallMethod="enterD", exitCallMethod="leftD"),
        @State(name="E", entryCallMethod="enterE", exitCallMethod="leftE"),
        @State(parent="E", name="E1", entryCallMethod="enterE1", exitCallMethod="leftE1"),
        })
    @Transitions({
        @Transit(from="A", to="C", on="Finish", callMethod="transitA2C"),
        @Transit(from="A1", to="A3", on="A12A3", callMethod="transitA12A3"),
        @Transit(from="A1", to="A4", on="A12A4", callMethod="transitA12A4"),
        @Transit(from="A3", to="A1", on="A32A1", callMethod="transitA32A1"),
        @Transit(from="A1", to="B3", on="A12B3", callMethod="transitA12B3"),
        @Transit(from="A1", to="A1a1", on="A12A1a1", callMethod="transitA12A1a1"),
        @Transit(from="A1a1", to="A1", on="A1a12A1", callMethod="transitA1a12A1"),
        @Transit(from="A1", to="A1a", on="A12A1a", callMethod="transitA12A1a"),
        @Transit(from="A1a", to="A1a1", on="A1a2A1a1", callMethod="transitA1a2A1a1", type=TransitionType.LOCAL),
        @Transit(from="A1a1", to="A1a", on="A1a12A1a", callMethod="transitA1a12A1a", type=TransitionType.LOCAL),
        @Transit(from="A2", to="A2a", on="A22A2a", callMethod="transitA22A2a", type=TransitionType.LOCAL),
        @Transit(from="B2", to="B2a", on="B22B2a", callMethod="transitB22B2a", type=TransitionType.LOCAL),
        @Transit(from="A", to="D", on="A2D", callMethod="transitA2D"),
        @Transit(from="D", to="E1", on="D2E1", callMethod="transitD2E1"),
        @Transit(from="A", to="C", on="Finish", callMethod="transitA2C"),
        })
    static class HierachicalStateMachine extends AbstractStateMachine<HierachicalStateMachine, HState, HEvent, Integer> {

        private StringBuilder logger = new StringBuilder();

        public void entryA(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryA");
        }

        public void exitA(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitA");
        }

        public void transitFromAToBOnA2B(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitFromAToBOnA2B");
        }

        public void entryA1(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryA1");
        }

        public void exitA1(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitA1");
        }

        public void transitFromA1ToA2OnA12A2(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitFromA1ToA2OnA12A2");
        }

        public void entryA2(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryA2");
        }

        public void exitA2(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitA2");
        }

        public void entryB(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryB");
        }

        public void exitB(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitB");
        }

        public void entryB1(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryB1");
        }

        public void exitB1(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitB1");
        }

        public void entryB2(HState from, HState to, HEvent event, Integer context) {
            logger.append("entryB2");
        }

        public void exitB2(HState from, HState to, HEvent event, Integer context) {
            logger.append("exitB2");
        }

        public void transitFromBToAOnB2A(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitFromBToAOnB2A");
        }

        public void transitFromB1ToB2OnB12B2(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitFromB1ToB2OnB12B2");
        }

        public void transitFromB2ToAOnB22A(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitFromB2ToAOnB22A");
        }

        public void enterA3(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterA3");
        }

        public void leftA3(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftA3");
        }

        public void transitA12A3(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA12A3");
        }

        public void transitA32A1(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA32A1");
        }

        public void enterB3(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterB3");
        }

        public void leftB3(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftB3");
        }

        public void transitA12B3(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA12B3");
        }

        public void enterA1a(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterA1a");
        }

        public void leftA1a(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftA1a");
        }

        public void enterA1a1(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterA1a1");
        }

        public void leftA1a1(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftA1a1");
        }

        public void transitA12A1a1(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA12A1a1");
        }

        public void transitA1a12A1(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA1a12A1");
        }

        public void transitA1a12A1a(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA1a12A1a");
        }

        public void transitA12A1a(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA12A1a");
        }

        public void transitA1a2A1a1(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA1a2A1a1");
        }

        public void enterA2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterA2a");
        }

        public void leftA2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftA2a");
        }

        public void transitA22A2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA22A2a");
        }

        public void enterB2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterB2a");
        }

        public void leftB2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftB2a");
        }

        public void transitB22B2a(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitB22B2a");
        }

        public void enterA4(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterA4");
        }

        public void leftA4(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftA4");
        }

        public void transitA12A4(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA12A4");
        }

        public void transitA2C(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA2C");
        }

        public void enterC(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterC");
        }

        public void leftC(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftC");
        }

        public void enterD(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterD");
        }

        public void leftD(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftD");
        }

        public void transitA2D(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitA2D");
        }

        public void enterE(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterE");
        }

        public void leftE(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftE");
        }

        public void enterE1(HState from, HState to, HEvent event, Integer context) {
            logger.append("enterE1");
        }

        public void leftE1(HState from, HState to, HEvent event, Integer context) {
            logger.append("leftE1");
        }

        public void transitD2E1(HState from, HState to, HEvent event, Integer context) {
            logger.append("transitD2E1");
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

    HierachicalStateMachine stateMachine;

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
        if(stateMachine.getStatus()!=StateMachineStatus.TERMINATED)
            stateMachine.terminate(null);
        System.out.println("-------------------------------------------------");
    }

    @Before
    public void setup() {
        StateMachineBuilder<HierachicalStateMachine, HState, HEvent, Integer> builder =
                StateMachineBuilderFactory.create(HierachicalStateMachine.class,
                        HState.class, HEvent.class, Integer.class, new Class<?>[0]);
        builder.externalTransition().from(HState.A).to(HState.B).on(HEvent.A2B);
        builder.externalTransition().from(HState.B).to(HState.A).on(HEvent.B2A);

        builder.defineSequentialStatesOn(HState.A, HistoryType.DEEP, HState.A1, HState.A2);
        builder.externalTransition().from(HState.A1).to(HState.A2).on(HEvent.A12A2);

        builder.defineSequentialStatesOn(HState.B, HistoryType.SHALLOW, HState.B1, HState.B2);
        builder.externalTransition().from(HState.B1).to(HState.B2).on(HEvent.B12B2);
        builder.externalTransition().from(HState.B2).to(HState.A).on(HEvent.B22A);

        builder.defineFinishEvent(HEvent.Finish);

        stateMachine = builder.newStateMachine(HState.A,
                StateMachineConfiguration.create().enableDebugMode(true),
                new Object[0]);
    }

    @Test
    public void testBasicHierarchicalState() {
        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));

        stateMachine.fire(HEvent.A12A2, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.transitFromA1ToA2OnA12A2.entryA2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2)));

        stateMachine.fire(HEvent.A2B, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA2.exitA.transitFromAToBOnA2B.entryB.entryB1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B1)));

        stateMachine.fire(HEvent.B12B2, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitB1.transitFromB1ToB2OnB12B2.entryB2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B2)));

        stateMachine.fire(HEvent.B22A, 1);
        // enter A2 by history
        assertThat(stateMachine.consumeLog(), is(equalTo("exitB2.exitB.transitFromB2ToAOnB22A.entryA.entryA2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2)));

        stateMachine.terminate();
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA2.exitA")));
    }

    @Test
    public void testTestEvent() {
        HState testResult = stateMachine.test(HEvent.A12A2, 1);
        assertThat(testResult, is(equalTo(HState.A2)));
        assertThat(stateMachine.consumeLog(), is(equalTo("")));
        assertThat(stateMachine.getStatus(), is(equalTo(StateMachineStatus.INITIALIZED)));

        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));

        stateMachine.fire(HEvent.A12A2, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.transitFromA1ToA2OnA12A2.entryA2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2)));

        testResult = stateMachine.test(HEvent.A2B, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("")));
        assertThat(testResult, is(equalTo(HState.B1)));
    }

    @Test
    public void testDeclarativeHierarchicalState() {
        stateMachine.fire(HEvent.A12A3, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1.exitA1.transitA12A3.enterA3")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A3)));

        stateMachine.fire(HEvent.A32A1, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("leftA3.transitA32A1.entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));
    }

    @Test
    public void testTransitionBetweenInnerStates() {
        stateMachine.fire(HEvent.A12B3, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1.exitA1.exitA.transitA12B3.entryB.enterB3")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B3)));
    }

    @Test
    public void testExternalTransitionBetweenParentAndChild() {
        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));

        stateMachine.fire(HEvent.A12A1a1, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.entryA1.transitA12A1a1.enterA1a.enterA1a1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1a1)));

        stateMachine.fire(HEvent.A1a12A1, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("leftA1a1.leftA1a.exitA1.transitA1a12A1.entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));
    }

    @Test
    public void testLocalTransitionBetweenParentAndChild() {
        stateMachine.start();
        stateMachine.consumeLog();

        stateMachine.fire(HEvent.A12A1a, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.entryA1.transitA12A1a.enterA1a")));

        stateMachine.fire(HEvent.A1a2A1a1, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("transitA1a2A1a1.enterA1a1")));

        stateMachine.fire(HEvent.A1a12A1a, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("leftA1a1.transitA1a12A1a")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1a)));
    }

    @Test
    public void testParentTransition() {
        stateMachine.start();
        stateMachine.consumeLog();
        stateMachine.fire(HEvent.A12A1a1, 1);
        stateMachine.consumeLog();
        stateMachine.fire(HEvent.A12B3, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("leftA1a1.leftA1a.exitA1.exitA.transitA12B3.entryB.enterB3")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B3)));
    }

    @Test
    public void testSavedData() {
        stateMachine.start();
        stateMachine.fire(HEvent.A12A3, 1);
        stateMachine.fire(HEvent.A32A1, 0);
        StateMachineData.Reader<HierachicalStateMachine, HState, HEvent, Integer> savedData =
                stateMachine.dumpSavedData();
        stateMachine.terminate();

        assertThat(savedData.currentState(), is(equalTo(HState.A1)));
        assertThat(savedData.initialState(), is(equalTo(HState.A)));
        assertThat(savedData.lastState(), is(equalTo(HState.A3)));

        assertThat(savedData.lastActiveChildStateOf(HState.A), is(equalTo(HState.A3)));
        setup();

        stateMachine.loadSavedData(savedData);
        StateMachineData.Reader<HierachicalStateMachine, HState, HEvent, Integer> savedData2 =
                stateMachine.dumpSavedData();
        assertThat(savedData2.lastActiveChildStateOf(HState.A), is(equalTo(HState.A3)));
    }

    @Test
    public void testDeepHistoryState() {
        stateMachine.start();
        stateMachine.consumeLog();
        stateMachine.fire(HEvent.A12A2, 1);
        stateMachine.consumeLog();
        stateMachine.fire(HEvent.A22A2a, 1);
        stateMachine.consumeLog();
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2a)));

        stateMachine.fire(HEvent.A2B, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("leftA2a.exitA2.exitA.transitFromAToBOnA2B.entryB.entryB1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B1)));

        stateMachine.fire(HEvent.B2A, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitB1.exitB.transitFromBToAOnB2A.entryA.entryA2.enterA2a")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2a)));
    }

    @Test
    public void testShallowHistoryState() {
        stateMachine.fire(HEvent.A2B, 1);
        stateMachine.fire(HEvent.B12B2, 1);
        stateMachine.fire(HEvent.B22B2a, 1);
        stateMachine.consumeLog();
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B2a)));

        stateMachine.fire(HEvent.B2A, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("leftB2a.exitB2.exitB.transitFromBToAOnB2A.entryA.entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));

        stateMachine.fire(HEvent.A2B, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.exitA.transitFromAToBOnA2B.entryB.entryB2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B2)));
    }

    @Test
    public void testNestedFinalState() {
        stateMachine.start();
        stateMachine.consumeLog();
        stateMachine.fire(HEvent.A12A4, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.transitA12A4.enterA4.exitA.transitA2C.enterC")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.C)));
    }

    @Test
    public void testExportAndImportHierarchicalStateMachine() {
        SCXMLVisitor visitor = SquirrelProvider.getInstance().newInstance(SCXMLVisitor.class);
        stateMachine.accept(visitor);
        //visitor.convertSCXMLFile("HierarchicalStateMachine", true);
        String xmlDef = visitor.getScxml(false);

        UntypedStateMachineBuilder builder = new UntypedStateMachineImporter().importDefinition(xmlDef);
        stateMachine = builder.newAnyStateMachine(HState.A);

        HState testResult = stateMachine.test(HEvent.A12A2, 1);
        assertThat(testResult, is(equalTo(HState.A2)));
        assertThat(stateMachine.consumeLog(), is(equalTo("")));
        assertThat(stateMachine.getStatus(), is(equalTo(StateMachineStatus.INITIALIZED)));

        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));

        stateMachine.fire(HEvent.A12A2, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.transitFromA1ToA2OnA12A2.entryA2")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2)));

        testResult = stateMachine.test(HEvent.A2B, 1);
        assertThat(stateMachine.consumeLog(), is(equalTo("")));
        assertThat(testResult, is(equalTo(HState.B1)));
    }

    @Test
    public void testExportDotHierarchicalStateMachine() {
        DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
        stateMachine.accept(visitor);
        visitor.convertDotFile("HierarchicalStateMachine");
    }

    @Test
    public void testChildTransition() {
      stateMachine.start();
        System.out.println(stateMachine.consumeLog());
      stateMachine.fire(HEvent.A2D, 1);
      System.out.println(stateMachine.consumeLog());
      stateMachine.fire(HEvent.D2E1, 2);
      assertThat(stateMachine.consumeLog(), is(equalTo("leftD.transitD2E1.enterE.enterE1")));
      assertThat(stateMachine.getCurrentState(), is(equalTo(HState.E1)));
    }
}
