package org.squirrel.foundation.fsm;

public interface SCXMLVisitor<T extends StateMachine<T, S, E, C>, S, E, C> extends Visitor<T, S, E, C> {
    String getScxml(boolean beautifyXml);
    
    void convertSCXMLFile(final String filename, boolean beautifyXml);
}
