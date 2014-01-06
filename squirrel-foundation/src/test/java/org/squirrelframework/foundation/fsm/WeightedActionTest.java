package org.squirrelframework.foundation.fsm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParamters;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class WeightedActionTest {
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="fromAToB"),
        @Transit(from="A", to="C", on="ToC", callMethod="fromAToC"),
        @Transit(from="A", to="D", on="ToD")
    })
    @States({
        @State(name="D", entryCallMethod="entryD") // test duplicate extension method definition
    })
    @StateMachineParamters(stateType=String.class, eventType=String.class, contextType=Void.class)
    @ContextInsensitive
    static class UntypedStateMachineBase extends AbstractUntypedStateMachine {
        
        protected StringBuilder logger = new StringBuilder();

        protected UntypedStateMachineBase(ImmutableUntypedState initialState,
                Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
        
        protected void fromAToB(String from, String to, String event) {
            addOptionalDot();
            logger.append("fromAToB");
        }
        
        protected void transitFromAToBOnToB(String from, String to, String event) {
            addOptionalDot();
            logger.append("transitFromAToBOnToB");
        }
        
        protected void fromAToC(String from, String to, String event) {
            addOptionalDot();
            logger.append("fromAToC");
        }
        
        protected void entryD(String from, String to, String event) {
            addOptionalDot();
            logger.append("entryD");
        }
        
        protected void addOptionalDot() {
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
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="beforeFromAToB"),
        @Transit(from="A", to="B", on="ToB", callMethod="afterFromAToB"),
        @Transit(from="A", to="C", on="ToC", callMethod="goAToC1:+150"),
        @Transit(from="A", to="C", on="ToC", callMethod="goAToC2:-150"),
    })
    @States({
        @State(name="D", entryCallMethod="beforeEntryD"),
        @State(name="D", entryCallMethod="goEntryD:-150"),
    })
    static class UntypedStateMachineExt extends UntypedStateMachineBase {

        protected UntypedStateMachineExt(ImmutableUntypedState initialState,
                Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
        
        protected void beforeFromAToB(String from, String to, String event) {
            addOptionalDot();
            logger.append("beforeFromAToB");
        }
        
        protected void afterFromAToB(String from, String to, String event) {
            addOptionalDot();
            logger.append("afterFromAToB");
        }
        
        protected void goAToC1(String from, String to, String event) {
            addOptionalDot();
            logger.append("goAToC1");
        }
        
        protected void goAToC2(String from, String to, String event) {
            addOptionalDot();
            logger.append("goAToC2");
        }
        
        protected void beforeEntryD(String from, String to, String event) {
            addOptionalDot();
            logger.append("beforeEntryD");
        }
        
        protected void goEntryD(String from, String to, String event) {
            addOptionalDot();
            logger.append("goEntryD");
        }
        
    }
    
    @States({
        @State(name="D", entryCallMethod="entryD:+200") // override extension method weight
    })
    static class UntypedStateMachineExt2 extends UntypedStateMachineExt {
        protected UntypedStateMachineExt2(ImmutableUntypedState initialState,
                Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
    }
    
    private UntypedStateMachineExt fsm;
    
    private StateMachineLogger logger;
    
    @After
    public void teardown() {
        if(fsm.getStatus()!=StateMachineStatus.TERMINATED)
            fsm.terminate(null);
        logger.terminateLogging();
    }
    
    @Before
    public void setup() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineExt.class);
        fsm = builder.newUntypedStateMachine("A", UntypedStateMachineExt.class);
        logger = new StateMachineLogger(fsm);
        logger.startLogging();
    }
    
    @Test
    public void testBeforeExtension() {
        fsm.fire("ToB");
        assertThat(fsm.consumeLog(), is(equalTo("beforeFromAToB.fromAToB.transitFromAToBOnToB.afterFromAToB")));
    }
    
    @Test
    public void testWeightTransitionAction() {
        fsm.fire("ToC");
        assertThat(fsm.consumeLog(), is(equalTo("goAToC1.fromAToC.goAToC2")));
    }
    
    @Test
    public void testWeightStateAction() {
        fsm.fire("ToD");
        assertThat(fsm.consumeLog(), is(equalTo("beforeEntryD.entryD.goEntryD")));
    }
    
    @Test
    public void testOverrideWeight() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineExt2.class);
        fsm = builder.newUntypedStateMachine("A", UntypedStateMachineExt.class);
        logger = new StateMachineLogger(fsm);
        logger.startLogging();
        fsm.fire("ToD");
        assertThat(fsm.consumeLog(), is(equalTo("entryD.beforeEntryD.goEntryD")));
    }
    
}
