package org.squirrel.foundation.fsm.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.squirrel.foundation.fsm.Action;
import org.squirrel.foundation.fsm.ImmutableState;
import org.squirrel.foundation.fsm.ImmutableTransition;
import org.squirrel.foundation.fsm.SCXMLVisitor;
import org.squirrel.foundation.fsm.StateMachine;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;


public class SCXMLVisitorImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements SCXMLVisitor<T, S, E, C> {
    
    private final StringBuilder scxml = new StringBuilder();
    
    @Override
    public void visitOnEntry(StateMachine<T, S, E, C> visitable) {
        writeLine("<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" initial="
                + quoteName(visitable.getInitialState().toString()) + " version=\"1.0\">");
    }

    @Override
    public void visitOnExit(StateMachine<T, S, E, C> visitable) {
        writeLine("<final id=\"Final\" />");
        writeLine("</scxml>");
    }

    @Override
    public void visitOnEntry(ImmutableState<T, S, E, C> visitable) {
        writeLine("<state id= " + quoteName(visitable.toString()) + ">");
        if(!visitable.getEntryActions().isEmpty()) {
            writeLine("<onentry>");
            for(Action<T, S, E, C> entryAction : visitable.getEntryActions()) {
                writeAction(entryAction);
            }
            writeLine("</onentry>");
        }
    }

    @Override
    public void visitOnExit(ImmutableState<T, S, E, C> visitable) {
        if(!visitable.getExitActions().isEmpty()) {
            writeLine("<onexit>");
            for(Action<T, S, E, C> exitAction : visitable.getExitActions()) {
                writeAction(exitAction);
            }
            writeLine("</onexit>");
        }
        writeLine("</state>");
    }

    @Override
    public void visitOnEntry(ImmutableTransition<T, S, E, C> visitable) {
        writeLine("<transition event="
                + quoteName(visitable.getEvent().toString()) + " target="
                + quoteName(visitable.getTargetState().toString()) + " cond="
                + quoteName(visitable.getCondition().getClass().getSimpleName())+">");
        for(Action<T, S, E, C> action : visitable.getActions()) {
            writeAction(action);
        }
    }

    @Override
    public void visitOnExit(ImmutableTransition<T, S, E, C> visitable) {
        writeLine("</transition>");
    }
    
    private String quoteName(final String id) {
        return "\"" + id + "\"";
    }
    
    private void writeAction(final Action<T, S, E, C> action) {
        writeLine("<raise event="+quoteName(action.toString())+"/>");
    }

    private void writeLine(final String msg) {
        scxml.append(msg).append("\n");
    }
    
    @Override
    public String getScxml(boolean beautifyXml) {
        return beautifyXml ? beautify(scxml.toString()) : scxml.toString();
    }
    
    @Override
    public void convertSCXMLFile(final String filename, boolean beautifyXml) {
        try {
            FileWriter file = new FileWriter(filename + ".scxml");
            file.write(getScxml(beautifyXml));
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String beautify(String unformattedXml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(unformattedXml));
            Document doc = db.parse(is);
            
            DOMImplementationRegistry domReg = DOMImplementationRegistry.newInstance();
            DOMImplementationLS lsImpl = (DOMImplementationLS) domReg.getDOMImplementation("LS");
            LSSerializer lsSerializer = lsImpl.createLSSerializer();
            lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            LSOutput output = lsImpl.createLSOutput();
            output.setEncoding("UTF-8");
            
            StringWriter destination = new StringWriter();
            output.setCharacterStream(destination);
            lsSerializer.write(doc, output);
            return destination.toString();
        } catch (Exception e) {
            // format failed, return unformatted xml
            return unformattedXml;
        } 
    }
}
