package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.Condition;

public class MvelConditionImpl<C> implements Condition<C> {
    
    private String mvelExpression;
    
    public MvelConditionImpl(String mvelExpression) {
        this.mvelExpression = mvelExpression;
    }

    @Override
    public boolean isSatisfied(C context) {
        System.out.println("MVEL Expression: " + mvelExpression);
        return true;
    }

}
