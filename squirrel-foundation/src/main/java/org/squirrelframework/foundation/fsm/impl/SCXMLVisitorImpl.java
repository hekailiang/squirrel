package org.squirrelframework.foundation.fsm.impl;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.SCXMLVisitor;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * Use visitor pattern to export SCXML definition.
 * 
 * @author Henry.He
 *
 * @param <T> state machine type
 * @param <S> state type
 * @param <E> event type
 * @param <C> context type
 */
class SCXMLVisitorImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractVisitor implements SCXMLVisitor<T, S, E, C> {
    
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
    	if(visitable.isParallelState()) {
    		writeLine("<parallel id= " + quoteName(visitable.toString()) + ">");
    	} else { 
    		StringBuilder builder = new StringBuilder("<state id= ");
    		builder.append(quoteName(visitable.toString()));
    		if(visitable.getInitialState()!=null) {
    			builder.append(" initial= ").append(quoteName(visitable.getInitialState().toString()));
    		}
    		builder.append(">");
			writeLine(builder.toString());
    	}
        if(!visitable.getEntryActions().isEmpty()) {
            writeLine("<onentry>");
            for(Action<T, S, E, C> entryAction : visitable.getEntryActions()) {
                writeAction(entryAction);
            }
            writeLine("</onentry>");
        }
        if(visitable.getHistoryType()!=HistoryType.NONE) {
			writeLine("<history type= "+ quoteName(visitable.getHistoryType().name().toLowerCase())+"/>");
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
        if(visitable.isParallelState()) 
        	writeLine("</parallel>"); 
        else 
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
    
    private void writeAction(final Action<T, S, E, C> action) {
        writeLine("<raise event="+quoteName(action.toString())+"/>");
    }

    @Override
    public String getScxml(boolean beautifyXml) {
        return beautifyXml ? beautify(buffer.toString()) : buffer.toString();
    }
    
    @Override
    public void convertSCXMLFile(final String filename, boolean beautifyXml) {
        saveFile(filename + ".scxml", getScxml(beautifyXml));
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
