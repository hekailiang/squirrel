package org.squirrelframework.foundation.fsm;

import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParamters;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class StateMachineExceptionTest {
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB"),
        @Transit(from="A", to="C", on="ToC"),
        @Transit(from="A", to="D", on="ToD")
    })
    @StateMachineParamters(stateType=String.class, eventType=String.class, contextType=Void.class)
    @ContextInsensitive
    static class StateMachineExceptionSample extends AbstractUntypedStateMachine {

        protected StateMachineExceptionSample(ImmutableUntypedState initialState,
                Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
        
        protected void transitFromAToBOnToB(String from, String to, String event) {
            throw new IllegalArgumentException("This exception can be recovered.");
        }
        
        protected void transitFromAToCOnToC(String from, String to, String event) {
            throw new UnsupportedOperationException("This exception cannot be recovered.");
        }
        
        @Override
        protected void afterTransitionCausedException(TransitionException te, Object fromState, 
                Object toState, Object event, Object context) {
            Throwable targeException = te.getTargetException();
            if(targeException instanceof IllegalArgumentException && 
                    fromState.equals("A") && toState.equals("B") && event.equals("ToB")) {
                Assert.assertEquals(te.getTargetException().getMessage(), "This exception can be recovered.");
                // do some error clean up job here
                // ...
                // after recovered from this exception, reset the state machine status back to normal
                setStatus(StateMachineStatus.IDLE);
            } else {
                super.afterTransitionCausedException(te, fromState, toState, event, context);
            }
        }
    }
    
    private StateMachineExceptionSample fsm;
    
    @After
    public void teardown() {
        if(fsm.getStatus()!=StateMachineStatus.TERMINATED)
            fsm.terminate(null);
    }
    
    @Before
    public void setup() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineExceptionSample.class);
        fsm = builder.newUntypedStateMachine("A", StateMachineExceptionSample.class);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testExceptionFail() throws Throwable {
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
        fsm.fire("ToB");
        Assert.assertEquals(fsm.getCurrentState(), "A");
        fsm.fire("ToD");
        Assert.assertEquals(fsm.getCurrentState(), "D");
    }

}
