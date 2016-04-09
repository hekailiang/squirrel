package org.squirrelframework.foundation.fsm.jmx;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.TestEvent;
import org.squirrelframework.foundation.fsm.TestState;
import org.squirrelframework.foundation.fsm.TransitionPriority;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import static org.junit.Assert.assertEquals;
import static org.squirrelframework.foundation.fsm.TestEvent.ToEnd;
import static org.squirrelframework.foundation.fsm.TestState.A;
import static org.squirrelframework.foundation.fsm.TestState.D;
import static org.squirrelframework.foundation.fsm.TestState.Final;

import org.junit.Before;

public class StateMachineManagementTest {
    interface CallSequenceMonitor {
        void entryA(TestState from, TestState to, TestEvent event, Integer context);
        void entryB(TestState from, TestState to, TestEvent event, Integer context);
        void entryC(TestState from, TestState to, TestEvent event, Integer context);
        void entryD(TestState from, TestState to, TestEvent event, Integer context);
        
        void transitFromAToBOnToB(TestState from, TestState to, TestEvent event, Integer context);
        void testAToBOnToB(TestState from, TestState to, TestEvent event, Integer context);
        void conditionalTransitToB(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromBToCOnToCBase(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromBToCOnToCOverride(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToD(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToDWithNormalPriority(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToDWithHighPriority(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromDToFinalOnToEnd(TestState from, TestState to, TestEvent event, Integer context);
        
        void exitA(TestState from, TestState to, TestEvent event, Integer context);
        void exitB(TestState from, TestState to, TestEvent event, Integer context);
        void afterExitB(TestState from, TestState to, TestEvent event, Integer context);
        void exitC(TestState from, TestState to, TestEvent event, Integer context);
        void exitD(TestState from, TestState to, TestEvent event, Integer context);
        
        void beforeEntryB(TestState from, TestState to, TestEvent event, Integer context);
        void beforeExitA(TestState from, TestState to, TestEvent event, Integer context);
        void afterEntryB(TestState from, TestState to, TestEvent event, Integer context);
        void afterExitA(TestState from, TestState to, TestEvent event, Integer context);
        void beforeTransitToB(TestState from, TestState to, TestEvent event, Integer context);
        void afterTransitToB(TestState from, TestState to, TestEvent event, Integer context);
    }
    
    @State(name="B", entryCallMethod="afterEntryB")
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="testAToBOnToB"),
        @Transit(from="C", to="D", on="ToD", callMethod="transitFromCToDOnToDWithNormalPriority", priority=TransitionPriority.NORMAL),
        @Transit(from="D", to="B", on="ToA")
    })
    interface DeclarativeStateMachine extends StateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> {
    }
    
    @States({
        @State(name="A", exitCallMethod="beforeExitA"),
        @State(name="A", exitCallMethod="afterExitA"),
        @State(name="B", entryCallMethod="beforeEntryB")
    })
    @Transitions({
        @Transit(from="B", to="C", on="ToC"),
        @Transit(from="A", to="B", on="ToB", callMethod="beforeTransitToB"),
        @Transit(from="A", to="B", on="ToB", callMethod="conditionalTransitToB"),
        @Transit(from="A", to="B", on="ToB", callMethod="afterTransitToB"),
        @Transit(from="C", to="D", on="ToD", callMethod="transitFromCToDOnToDWithHighPriority", priority=TransitionPriority.HIGH),
        @Transit(from="D", to="A", on="ToA", priority=TransitionPriority.HIGH)
    })
    static class StateMachineImpl extends AbstractDeclarativeStateMachine {

        public StateMachineImpl(CallSequenceMonitor monitor) {
            super(monitor);
        }
        
        }
    
    abstract static class AbstractDeclarativeStateMachine extends 
        AbstractStateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> implements DeclarativeStateMachine {
        
        protected CallSequenceMonitor monitor;
        
        public AbstractDeclarativeStateMachine(CallSequenceMonitor monitor) {
            this.monitor = monitor;
        }

           }
    
    @Mock
    private CallSequenceMonitor monitor;
    
    private DeclarativeStateMachine stateMachine;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StateMachineBuilder<DeclarativeStateMachine, TestState, TestEvent, Integer> builder = 
                StateMachineBuilderFactory.<DeclarativeStateMachine, TestState, TestEvent, Integer>
                create(StateMachineImpl.class, TestState.class, 
                        TestEvent.class, Integer.class, CallSequenceMonitor.class);
        builder.externalTransition().from(D).toFinal(Final).on(ToEnd);
        stateMachine = builder.newStateMachine(A, monitor);
    }
    
    @Test
    public void identifierTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.getIdentifier(),stateMachineManagement.getIdentifier());
    }
    @Test
    public void currentStateTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.getCurrentState(),"[NULL]");
    }
    @Test
    public void perfStatDetailsTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.getPerfStatDetails(),"[Empty]");
    }
    @Test
    public void getTotalTransitionInvokedTimesTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.getTotalTransitionInvokedTimes(),0);
    }
    @Test
    public void getTotalTransitionFailedTimesTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.getTotalTransitionFailedTimes(),0);
    }
    @Test
    public void getTotalTransitionDeclinedTimesTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.getTotalTransitionDeclinedTimes(),0);
    }
    @Test
    public void getLastErrorMessageTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.getLastErrorMessage(),"[NoException]");
    }
    @Test
    public void toggleLoggingTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.toggleLogging(),"Logging Started");
    }
    @Test
    public void togglePerfMonTest(){
    	StateMachineManagement stateMachineManagement = new StateMachineManagement(stateMachine);
		assertEquals(stateMachineManagement.togglePerfMon(),"Performance Monitor Start");
    }
    @Test
    public void registerTest(){
    	ManagementService managementService = new ManagementService();
		managementService.register(stateMachine);
    }
    @Test
    public void onStateMachineStartTest(){
    	ManagementService managementService = new ManagementService();
		managementService.onStateMachineStart(stateMachine);
    }
    @Test
    public void onStateMachineTerminateTest(){
    	ManagementService managementService = new ManagementService();
		managementService.onStateMachineTerminate(stateMachine);
    }
    @SuppressWarnings("static-access")
	@Test
    public void quoteTest(){
    	ManagementService managementService = new ManagementService();
		assertEquals(managementService.quote("text"),"text");
    }
}
