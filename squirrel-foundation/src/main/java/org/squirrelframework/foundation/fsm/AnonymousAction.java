package org.squirrelframework.foundation.fsm;

public abstract class AnonymousAction<T extends StateMachine<T, S, E, C>, S, E, C> 
    implements Action<T, S, E, C> {

    @Override
    public String name() {
        return getClass().getSimpleName();
    }
    
    @Override
    public int weight() {
        return NORMAL_WEIGHT;
    }
    
    @Override
    final public String toString() {
        return "instance#"+getClass().getName();
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
