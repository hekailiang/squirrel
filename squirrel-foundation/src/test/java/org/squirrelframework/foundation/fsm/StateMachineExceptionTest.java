package org.squirrelframework.foundation.fsm;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.exception.SquirrelRuntimeException;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.annotation.ContextEvent;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class StateMachineExceptionTest {
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB"),
        @Transit(from="A", to="C", on="ToC"),
        @Transit(from="A", to="D", on="ToD")
    })
    @StateMachineParameters(stateType=String.class, eventType=String.class, contextType=Void.class)
    @ContextInsensitive
    static class StateMachineExceptionSample extends AbstractUntypedStateMachine {

        protected void transitFromAToBOnToB(String from, String to, String event) {
            throw new IllegalArgumentException("This exception can be recovered.");
        }
        
        protected void transitFromAToCOnToC(String from, String to, String event) {
            throw new UnsupportedOperationException("This exception cannot be recovered.");
        }
        
        @Override
        protected void afterTransitionCausedException(Object fromState, Object toState, Object event, Object context) {
            Throwable targeException = getLastException().getTargetException();
            if(targeException instanceof IllegalArgumentException && 
                    fromState.equals("A") && toState.equals("B") && event.equals("ToB")) {
                Assert.assertEquals(targeException.getMessage(), "This exception can be recovered.");
                // do some error clean up job here
                // ...
                // after recovered from this exception, reset the state machine status back to normal
                setStatus(StateMachineStatus.IDLE);
            } else {
                super.afterTransitionCausedException(fromState, toState, event, context);
            }
        }
    }
    
    private StateMachineExceptionSample fsm;
    
    @After
    public void teardown() {
    }
    
    @Before
    public void setup() {
        fsm = null;
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testExceptionFail() throws Throwable {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample.class);
        fsm = builder.newUntypedStateMachine("A");
        try {
            fsm.fire("ToC");
        } catch(TransitionException e) {
            Assert.assertEquals(fsm.getStatus(), StateMachineStatus.ERROR);
            Assert.assertEquals(e.getTargetException().getMessage(), "This exception cannot be recovered.");
            throw e.getTargetException();
        }
        Assert.fail();
    }
    
    @Test
    public void testExceptionRecovered() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample.class);
        fsm = builder.newUntypedStateMachine("A");
        fsm.fire("ToB");
        Assert.assertEquals(fsm.getCurrentState(), "A");
        fsm.fire("ToD");
        Assert.assertEquals(fsm.getCurrentState(), "D");
        if(fsm.getStatus()!=StateMachineStatus.TERMINATED)
            fsm.terminate(null);
    }
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB", type=TransitionType.INTERNAL)
    })
    @StateMachineParameters(stateType=String.class, eventType=String.class, contextType=Void.class)
    @ContextInsensitive
    static class StateMachineExceptionSample2 extends AbstractUntypedStateMachine {
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidInternalTransition() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample2.class);
        fsm = builder.newUntypedStateMachine("A");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testUpdateBuilderAfterPrepared() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample.class);
        fsm = builder.newUntypedStateMachine("A");
        builder.defineState("Invalid");
    }
    
    @Test(expected=IllegalArgumentException.class) 
    public void testUnexitedInitialState() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample.class);
        fsm = builder.newUntypedStateMachine("NoSuchState");
    }
    
    class MyEvent {}
    class MyState {}
    @Transitions({
        @Transit(from="A", to="B", on="ToB")
    })
    @StateMachineParameters(stateType=MyState.class, eventType=MyEvent.class, contextType=Void.class)
    @ContextInsensitive
    static class StateMachineExceptionSample3 extends AbstractUntypedStateMachine {
    }
    @Test(expected=IllegalStateException.class) 
    public void testUnregisterStateConverter() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample3.class);
        builder.newUntypedStateMachine("A");
    }
    
    @ContextEvent(finishEvent="FINISH")
    static class StateMachineExceptionSample4 extends StateMachineExceptionSample3 {
    }
    @Test(expected=SquirrelRuntimeException.class) 
    public void testUnregisterEventConverter() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample4.class);
        builder.newUntypedStateMachine("A");
    }
}
