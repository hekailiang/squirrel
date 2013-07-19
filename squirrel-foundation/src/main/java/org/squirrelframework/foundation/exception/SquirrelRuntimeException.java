package org.squirrelframework.foundation.exception;

public class SquirrelRuntimeException extends RuntimeException {
    
    private static final long serialVersionUID = -4324278329515258777L;
    
    private int errorCodeId;
    
    private Throwable targetException;
    
    private String errorMessage;
    
//    private String localizedErrorMessage;
    
    public SquirrelRuntimeException(ErrorCodes errorCode) {
        this.errorCodeId = errorCode.getCode();
        this.errorMessage = String.format("%08d : %s.", getErrorCodeId(), errorCode.getDescription());
    }
    
    public SquirrelRuntimeException(Throwable targetException, ErrorCodes errorCode) {
        this(errorCode);
        this.targetException = targetException;
    }
    
    public SquirrelRuntimeException(ErrorCodes errorCode, Object...parameters) {
        this.errorCodeId = errorCode.getCode();
        this.errorMessage = String.format("%08d : %s.", getErrorCodeId(), 
                String.format(errorCode.getDescription(), parameters));
    }
    
    public SquirrelRuntimeException(Throwable targetException, ErrorCodes errorCode, Object...parameters) {
        this(errorCode, parameters);
        this.targetException = targetException;
    }
    
    public int getErrorCodeId() {
        return errorCodeId;
    }
    
    public Throwable getTargetException() {
        return targetException;
    }
    
    @Override
    public String getMessage() {
        return errorMessage;
    }
}
