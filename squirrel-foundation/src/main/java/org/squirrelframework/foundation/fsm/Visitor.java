package org.squirrelframework.foundation.fsm;

public interface Visitor<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    void visitOnEntry(StateMachine<T, S, E, C> visitable);
    
    void visitOnExit(StateMachine<T, S, E, C> visitable);
    
    void visitOnEntry(ImmutableState<T, S, E, C> visitable);
    
    void visitOnExit(ImmutableState<T, S, E, C> visitable);
    
    void visitOnEntry(ImmutableTransition<T, S, E, C> visitable);
    
    void visitOnExit(ImmutableTransition<T, S, E, C> visitable);
}
