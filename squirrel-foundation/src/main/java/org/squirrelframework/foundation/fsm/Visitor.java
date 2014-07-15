package org.squirrelframework.foundation.fsm;

/**
 * @author Henry.He
 */
public interface Visitor {
    
    /**
     * @param visitable the element to be visited.
     */
    void visitOnEntry(StateMachine<?, ?, ?, ?> visitable);
    
    /**
     * @param visitable the element to be visited.
     */
    void visitOnExit(StateMachine<?, ?, ?, ?> visitable);
    
    /**
     * @param visitable the element to be visited.
     */
    void visitOnEntry(ImmutableState<?, ?, ?, ?> visitable);
    
    /**
     * @param visitable the element to be visited.
     */
    void visitOnExit(ImmutableState<?, ?, ?, ?> visitable);
    
    /**
     * @param visitable the element to be visited.
     */
    void visitOnEntry(ImmutableTransition<?, ?, ?, ?> visitable);
    
    /**
     * @param visitable the element to be visited.
     */
    void visitOnExit(ImmutableTransition<?, ?, ?, ?> visitable);
}
