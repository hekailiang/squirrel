package org.squirrelframework.foundation.fsm.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineImporter;
import org.squirrelframework.foundation.util.ReflectUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StateMachineImporterImpl<T extends StateMachine<T, S, E, C>, S, E, C>
        extends DefaultHandler implements StateMachineImporter<T, S, E, C> {

    public static String SQRL_NAMESPACE = "http://squirrelframework.org/squirrel";
    
    public static String SCXML_NAMESPACE = "http://www.w3.org/2005/07/scxml";

    protected String value = StringUtils.EMPTY;
    
    protected String sqrlPrefix = StringUtils.EMPTY;
    
    protected Converter<S> stateConverter;
    
    protected Converter<E> eventConverter;
    
    protected StateMachineBuilder<T, S, E, C> stateMachineBuilder;
    
    protected MutableState<T, S, E, C> currentState;
    
    protected TransitionBuilderImpl<T, S, E, C> currentTranstionBuilder;
    
    protected Boolean isEntryAction;
    
    public void startPrefixMapping (String prefix, String uri)
            throws SAXException {
        if(uri.equals(SQRL_NAMESPACE)) {
            sqrlPrefix = prefix;
        }
    }

    /*
     * When the parser encounters plain text (not XML elements), it calls(this
     * method, which accumulates them in a string buffer
     */
    public void characters(char[] buffer, int start, int length) {
        value = new String(buffer, start, length);
    }

    /*
     * Every time the parser encounters the beginning of a new element, it calls
     * this method, which resets the string buffer
     */
    @SuppressWarnings("unchecked")
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if(localName.equals("fsm") && uri.equals(SQRL_NAMESPACE)) {
            // create state machine builder instance
            // TODO-hhe: attribute with namespace
            String fsmType = attributes.getValue("fsm-type");
            Class<T> stateMachineClazz = (Class<T>)ReflectUtils.getClass(fsmType);
            checkNotNull(stateMachineClazz);
            
            String stateType = attributes.getValue("state-type");
            Class<S> stateClazz = (Class<S>)ReflectUtils.getClass(stateType);
            checkNotNull(stateClazz);
            stateConverter = ConverterProvider.INSTANCE.getConverter(stateClazz);
            checkNotNull(stateConverter);
            
            String eventType =  attributes.getValue("event-type");
            Class<E> eventClazz = (Class<E>)ReflectUtils.getClass(eventType);
            checkNotNull(eventClazz);
            eventConverter = ConverterProvider.INSTANCE.getConverter(eventClazz);
            checkNotNull(eventConverter);
            
            String contextType = attributes.getValue("context-type");
            Class<C> contextClazz = (Class<C>)ReflectUtils.getClass(contextType);
            checkNotNull(contextClazz);
            
            // TODO-hhe: export extra parameters from builder to state machine instance
            stateMachineBuilder = StateMachineBuilderFactory.create(
                    stateMachineClazz, stateClazz, eventClazz, contextClazz, new Class<?>[0]);
            
            currentState = null;
            currentTranstionBuilder = null;
        } else if(qName.equals("state")) {
            String stateIdName = attributes.getValue("id");
            S stateId = stateConverter.convertFromString(stateIdName);
            currentState = stateMachineBuilder.defineState(stateId);
        } else if(qName.equals("onentry")) {
            isEntryAction = Boolean.TRUE;
        } else if(qName.equals("onexit")) {
            isEntryAction = Boolean.FALSE;
        } else if(localName.equals("action") && uri.equals(SQRL_NAMESPACE)) {
            String actionContent = attributes.getValue("content");
            int pos = actionContent.indexOf("#");
            String actionSchema = actionContent.substring(0, pos);
            String _tmp = actionContent.substring(pos+1);
            pos = _tmp.indexOf(":");
            String actionValue = _tmp.substring(0, pos);
            String actionWeightValue = _tmp.substring(pos+1);
            if(actionSchema.equals("method")) {
                String methodCallDesc = actionValue+":"+actionWeightValue;
                if(isConstructState()) {
                    if(Boolean.TRUE==isEntryAction) {
                        stateMachineBuilder.onEntry(currentState.getStateId()).callMethod(methodCallDesc);
                    } else {
                        stateMachineBuilder.onExit(currentState.getStateId()).callMethod(methodCallDesc);
                    }
                } else if(isConstructTransition()) {
                    
                }
            } else if(actionSchema.equals("instance")) {
                // TODO-hhe: handle if action instance constructor has parameter
                Action<T, S, E, C> action = ReflectUtils.newInstance(actionValue);
                if(isConstructState()) {
                    if(Boolean.TRUE==isEntryAction) {
                        stateMachineBuilder.onEntry(currentState.getStateId()).perform(action);
                    } else if (Boolean.FALSE==isEntryAction) { 
                        stateMachineBuilder.onExit(currentState.getStateId()).perform(action);
                    }
                } else if(isConstructTransition()) {
                    currentTranstionBuilder.perform(action);
                }
            } else if(actionSchema.equals("mvel")) {
                if(isConstructState()) {
                    if(Boolean.TRUE==isEntryAction) {
                        stateMachineBuilder.onEntry(currentState.getStateId()).evalMvel(actionValue);
                    } else if (Boolean.FALSE==isEntryAction) { 
                        stateMachineBuilder.onExit(currentState.getStateId()).evalMvel(actionValue);
                    }
                } else if(isConstructTransition()) {
                    currentTranstionBuilder.evalMvel(actionValue);
                }
            }
        } else if(qName.equals("transition")) {
            String eventName = attributes.getValue("event");
            E event = eventConverter.convertFromString(eventName);
            
            String targetState = attributes.getValue("target");
            S targetStateId = stateConverter.convertFromString(targetState);
            
            //TODO-hhe: transition type/priority should exported and be aware here
            currentTranstionBuilder = (TransitionBuilderImpl<T, S, E, C>) stateMachineBuilder.externalTransition();
            currentTranstionBuilder.from(currentState.getStateId()).to(targetStateId).on(event);
            String conditionScript = attributes.getValue("cond");
            int condPos = conditionScript.indexOf("#");
            String condSchema = conditionScript.substring(0, condPos);
            String condContent = conditionScript.substring(condPos+1);
            if(condSchema.equals("instance")) {
                // TODO-hhe: handle if condition instance constructor has parameter
                Condition<C> cond = ReflectUtils.newInstance(condContent);
                currentTranstionBuilder.when(cond);
            } else if(condSchema.equals("mvel")) {
                currentTranstionBuilder.whenMvel(condContent);
            }
        }
    }
    
    protected boolean isConstructState() {
        return currentState!=null && currentTranstionBuilder==null && isEntryAction!=null;
    }
    
    protected boolean isConstructTransition() {
        return currentState!=null && currentTranstionBuilder!=null && isEntryAction==null;
    }
    
    /*
     * When the parser encounters the end of an element, it calls this method
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if(qName.equals("onentry") || qName.equals("onexit")) {
            isEntryAction = null;
        } else if(qName.equals("state")) {
            currentState = null;
        } else if(qName.equals("transition")) {
            currentTranstionBuilder = null;
        }
    }

    @Override
    public StateMachineBuilder<T, S, E, C> importFromInputStream(InputStream content) {
        SAXParserFactory spfac = SAXParserFactory.newInstance();
        spfac.setNamespaceAware(true);
        try {
            SAXParser sp = spfac.newSAXParser();
            sp.parse(content, this);
            return stateMachineBuilder;
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect content format.", e);
        } 
    }

    @Override
    public StateMachineBuilder<T, S, E, C> importFromString(String content) {
        return importFromInputStream(new ByteArrayInputStream(content.getBytes()));
    }

    @Override
    public StateMachineBuilder<T, S, E, C> importFromFile(File content) {
        try {
            return importFromInputStream(new FileInputStream(content));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Canont find file", e);
        }
    }
}
