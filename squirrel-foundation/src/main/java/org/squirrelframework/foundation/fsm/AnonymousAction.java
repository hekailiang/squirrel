package org.squirrelframework.foundation.fsm;

public abstract class AnonymousAction<T extends StateMachine<T, S, E, C>, S, E, C> 
    implements Action<T, S, E, C> {

    @Override
    public String name() {
        return "_ANONYMOUS_ACTION";
    }
    
    @Override
    public int weight() {
        return NORMAL_WEIGHT;
    }

}
