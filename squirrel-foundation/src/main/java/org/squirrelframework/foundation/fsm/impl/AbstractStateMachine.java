package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.component.impl.AbstractSubject;
import org.squirrelframework.foundation.event.ListenerMethod;
import org.squirrelframework.foundation.fsm.ActionExecutionService;
import org.squirrelframework.foundation.fsm.ActionExecutionService.*;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ImmutableLinkedState;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.MvelScriptManager;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineData;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.TransitionResult;
import org.squirrelframework.foundation.fsm.Visitor;
import org.squirrelframework.foundation.fsm.annotation.OnActionExecute;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionBegin;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionComplete;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionDecline;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionEnd;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionException;
import org.squirrelframework.foundation.util.Pair;
import org.squirrelframework.foundation.util.ReflectUtils;
import org.squirrelframework.foundation.util.TypeReference;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Abstract state machine provide several extension ability to cover different extension granularity. 
 * <ol>
 * <li>Method <b>beforeStateExit</b>/<b>afterStateEntry</b> is used to add custom logic on all kinds of state exit/entry.</li>
 * <li>Method <b>exit[stateName]</b>/<b>entry[stateName]</b> is extension method which is used to add custom logic on specific state.</li>
 * <li>Method <b>beforeTransitionBegin</b>/<b>afterTransitionComplete</b> is used to add custom logic on all kinds of transition 
 * accepted all conditions.</li>
 * <li>Method <b>transitFrom[fromStateName]To[toStateName]On[eventName]</b> is used to add custom logic on specific transition 
 * accepted all conditions.</li>
 * <li>Method <b>transitFromAnyTo[toStateName]On[eventName]</b> is used to add custom logic on any state transfer to specific target 
 * state on specific event happens, so as the <b>transitFrom[fromStateName]ToAnyOn[eventName]</b>, <b>transitFrom[fromState]To[ToStateName]</b>, 
 * and <b>on[EventName]</b>.</li>
 * </ol>
 * @author Henry.He
 *
 * @param <T> state machine type
 * @param <S> state type
 * @param <E> event type
 * @param <C> context type
 */
public abstract class AbstractStateMachine<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractSubject implements StateMachine<T, S, E, C> {
    
