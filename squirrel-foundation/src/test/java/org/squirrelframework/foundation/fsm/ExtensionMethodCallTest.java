package org.squirrelframework.foundation.fsm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class ExtensionMethodCallTest {
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="fromAToB", whenMvel="Excellect:::(context>=90)"),
        
        @Transit(from="A", to="C", on="ToC", callMethod="fromAToC"),
        @Transit(from="A", to="D", on="ToD", callMethod="fromAToD"),
        @Transit(from="A", to="E", on="ToE", callMethod="fromAToE"),
        
        @Transit(from="B", to="C", on="ToC", callMethod="fromBToCOnToC"),
        @Transit(from="B", to="C", on="StillToC", callMethod="fromBToCOnStillToC"),
        @Transit(from="B", to="D", on="ToD", callMethod="fromBToD"),
        @Transit(from="B", to="E", on="ToE", callMethod="fromBToE"),
        @Transit(from="B", to="E", on="*", callMethod="fromBToEOnAny"),
        @Transit(from="B", to="E", on="*", whenMvel="Excellect:::(context>=90)", callMethod="fromBToEOnAnyWithCondition")
    })
    @States({
        @State(name="A", exitCallMethod="leftA"), 
        @State(name="B", entryCallMethod="enterB")})
    @StateMachineParameters(stateType=String.class, eventType=String.class, contextType=Integer.class)
    @ContextInsensitive
    static class UntypedStateMachineBase extends AbstractUntypedStateMachine {
        
        protected StringBuilder logger = new StringBuilder();

        protected void leftA(String from, String to, String event) {
            logger.append("leftA");
        }
        
        protected void exitA(String from, String to, String event) {
            logger.append("exitA");
        }
        
        protected void beforeExitAny(String from, String to, String event) {
            logger.append("beforeExitAny");
        }
        
        protected void afterExitAny(String from, String to, String event) {
            logger.append("afterExitAny");
        }
        
        protected void enterB(String from, String to, String event) {
            logger.append("enterB");
        }
        
        protected void entryB(String from, String to, String event) {
            logger.append("entryB");
        }
        
        protected void beforeEntryAny(String from, String to, String event) {
            logger.append("beforeEntryAny");
        }
        
        protected void afterEntryAny(String from, String to, String event) {
            logger.append("afterEntryAny");
        }
        
        protected void fromAToB(String from, String to, String event) {
            logger.append("fromAToB");
        }
        
        protected void transitFromAToBOnToBWhenExcellect(String from, String to, String event) {
            logger.append("transitFromAToBOnToBWhenExcellect");
        }
        
        protected void transitFromAToBOnToB(String from, String to, String event) {
            logger.append("transitFromAToBOnToB");
        }
        
        protected void transitFromAnyToBOnToB(String from, String to, String event) {
            logger.append("transitFromAnyToBOnToB");
        }
        
        protected void transitFromAnyToBOnToBEx(String from, String to, String event) {
            logger.append("transitFromAnyToBOnToBEx");
        }
        
        protected void transitFromAToAnyOnToB(String from, String to, String event) {
            logger.append("transitFromAToAnyOnToB");
        }
        
        protected void transitFromAToB(String from, String to, String event) {
            logger.append("transitFromAToB");
        }
        
        protected void onToB(String from, String to, String event) {
            logger.append("onToB");
        }
        
        protected void fromAToC(String from, String to, String event) {
            logger.append("fromAToC");
        }
        
        protected void fromAnyToC(String from, String to, String event) {
            logger.append("fromAnyToC");
        }
        
        protected void fromAToD(String from, String to, String event) {
            logger.append("fromAToD");
        }
        
        protected void fromAToE(String from, String to, String event) {
            logger.append("fromAToE");
        }
        
        protected void fromBToAny(String from, String to, String event) {
            logger.append("fromBToAny");
        }
        
        protected void fromBToCOnToC(String from, String to, String event) {
            logger.append("fromBToCOnToC");
        }
        
        protected void fromBToCOnStillToC(String from, String to, String event) {
            logger.append("fromBToCOnStillToC");
        }
        
        protected void fromBToD(String from, String to, String event) {
            logger.append("fromBToD");
        }
        
        protected void fromBToE(String from, String to, String event) {
            logger.append("fromBToE");
        }
        
        protected void fromBToEOnAny(String from, String to, String event) {
            logger.append("fromBToEOnAny");
        }
        
        protected void fromBToEOnAnyWithCondition(String from, String to, String event) {
            logger.append("fromBToEOnAnyWithCondition");
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
    
    @Test
    public void testExtensionMethodCallSequence() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineBase.class);
        builder.transit().fromAny().to("B").on("ToB").callMethod("transitFromAnyToBOnToBEx");
        UntypedStateMachineBase fsm = builder.newUntypedStateMachine("A", 
                StateMachineConfiguration.create().enableDebugMode(true), 
                new Object[0]);
        fsm.start();
        fsm.consumeLog();
        fsm.fire("ToB", 91);
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.leftA.exitA.afterExitAny." +
                "fromAToB.transitFromAnyToBOnToBEx.transitFromAToBOnToBWhenExcellect.transitFromAToBOnToB." +
                "transitFromAnyToBOnToB.transitFromAToAnyOnToB.transitFromAToB.onToB." +
                "beforeEntryAny.enterB.entryB.afterEntryAny")));
        fsm.terminate();
        assertTrue(fsm.getListenerSize()==2); // start event listener to attach logger and terminate event listener to detach logger
    }
    
    @Test
    public void testDeferBoundActionFromAny() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineBase.class);
        builder.transit().fromAny().to("C").on("ToC").callMethod("fromAnyToC");
        UntypedStateMachineBase fsm = builder.newUntypedStateMachine("A");
        assertNull(fsm.getCurrentState());
        assertNull(fsm.getCurrentRawState());
        fsm.start();
        fsm.consumeLog();
        fsm.fire("ToC");
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.leftA.exitA.afterExitAny.fromAToC.fromAnyToC.beforeEntryAny.afterEntryAny")));
        fsm.terminate();
        
        fsm.start(); // start again
        fsm.fire("ToB", 91);
        fsm.consumeLog();
        fsm.fire("ToC");
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.afterExitAny.fromBToCOnToC.fromAnyToC.beforeEntryAny.afterEntryAny")));
        fsm.terminate();
    }
    
    @Test
    public void testDeferBoundActionToAny() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineBase.class);
        builder.transit().from("B").toAny().on("ToC").callMethod("fromBToAny");
        
        UntypedStateMachineBase fsm = builder.newUntypedStateMachine("B");
        fsm.start(); 
        fsm.consumeLog();
        fsm.fire("ToC");
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.afterExitAny.fromBToCOnToC.fromBToAny.beforeEntryAny.afterEntryAny")));
        fsm.terminate();
        
        fsm.start(); 
        fsm.consumeLog();
        fsm.fire("ToD");
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.afterExitAny.fromBToD.beforeEntryAny.afterEntryAny")));
        fsm.terminate();
    }
    
    @Test
    public void testDeferBoundActionOnAny() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineBase.class);
        builder.transit().from("B").toAny().onAny().callMethod("fromBToAny");
        
        UntypedStateMachineBase fsm = builder.newUntypedStateMachine("B");
        fsm.start(); 
        fsm.consumeLog();
        fsm.fire("ToD");
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.afterExitAny.fromBToD.fromBToAny.beforeEntryAny.afterEntryAny")));
        fsm.terminate();
    }
    
    @Test
    public void testDeferBoundActionAnnotation() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineBase.class);
        UntypedStateMachineBase fsm = builder.newUntypedStateMachine("B");
        fsm.start(); 
        fsm.consumeLog();
        fsm.fire("ToE");
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.afterExitAny.fromBToE.fromBToEOnAny..beforeEntryAny.afterEntryAny")));
        fsm.terminate();
    }
    
    @Test
    public void testDeferBoundActionAnnotation2() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineBase.class);
        UntypedStateMachineBase fsm = builder.newUntypedStateMachine("B");
        fsm.start(); 
        fsm.consumeLog();
        fsm.fire("ToE", 91);
        assertThat(fsm.consumeLog(), is(equalTo(
                "beforeExitAny.afterExitAny.fromBToE.fromBToEOnAny.fromBToEOnAnyWithCondition.beforeEntryAny.afterEntryAny")));
        fsm.terminate();
    }
}
