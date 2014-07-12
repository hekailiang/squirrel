package org.squirrelframework.foundation.fsm;

/**
 * Visit state machine model structure and export SCXML definition.
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface SCXMLVisitor extends Visitor {

    /**
     * @param beautifyXml whether beautify XML format or not
     * @return SCXML definition
     */
    String getScxml(boolean beautifyXml);
    
    /**
     * Create scxml file 
     * @param filename file name
     * @param beautifyXml whether beautify XML format or not
     */
    void convertSCXMLFile(final String filename, boolean beautifyXml);
}
