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
    
    public TransitionException(Throwable targetException, ErrorCodes errorCode, Object[] parameters) {
        super(targetException, errorCode, parameters);
    }
}
