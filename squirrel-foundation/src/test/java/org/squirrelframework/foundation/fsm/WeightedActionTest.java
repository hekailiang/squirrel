package org.squirrelframework.foundation.fsm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
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
    @StateMachineParameters(stateType=String.class, eventType=String.class, contextType=Void.class)
    @ContextInsensitive
    static class UntypedStateMachineBase extends AbstractUntypedStateMachine {
        
        protected StringBuilder logger = new StringBuilder();

        protected void fromAToB(String from, String to, String event) {
            logger.append("fromAToB");
        }
        
        protected void transitFromAToBOnToB(String from, String to, String event) {
            logger.append("transitFromAToBOnToB");
        }
        
        protected void fromAToC(String from, String to, String event) {
            logger.append("fromAToC");
        }
        
        protected void entryD(String from, String to, String event) {
            logger.append("entryD");
        }
        
        @Override
        protected void beforeActionInvoked(Object fromState, Object toState, Object event, Object context) {
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

        protected void beforeFromAToB(String from, String to, String event) {
            logger.append("beforeFromAToB");
        }
        
        protected void afterFromAToB(String from, String to, String event) {
            logger.append("afterFromAToB");
        }
        
        protected void goAToC1(String from, String to, String event) {
            logger.append("goAToC1");
        }
        
        protected void goAToC2(String from, String to, String event) {
            logger.append("goAToC2");
        }
        
        protected void beforeEntryD(String from, String to, String event) {
            logger.append("beforeEntryD");
        }
        
        protected void goEntryD(String from, String to, String event) {
            for(int i=0; i<10000; ++i) {
                RandomStringUtils.randomAlphabetic(10);
            }
            logger.append("goEntryD");
        }
        
    }
    
    @States({
        @State(name="D", entryCallMethod="entryD:+200") // override extension method weight
    })
    static class UntypedStateMachineExt2 extends UntypedStateMachineExt {
    }
    
    private UntypedStateMachineExt fsm;
    
    private StateMachineLogger logger;
    
    @After
    public void teardown() {
        if(fsm.getStatus()!=StateMachineStatus.TERMINATED)
            fsm.terminate(null);
    }
    
    @Before
    public void setup() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineExt.class);
        fsm = builder.newUntypedStateMachine("A", 
                StateMachineConfiguration.create().enableDebugMode(true),
                new Object[0]);
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
        fsm = builder.newUntypedStateMachine("A");
        logger = new StateMachineLogger(fsm);
        logger.startLogging();
        fsm.fire("ToD");
        assertThat(fsm.consumeLog(), is(equalTo("entryD.beforeEntryD.goEntryD")));
    }
    
    @Test
    public void testIgnoreWeight() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineExt2.class);
        builder.onEntry("D").callMethod("entryD:ignore"); // entryD will not be invoked
        fsm = builder.newUntypedStateMachine("A");
        logger = new StateMachineLogger(fsm);
        logger.startLogging();
        fsm.fire("ToD");
        assertThat(fsm.consumeLog(), is(equalTo("beforeEntryD.goEntryD")));
    }
}
