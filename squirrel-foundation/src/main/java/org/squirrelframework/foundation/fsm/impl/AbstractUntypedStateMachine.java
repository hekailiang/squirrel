package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.UntypedStateMachine;

import com.google.common.base.Preconditions;

public abstract class AbstractUntypedStateMachine 
    extends AbstractStateMachine<UntypedStateMachine, Object, Object, Object> 
    implements UntypedStateMachine {

    private void verifyParameterType(Object event, Object context) {
        if(event!=null && !typeOfEvent().isAssignableFrom(event.getClass())) {
            throw new RuntimeException("Event type of state machine '"+typeOfEvent()+
                    "' is not match '"+event.getClass()+"'.");
        }
        if(context!=null && !typeOfContext().isAssignableFrom(context.getClass())) {
            throw new RuntimeException("Context type of state machine '"+typeOfContext()+
                    "' is not match '"+context.getClass()+"'.");
        }
    }
    
    @Override
    public void fire(Object event, Object context) {
        Preconditions.checkNotNull(event, "Cannot fire null event.");
        verifyParameterType(event, context);
        super.fire(event, context);
    }
    
    @Override
    public void fireImmediate(Object event, Object context) {
        Preconditions.checkNotNull(event, "Cannot fire null event.");
        verifyParameterType(event, context);
        super.fire(event, context);
    }
    
    @Override
    public Object test(Object event, Object context) {
        Preconditions.checkNotNull(event, "Cannot fire null event.");
        verifyParameterType(event, context);
        return super.test(event, context);
    }
    
    @Override
    public void start(Object context) {
        verifyParameterType(null, context);
        super.start(context);
    }
    
    @Override
    public void terminate(Object context) {
        verifyParameterType(null, context);
        super.terminate(context);
    }
}
