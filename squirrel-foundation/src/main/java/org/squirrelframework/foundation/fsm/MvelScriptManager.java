package org.squirrelframework.foundation.fsm;

public interface MvelScriptManager {
    
    public final static String SEPARATOR_CHARS = ":::";
    
    public final static String VAR_FROM = "from";
    
    public final static String VAR_TO = "to";
    
    public final static String VAR_EVENT = "event";
    
    public final static String VAR_CONTEXT = "context";
    
    public final static String VAR_STATE_MACHINE = "stateMachine";
    
    public final static String VAR_EXCEPTION = "exception";
    
    <T> T eval(String script, Object context, Class<T> returnType);
    
    void compile(String script);
    
    boolean evalBoolean(String script, Object context);
}
