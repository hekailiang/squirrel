package org.squirrelframework.foundation.fsm.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.Conditions;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.StateCompositeType;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineImporter;
import org.squirrelframework.foundation.util.ReflectUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

public class StateMachineImporterImpl<T extends StateMachine<T, S, E, C>, S, E, C>
        extends DefaultHandler implements StateMachineImporter<T, S, E, C> {

    public static String SQRL_NAMESPACE = "http://squirrelframework.org/squirrel";
    
    public static String SCXML_NAMESPACE = "http://www.w3.org/2005/07/scxml";

    protected String value = StringUtils.EMPTY;
    
    protected String sqrlPrefix = StringUtils.EMPTY;
    
    protected Converter<S> stateConverter;
    
    protected Converter<E> eventConverter;
    
    protected StateMachineBuilder<T, S, E, C> stateMachineBuilder;
    
    protected final Stack<MutableState<T, S, E, C>> currentStates = new Stack<MutableState<T, S, E, C>>();
    
    protected TransitionBuilderImpl<T, S, E, C> currentTranstionBuilder;
    
    protected final ListMultimap<MutableState<T, S, E, C>, MutableState<T, S, E, C>> hierarchicalStateStore = 
            ArrayListMultimap.create();
    
    protected Boolean isEntryAction;
    
    protected final Map<String, Object> reusableInstance = Maps.newHashMap();
    
    public StateMachineImporterImpl() {
        registerReusableInstance(Conditions.always());
        registerReusableInstance(Conditions.never());
    }
    
    public void startPrefixMapping (String prefix, String uri)
            throws SAXException {
        if(uri.equals(SQRL_NAMESPACE)) {
            sqrlPrefix = prefix;
        }
    }

    public void characters(char[] buffer, int start, int length) {
        value = new String(buffer, start, length);
    }

    @SuppressWarnings("unchecked")
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if(localName.equals("fsm") && uri.equals(SQRL_NAMESPACE)) {
            // create state machine builder instance
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
            
            String extraParams = attributes.getValue("extra-parameters");
            Class<?>[] extraParamTypes = new Class<?>[0];
            if(extraParams!=null && extraParams.length()>2) {
                String[] typeNames = StringUtils.split(extraParams.substring(1, extraParams.length()-1), ',');
                extraParamTypes = new Class<?>[typeNames.length];
                for(int i=0; i<typeNames.length; ++i) {
                    extraParamTypes[i] = ReflectUtils.getClass(typeNames[i]);
                }
            }
            stateMachineBuilder = StateMachineBuilderFactory.create(
                    stateMachineClazz, stateClazz, eventClazz, contextClazz, extraParamTypes);
            ((StateMachineBuilderImpl<T, S, E, C>)stateMachineBuilder).setScanAnnotations(false);
            
            String finishEventName = attributes.getValue("finish-event");
            if(finishEventName!=null) {
                E finishEvent = eventConverter.convertFromString(finishEventName);
                stateMachineBuilder.defineFinishEvent(finishEvent);
            }
            
            String startEventName = attributes.getValue("start-event");
            if(startEventName!=null) {
                E startEvent = eventConverter.convertFromString(startEventName);
                stateMachineBuilder.defineStartEvent(startEvent);
            }
            
            String terminateEventName = attributes.getValue("terminate-event");
            if(terminateEventName!=null) {
                E terminateEvent = eventConverter.convertFromString(terminateEventName);
                stateMachineBuilder.defineTerminateEvent(terminateEvent);
            }
            
            currentStates.clear();
            currentTranstionBuilder=null;
        } else if(qName.equals("state") || qName.equals("final") || qName.equals("parallel")) {
            MutableState<T, S, E, C> parentState = null;
            if(currentStates.size()>0) {
                parentState = getCurrentState();
            }
            String stateIdName = attributes.getValue("id");
            S stateId = stateConverter.convertFromString(stateIdName);
            if(qName.equals("final")) {
                currentStates.push(stateMachineBuilder.defineFinalState(stateId));
            } else {
                currentStates.push(stateMachineBuilder.defineState(stateId));
                if(qName.equals("parallel")) {
                    getCurrentState().setCompositeType(StateCompositeType.PARALLEL);
                }
            }
            String initStateIdName = attributes.getValue("initial");
            if(initStateIdName!=null) {
                S initStateId = stateConverter.convertFromString(initStateIdName);
                getCurrentState().setInitialState(stateMachineBuilder.defineState(initStateId));
            }
            
            if(parentState!=null) {
                hierarchicalStateStore.put(parentState, getCurrentState());
            }
        } else if(qName.equals("history")) {
            String historyType = attributes.getValue("type");
            if(historyType.equals("deep")) {
                getCurrentState().setHistoryType(HistoryType.DEEP);
            } else if(historyType.equals("shallow")) {
                getCurrentState().setHistoryType(HistoryType.SHALLOW);
            }
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
            String actionValue = _tmp;
            String actionWeightValue = "";
            if(pos>0) {
                actionValue = _tmp.substring(0, pos);
                actionWeightValue = _tmp.substring(pos+1);
            }
            if(actionSchema.equals("method")) {
                String methodCallDesc = actionValue+":"+actionWeightValue;
                if(isConstructState()) {
                    if(Boolean.TRUE==isEntryAction) {
                        stateMachineBuilder.onEntry(getCurrentState().getStateId()).callMethod(methodCallDesc);
                    } else {
                        stateMachineBuilder.onExit(getCurrentState().getStateId()).callMethod(methodCallDesc);
                    }
                } else if(isConstructTransition()) {
                    getCurrentTranstionBuilder().callMethod(methodCallDesc);
                }
            } else if(actionSchema.equals("instance")) {
                // NOTE: user should provider no-args constructor for action always
                Action<T, S, E, C> action = newInstance(actionValue);
                if(isConstructState()) {
                    if(Boolean.TRUE==isEntryAction) {
                        stateMachineBuilder.onEntry(getCurrentState().getStateId()).perform(action);
                    } else if (Boolean.FALSE==isEntryAction) { 
                        stateMachineBuilder.onExit(getCurrentState().getStateId()).perform(action);
                    }
                } else if(isConstructTransition()) {
                    getCurrentTranstionBuilder().perform(action);
                }
            } else if(actionSchema.equals("mvel")) {
                if(isConstructState()) {
                    if(Boolean.TRUE==isEntryAction) {
                        stateMachineBuilder.onEntry(getCurrentState().getStateId()).evalMvel(actionValue);
                    } else if (Boolean.FALSE==isEntryAction) { 
                        stateMachineBuilder.onExit(getCurrentState().getStateId()).evalMvel(actionValue);
                    }
                } else if(isConstructTransition()) {
                    getCurrentTranstionBuilder().evalMvel(actionValue);
                }
            }
        } else if(qName.equals("transition")) {
            String eventName = attributes.getValue("event");
            E event = eventConverter.convertFromString(eventName);
            
            String targetState = attributes.getValue("target");
            S targetStateId = stateConverter.convertFromString(targetState);
            
            String transitionType = attributes.getValue(SQRL_NAMESPACE, "type");
            Integer transitionPriority = 1;
            try {
                transitionPriority = Integer.valueOf(attributes.getValue(SQRL_NAMESPACE, "priority"));
            } catch (NumberFormatException e) {}
            TransitionBuilderImpl<T, S, E, C> builder = null;
            if(transitionType.equals("INTERNAL")) {
                builder = (TransitionBuilderImpl<T, S, E, C>) stateMachineBuilder.internalTransition(transitionPriority);
            } else if(transitionType.equals("LOCAL")) {
                builder = (TransitionBuilderImpl<T, S, E, C>) stateMachineBuilder.localTransition(transitionPriority);
            } else {
                builder = (TransitionBuilderImpl<T, S, E, C>) stateMachineBuilder.externalTransition(transitionPriority);
            }
            currentTranstionBuilder=builder;
            getCurrentTranstionBuilder().from(getCurrentState().getStateId()).to(targetStateId).on(event);
            String conditionScript = attributes.getValue("cond");
            int condPos = conditionScript.indexOf("#");
            String condSchema = conditionScript.substring(0, condPos);
            String condContent = conditionScript.substring(condPos+1);
            if(condSchema.equals("instance")) {
                // NOTE: user should provider no-args constructor for condition always
                Condition<C> cond = newInstance(condContent);
                getCurrentTranstionBuilder().when(cond);
            } else if(condSchema.equals("mvel")) {
                getCurrentTranstionBuilder().whenMvel(condContent);
            }
        }
    }
    
    protected boolean isConstructState() {
        return currentStates.size()>0 && currentTranstionBuilder==null && isEntryAction!=null;
    }
    
    protected boolean isConstructTransition() {
        return currentStates.size()>0 && currentTranstionBuilder!=null && isEntryAction==null;
    }
    
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if(qName.equals("onentry") || qName.equals("onexit")) {
            isEntryAction = null;
        } else if(qName.equals("state") || qName.equals("final") || qName.equals("parallel")) {
            MutableState<T, S, E, C> currentState = currentStates.pop();
            List<MutableState<T, S, E, C>> subStates = 
                    hierarchicalStateStore.removeAll(currentState);
            if(!subStates.isEmpty()) {
                for(MutableState<T, S, E, C> subState : subStates) {
                    subState.setParentState(currentState);
                    currentState.addChildState(subState);
                }
            }
        } else if(qName.equals("transition")) {
            currentTranstionBuilder=null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private <M> M newInstance(String instanceClassName) {
        Object instance = reusableInstance.get(instanceClassName);
        if(instance==null) {
            instance = ReflectUtils.newInstance(instanceClassName);
        }
        return (M)instance;
    }
    
    protected MutableState<T, S, E, C> getCurrentState() {
        return currentStates.peek();
    }
    
    protected TransitionBuilderImpl<T, S, E, C> getCurrentTranstionBuilder() {
        return currentTranstionBuilder;
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

    @Override
    public void registerReusableInstance(Object instance) {
        registerReusableInstance(instance.getClass().getName(), instance);
    }

    @Override
    public void unregisterReusableInstance(String instanceName) {
        reusableInstance.remove(instanceName);
    }

    @Override
    public void registerReusableInstance(String instanceName, Object instance) {
        reusableInstance.put(instanceName, instance);
    }
}
