package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.*;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Use visitor pattern to export SCXML definition.
 * 
 * @author Henry.He
 *
 */
class SCXMLVisitorImpl extends AbstractVisitor implements SCXMLVisitor {
    
    @Override
    public void visitOnEntry(StateMachine<?, ?, ?, ?> visitable) {
        writeLine("<scxml initial=" + quoteName(visitable.getInitialState().toString()) + " version=\"1.0\" " +
                "xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:sqrl=\"http://squirrelframework.org/squirrel\">");
        writeLine("<sqrl:fsm "+visitable.getDescription()+" />");
    }

    @Override
    public void visitOnExit(StateMachine<?, ?, ?, ?> visitable) {
        writeLine("</scxml>");
    }

    @Override
    public void visitOnEntry(ImmutableState<?, ?, ?, ?> visitable) {
        if(visitable.isParallelState()) {
            writeLine("<parallel id= " + quoteName(visitable.toString()) + ">");
        } else if(visitable.isFinalState()) {
            writeLine("<final id= " + quoteName(visitable.toString()) + ">");
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
            for(Action<?, ?, ?, ?> entryAction : visitable.getEntryActions()) {
                writeAction(entryAction);
            }
            writeLine("</onentry>");
        }
        if(visitable.getHistoryType()!=HistoryType.NONE) {
            writeLine("<history type= "+ quoteName(visitable.getHistoryType().name().toLowerCase())+"/>");
        }
    }

    @Override
    public void visitOnExit(ImmutableState<?, ?, ?, ?> visitable) {
        if(!visitable.getExitActions().isEmpty()) {
            writeLine("<onexit>");
            for(Action<?, ?, ?, ?> exitAction : visitable.getExitActions()) {
                writeAction(exitAction);
            }
            writeLine("</onexit>");
        }
        if(visitable.isParallelState()) 
            writeLine("</parallel>");
        else if(visitable.isFinalState()) 
            writeLine("</final>");
        else 
            writeLine("</state>");
    }

    @Override
    public void visitOnEntry(ImmutableTransition<?, ?, ?, ?> visitable) {
        writeLine("<transition event="
                + quoteName(visitable.getEvent().toString()) + " sqrl:priority="
                + quoteName(Integer.toString(visitable.getPriority())) + " sqrl:type="
                + quoteName(visitable.getType().toString()) + " target="
                + quoteName(visitable.getTargetState().toString()) + " cond="
                + quoteName(visitable.getCondition().toString())+">");
        for(Action<?, ?, ?, ?> action : visitable.getActions()) {
            writeAction(action);
        }
    }

    @Override
    public void visitOnExit(ImmutableTransition<?, ?, ?, ?> visitable) {
        writeLine("</transition>");
    }
    
    private void writeAction(final Action<?, ?, ?, ?> action) {
        if(isExternalAction(action))
            writeLine("<sqrl:action content="+quoteName(action.toString())+"/>");
    }
    
    private boolean isExternalAction(final Action<?, ?, ?, ?> action) {
        return action.name().startsWith("__")==false;
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
