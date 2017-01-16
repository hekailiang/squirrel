package org.squirrelframework.foundation.fsm.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.MvelScriptManager;

class MvelConditionImpl<C> implements Condition<C> {
    
    private static final Logger logger = LoggerFactory.getLogger(MvelConditionImpl.class);
    
    private final String mvelExpression;
    
    private final String name;
    
    private final MvelScriptManager scriptManager;
    
    private final String script;
    
    MvelConditionImpl(String script, MvelScriptManager scriptManager) {
        String[] arrays = StringUtils.split(script, MvelScriptManager.SEPARATOR_CHARS);
        if(arrays.length==2) {
            this.name = arrays[0].trim();
            this.mvelExpression = arrays[1].trim();
        } else {
            this.name = "_NoName_";
            this.mvelExpression = arrays[0].trim();
        }
        
        this.script = script;
        this.scriptManager = scriptManager;
        
        scriptManager.compile(mvelExpression);
    }

    @Override
    public boolean isSatisfied(C context) {
        try {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put(MvelScriptManager.VAR_CONTEXT, context);
            return scriptManager.evalBoolean(mvelExpression, variables);
        } catch (Exception e) {
            logger.error("Evaluate \""+mvelExpression+"\" failed with "+e.getMessage()+(e.getCause()!=null ? ", which caused by "+e.getCause().getMessage() : ""));
            return false;
        }
    }

    @Override
    public String name() {
        return name;
    }
    
    @Override
    final public String toString() {
        return "mvel#"+script;
    }

}
