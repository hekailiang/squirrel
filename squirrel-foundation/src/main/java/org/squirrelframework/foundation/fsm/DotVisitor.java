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
public interface DotVisitor extends Visitor {
    
    /**
     * Create dot file
     * @param filename name of dot file
     */
    void convertDotFile(String filename);
}
