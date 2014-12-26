package org.squirrelframework.foundation.fsm.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.component.SquirrelInstanceProvider;
import org.squirrelframework.foundation.component.SquirrelPostProcessor;
import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.exception.SquirrelRuntimeException;
import org.squirrelframework.foundation.fsm.*;
import org.squirrelframework.foundation.fsm.annotation.*;
import org.squirrelframework.foundation.fsm.builder.*;
import org.squirrelframework.foundation.fsm.jmx.ManagementService;
import org.squirrelframework.foundation.util.DuplicateChecker;
import org.squirrelframework.foundation.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

public class StateMachineBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements StateMachineBuilder<T, S, E, C> {

    static {
        DuplicateChecker.checkDuplicate(StateMachineBuilder.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(StateMachineBuilderImpl.class);
    
    private final Map<S, MutableState<T, S, E, C>> states = Maps.newConcurrentMap();
    
    private final Class<? extends T> stateMachineImplClazz;
    
    private final Class<S> stateClazz;
    
    private final Class<E> eventClazz;
    
    private final Class<C> contextClazz;
    
    private boolean prepared = false;
    
    private final Constructor<? extends T> constructor;
    
    private final Method postConstructMethod;
    
    protected final Converter<S> stateConverter;
    
    protected final Converter<E> eventConverter;
    
    private final Class<?>[] methodCallParamTypes;
    
    private Map<String, String> stateAliasToDescription = null;
    
    private final MvelScriptManager scriptManager;
    
    private E startEvent, finishEvent, terminateEvent;
    
    private final ExecutionContext executionContext;
    
    private final List<DeferBoundActionInfo<T, S, E, C>> deferBoundActionInfoList = Lists.newArrayList();
    
    private boolean isScanAnnotations = true;
    
    private final Class<?>[] extraParamTypes;
    
    private StateMachineConfiguration defaultConfiguration = StateMachineConfiguration.getInstance();
    
    @SuppressWarnings("unchecked")
    private StateMachineBuilderImpl(Class<? extends T> stateMachineImplClazz, Class<S> stateClazz, 
            Class<E> eventClazz, Class<C> contextClazz, Class<?>... extraParamTypes) {
        Preconditions.checkArgument(isInstantiableType(stateMachineImplClazz), "The state machine class \""
                + stateMachineImplClazz.getName() + "\" cannot be instantiated.");
        Preconditions.checkArgument(isStateMachineType(stateMachineImplClazz), 
            "The implementation class of state machine \"" + stateMachineImplClazz.getName() + 
            "\" must be extended from AbstractStateMachine.class.");
        
        this.stateMachineImplClazz = stateMachineImplClazz;
        this.extraParamTypes = extraParamTypes!=null ? extraParamTypes : new Class<?>[0];
        
        StateMachineParameters genericsParameters = findAnnotation(StateMachineParameters.class);
        if(stateClazz==Object.class && genericsParameters!=null) {
            this.stateClazz = (Class<S>) genericsParameters.stateType();
        } else {
            this.stateClazz = stateClazz;
        }
        if(eventClazz==Object.class && genericsParameters!=null) {
            this.eventClazz = (Class<E>) genericsParameters.eventType();
        } else {
            this.eventClazz = eventClazz;
        }
        if(contextClazz==Object.class && genericsParameters!=null) {
            this.contextClazz = (Class<C>) genericsParameters.contextType();
        } else {
            this.contextClazz = contextClazz;
        }
        
        this.stateConverter = ConverterProvider.INSTANCE.getConverter(this.stateClazz);
        this.eventConverter = ConverterProvider.INSTANCE.getConverter(this.eventClazz);
        this.scriptManager = SquirrelProvider.getInstance().newInstance(MvelScriptManager.class);
        
        boolean contextInsensitive = findAnnotation(ContextInsensitive.class)!=null;
        methodCallParamTypes = contextInsensitive ? 
                new Class<?>[]{this.stateClazz, this.stateClazz, this.eventClazz} : 
                new Class<?>[]{this.stateClazz, this.stateClazz, this.eventClazz, this.contextClazz};
        
        Constructor<? extends T> fsmConstructor;
        try {
            fsmConstructor = ReflectUtils.getConstructor(stateMachineImplClazz, this.extraParamTypes);
        } catch(Exception e1) {
            try {
                fsmConstructor = ReflectUtils.getConstructor(stateMachineImplClazz, new Class<?>[0]);
            } catch(Exception e2) {
                throw new IllegalArgumentException("Cannot find matched constructor for \'"+stateMachineImplClazz.getName()+"\'.");
            }
        }
        this.constructor = fsmConstructor;
        
        Method postInit = null;
        try {
            postInit = ReflectUtils.getMethod(stateMachineImplClazz, "postConstruct", this.extraParamTypes);
        } catch (Exception e) {}
        this.postConstructMethod = postInit;
        
        this.executionContext = new ExecutionContext(scriptManager, stateMachineImplClazz, methodCallParamTypes);
        // after initialized state machine builder
        defineContextEvent();
    }
    
    private void defineContextEvent() {
        ContextEvent contextEvent = findAnnotation(ContextEvent.class);
        if(contextEvent!=null) {
            Preconditions.checkState(eventConverter!=null, "Do not register event converter");
            if(!contextEvent.startEvent().isEmpty()) {
                defineStartEvent(eventConverter.convertFromString(contextEvent.startEvent()));
            }
            if(!contextEvent.finishEvent().isEmpty()) {
                defineFinishEvent(eventConverter.convertFromString(contextEvent.finishEvent()));
            }
            if(!contextEvent.terminateEvent().isEmpty()) {
                defineTerminateEvent(eventConverter.convertFromString(contextEvent.terminateEvent()));
            }
        }
    }
    
    private <M extends Annotation> M findAnnotation(final Class<M> annotationClass) {
        final AtomicReference<M> genericsParametersRef = new AtomicReference<M>();;
        walkThroughStateMachineClass(new Function<Class<?>, Boolean>() {
            @Override
            public Boolean apply(Class<?> input) {
                M anno = input.getAnnotation(annotationClass);
                if(anno!=null) {
                    genericsParametersRef.set(anno);
                    return false;
                }
                return true;
            }
        });
        M genericsParameters = genericsParametersRef.get();
        return genericsParameters;
    }
    
    private void checkState() {
        if(prepared) {
            throw new IllegalStateException("The state machine builder has been freezed and " +
                    "cannot be changed anymore.");
        }
    }
    
    @Override
    public ExternalTransitionBuilder<T, S, E, C> externalTransition() {
        checkState();
        return externalTransition(TransitionPriority.NORMAL);
    }

    @Override
    public MultiTransitionBuilder<T, S, E, C> externalTransitions() {
        checkState();
        return transitions(TransitionPriority.NORMAL);
    }
    
    @Override
    public ExternalTransitionBuilder<T, S, E, C> transition() {
        checkState();
        return externalTransition(TransitionPriority.NORMAL);
    }

    @Override
    public MultiTransitionBuilder<T, S, E, C> transitions() {
        checkState();
        return transitions(TransitionPriority.NORMAL);
    }

    @Override
    public DeferBoundActionBuilder<T, S, E, C> transit() {
        checkState();
        return FSM.newDeferBoundActionBuilder(deferBoundActionInfoList, executionContext);
    }
    
    @Override
    public LocalTransitionBuilder<T, S, E, C> localTransition() {
        checkState();
        return localTransition(TransitionPriority.NORMAL);
    }

    @Override
    public MultiTransitionBuilder<T, S, E, C> localTransitions() {
        checkState();
        return localTransitions(TransitionPriority.NORMAL);
    }

    @Override
    public InternalTransitionBuilder<T, S, E, C> internalTransition() {
        checkState();
        return internalTransition(TransitionPriority.NORMAL);
    }

    @Override
    public ExternalTransitionBuilder<T, S, E, C> externalTransition(int priority) {
        checkState();
        return FSM.newExternalTransitionBuilder(states, priority, executionContext);
    }

    @Override
    public MultiTransitionBuilder<T, S, E, C> externalTransitions(int priority) {
        checkState();
        return transitions(priority);
    }

    @Override
    public ExternalTransitionBuilder<T, S, E, C> transition(int priority) {
        checkState();
        return externalTransition(priority);
    }

    @Override
    public MultiTransitionBuilder<T, S, E, C> transitions(int priority) {
        checkState();
        return FSM.newMultiTransitionBuilder(states, TransitionType.EXTERNAL, priority, executionContext);
    }
    
    @Override
    public LocalTransitionBuilder<T, S, E, C> localTransition(int priority) {
        checkState();
        return FSM.newLocalTransitionBuilder(states, priority, executionContext);
    }

    @Override
    public MultiTransitionBuilder<T, S, E, C> localTransitions(int priority) {
        checkState();
        return FSM.newMultiTransitionBuilder(states, TransitionType.LOCAL, priority, executionContext);
    }

    @Override
    public InternalTransitionBuilder<T, S, E, C> internalTransition(int priority) {
        checkState();
        return FSM.newInternalTransitionBuilder(states, priority, executionContext);
    }
    
    private void addStateEntryExitMethodCallAction(String methodName, Class<?>[] parameterTypes, 
            MutableState<T, S, E, C> mutableState, boolean isEntryAction) {
        Method method = findMethodCallActionInternal(stateMachineImplClazz, methodName, parameterTypes);
        if(method!=null) {
            int weight = Action.EXTENSION_WEIGHT;
            if(methodName.startsWith("before")) {
                weight = Action.BEFORE_WEIGHT;
            } else if(methodName.startsWith("after")) {
                weight = Action.AFTER_WEIGHT;
            }
            Action<T, S, E, C> methodCallAction = FSM.newMethodCallAction(method, weight, executionContext);
            if(isEntryAction) {
                mutableState.addEntryAction(methodCallAction);
            } else {
                mutableState.addExitAction(methodCallAction);
            }
        }
    }
    
    private void addTransitionMethodCallAction(String methodName, Class<?>[] parameterTypes, 
            MutableTransition<T, S, E, C> mutableTransition) {
        Method method = findMethodCallActionInternal(stateMachineImplClazz, methodName, parameterTypes);
        if(method!=null) {
            Action<T, S, E, C> methodCallAction = FSM.newMethodCallAction(method, Action.EXTENSION_WEIGHT, executionContext);
            mutableTransition.addAction(methodCallAction);
        }
    }
    
    private boolean isDeferBoundAction(Transit transit) {
        return "*".equals(transit.from()) || "*".equals(transit.to()) || "*".equals(transit.on());
    }
    
    private Action<T, S, E, C> warpConditionalAction(Action<T, S, E, C> action, final Condition<C> condition) {
        return new ActionWrapper<T, S, E, C>(action) {
            @Override
            public void execute(S from, S to, E event, C context, T stateMachine) {
                if(condition.isSatisfied(context)) {
                    super.execute(from, to, event, context, stateMachine);
                }
            }
        };
    }
    
    private void buildDeferBoundAction(Transit transit) {
        S from = "*".equals(transit.from()) ? null : 
            stateConverter.convertFromString(parseStateId(transit.from()));
        S to  = "*".equals(transit.to()) ? null :
            stateConverter.convertFromString(parseStateId(transit.to()));
        E event = "*".equals(transit.on()) ? null :
            eventConverter.convertFromString(transit.on());
        DeferBoundActionInfo<T, S, E, C> deferBoundActionInfo = 
                new DeferBoundActionInfo<T, S, E, C>(from, to, event);
        if(!transit.callMethod().isEmpty()) {
            Action<T, S, E, C> action = FSM.newMethodCallActionProxy(transit.callMethod(), executionContext);
            if(!transit.whenMvel().isEmpty()) {
                final Condition<C> condition = FSM.newMvelCondition(transit.whenMvel(), scriptManager);
                action = warpConditionalAction(action, condition);
            }
            if(transit.when()!=Conditions.Always.class) {
                @SuppressWarnings("unchecked")
                final Condition<C> condition = ReflectUtils.newInstance(transit.when());
                action = warpConditionalAction(action, condition);
            }
            deferBoundActionInfo.setActions(Collections.singletonList(action));
        }
        
        deferBoundActionInfoList.add(deferBoundActionInfo);
    }
    
    @SuppressWarnings("unchecked")
    private void buildDeclareTransition(Transit transit) {
        if(transit==null) return;
        
        Preconditions.checkState(stateConverter!=null, "Do not register state converter");
        Preconditions.checkState(eventConverter!=null, "Do not register event converter");
        
        // if not explicit specify 'from', 'to' and 'event', it is declaring a defer bound action.
        if(isDeferBoundAction(transit)) {
            buildDeferBoundAction(transit);
            return;
        }
        
        Preconditions.checkArgument(isInstantiableType(transit.when()), 
                "Condition \'when\' should be concrete class or static inner class.");
        Preconditions.checkArgument(
                transit.type()!=TransitionType.INTERNAL || transit.from().equals(transit.to()),
                "Internal transition must transit to the same source state.");
        
        S fromState = stateConverter.convertFromString(parseStateId(transit.from()));
        Preconditions.checkNotNull(fromState, "Cannot convert state of name \""+fromState+"\".");
        S toState = stateConverter.convertFromString(parseStateId(transit.to()));
        E event = eventConverter.convertFromString(transit.on());
        Preconditions.checkNotNull(event, "Cannot convert event of name \""+event+"\".");
        
        // check exited transition which satisfied the criteria
        if(states.get(fromState)!=null) {
            MutableState<T, S, E, C> theFromState = states.get(fromState);
            for(ImmutableTransition<T, S, E, C> t : theFromState.getAllTransitions()) {
                if(t.isMatch(fromState, toState, event, transit.priority(), transit.when(), transit.type())) {
                    MutableTransition<T, S, E, C> mutableTransition = (MutableTransition<T, S, E, C>)t;
                    String callMethodExpression = transit.callMethod();
                    if(callMethodExpression!=null && callMethodExpression.length()>0) {
                        Action<T, S, E, C> methodCallAction = FSM.newMethodCallActionProxy(callMethodExpression, executionContext);
                        mutableTransition.addAction(methodCallAction);
                    }
                    return;
                }
            }
        }
        
        // if no existed transition is matched then create a new transition
        final To<T, S, E, C> toBuilder;
        if(transit.type()==TransitionType.INTERNAL) {
            InternalTransitionBuilder<T, S, E, C> transitionBuilder =
                    FSM.newInternalTransitionBuilder(states, transit.priority(), executionContext);
            toBuilder = transitionBuilder.within(fromState);
        } else {
            ExternalTransitionBuilder<T, S, E, C> transitionBuilder = (transit.type()==TransitionType.LOCAL) ?
                    FSM.newLocalTransitionBuilder(states, transit.priority(), executionContext) :
                        FSM.newExternalTransitionBuilder(states, transit.priority(), executionContext);
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
            } else if(StringUtils.isNotEmpty(transit.whenMvel())) {
                c = FSM.newMvelCondition(transit.whenMvel(), scriptManager);
            }
        } catch (Exception e) {
            logger.error("Instantiate Condition \""+transit.when().getName()+"\" failed.");
            c = Conditions.never();
        } 
        When<T, S, E, C> whenBuilder = c!=null ? onBuilder.when(c) : onBuilder;
        
        if(!Strings.isNullOrEmpty(transit.callMethod())) {
            Action<T, S, E, C> methodCallAction = FSM.newMethodCallActionProxy(transit.callMethod(), executionContext);
            whenBuilder.perform(methodCallAction);
        }
    }
    
    private String parseStateId(String value) {
        return (value!=null && value.startsWith("#")) ? 
                stateAliasToDescription.get(value.substring(1)) : value;
    }
    
    private void buildDeclareState(State state) {
        if(state==null) return;
        
        Preconditions.checkState(stateConverter!=null, "Do not register state converter");
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
            Action<T, S, E, C> methodCallAction = FSM.newMethodCallActionProxy(state.entryCallMethod(), executionContext);
            onEntry(stateId).perform(methodCallAction);
        }
        
        if(!Strings.isNullOrEmpty(state.exitCallMethod())) {
            Action<T, S, E, C> methodCallAction = FSM.newMethodCallActionProxy(state.exitCallMethod(), executionContext);
            onExit(stateId).perform(methodCallAction);
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
    
    private void walkThroughStateMachineClass(Function<Class<?>, Boolean> func) {
        Stack<Class<?>> stack = new Stack<Class<?>>();
        stack.push(stateMachineImplClazz);
        while(!stack.isEmpty()) {
            Class<?> k = stack.pop();
            boolean isContinue = func.apply(k);
            if(!isContinue) break;
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
    
    private void installDeferBoundActions() {
        if(deferBoundActionInfoList.isEmpty()) 
            return;
        for(DeferBoundActionInfo<T, S, E, C> deferBoundActionInfo : deferBoundActionInfoList) {
            installDeferBoundAction(deferBoundActionInfo);
        }
    }
    
    private void installDeferBoundAction(DeferBoundActionInfo<T, S, E, C> deferBoundActionInfo) {
        for(MutableState<T, S, E, C> mutableState : states.values()) {
            if(!deferBoundActionInfo.isFromStateMatch(mutableState.getStateId())) {
                continue;
            }
            for(ImmutableTransition<T, S, E, C> transition : mutableState.getAllTransitions()) {
                if(deferBoundActionInfo.isToStateMatch(transition.getTargetState().getStateId())
                        && deferBoundActionInfo.isEventStateMatch(transition.getEvent())) {
                    MutableTransition<T, S, E, C> mutableTransition = (MutableTransition<T, S, E, C>)transition;
                    mutableTransition.addActions(deferBoundActionInfo.getActions());
                }
            }
        }
    }
    
    private synchronized void prepare() {
        if(prepared) return;
        
        if(isScanAnnotations) {
            // 1. install all the declare states, states must be installed before installing transition and extension methods
            walkThroughStateMachineClass(new DeclareStateFunction());
            // 2. install all the declare transitions
            walkThroughStateMachineClass(new DeclareTransitionFunction());
            // 2.5 install all the defer bound actions
            installDeferBoundActions();
        }
        // 3. install all the extension method call when state machine builder freeze
        installExtensionMethods();
        // 4. prioritize transitions
        prioritizeTransitions();
        // 5. install final state actions
        installFinalStateActions();
        // 6. verify correctness of state machine
        verifyStateMachineDefinition();
        // 7. proxy untyped states
        proxyUntypedStates();
        prepared = true;
    }
    
    @SuppressWarnings("unchecked")
    private void proxyUntypedStates() {
        if(UntypedStateMachine.class.isAssignableFrom(stateMachineImplClazz)) {
            Map<S, MutableState<T, S, E, C>> untypedStates = Maps.newHashMap();
            for(final MutableState<T, S, E, C> state : states.values()) {
                UntypedMutableState untypedState = (UntypedMutableState) Proxy.newProxyInstance(
                        UntypedMutableState.class.getClassLoader(), 
                        new Class[]{UntypedMutableState.class, UntypedImmutableState.class}, 
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args)
                                    throws Throwable {
                                if (method.getName().equals("getStateId")) {
                                    return state.getStateId();
                                } else if(method.getName().equals("getThis")) {
                                    return state.getThis();
                                } else if(method.getName().equals("equals")) {
                                    return state.equals(args[0]);
                                } else if(method.getName().equals("hashCode")) {
                                    return state.hashCode();
                                } 
                                return method.invoke(state, args);
                            }
                        });
                untypedStates.put(state.getStateId(), MutableState.class.cast(untypedState));
            }
            states.clear();
            states.putAll(untypedStates);
        }
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
        String conditionName = transition.getCondition().name();
        
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
            for (String exitMethodCallCandidate : exitMethodCallCandidates) {
                addStateEntryExitMethodCallAction(exitMethodCallCandidate,
                        methodCallParamTypes, state, false);
            }
            
            // transition extension methods
            for(ImmutableTransition<T, S, E, C> transition : state.getAllTransitions()) {
                String[] transitionMethodCallCandidates = getTransitionMethodNames(transition);
                for (String transitionMethodCallCandidate : transitionMethodCallCandidates) {
                    addTransitionMethodCallAction(transitionMethodCallCandidate, methodCallParamTypes,
                            (MutableTransition<T, S, E, C>) transition);
                }
            }
            
            // state entry extension method
            String[] entryMethodCallCandidates = getEntryExitStateMethodNames(state, true);
            for (String entryMethodCallCandidate : entryMethodCallCandidates) {
                addStateEntryExitMethodCallAction(entryMethodCallCandidate,
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
            state.addExitAction(new AnonymousAction<T, S, E, C>() {
                @Override
                public void execute(S from, S to, E event, C context, T stateMachine) {
                    throw new IllegalStateException("Final state cannot be exited anymore.");
                }
                
                @Override
                public String name() {
                    return "__FINAL_STATE_ACTION_GUARD";
                }
            });
            
        }
    }
    
    private boolean isInstantiableType(Class<?> type) {
        return type != null && !type.isInterface() && !Modifier.isAbstract(type.getModifiers()) &&
                ( type.getEnclosingClass() == null || Modifier.isStatic(type.getModifiers()) );
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
    
    static Method findMethodCallActionInternal(Class<?> target, String methodName, Class<?>[] parameterTypes) {
        return searchMethod(target, AbstractStateMachine.class, methodName, parameterTypes);
    }
    
    @Override
    public T newStateMachine(S initialStateId) {
        return newStateMachine(initialStateId, new Object[0]);
    }
    
    @Override
    public T newStateMachine(S initialStateId, Object... extraParams) {
        return newStateMachine(initialStateId, defaultConfiguration, extraParams);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T newStateMachine(S initialStateId, StateMachineConfiguration configuration, Object... extraParams) {
        if(!prepared) prepare();
        if(!isValidState(initialStateId)) {
            throw new IllegalArgumentException(getClass()+" cannot find Initial state \'"+ 
                    initialStateId+"\' in state machine.");
        }
        
        Class<?>[] constParamTypes = constructor.getParameterTypes();
        final T stateMachine;
        try {
            if(constParamTypes==null || constParamTypes.length==0) {
                stateMachine = ReflectUtils.newInstance(constructor);
            } else { 
                stateMachine = ReflectUtils.newInstance(constructor, extraParams);
            }
        } catch(SquirrelRuntimeException e) {
            throw new IllegalStateException(
                    "New state machine instance failed.", e.getTargetException());
        }
                
        final AbstractStateMachine<T, S, E, C> stateMachineImpl = (AbstractStateMachine<T, S, E, C>)stateMachine;
        stateMachineImpl.prePostConstruct(initialStateId, states, configuration, new Runnable() {
            @Override
            public void run() {
                stateMachineImpl.setStartEvent(startEvent);
                stateMachineImpl.setFinishEvent(finishEvent);
                stateMachineImpl.setTerminateEvent(terminateEvent);
                stateMachineImpl.setExtraParamTypes(extraParamTypes);
                
                stateMachineImpl.setTypeOfStateMachine(stateMachineImplClazz);
                stateMachineImpl.setTypeOfState(stateClazz);
                stateMachineImpl.setTypeOfEvent(eventClazz);
                stateMachineImpl.setTypeOfContext(contextClazz);
                stateMachineImpl.setScriptManager(scriptManager);
            }
        });
        
        if(postConstructMethod!=null && extraParamTypes.length==extraParams.length) {
            try {
                ReflectUtils.invoke(postConstructMethod, stateMachine, extraParams);
            } catch(SquirrelRuntimeException e) {
                throw new IllegalStateException(
                        "Invoke state machine postConstruct method failed.", e.getTargetException());
            }
        } 
        postProcessStateMachine((Class<T>)stateMachineImplClazz, stateMachine);
        
        if(configuration.isRemoteMonitorEnabled()) {
            ManagementService managementService = new ManagementService();
            managementService.register(stateMachine);
        }
        return stateMachine;
    }

    private boolean isValidState(S initialStateId) {
        return initialStateId!=null && states.get(initialStateId) != null;
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
        checkState();
        return FSM.getState(states, stateId);
    }
    
    @Override
    public MutableState<T, S, E, C> defineFinalState(S stateId) {
        checkState();
        MutableState<T, S, E, C> newState = defineState(stateId);
        newState.setFinal(true);
        return newState;
    }
    
    @Override
    public MutableState<T, S, E, C> defineLinkedState(final S stateId, 
            final StateMachineBuilder<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachineBuilder, 
            final S initialLinkedState, final Object... extraParams) {
        checkState();
        MutableState<T, S, E, C> state = states.get(stateId);
        if(state==null) {
            MutableLinkedState<T, S, E, C> linkedState = FSM.newLinkedState(stateId);
            SquirrelInstanceProvider<StateMachine<?, S, E, C>> provider = new SquirrelInstanceProvider<StateMachine<?, S, E, C>>() {
                @Override
                public StateMachine<?, S, E, C> get() {
                    return linkedStateMachineBuilder.newStateMachine(initialLinkedState, extraParams);
                }
            };
            linkedState.setLinkedStateMachineProvider(provider);
            states.put(stateId, linkedState);
            state = linkedState;
        }
        return state;
    }
    
    @Override
    public MutableState<T, S, E, C> defineTimedState(S stateId,
            long initialDelay, long timeInterval, E autoEvent, C autoContext) {
        checkState();
        MutableState<T, S, E, C> state = states.get(stateId);
        if(state==null) {
            MutableTimedState<T, S, E, C> timedState = FSM.newTimedState(stateId);
            timedState.setAutoFireContext(autoContext);
            timedState.setAutoFireEvent(autoEvent);
            timedState.setInitialDelay(initialDelay);
            timedState.setTimeInterval(timeInterval);
            states.put(stateId, timedState);
            state = timedState;
        }
        return state;
    }

    @Override
    public void defineSequentialStatesOn(S parentStateId, S... childStateIds) {
        checkState();
        defineSequentialStatesOn(parentStateId, HistoryType.NONE, childStateIds);
    }

    @Override
    public void defineNoInitSequentialStatesOn(S parentStateId, S... childStateIds) {
        checkState();
        defineNoInitSequentialStatesOn(parentStateId, HistoryType.NONE, childStateIds);
    }

    @Override
    public void defineNoInitSequentialStatesOn(S parentStateId, HistoryType historyType, S... childStateIds) {
        checkState();
        defineChildStatesOn(parentStateId, StateCompositeType.SEQUENTIAL, historyType, true, childStateIds);
    }
    
    @Override
    public void defineSequentialStatesOn(S parentStateId, HistoryType historyType, S... childStateIds) {
        checkState();
        defineChildStatesOn(parentStateId, StateCompositeType.SEQUENTIAL, historyType, false, childStateIds);
    }
    
    @Override
    public void defineParallelStatesOn(S parentStateId, S... childStateIds) {
        checkState();
        defineChildStatesOn(parentStateId, StateCompositeType.PARALLEL, HistoryType.NONE, true, childStateIds);
    }

    private void defineChildStatesOn(S parentStateId, StateCompositeType compositeType,
                                     HistoryType historyType, boolean ignoreInitialState, S... childStateIds) {
        checkState();
        if(childStateIds!=null && childStateIds.length>0) {
            MutableState<T, S, E, C> parentState = FSM.getState(states, parentStateId);
            parentState.setCompositeType(compositeType);
            parentState.setHistoryType(historyType);
            for(int i=0, size=childStateIds.length; i<size; ++i) {
                MutableState<T, S, E, C> childState = FSM.getState(states, childStateIds[i]);
                if(!ignoreInitialState && i==0) {
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
        return FSM.newEntryExitActionBuilder(state, true, executionContext);
    }

    @Override
    public EntryExitActionBuilder<T, S, E, C> onExit(S stateId) {
        checkState();
        MutableState<T, S, E, C> state = FSM.getState(states, stateId);
        return FSM.newEntryExitActionBuilder(state, false, executionContext);
    }

    private class DeclareTransitionFunction implements Function<Class<?>, Boolean> {
        @Override
        public Boolean apply(Class<?> k) {
            buildDeclareTransition(k.getAnnotation(Transit.class));
            Transitions transitions = k.getAnnotation(Transitions.class);
            if(transitions!=null && transitions.value()!=null) {
                for(Transit t : transitions.value()) {
                    StateMachineBuilderImpl.this.buildDeclareTransition(t);
                }
            }
            return true;
        }
    }
    
    private class DeclareStateFunction implements Function<Class<?>, Boolean> {
        @Override
        public Boolean apply(Class<?> k) {
            buildDeclareState(k.getAnnotation(State.class));
            States states = k.getAnnotation(States.class);
            if(states!=null && states.value()!=null) {
                for(State s : states.value()) {
                    StateMachineBuilderImpl.this.buildDeclareState(s);
                }
            }
            return true;
        }
    }

    @Override
    public void defineFinishEvent(E finishEvent) {
        checkState();
        this.finishEvent = finishEvent;
    }

    @Override
    public void defineStartEvent(E startEvent) {
        checkState();
        this.startEvent = startEvent;
    }

    @Override
    public void defineTerminateEvent(E terminateEvent) {
        checkState();
        this.terminateEvent = terminateEvent;
    }
    
    void setScanAnnotations(boolean isScanAnnotations) {
        this.isScanAnnotations = isScanAnnotations;
    }

    @Override
    public void setStateMachineConfiguration(StateMachineConfiguration configure) {
        checkState();
        this.defaultConfiguration = configure;
    }
}
