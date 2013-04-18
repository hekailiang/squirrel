package org.squirrelframework.foundation.fsm;

/**
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface Visitable<T extends StateMachine<T, S, E, C>, S, E, C> {
	/**
	 * Accepts a {@link #Visitor}.
	 * 
	 * @param visitor the visitor.
	 */
    void accept(final Visitor<T, S, E, C> visitor);
}
