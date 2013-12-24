package org.squirrelframework.foundation.fsm;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.fsm.StateMachine.StateMachineEvent;
import org.squirrelframework.foundation.fsm.StateMachine.StateMachineListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionBeginEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionBeginListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionEndEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionEndListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionEvent;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParamters;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class UntypedStateMachineTest {
    
    enum TestEvent {
        toA, toB, toC, toD
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB", callMethod="fromAToB"),
        @Transit(from="b", to="c", on="toC"),
        @Transit(from="c", to="d", on="toD")
    })
    @StateMachineParamters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    static class UntypedStateMachineSample extends AbstractUntypedStateMachine {
        
        @Mock
        private UntypedStateMachineSample mockObject;
        
        protected UntypedStateMachineSample(ImmutableUntypedState initialState, 
                Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
        
        protected void fromAToB(String from, String to, TestEvent event, Integer context) {
            mockObject.fromAToB(from, to, event, context);
        }
        
        protected void transitFromdToaOntoA(String from, String to, TestEvent event, Integer context) {
            mockObject.transitFromdToaOntoA(from, to, event, context);
        }
        
        public UntypedStateMachineSample mockedObject() {
            return mockObject;
        }
    }
    
    private UntypedStateMachineSample fsm;
    
    @After
    public void teardown() {
        if(fsm.getStatus()!=StateMachineStatus.TERMINATED)
            fsm.terminate(null);
    }
    
    
    @Before
    public void setup() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineSample.class);
        builder.externalTransition().from("d").to("a").on(TestEvent.toA);
        fsm = builder.newUntypedStateMachine("a", UntypedStateMachineSample.class);
        MockitoAnnotations.initMocks(fsm);
    }
    
    @Test
    public void testUntypedStateMachine() {
        UntypedStateMachineSample mockedObject = fsm.mockedObject();
        InOrder callSequence = Mockito.inOrder(mockedObject);
        
        Assert.assertTrue(fsm.getCurrentState().equals("a"));
        fsm.fire(TestEvent.toB, 1);
        Assert.assertTrue(fsm.getCurrentState().equals("b"));
        fsm.fire(TestEvent.toC, 2);
        Assert.assertTrue(fsm.getCurrentState().equals("c"));
        fsm.fire(TestEvent.toD, 3);
        Assert.assertTrue(fsm.getCurrentState().equals("d"));
        fsm.fire(TestEvent.toA, 4);
        Assert.assertTrue(fsm.getCurrentState().equals("a"));
        
        callSequence.verify(mockedObject, Mockito.times(1)).fromAToB("a", "b", TestEvent.toB, 1);
        callSequence.verify(mockedObject, Mockito.times(1)).transitFromdToaOntoA("d", "a", TestEvent.toA, 4);
    }
    
    @Test
    public void testTransitionEvent() {
        final AtomicInteger smCallTimes = new AtomicInteger(0);
        final AtomicInteger tCallTimes  = new AtomicInteger(0);
        final AtomicInteger teCallTimes = new AtomicInteger(0);
        final AtomicInteger tbCallTimes = new AtomicInteger(0);
        final AtomicInteger tcCallTimes = new AtomicInteger(0);
        fsm.addStateMachineListener(new StateMachineListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void stateMachineEvent(StateMachineEvent<UntypedStateMachine, Object, Object, Object> event) {
                smCallTimes.incrementAndGet();
            }
        });
        fsm.addListener(TransitionEvent.class, new StateMachineListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void stateMachineEvent(StateMachineEvent<UntypedStateMachine, Object, Object, Object> event) {
                tCallTimes.incrementAndGet();
            }
        }, "stateMachineEvent");
        fsm.addTransitionEndListener(new TransitionEndListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionEnd(TransitionEndEvent<UntypedStateMachine, Object, Object, Object> event) {
                teCallTimes.incrementAndGet();
            }
        });
        fsm.addTransitionBeginListener(new TransitionBeginListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionBegin(TransitionBeginEvent<UntypedStateMachine, Object, Object, Object> event) {
                tbCallTimes.incrementAndGet();
            }
        });
        fsm.addTransitionCompleteListener(new TransitionCompleteListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionComplete(TransitionCompleteEvent<UntypedStateMachine, Object, Object, Object> event) {
                tcCallTimes.incrementAndGet();
            }
        });
        // StateMachineStart, TransitionBegin, TransitionComplete, TransitionEnd
        fsm.fire(TestEvent.toB, 1);
        // TransitionBegin, TransitionDeclined, TransitionEnd
        fsm.fire(TestEvent.toB, 1);
        Assert.assertTrue(smCallTimes.get()==7);
        Assert.assertTrue( tCallTimes.get()==6);
        Assert.assertTrue(teCallTimes.get()==2);
        Assert.assertTrue(tbCallTimes.get()==2);
        Assert.assertTrue(tcCallTimes.get()==1);
    }
}
