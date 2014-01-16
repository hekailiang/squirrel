package org.squirrelframework.foundation.fsm;

public interface MutableTimedState<T extends StateMachine<T, S, E, C>, S, E, C> extends MutableState<T, S, E, C> {
    
    void setTimeInterval(Integer timeInterval);
    
    void setInitialDelay(Integer initialDelay);
    
    void setAutoFireEvent(E event);
    
    void setAutoFireContext(C context);
}
