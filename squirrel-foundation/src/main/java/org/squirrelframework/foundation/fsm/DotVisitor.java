package org.squirrelframework.foundation.fsm;

/**
 * Visit state machine model structure and export dot file which can be opened by Graphviz.
 * 
 * @author Henry.He
 *
 */
public interface DotVisitor extends Visitor {
    
    /**
     * Create dot file
     * @param filename name of dot file
     */
    void convertDotFile(String filename);
}
