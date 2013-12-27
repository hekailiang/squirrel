package org.squirrelframework.foundation.fsm;

public interface MvelScriptManager {
    
    public final static String SEPARATOR_CHARS = ":::";
    
    <T> T eval(String script, Object context, Class<T> returnType);
    
    void compile(String script);
    
    boolean evalBoolean(String script, Object context);
}
