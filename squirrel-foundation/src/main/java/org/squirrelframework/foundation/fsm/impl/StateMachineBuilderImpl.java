package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.component.SquirrelPostProcessor;
import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.Conditions;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.EventKind;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.MutableLinkedState;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateCompositeType;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.TransitionPriority;
import org.squirrelframework.foundation.fsm.TransitionType;
import org.squirrelframework.foundation.fsm.annotation.EventType;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.builder.EntryExitActionBuilder;
import org.squirrelframework.foundation.fsm.builder.ExternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.From;
import org.squirrelframework.foundation.fsm.builder.InternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.LocalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.On;
import org.squirrelframework.foundation.fsm.builder.To;
import org.squirrelframework.foundation.fsm.builder.When;
import org.squirrelframework.foundation.util.DuplicateChecker;
import org.squirrelframework.foundation.util.ReflectUtils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class StateMachineBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements StateMachineBuilder<T, S, E, C> {
	
	static {
		DuplicateChecker.checkDuplicate(StateMachineBuilder.class);
	}
    
    private static final Logger logger = LoggerFactory.getLogger(StateMachineBuilderImpl.class);
    
    private final Map<S, MutableState<T, S, E, C>> states = Maps.newHashMap();
    
    private final Class<? extends T> stateMachineClazz;
    
    private final Class<S> stateClazz;
    
    private final Class<E> eventClazz;
    
    private final Class<C> contextClazz;
    
    private boolean prepared = false;
    
    private Constructor<? extends T> contructor = null;
    
    protected Converter<S> stateConverter = null;
    
    protected Converter<E> eventConverter = null;
    
    private final Class<?>[] methodCallParamTypes;
    
    private Map<String, String> stateAliasToDescription = null;
    
    private E startEvent, finishEvent, terminateEvent;
    
    private StateMachineBuilderImpl(Class<? extends T> stateMachineClazz, Class<S> stateClazz, 
            Class<E> eventClazz, Class<C> contextClazz, boolean isContextInsensitive, 
            Class<?>... extraConstParamTypes) {
        Preconditions.checkArgument(isInstantiableType(stateMachineClazz), "The state machine class \""
                + stateMachineClazz.getName() + "\" cannot be instantiated.");
        Preconditions.checkArgument(isStateMachineType(stateMachineClazz), 
            "The implementation class of state machine \"" + stateMachineClazz.getName() + 
            "\" must be extended from AbstractStateMachine.class.");
        
        this.stateMachineClazz = stateMachineClazz;
        this.stateClazz = stateClazz;
        this.eventClazz = eventClazz;
        this.contextClazz = contextClazz;
        this.stateConverter = ConverterProvider.INSTANCE.getConverter(stateClazz);
        this.eventConverter = ConverterProvider.INSTANCE.getConverter(eventClazz);
        
        initailContextEvents(eventClazz);
        methodCallParamTypes = isContextInsensitive ? 
                new Class<?>[]{stateClazz, stateClazz, eventClazz} : 
                new Class<?>[]{stateClazz, stateClazz, eventClazz, contextClazz};
        Class<?>[] constParamTypes = getConstParamTypes(extraConstParamTypes);
        this.contructor = ReflectUtils.getConstructor(stateMachineClazz, constParamTypes);
    }
    
    protected void initailContextEvents(Class<E> eventClazz) {
    	Field[]  fields = ReflectUtils.getAnnotatedFields(eventClazz, EventType.class);
        if(fields==null || fields.length<1) return;
        
        for(Field field : fields) {
    		if(eventClazz.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
    			EventType eventType = field.getAnnotation(EventType.class);
    			E event = eventClazz.cast(ReflectUtils.getStatic(field));
    			if(eventType.value()==EventKind.START) {
    				startEvent = event;
    			} else if(eventType.value()==EventKind.FINISH) {
    				finishEvent = event;
    			} else if(eventType.value()==EventKind.TERMINATE) {
    				terminateEvent = event;
    			} 
    		}
    	}
    }
    
    private Class<?>[] getConstParamTypes(Class<?>[] extraConstParamTypes) {
        Class<?>[] parameterTypes = null;
        if(extraConstParamTypes!=null) {
            parameterTypes = new Class<?>[extraConstParamTypes.length+2];
        } else {
            parameterTypes = new Class<?>[2];
        }
        // add fixed constructor parameters
        parameterTypes[0] = ImmutableState.class;
        parameterTypes[1] = Map.class;
        
        //  add additional constructor parameters extended by derived state machine implementation 
        if(extraConstParamTypes!=null) {
            System.arraycopy(extraConstParamTypes, 0, parameterTypes, 2, extraConstParamTypes.length);
        }
        return parameterTypes;
    }
    
    private void checkState() {
        if(prepared) {
            throw new RuntimeException("The state machine builder has been freesed and " +
            		"cannot be changed anymore.");
        }
    }
    
    @Override
    public ExternalTransitionBuilder<T, S, E, C> externalTransition() {
        return externalTransition(TransitionPriority.NORMAL);
    }
    
    @Override
    public LocalTransitionBuilder<T, S, E, C> localTransition() {
        return localTransition(TransitionPriority.NORMAL);
    }

	@Override
    public InternalTransitionBuilder<T, S, E, C> internalTransition() {
        return internalTransition(TransitionPriority.NORMAL);
    }
	
	@Override
    public ExternalTransitionBuilder<T, S, E, C> externalTransition(int priority) {
        checkState();
        return FSM.newExternalTransitionBuilder(states, priority);
    }
    
    @Override
    public LocalTransitionBuilder<T, S, E, C> localTransition(int priority) {
        checkState();
        return FSM.newLocalTransitionBuilder(states, priority);
    }

    @Override
    public InternalTransitionBuilder<T, S, E, C> internalTransition(int priority) {
        checkState();
        return FSM.newInternalTransitionBuilder(states, priority);
    }
    
    private void addStateEntryExitMethodCallAction(String methodName, Class<?>[] parameterTypes, 
            MutableState<T, S, E, C> mutableState, boolean isEntryAction) {
        Method method = findMethodCallAction(stateMachineClazz, methodName, parameterTypes);
        if(method!=null) {
            Action<T, S, E, C> methodCallAction = FSM.newMethodCallAction(method);
            if(isEntryAction) {
                mutableState.addEntryAction(methodCallAction);
            } else {
                mutableState.addExitAction(methodCallAction);
            }
        }
    }
    
    private void addTransitionMethodCallAction(String methodName, Class<?>[] parameterTypes, 
            MutableTransition<T, S, E, C> mutableTransition) {
        Method method = findMethodCallAction(stateMachineClazz, methodName, parameterTypes);
        if(method!=null) {
            Action<T, S, E, C> methodCallAction = FSM.newMethodCallAction(method);
            mutableTransition.addAction(methodCallAction);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void buildDeclareTransition(Transit transit) {
        if(transit==null) return;
        
        if(stateConverter==null) {
        	throw new RuntimeException("Do not register state converter");
        }
        
        if(eventConverter==null) {
        	throw new RuntimeException("Do not register event converter");
        }
        
        if(!isInstantiableType(transit.when())) {
            throw new RuntimeException("Condition \'when\' should be concrete class or static inner class.");
        }
        if(transit.type()==TransitionType.INTERNAL && !transit.from().equals(transit.to())) {
            throw new RuntimeException("Internal transiton must transit to the same source state.");
        }
        
        S fromState = stateConverter.convertFromString(parseStateId(transit.from()));
        Preconditions.checkNotNull(fromState, "Cannot convert state of name \""+fromState+"\".");
        S toState = stateConverter.convertFromString(parseStateId(transit.to()));
        E event = eventConverter.convertFromString(transit.on());
        Preconditions.checkNotNull(event, "Cannot convert event of name \""+event+"\".");
        
        // check exited transition which satisfied the criteria
        if(states.get(fromState)!=null) {
            MutableState<T, S, E, C> theFromState = states.get(fromState);
            for(ImmutableTransition<T, S, E, C> t : theFromState.getAllTransitions()) {
                if(t.isMatch(fromState, toState, event, transit.when(), transit.type())) {
                    MutableTransition<T, S, E, C> mutableTransition = (MutableTransition<T, S, E, C>)t;
                    Method method = findMethodCallAction(stateMachineClazz, transit.callMethod(), methodCallParamTypes);
                    if(method!=null) {
                        mutableTransition.addAction(FSM.<T, S, E, C>newMethodCallAction(method));
                    }
                    return;
                }
            }
        }
        
        // if no existed transition is matched then create a new transition
        To<T, S, E, C> toBuilder = null;
        if(transit.type()==TransitionType.INTERNAL) {
        	InternalTransitionBuilder<T, S, E, C> transitionBuilder = FSM.newInternalTransitionBuilder(states, transit.priority());
            toBuilder = transitionBuilder.within(fromState);
        } else {
        	ExternalTransitionBuilder<T, S, E, C> transitionBuilder = (transit.type()==TransitionType.LOCAL) ? 
        	        FSM.newLocalTransitionBuilder(states, transit.priority()) : 
        	            FSM.newExternalTransitionBuilder(states, transit.priority());
            From<T, S, E, C> fromBuilder = transitionBuilder.from(fromState);
            boolean isTargetFinal = transit.isTargetFinal() || FSM.getState(states, toState).isFinalState();
            toBuilder = isTargetFinal ? fromBuilder.toFinal(toState) : fromBuilder.to(toState);
        } 
        On<T, S, E, C> onBuilder = toBuilder.on(event);
        Condition<C> c = null;
        try {
            if(transit.when()!=Conditions.Always.class) {
                Constructor<?> constructor = transit.when().getDeclaredConstructor();
                constructor.setAccessible(true);
                c = (Condition<C>)constructor.newInstance();
            }
        } catch (Exception e) {
            logger.error("Instantiate Condition \""+transit.when().getName()+"\" failed.");
            c = Conditions.never();
        } 
        When<T, S, E, C> whenBuilder = c!=null ? onBuilder.when(c) : onBuilder;
        
        if(!Strings.isNullOrEmpty(transit.callMethod())) {
            Method method = findMethodCallAction(stateMachineClazz, transit.callMethod(), methodCallParamTypes);
            if(method!=null) {
                Action<T, S, E, C> methodCallAction = FSM.newMethodCallAction(method);
                whenBuilder.perform(methodCallAction);
            }
        }
    }
    
    private String parseStateId(String value) {
        return (value!=null && value.startsWith("#")) ? 
                stateAliasToDescription.get(value.substring(1)) : value;
    }
    
    private void buidlDeclareState(State state) {
        if(state==null) return;
        
        if(stateConverter==null) {
        	throw new RuntimeException("Do not register state converter");
        }
        
        S stateId = stateConverter.convertFromString(state.name());
        Preconditions.checkNotNull(stateId, "Cannot convert state of name \""+state.name()+"\".");
        MutableState<T, S, E, C> newState = defineState(stateId);
        newState.setCompositeType(state.compositeType());
        if(!newState.isParallelState()) {
        	newState.setHistoryType(state.historyType());
        }
        newState.setFinal(state.isFinal());
        
        if(!Strings.isNullOrEmpty(state.parent())) {
        	S parentStateId = stateConverter.convertFromString(parseStateId(state.parent()));
        	MutableState<T, S, E, C> parentState = defineState(parentStateId);
        	newState.setParentState(parentState);
        	parentState.addChildState(newState);
        	if(!parentState.isParallelState() && state.initialState()) {
        		parentState.setInitialState(newState);
        	}
        }
        
        if(!Strings.isNullOrEmpty(state.entryCallMethod())) {
            Method method = findMethodCallAction(stateMachineClazz, state.entryCallMethod(), methodCallParamTypes);
            if(method!=null) {
                Action<T, S, E, C> methodCallAction = FSM.newMethodCallAction(method);
                onEntry(stateId).perform(methodCallAction);
            }
        }
        
        if(!Strings.isNullOrEmpty(state.exitCallMethod())) {
            Method method = findMethodCallAction(stateMachineClazz, state.exitCallMethod(), methodCallParamTypes);
            if(method!=null) {
                Action<T, S, E, C> methodCallAction = FSM.newMethodCallAction(method);
                onExit(stateId).perform(methodCallAction);
            }
        }
        rememberStateAlias(state);
    }
    
    private void rememberStateAlias(State state) {
        if(Strings.isNullOrEmpty(state.alias())) return;
        if(stateAliasToDescription==null) 
            stateAliasToDescription=Maps.newHashMap();
        if(!stateAliasToDescription.containsKey(state.alias())) {
            stateAliasToDescription.put(state.alias(), state.name());
        } else {
            throw new RuntimeException("Cannot define duplicate state alias \""+
                    state.alias()+"\" for state \""+state.name()+"\" and "+
                    stateAliasToDescription.get(state.alias())+"\".");
        }
    }
    
    private void install(Function<Class<?>, Void> func) {
        Stack<Class<?>> stack = new Stack<Class<?>>();
        stack.push(stateMachineClazz);
        while(!stack.isEmpty()) {
            Class<?> k = stack.pop();
            func.apply(k);
            for(Class<?> i : k.getInterfaces()) {
                if(isStateMachineInterface(i)) {stack.push(i);}
            }
            if(isStateMachineType(k.getSuperclass())) {
                stack.push(k.getSuperclass());
            }
        }
    }
    
    private void verifyStateMachineDefinition() {
        for(MutableState<T, S, E, C> state : states.values()) {
            state.verify();
        }
    }
    
    private synchronized void prepare() {
        if(prepared) return;
        // install all the declare states, states must be installed before installing transition and extension methods
        install(new DeclareStateFunction());
        // install all the declare transitions
        install(new DeclareTransitionFunction());
        // install all the extension method call when state machine builder freeze
        installExtensionMethods();
        // prioritize transitions
        prioritizeTransitions();
        // install final state actions
        installFinalStateActions();
        // verify correctness of state machine
        verifyStateMachineDefinition();
        prepared = true;
    }
    
    private String[] getEntryExitStateMethodNames(ImmutableState<T, S, E, C> state, boolean isEntry) {
        String prefix = (isEntry ? "entry" : "exit");
        String postfix = (isEntry ? "EntryAny" : "ExitAny");
        
        return new String[]{
                "before" + postfix,
                prefix + ((stateConverter!=null && !state.isFinalState()) ? 
                stateConverter.convertToString(state.getStateId()) : StringUtils.capitalize(state.toString())),
                "after" + postfix
        };
    }
    
    private String[] getTransitionMethodNames(ImmutableTransition<T, S, E, C> transition) {
        ImmutableState<T, S, E, C> fromState = transition.getSourceState();
        ImmutableState<T, S, E, C> toState = transition.getTargetState();
        E event = transition.getEvent();
        String fromStateName = stateConverter!=null ? stateConverter.convertToString(fromState.getStateId()) : 
            StringUtils.capitalize(fromState.toString());
        String toStateName = (stateConverter!=null && !toState.isFinalState()) ? 
                stateConverter.convertToString(toState.getStateId()) : StringUtils.capitalize(toState.toString());
        String eventName = eventConverter!=null ? eventConverter.convertToString(event) : 
            StringUtils.capitalize(event.toString());
        
        Class<?> condClass = transition.getCondition().getClass();
        String conditionName = condClass.isAnonymousClass() ? 
                "Condition$"+StringUtils.substringAfterLast(condClass.getName(), "$") : condClass.getSimpleName();
        
        return new String[] { 
                "transitFrom"+fromStateName+"To"+toStateName+"On"+eventName+"When"+conditionName,
                "transitFrom"+fromStateName+"To"+toStateName+"On"+eventName,
                "transitFromAnyTo"+toStateName+"On"+eventName,
                "transitFrom"+fromStateName+"ToAnyOn"+eventName,
                "transitFrom"+fromStateName+"To"+toStateName,
                "on"+eventName
        };
    }
    
    private void installExtensionMethods() {
        for(MutableState<T, S, E, C> state : states.values()) {
            // Ignore all the transition start from a final state
            if(state.isFinalState()) continue;
            
            // state exit extension method
            String[] exitMethodCallCandidates = getEntryExitStateMethodNames(state, false);
            for(int i=0, size=exitMethodCallCandidates.length; i<size; ++i) {
                addStateEntryExitMethodCallAction(exitMethodCallCandidates[i], 
                        methodCallParamTypes, state, false);
            }
            
            // transition extension methods
            for(ImmutableTransition<T, S, E, C> transition : state.getAllTransitions()) {
                String[] transitionMethodCallCandidates = getTransitionMethodNames(transition);
                for(int i=0, size=transitionMethodCallCandidates.length; i<size; ++i) {
                    addTransitionMethodCallAction(transitionMethodCallCandidates[i], methodCallParamTypes, 
                            (MutableTransition<T, S, E, C>)transition);
                }
            }
            
            // state entry extension method
            String[] entryMethodCallCandidates = getEntryExitStateMethodNames(state, true);
            for(int i=0, size=entryMethodCallCandidates.length; i<size; ++i) {
                addStateEntryExitMethodCallAction(entryMethodCallCandidates[i], 
                        methodCallParamTypes, state, true);
            }
        }
    }
    
    private void prioritizeTransitions() {
        for(MutableState<T, S, E, C> state : states.values()) {
            if(state.isFinalState()) continue;
            state.prioritizeTransitions();
        }
    }
    
    private void installFinalStateActions() {
        for(MutableState<T, S, E, C> state : states.values()) {
            if(!state.isFinalState()) continue;
            // defensive code: final state cannot be exited anymore
            state.addExitAction(new Action<T, S, E, C>() {
                @Override
                public void execute(S from, S to, E event, C context, T stateMachine) {
                    throw new RuntimeException("Final state cannot be exited anymore.");
                }
            });
        }
    }
    
    private boolean isInstantiableType(Class<?> type) {
        return type!=null && !type.isInterface() && !Modifier.isAbstract(type.getModifiers()) &&
                ((type.getEnclosingClass()==null) || (type.getEnclosingClass()!=null && 
                Modifier.isStatic(type.getModifiers())));
    }
    
    private boolean isStateMachineType(Class<?> stateMachineClazz) {
        return stateMachineClazz!= null && AbstractStateMachine.class != stateMachineClazz &&
                AbstractStateMachine.class.isAssignableFrom(stateMachineClazz);
    }
    
    private boolean isStateMachineInterface(Class<?> stateMachineClazz) {
        return stateMachineClazz!= null && stateMachineClazz.isInterface() && 
                StateMachine.class.isAssignableFrom(stateMachineClazz);
    }
    
    private static Method searchMethod(Class<?> targetClass, Class<?> superClass, 
            String methodName, Class<?>[] parameterTypes) {
        if(superClass.isAssignableFrom(targetClass)) {
            Class<?> clazz = targetClass;
            while(!superClass.equals(clazz)) {
                try {
                    return clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        }
        return null;
    }
    
    private Method findMethodCallAction(Class<?> target, String methodName, Class<?>[] parameterTypes) {
        // TODO-hhe: cache action method in a map and try to get from cache first
        return searchMethod(target, AbstractStateMachine.class, methodName, parameterTypes);
    }
    
    public T newStateMachine(S initialStateId) {
    	return newStateMachine(initialStateId, new Object[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T newStateMachine(S initialStateId, Object... extraParams) {
        if(!prepared) prepare();
        Object[] parameters = new Object[extraParams.length+2];
        parameters[0] = states.get(initialStateId);
        if(parameters[0] == null) {
            throw new RuntimeException(getClass()+" cannot find Initial state \'"+initialStateId+"\' in state machine.");
        }
        parameters[1] = states;
        if(extraParams!=null) {
            System.arraycopy(extraParams, 0, parameters, 2, extraParams.length);
        }
        T stateMachine = postProcessStateMachine((Class<T>)stateMachineClazz, ReflectUtils.newInstance(contructor, parameters));
        
        AbstractStateMachine<T, S, E, C> stateMachineImpl = (AbstractStateMachine<T, S, E, C>)stateMachine;
        stateMachineImpl.setStartEvent(startEvent);
        stateMachineImpl.setFinishEvent(finishEvent);
        stateMachineImpl.setTerminateEvent(terminateEvent);
        
        stateMachineImpl.setTypeOfStateMachine(stateMachineClazz);
        stateMachineImpl.setTypeOfState(stateClazz);
        stateMachineImpl.setTypeOfEvent(eventClazz);
        stateMachineImpl.setTypeOfContext(contextClazz);
        
        return stateMachine;
    }
    
    private T postProcessStateMachine(Class<T> clz, T component) {
        if(component!=null) {
            List<SquirrelPostProcessor<? super T>> postProcessors = 
                    SquirrelPostProcessorProvider.getInstance().getCallablePostProcessors(clz);
            for(SquirrelPostProcessor<? super T> postProcessor : postProcessors) {
                postProcessor.postProcess(component);
            }
        }
        return component;
    }
    
    @Override
    public MutableState<T, S, E, C> defineState(S stateId) {
        return FSM.getState(states, stateId);
    }
    
    @Override
    public MutableState<T, S, E, C> defineFinalState(S stateId) {
    	MutableState<T, S, E, C> newState = defineState(stateId);
    	newState.setFinal(true);
    	return newState;
    }
    
    @Override
    public MutableState<T, S, E, C> definedLinkedState(S stateId, 
            StateMachineBuilder<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachineBuilder, S initialLinkedState, Object... extraParams) {
        MutableLinkedState<T, S, E, C> linkedState = (MutableLinkedState<T, S, E, C>) FSM.getState(states, stateId, true);
        linkedState.setLinkedStateMachine(linkedStateMachineBuilder.newStateMachine(initialLinkedState, extraParams));
        return linkedState;
    }
    
    @Override
    public void defineSequentialStatesOn(S parentStateId, S... childStateIds) {
    	defineChildStatesOn(parentStateId, StateCompositeType.SEQUENTIAL, HistoryType.NONE, childStateIds);
    }
    
    @Override
    public void defineSequentialStatesOn(S parentStateId, HistoryType historyType, S... childStateIds) {
    	defineChildStatesOn(parentStateId, StateCompositeType.SEQUENTIAL, historyType, childStateIds);
    }
    
    @Override
    public void defineParallelStatesOn(S parentStateId, S... childStateIds) {
    	defineChildStatesOn(parentStateId, StateCompositeType.PARALLEL, HistoryType.NONE, childStateIds);
    }

    private void defineChildStatesOn(S parentStateId, StateCompositeType compositeType, HistoryType historyType, S... childStateIds) {
    	if(childStateIds!=null && childStateIds.length>0) {
    		MutableState<T, S, E, C> parentState = FSM.getState(states, parentStateId);
    		parentState.setCompositeType(compositeType);
    		parentState.setHistoryType(historyType);
    		for(int i=0, size=childStateIds.length; i<size; ++i) {
    			MutableState<T, S, E, C> childState = FSM.getState(states, childStateIds[i]);
    			if(i==0 && compositeType==StateCompositeType.SEQUENTIAL) { 
    				parentState.setInitialState(childState); 
    			}
    			childState.setParentState(parentState);
    			parentState.addChildState(childState);
    		}
    	}
    }

    @Override
    public EntryExitActionBuilder<T, S, E, C> onEntry(S stateId) {
        checkState();
        MutableState<T, S, E, C> state = FSM.getState(states, stateId);
        return FSM.newEntryExitActionBuilder(state, true);
    }

    @Override
    public EntryExitActionBuilder<T, S, E, C> onExit(S stateId) {
        checkState();
        MutableState<T, S, E, C> state = FSM.getState(states, stateId);
        return FSM.newEntryExitActionBuilder(state, false);
    }

    private class DeclareTransitionFunction implements Function<Class<?>, Void> {
        @Override
        public Void apply(Class<?> k) {
            buildDeclareTransition(k.getAnnotation(Transit.class));
            Transitions transitions = k.getAnnotation(Transitions.class);
            if(transitions!=null && transitions.value()!=null) {
                for(Transit t : transitions.value()) {
                    StateMachineBuilderImpl.this.buildDeclareTransition(t);
                }
            }
            return null;
        }
    }
    
    private class DeclareStateFunction implements Function<Class<?>, Void> {
        @Override
        public Void apply(Class<?> k) {
            buidlDeclareState(k.getAnnotation(State.class));
            States states = k.getAnnotation(States.class);
            if(states!=null && states.value()!=null) {
                for(State s : states.value()) {
                    StateMachineBuilderImpl.this.buidlDeclareState(s);
                }
            }
            return null;
        }
    }
}
