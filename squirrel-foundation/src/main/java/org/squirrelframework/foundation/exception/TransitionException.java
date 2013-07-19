package org.squirrelframework.foundation.exception;

/**
 * 
 * Transition Exception class
 * 
 * @author Henry.He
 *
 */
public class TransitionException extends SquirrelRuntimeException {

    private static final long serialVersionUID = -3907699293556397027L;
    
    private final Object sourceState;
    
    private final Object targetState;
    
    private final Object event; 
    
    private final Object context;
    
    private final Object stateMachine;

    public TransitionException(Throwable targetException, ErrorCodes errorCode, 
            Object sourceState, Object targetState, Object event, Object context, Object stateMachine) {
        super(errorCode, errorCode);
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.event = event;
        this.context = context;
        this.stateMachine = stateMachine;
    }
    
    public Object getSourceState() {
        return sourceState;
    }

    public Object getTargetState() {
        return targetState;
    }

    public Object getEvent() {
        return event;
    }

    public Object getContext() {
        return context;
    }

    public Object getStateMachine() {
        return stateMachine;
    }

}
