package org.squirrelframework.foundation.fsm;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParamters;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class UntypedStateMachineTest {
    
    enum TestEvent {
        toA, toB, toC, toD
    }
    
    interface CallSequenceMonitor {
        void fromAToB(String from, String to, TestEvent event, Integer context);
        void transitFromdToaOntoA(String from, String to, TestEvent event, Integer context);
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB", callMethod="fromAToB"),
        @Transit(from="b", to="c", on="toC"),
        @Transit(from="c", to="d", on="toD")
    })
    @StateMachineParamters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    static class UntypedStateMachineSample extends AbstractUntypedStateMachine {
        
        @Mock
        private CallSequenceMonitor monitor;
        
        protected UntypedStateMachineSample(ImmutableUntypedState initialState, 
                Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
        
        protected void fromAToB(String from, String to, TestEvent event, Integer context) {
            monitor.fromAToB(from, to, event, context);
        }
        
        protected void transitFromdToaOntoA(String from, String to, TestEvent event, Integer context) {
            monitor.transitFromdToaOntoA(from, to, event, context);
        }
        
        public CallSequenceMonitor getMonitor() {
            return monitor;
        }
    }
    
    @Test
    public void testUntypedStateMachine() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineSample.class);
        builder.externalTransition().from("d").to("a").on(TestEvent.toA);
        UntypedStateMachineSample fsm = builder.newUntypedStateMachine("a", UntypedStateMachineSample.class);
        
        MockitoAnnotations.initMocks(fsm);
        CallSequenceMonitor monitor = fsm.getMonitor();
        InOrder callSequence = Mockito.inOrder(monitor);
        Assert.assertTrue(fsm.getCurrentState().equals("a"));
        fsm.fire(TestEvent.toB, 1);
        Assert.assertTrue(fsm.getCurrentState().equals("b"));
        fsm.fire(TestEvent.toC, 2);
        Assert.assertTrue(fsm.getCurrentState().equals("c"));
        fsm.fire(TestEvent.toD, 3);
        Assert.assertTrue(fsm.getCurrentState().equals("d"));
        fsm.fire(TestEvent.toA, 4);
        Assert.assertTrue(fsm.getCurrentState().equals("a"));
        
        callSequence.verify(monitor, Mockito.times(1)).fromAToB("a", "b", TestEvent.toB, 1);
        callSequence.verify(monitor, Mockito.times(1)).transitFromdToaOntoA("d", "a", TestEvent.toA, 4);
    }
}
