package org.squirrelframework.foundation.fsm;

/**
 * Visit state machine model structure and export dot file which can be opened by Graphviz.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface DotVisitor<T extends StateMachine<T, S, E, C>, S, E, C> extends Visitor<T, S, E, C> {
    
    /**
     * Create dot file
     * @param filename name of dot file
     */
    void convertDotFile(String filename);
}