    static {
        SquirrelProvider.getInstance().register(ActionExecutionService.class, SynchronizedExecutionService.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractStateMachine.class);
    
    private boolean autoStart = true;
    
    private boolean autoTerminate = true;
    
    private final ActionExecutionService<T, S, E, C> executor = SquirrelProvider.getInstance().newInstance(
    		new TypeReference<ActionExecutionService<T, S, E, C>>(){});
    
    protected final StateMachineData<T, S, E, C> data;
    
    private volatile StateMachineStatus status = StateMachineStatus.INITIALIZED;
    
    private LinkedBlockingQueue<Pair<E, C>> queuedEvents = new LinkedBlockingQueue<Pair<E, C>>();
    
    private E startEvent, finishEvent, terminateEvent;
    
    private final Lock processingLock = new ReentrantLock();
    
    private MvelScriptManager scriptManager;
    
    protected AbstractStateMachine(ImmutableState<T, S, E, C> initialState, Map<S, ? extends ImmutableState<T, S, E, C>> states) {
        data = SquirrelProvider.getInstance().newInstance( 
                new TypeReference<StateMachineData<T, S, E, C>>(){}, 
                new Class[]{Map.class}, new Object[]{states} );
        
        S intialStateId = initialState.getStateId();
        data.write().initalState(intialStateId);
        data.write().currentState(intialStateId);
    }
    
    private void processEvent(E event, C context) {
        ImmutableState<T, S, E, C> fromState = data.read().currentRawState();
        S fromStateId = data.read().currentState(), toStateId = null;
        Stopwatch sw = null;
        if(logger.isDebugEnabled()) {
            logger.debug("Transition from state \""+fromState+"\" on event \""+event+"\" begins.");
            sw = new Stopwatch().start();
        }
        try {
            beforeTransitionBegin(fromStateId, event, context);
            fireEvent(new TransitionBeginEventImpl<T, S, E, C>(fromStateId, event, context, getThis()));
            
            executor.begin();
            TransitionResult<T, S, E, C> result = FSM.newResult(false, fromState, null);
            fromState.internalFire( FSM.newStateContext(this, data, fromState, event, context, result, executor) );
            toStateId = result.getTargetState().getStateId();
            executor.execute();
            
            if(result.isAccepted()) {
                data.write().lastState(fromStateId);
                data.write().currentState(toStateId);
                afterTransitionCompleted(fromStateId, getCurrentState(), event, context);
                fireEvent(new TransitionCompleteEventImpl<T, S, E, C>(fromStateId, toStateId, 
                        event, context, getThis()));
            } else {
                afterTransitionDeclined(fromStateId, event, context);
                fireEvent(new TransitionDeclinedEventImpl<T, S, E, C>(fromStateId, event, context, getThis()));
            }
        } catch(Exception e) {
            setStatus(StateMachineStatus.ERROR);
            logger.error("Transition from state \""+fromState+"\" to state \""+toStateId+
                    "\" on event \""+event+"\" failed, which is caused by exception \""+e.getMessage()+"\".");
            afterTransitionCausedException(e, fromStateId, toStateId, event, context);
            fireEvent(new TransitionExceptionEventImpl<T, S, E, C>(e, fromStateId, 
                    data.read().currentState(), event, context, getThis()));
        } finally {
            if(logger.isDebugEnabled()) {
                logger.debug("Transition from state \""+fromState+"\" on event \""+event+
                        "\" tooks "+sw.stop().elapsedMillis()+"ms.");
            }
            afterTransitionEnd(fromStateId, getCurrentState(), event, context);
            fireEvent(new TransitionEndEventImpl<T, S, E, C>(fromStateId, event, context, getThis()));
        }
    }
    
    private void processEvents() {
        if (isIdle()) {
            processingLock.lock();
            setStatus(StateMachineStatus.BUSY);
            try {
                Pair<E, C> eventInfo = null;
                while ((eventInfo=queuedEvents.poll())!=null) {
                    // response to cancel operation
                    if(Thread.interrupted()) {
                        queuedEvents.clear();
                        break;
                    }
                    processEvent(eventInfo.first(), eventInfo.second());
                }
            } finally {
            	if(getStatus()==StateMachineStatus.BUSY)
            	    setStatus(StateMachineStatus.IDLE);
            	processingLock.unlock();
            }
        }
    }
    
    @Override
    public void fire(E event, C context) {
        if(getStatus()==StateMachineStatus.INITIALIZED) {
            if(autoStart) {
                start(context);
            } else {
                throw new RuntimeException("The state machine is not running.");
            }
        }
        if(getStatus()==StateMachineStatus.TERMINATED) {
            throw new RuntimeException("The state machine is already terminated.");
        }
        if(getStatus()==StateMachineStatus.ERROR) {
            throw new RuntimeException("The state machine is corruptted.");
        }
        queuedEvents.add(new Pair<E, C>(event, context));
        processEvents();
        
        if(autoTerminate && getCurrentRawState().isRootState() 
                && getCurrentRawState().isFinalState()) {
            terminate(context);
        }
    }
    
    @Override
    public S test(E event, C context) {
        if( getStatus()==StateMachineStatus.ERROR || getStatus()==StateMachineStatus.TERMINATED) {
            throw new RuntimeException("Cannot test state machine under "+status+" status.");
        }
        
        S testResult = null;
        if(processingLock.tryLock()) {
            StateMachineStatus orgStatus = null;
            StateMachineData.Reader<T, S, E, C> oldData = null;
            try {
                orgStatus = getStatus();
                oldData = dumpSavedData();
                executor.setDummyExecution(true);
                fire(event, context);
                testResult = data.read().currentState();
            } finally {
                queuedEvents.clear();
                loadSavedData(oldData);
                setStatus(orgStatus);
                executor.setDummyExecution(false);
                processingLock.unlock();
            }
        }
        return testResult;
    }
    
    protected boolean isIdle() {
    	return getStatus()!=StateMachineStatus.BUSY;
    }
    
    protected void afterTransitionCausedException(Exception e, S fromState, S toState, E event, C context) {
    }
    
    protected void beforeTransitionBegin(S fromState, E event, C context) {
    }
    
    protected void afterTransitionCompleted(S fromState, S toState, E event, C context) {
    }
    
    protected void afterTransitionEnd(S fromState, S toState, E event, C context) {
    }
    
    protected void afterTransitionDeclined(S fromState, E event, C context) {
    }
    
    @Override
    public ImmutableState<T, S, E, C> getCurrentRawState() {
        return getRawStateFrom(getCurrentState());
    }
    
    @Override
    public ImmutableState<T, S, E, C> getLastRawState() {
        return getRawStateFrom(getLastState());
    }
    
    @Override
    public ImmutableState<T, S, E, C> getInitialRawState() {
        return getRawStateFrom(getInitialState());
    }
    
    @Override
    public ImmutableState<T, S, E, C> getRawStateFrom(S stateId) {
        return data.read().rawStateFrom(stateId);
    }
    
    @Override
    public Collection<ImmutableState<T, S, E, C>> getAllRawStates() {
        return data.read().rawStates();
    }
    
    @Override
    public Collection<S> getAllStates() {
        return data.read().states();
    }
    
    @Override
    public S getCurrentState() {
        processingLock.lock();
        try {
            return data.read().currentState();
        } finally {
            processingLock.unlock();
        }
    }
    
    @Override
    public S getLastState() {
        processingLock.lock();
        try {
            return data.read().lastState();
        } finally {
            processingLock.unlock();
        }
    }
    
    @Override
    public S getInitialState() {
        processingLock.lock();
        try {
            return data.read().initialState();
        } finally {
            processingLock.unlock();
        }
    }

    private void entryAll(ImmutableState<T, S, E, C> origin, StateContext<T, S, E, C> stateContext) {
    	Stack<ImmutableState<T, S, E, C>> stack = new Stack<ImmutableState<T, S, E, C>>();

    	ImmutableState<T, S, E, C> state = origin;
		while (state != null) {
			stack.push(state);
			state = state.getParentState();
		}
		while (stack.size() > 0) {
			state = stack.pop();
			state.entry(stateContext);
		}
	}
    
    @Override
    public synchronized void start(C context) {
    	if(isStarted()) {
            return;
        }
    	setStatus(StateMachineStatus.IDLE);
        
    	executor.begin();
    	StateContext<T, S, E, C> stateContext = FSM.newStateContext(
    			this, data, data.read().currentRawState(), getStartEvent(), 
    			context, null, executor);
        entryAll(data.read().initialRawState(), stateContext);
        ImmutableState<T, S, E, C> currentState = data.read().currentRawState();
        ImmutableState<T, S, E, C> historyState = currentState.enterByHistory(stateContext);
        executor.execute();
        data.write().currentState(historyState.getStateId());
        
        processEvents();
        fireEvent(new StartEventImpl<T, S, E, C>(getThis()));
    }
    
    @Override
    public void start() {
        start(null);
    }
    
    private boolean isStarted() {
        return getStatus()==StateMachineStatus.IDLE || getStatus()==StateMachineStatus.BUSY;
    }
    
    private boolean isTerminiated() {
    	return getStatus()==StateMachineStatus.TERMINATED;
    }
    
    @Override
    public StateMachineStatus getStatus() {
        return status;
    }
    
    protected void setStatus(StateMachineStatus status) {
        this.status = status;
    }
    
    @Override
    public S getLastActiveChildStateOf(S parentStateId) {
        processingLock.lock();
        try {
            return data.read().lastActiveChildStateOf(parentStateId);
        } finally {
            processingLock.unlock();
        }
    }
    
    @Override
    public List<S> getSubStatesOn(S parentStateId) {
        processingLock.lock();
        try {
            return data.read().subStatesOn(parentStateId);
        } finally {
            processingLock.unlock();
        }
    }
    
    @Override
    public synchronized void terminate(C context) {
    	if(isTerminiated()) {
            return;
        }
        
    	executor.begin();
        StateContext<T, S, E, C> stateContext = FSM.newStateContext(
                this, data, data.read().currentRawState(), getTerminateEvent(), 
                context, null, executor);
        exitAll(data.read().currentRawState(), stateContext);
        executor.execute();
        
        setStatus(StateMachineStatus.TERMINATED);
        fireEvent(new TerminateEventImpl<T, S, E, C>(getThis()));
    }
    
    @Override
    public void terminate() {
        terminate(null);
    }
    
    private void exitAll(ImmutableState<T, S, E, C> current, StateContext<T, S, E, C> stateContext) {
    	ImmutableState<T, S, E, C> state = current;
        while (state != null) {
        	state.exit(stateContext);
        	state = state.getParentState();
		}
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T getThis() {
    	return (T)this;
    }
    
    void setTypeOfStateMachine(Class<? extends T> stateMachineType) {
        data.write().typeOfStateMachine(stateMachineType);
    }
    
    void setTypeOfState(Class<S> stateType) {
        data.write().typeOfState(stateType);
    }
    
    void setTypeOfEvent(Class<E> eventType) {
        data.write().typeOfEvent(eventType);
    }
    
    void setTypeOfContext(Class<C> contextType) {
        data.write().typeOfContext(contextType);
    }
    
    void setScriptManager(MvelScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        for(ImmutableState<T, S, E, C> state : data.read().rawStates()) {
        	if(state.getParentState()==null)
        		state.accept(visitor);
        }
        visitor.visitOnExit(this);
    }
    
    public void setStartEvent(E startEvent) {
    	this.startEvent=startEvent;
    }
    
    public E getStartEvent() {
    	return startEvent;
    }
    
    public void setTerminateEvent(E terminateEvent) {
    	this.terminateEvent=terminateEvent;
    }
    
    public E getTerminateEvent() {
    	return terminateEvent;
    }
    
    public void setFinishEvent(E finishEvent) {
    	this.finishEvent=finishEvent;
    }
    
    public E getFinishEvent() {
    	return finishEvent;
    }
    
    @Override
    public StateMachineData.Reader<T, S, E, C> dumpSavedData() {
        StateMachineData<T, S, E, C> savedData = null;
        if(processingLock.tryLock()) {
            try {
                savedData = SquirrelProvider.getInstance().newInstance( 
                        new TypeReference<StateMachineData<T, S, E, C>>(){});
                savedData.dump(data.read());
                
                // process linked state if any
                saveLinkedStateData(data.read(), savedData.write());
            } finally {
                processingLock.unlock();
            }
        } 
        return savedData==null ? null : savedData.read();
    }
    
    private void saveLinkedStateData(StateMachineData.Reader<T, S, E, C> src, StateMachineData.Writer<T, S, E, C> target) {
        dumpLinkedStateFor(src.currentRawState(), target);
//        dumpLinkedStateFor(src.lastRawState(), target);
        // TODO-hhe: dump linked state info for last active child state
        // TODO-hhe: dump linked state info for parallel state
    }
    
    private void dumpLinkedStateFor(ImmutableState<T, S, E, C> rawState, StateMachineData.Writer<T, S, E, C> target) {
        if(rawState!=null && rawState instanceof ImmutableLinkedState) {
            ImmutableLinkedState<T, S, E, C> linkedRawState = (ImmutableLinkedState<T, S, E, C>)rawState;
            StateMachineData.Reader<? extends StateMachine<?, S, E, C>, S, E, C> linkStateData = 
                    linkedRawState.getLinkedStateMachine().dumpSavedData();
            target.linkedStateDataOn(rawState.getStateId(), linkStateData);
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean loadSavedData(StateMachineData.Reader<T, S, E, C> savedData) {
        Preconditions.checkNotNull(savedData, "Saved data cannot be null");
        if(processingLock.tryLock()) {
            try {
                data.dump(savedData);
                // process linked state if any
                for(S linkedState : savedData.linkedStates()) {
                    StateMachineData.Reader linkedStateData = savedData.linkedStateDataOf(linkedState);
                    ImmutableState<T, S, E, C> rawState = data.read().rawStateFrom(linkedState);
                    if(linkedStateData!=null && rawState instanceof ImmutableLinkedState) {
                        ImmutableLinkedState<T, S, E, C> linkedRawState = (ImmutableLinkedState<T, S, E, C>)rawState;
                        linkedRawState.getLinkedStateMachine().loadSavedData(linkedStateData);
                    }
                }
                setStatus(StateMachineStatus.IDLE);
                return true;
            } finally {
                processingLock.unlock();
            }
        }
        return false;
    }
    
    @Override
    public boolean isContextSensitive() {
        return true;
    }
    
    public Class<C> typeOfContext() {
        return data.read().typeOfContext();
    }
    
    public Class<E> typeOfEvent() {
        return data.read().typeOfEvent();
    }
    
    public Class<S> typeOfState() {
        return data.read().typeOfState();
    }
    
    private Object newListenerMethodProxy(final Object listenTarget, 
            final Method listenerMethod, final Class<?> listenerInterface, final String condition) {
        final String listenerMethodName = ReflectUtils.getStatic(
                ReflectUtils.getField(listenerInterface, "METHOD_NAME")).toString();
        InvocationHandler invokationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getName().equals("getListenTarget")) {
                    return listenTarget;
                } else if(method.getName().equals(listenerMethodName)) {
                    if(args[0] instanceof TransitionEvent) {
                        @SuppressWarnings("unchecked")
                        TransitionEvent<T, S, E, C> event = (TransitionEvent<T, S, E, C>)args[0];
                        return invokeTransitionListenerMethod(listenTarget, listenerMethod, condition, event);
                    } else if(args[0] instanceof ExecActionEvent) {
                        @SuppressWarnings("unchecked")
                        ExecActionEvent<T, S, E, C> event = (ExecActionEvent<T, S, E, C>)args[0];
                        return invokeActionListenerMethod(listenTarget, listenerMethod, condition, event);
                    } else {
                        throw new IllegalArgumentException("Unable to recognize argument type "+args[0].getClass().getName()+".");
                    }
                } else if(method.getName().equals("equals")) {
                    return super.equals(args[0]);
                } else if(method.getName().equals("hashCode")) {
                    return super.hashCode();
                } else if(method.getName().equals("toString")) {
                    return super.toString();
                } 
                throw new UnsupportedOperationException("Cannot invoke method "+method.getName()+".");
            }
        };
        
        Object proxyListener = Proxy.newProxyInstance(StateMachine.class.getClassLoader(), 
                new Class<?>[]{listenerInterface, DeclarativeLisener.class}, invokationHandler);
        return proxyListener;
    }
    
    private Object invokeActionListenerMethod(final Object listenTarget, 
            final Method listenerMethod, final String condition, 
            final ExecActionEvent<T, S, E, C> event) {
        Class<?>[] parameterTypes = listenerMethod.getParameterTypes();
        
        final Map<String, Object> variables = Maps.newHashMap();
        variables.put("from", event.getFrom());
        variables.put("to", event.getTo());
        variables.put("event", event.getEvent());
        variables.put("context", event.getContext());
        variables.put("stateMachine", event.getStateMachine());
        
        boolean isSatisfied = true;
        if(condition!=null && condition.length()>0) {
            isSatisfied = scriptManager.evalBoolean(condition, variables);
        }
        if(!isSatisfied) return null;
        
        if(parameterTypes.length == 0) {
            return ReflectUtils.invoke(listenerMethod, listenTarget);
        }
        // parameter values infer
        List<Object> parameterValues = Lists.newArrayList();
        boolean isSourceStateSet = false, isTargetStateSet=false, isEventSet=false, isContextSet=false;
        for(Class<?> parameterType : parameterTypes) {
            if(!isSourceStateSet && parameterType.isAssignableFrom(typeOfState())) {
                parameterValues.add(event.getFrom());
                isSourceStateSet = true;
            } else if(!isTargetStateSet && parameterType.isAssignableFrom(typeOfState())) {
                parameterValues.add(event.getTo());
                isTargetStateSet = true;
            } else if(!isEventSet && parameterType.isAssignableFrom(typeOfEvent())) {
                parameterValues.add(event.getEvent());
                isEventSet = true;
            } else if(!isContextSet && parameterType.isAssignableFrom(typeOfContext())) {
                parameterValues.add(event.getContext());
                isContextSet = true;
            } else if(parameterType.isAssignableFrom(AbstractStateMachine.this.getClass())) {
                parameterValues.add(event.getStateMachine());
            } else if(parameterType.isAssignableFrom(Action.class)) {
                parameterValues.add(event.getExecutionTarget());
            } else if(parameterType==int[].class) {
                parameterValues.add(event.getMOfN());
            } else {
                parameterValues.add(null);
            }
        }
        return ReflectUtils.invoke(listenerMethod, listenTarget, parameterValues.toArray());
    }
    
    private Object invokeTransitionListenerMethod(final Object listenTarget, 
            final Method listenerMethod, final String condition, 
            final TransitionEvent<T, S, E, C> event) {
        Class<?>[] parameterTypes = listenerMethod.getParameterTypes();
        
        final Map<String, Object> variables = Maps.newHashMap();
        variables.put("from", event.getSourceState());
        variables.put("event", event.getCause());
        variables.put("context", event.getContext());
        variables.put("stateMachine", event.getStateMachine());
        if(event instanceof TransitionCompleteEvent) {
            variables.put("to", ((TransitionCompleteEvent<T, S, E, C>)event).getTargetState());
        } else if(event instanceof TransitionExceptionEvent) {
            variables.put("to", ((TransitionExceptionEvent<T, S, E, C>)event).getTargetState());
            variables.put("exception", ((TransitionExceptionEvent<T, S, E, C>)event).getException());
        }
        
        boolean isSatisfied = true;
        if(condition!=null && condition.length()>0) {
            isSatisfied = scriptManager.evalBoolean(condition, variables);
        }
        if(!isSatisfied) return null;
        
        if(parameterTypes.length == 0) {
            return ReflectUtils.invoke(listenerMethod, listenTarget);
        }
        // parameter values infer
        List<Object> parameterValues = Lists.newArrayList();
        boolean isSourceStateSet = false, isTargetStateSet=false, isEventSet=false, isContextSet=false;
        for(Class<?> parameterType : parameterTypes) {
            if(!isSourceStateSet && parameterType.isAssignableFrom(typeOfState())) {
                parameterValues.add(event.getSourceState());
                isSourceStateSet = true;
            } else if(!isTargetStateSet && event instanceof TransitionCompleteEvent && 
                    parameterType.isAssignableFrom(typeOfState())) {
                parameterValues.add(((TransitionCompleteEvent<T, S, E, C>)event).getTargetState());
                isTargetStateSet = true;
            } else if(!isTargetStateSet && event instanceof TransitionExceptionEvent && 
                    parameterType.isAssignableFrom(typeOfState()) && !isTargetStateSet) {
                parameterValues.add(((TransitionExceptionEvent<T, S, E, C>)event).getTargetState());
                isTargetStateSet = true;
            } else if(!isEventSet && parameterType.isAssignableFrom(typeOfEvent())) {
                parameterValues.add(event.getCause());
                isEventSet = true;
            } else if(!isContextSet && parameterType.isAssignableFrom(typeOfContext())) {
                parameterValues.add(event.getContext());
                isContextSet = true;
            } else if(parameterType.isAssignableFrom(AbstractStateMachine.this.getClass())) {
                parameterValues.add(event.getStateMachine());
            } else if(event instanceof TransitionExceptionEvent && parameterType.isAssignableFrom(Exception.class)) {
                parameterValues.add(((TransitionExceptionEvent<T, S, E, C>)event).getException());
            } else {
                parameterValues.add(null);
            }
        }
        return ReflectUtils.invoke(listenerMethod, listenTarget, parameterValues.toArray());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addDeclarativeListener(final Object listenTarget) {
        List<String> visitedMethods = Lists.newArrayList();
        for(final Method dMethod : listenTarget.getClass().getMethods()) {
            String methodSignature = dMethod.toString();
            if(visitedMethods.contains(methodSignature)) continue;
            visitedMethods.add(methodSignature);
            
            OnTransitionBegin tb = dMethod.getAnnotation(OnTransitionBegin.class);
            if(tb!=null) {
                TransitionBeginListener<T, S, E, C> tbListener = (TransitionBeginListener<T, S, E, C>)
                        newListenerMethodProxy(listenTarget, dMethod, TransitionBeginListener.class, tb.when());
                addTransitionBeginListener(tbListener);
            }
            
            OnTransitionComplete tc = dMethod.getAnnotation(OnTransitionComplete.class);
            if(tc!=null) {
                TransitionCompleteListener<T, S, E, C> tcListener = (TransitionCompleteListener<T, S, E, C>)
                        newListenerMethodProxy(listenTarget, dMethod, TransitionCompleteListener.class, tc.when());
                addTransitionCompleteListener(tcListener);
            }
            
            OnTransitionDecline td = dMethod.getAnnotation(OnTransitionDecline.class);
            if(td!=null) {
                TransitionDeclinedListener<T, S, E, C> tdListener = (TransitionDeclinedListener<T, S, E, C>)
                        newListenerMethodProxy(listenTarget, dMethod, TransitionDeclinedListener.class, td.when());
                addTransitionDeclinedListener(tdListener);
            }
            
            OnTransitionEnd te = dMethod.getAnnotation(OnTransitionEnd.class);
            if(te!=null) {
                TransitionEndListener<T, S, E, C> teListener = (TransitionEndListener<T, S, E, C>)
                        newListenerMethodProxy(listenTarget, dMethod, TransitionEndListener.class, te.when());
                addTransitionEndListener(teListener);
            }
            
            OnTransitionException tex = dMethod.getAnnotation(OnTransitionException.class);
            if(tex!=null) {
                TransitionExceptionListener<T, S, E, C> texListener = (TransitionExceptionListener<T, S, E, C>)
                        newListenerMethodProxy(listenTarget, dMethod, TransitionExceptionListener.class, tex.when());
                addTransitionExceptionListener(texListener);
            }
            
            OnActionExecute ad = dMethod.getAnnotation(OnActionExecute.class);
            if(ad!=null) {
                ExecActionListener<T, S, E, C> adListener = (ExecActionListener<T, S, E, C>)
                        newListenerMethodProxy(listenTarget, dMethod, ExecActionListener.class, ad.when());
                addExecActionListener(adListener);
            }
        }
    }
    
    @Override
    public void removeDeclarativeListener(final Object listenTarget) {
        if (eventDispatcher!=null) {
            eventDispatcher.unregister(new Predicate<ListenerMethod>() {
                @Override
                public boolean apply(ListenerMethod input) {
                    return (input.getTarget() instanceof DeclarativeLisener) && 
                          ((DeclarativeLisener)input.getTarget()).getListenTarget()==listenTarget;
                }
            });
        }
    }
    
    @Override
    public void addStateMachineListener(StateMachineListener<T, S, E, C> listener) {
        addListener(StateMachineEvent.class, listener, StateMachineListener.METHOD);
    }
    
    @Override
    public void removeStateMachineListener(StateMachineListener<T, S, E, C> listener) {
        removeListener(StateMachineEvent.class, listener, StateMachineListener.METHOD);
    }
    
    @Override
    public void addStartListener(StartListener<T, S, E, C> listener) {
        addListener(StartEvent.class, listener, StartListener.METHOD);
    }
    
    @Override
    public void removeStartListener(StartListener<T, S, E, C> listener) {
        removeListener(StartEvent.class, listener, StartListener.METHOD);
    }
    
    @Override
    public void addTerminateListener(TerminateListener<T, S, E, C> listener) {
        addListener(TerminateEvent.class, listener, TerminateListener.METHOD);
    }
    
    @Override
    public void removeTerminateListener(TerminateListener<T, S, E, C> listener) {
        removeListener(TerminateEvent.class, listener, TerminateListener.METHOD);
    }
    
    @Override
    public void addStateMachineExceptionListener(StateMachineExceptionListener<T, S, E, C> listener) {
        addListener(StateMachineExceptionEvent.class, listener, StateMachineExceptionListener.METHOD);
    }
    
    @Override
    public void removeStateMachineExceptionListener(StateMachineExceptionListener<T, S, E, C> listener) {
        removeListener(StateMachineExceptionEvent.class, listener, StateMachineExceptionListener.METHOD);
    }
    
    @Override
    public void addTransitionBeginListener(TransitionBeginListener<T, S, E, C> listener) {
        addListener(TransitionBeginEvent.class, listener, TransitionBeginListener.METHOD);
    }
    
    @Override
    public void removeTransitionBeginListener(TransitionBeginListener<T, S, E, C> listener) {
        removeListener(TransitionBeginEvent.class, listener, TransitionBeginListener.METHOD);
    }
    
    @Override
    public void addTransitionCompleteListener(TransitionCompleteListener<T, S, E, C> listener) {
        addListener(TransitionCompleteEvent.class, listener, TransitionCompleteListener.METHOD);
    }
    
    @Override
    public void removeTransitionCompleteListener(TransitionCompleteListener<T, S, E, C> listener) {
        removeListener(TransitionCompleteEvent.class, listener, TransitionCompleteListener.METHOD);
    }
    
    @Override
    public void addTransitionExceptionListener(TransitionExceptionListener<T, S, E, C> listener) {
        addListener(TransitionExceptionEvent.class, listener, TransitionExceptionListener.METHOD);
    }
    
    @Override
    public void removeTransitionExceptionListener(TransitionExceptionListener<T, S, E, C> listener) {
        removeListener(TransitionExceptionEvent.class, listener, TransitionExceptionListener.METHOD);
    }
    
    @Override
    public void addTransitionDeclinedListener(TransitionDeclinedListener<T, S, E, C> listener) {
        addListener(TransitionDeclinedEvent.class, listener, TransitionDeclinedListener.METHOD);
    }
    
    @Override
    public void removeTransitionDecleindListener(TransitionDeclinedListener<T, S, E, C> listener) {
        removeListener(TransitionDeclinedEvent.class, listener, TransitionDeclinedListener.METHOD);
    }
    
    @Override
    public void addTransitionEndListener(TransitionEndListener<T, S, E, C> listener) {
        addListener(TransitionEndEvent.class, listener, TransitionEndListener.METHOD);
    }
    
    @Override
    public void removeTransitionEndListener(TransitionEndListener<T, S, E, C> listener) {
        removeListener(TransitionEndListener.class, listener, TransitionEndListener.METHOD);
    }
    
    @Override
    public void addExecActionListener(ExecActionListener<T, S, E, C> listener) {
    	executor.addExecActionListener(listener);
    }
	
    @Override
	public void removeExecActionListener(ExecActionListener<T, S, E, C> listener) {
		executor.removeExecActionListener(listener);
	}
    
	private interface DeclarativeLisener {
        Object getListenTarget();
    }
    
    public static abstract class AbstractStateMachineEvent<T extends StateMachine<T, S, E, C>, S, E, C> 
    implements StateMachine.StateMachineEvent<T, S, E, C> {
    	private final T stateMachine;
        public AbstractStateMachineEvent(T stateMachine) {
        	this.stateMachine = stateMachine;
        }
        
        @Override
        public T getStateMachine() {
            return stateMachine;
        }
    }
    
    public static class StartEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractStateMachineEvent<T, S, E, C> implements StateMachine.StartEvent<T, S, E, C> {
        public StartEventImpl(T source) {
            super(source);
        }
    }
    
    public static class TerminateEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractStateMachineEvent<T, S, E, C> implements StateMachine.TerminateEvent<T, S, E, C> {
        public TerminateEventImpl(T source) {
            super(source);
        }
    }
    
    public static class StateMachineExceptionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractStateMachineEvent<T, S, E, C> implements StateMachine.StateMachineExceptionEvent<T, S, E, C> {
        private final Exception e;
        public StateMachineExceptionEventImpl(Exception e, T source) {
            super(source);
            this.e = e;
        }
        
        @Override
        public Exception getException() {
            return e;
        }
    }
    
    public static abstract class AbstractTransitionEvent<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractStateMachineEvent<T, S, E, C> implements StateMachine.TransitionEvent<T, S, E, C> {
        private final S sourceState;
        private final E event;
        private final C context;
        public AbstractTransitionEvent(S sourceState, E event, C context, T stateMachine) {
            super(stateMachine);
            this.sourceState = sourceState;
            this.event = event;
            this.context = context;
        }
        
        @Override
        public S getSourceState() {
            return sourceState;
        }
        
        @Override
        public E getCause() {
            return event;
        }
        
        @Override
        public C getContext() {
            return context;
        }
    }
    
    public static class TransitionBeginEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractTransitionEvent<T, S, E, C> implements StateMachine.TransitionBeginEvent<T, S, E, C> {
        public TransitionBeginEventImpl(S sourceState, E event, C context,T stateMachine) {
            super(sourceState, event, context, stateMachine);
        }
    }
    
    public static class TransitionCompleteEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractTransitionEvent<T, S, E, C> implements StateMachine.TransitionCompleteEvent<T, S, E, C> {
        private final S targetState;
        public TransitionCompleteEventImpl(S sourceState, S targetState, E event, C context,T stateMachine) {
            super(sourceState, event, context, stateMachine);
            this.targetState = targetState;
        }
        
        @Override
        public S getTargetState() {
            return targetState;
        }
    }
    
    public static class TransitionExceptionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractTransitionEvent<T, S, E, C> implements StateMachine.TransitionExceptionEvent<T, S, E, C> {
        private final S targetState;
        private final Exception e;
        public TransitionExceptionEventImpl(Exception e, S sourceState, S targetState, E event, C context,T stateMachine) {
            super(sourceState, event, context, stateMachine);
            this.targetState = targetState;
            this.e = e;
        }
        
        @Override
        public S getTargetState() {
            return targetState;
        }
        
        @Override
        public Exception getException() {
            return e;
        }
    }
    
    public static class TransitionDeclinedEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractTransitionEvent<T, S, E, C> implements StateMachine.TransitionDeclinedEvent<T, S, E, C> {
        public TransitionDeclinedEventImpl(S sourceState, E event, C context,T stateMachine) {
            super(sourceState, event, context, stateMachine);
        }
    }
    
    public static class TransitionEndEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractTransitionEvent<T, S, E, C> implements StateMachine.TransitionEndEvent<T, S, E, C> {
        public TransitionEndEventImpl(S sourceState, E event, C context,T stateMachine) {
            super(sourceState, event, context, stateMachine);
        }
    }
}
