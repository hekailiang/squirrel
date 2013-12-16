package org.squirrelframework.foundation.fsm.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.MvelScriptManager;

class MvelConditionImpl<C> implements Condition<C> {
    
    public final static String separatorChars = ":::";
    
    private final String mvelExpression;
    
    private final String name;
    
    private final MvelScriptManager scriptManager;
    
    MvelConditionImpl(String script, MvelScriptManager scriptManager) {
        String[] arrays = StringUtils.split(script, separatorChars);
        if(arrays.length==2) {
            this.name = arrays[0].trim();
            this.mvelExpression = arrays[1].trim();
        } else {
            this.name = "_NoName_";
            this.mvelExpression = arrays[1].trim();
        }
        this.scriptManager = scriptManager;
        
        scriptManager.compile(mvelExpression);
    }

    @Override
    public boolean isSatisfied(C context) {
        try {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("context", context);
            return scriptManager.evalBoolean(mvelExpression, variables);
        } catch (Exception e) {
            System.err.println("Evaluate \""+mvelExpression+"\" failed, which caused by "+e.getCause().getMessage());
            return false;
        }
    }

    @Override
    public String name() {
        return name;
    }

}
