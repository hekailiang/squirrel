package org.squirrelframework.foundation.fsm.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.MvelScriptManager;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.base.Preconditions;

class MvelActionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Action<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(MvelActionImpl.class);
    
    private final String mvelExpression;
    
    private final MvelScriptManager scriptManager;
    
    private final String name;
    
    private final String script;
    
    MvelActionImpl(String script, ExecutionContext executionContext) {
        Preconditions.checkNotNull(script);
        String[] arrays = StringUtils.split(script, MvelScriptManager.SEPARATOR_CHARS);
        if(arrays.length==2) {
            this.name = arrays[0].trim();
            this.mvelExpression = arrays[1].trim();
        } else {
            this.name = "_NoName_";
            this.mvelExpression = arrays[0].trim();
        }
        
        this.script = script;
        this.scriptManager = executionContext.getScriptManager();
        scriptManager.compile(mvelExpression);
    }
    
    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        try {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put(MvelScriptManager.VAR_FROM, from);
            variables.put(MvelScriptManager.VAR_TO, to);
            variables.put(MvelScriptManager.VAR_EVENT, event);
            variables.put(MvelScriptManager.VAR_CONTEXT, context);
            variables.put(MvelScriptManager.VAR_STATE_MACHINE, stateMachine);
            scriptManager.eval(mvelExpression, variables, Void.class);
        } catch (RuntimeException e) {
            logger.error("Evaluate \""+mvelExpression+"\" failed, which caused by "+e.getCause().getMessage());
            throw e;
        }
    }
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public int weight() {
        return Action.NORMAL_WEIGHT;
    }
    
    @Override
    final public String toString() {
        return "mvel#"+script;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 + mvelExpression.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        MvelActionImpl<T, S, E, C> other = (MvelActionImpl<T, S, E, C>) obj;
        if (mvelExpression == null) {
            if (other.mvelExpression != null)
                return false;
        } else if (!mvelExpression.equals(other.mvelExpression))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public long timeout() {
        return -1;
    }
}
