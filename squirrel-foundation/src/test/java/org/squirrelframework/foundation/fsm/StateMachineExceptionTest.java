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
        protected void afterTransitionCausedException(Exception e, Object fromState, 
                Object toState, Object event, Object context) {
            // currently the passed in parameter e should be always TransitionException, 
            // in the future this parameter type will be changed to TransitionException.
            // so there is no need to perform type casting anymore in future.
            if(e instanceof TransitionException) {
                TransitionException te = (TransitionException)e;
                Throwable targeException = te.getTargetException();
                if(targeException instanceof IllegalArgumentException && 
                        fromState.equals("A") && toState.equals("B") && event.equals("ToB")) {
                    Assert.assertEquals(te.getTargetException().getMessage(), "This exception can be recovered.");
                    // do some error clean up job here
                    // ...
                    // after recovered from this exception, reset the state machine status back to normal
                    setStatus(StateMachineStatus.IDLE);
                    return;
                }
                // in the future the default implementation of afterTransitionCausedException 
                // will be do nothing but continue throw out exception, so this line of code 
                // could be changed to "super.afterTransitionCausedException(...)" in future.
                throw te;
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
    
    @Test(expected=TransitionException.class)
    public void testExceptionFail() throws Exception {
        try {
            fsm.fire("ToC", null);
        } catch(TransitionException e) {
            Assert.assertEquals(fsm.getStatus(), StateMachineStatus.ERROR);
            throw e;
        }
        Assert.fail();
    }
    
    @Test
    public void testExceptionRecovered() {
        fsm.fire("ToB", null);
        Assert.assertEquals(fsm.getCurrentState(), "A");
        fsm.fire("ToD", null);
        Assert.assertEquals(fsm.getCurrentState(), "D");
    }

}
