package org.squirrelframework.foundation.fsm;

/**
 * Visit state machine model structure and export SCXML definition.
 * @author Henry.He
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
