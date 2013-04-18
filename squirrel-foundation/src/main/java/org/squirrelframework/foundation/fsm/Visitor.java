package org.squirrelframework.foundation.fsm;

/**
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface Visitor<T extends StateMachine<T, S, E, C>, S, E, C> {
    
	/**
	 * @param visitable the element to be visited.
	 */
    void visitOnEntry(StateMachine<T, S, E, C> visitable);
    
    /**
	 * @param visitable the element to be visited.
	 */
    void visitOnExit(StateMachine<T, S, E, C> visitable);
    
    /**
	 * @param visitable the element to be visited.
	 */
    void visitOnEntry(ImmutableState<T, S, E, C> visitable);
    
    /**
	 * @param visitable the element to be visited.
	 */
    void visitOnExit(ImmutableState<T, S, E, C> visitable);
    
    /**
	 * @param visitable the element to be visited.
	 */
    void visitOnEntry(ImmutableTransition<T, S, E, C> visitable);
    
    /**
	 * @param visitable the element to be visited.
	 */
    void visitOnExit(ImmutableTransition<T, S, E, C> visitable);
}
