package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.component.impl.AbstractSubject;
import org.squirrelframework.foundation.fsm.ActionExecutor;
import org.squirrelframework.foundation.fsm.ActionExecutor.ExecActionLisenter;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.MutableStateMachine;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.TransitionResult;
import org.squirrelframework.foundation.fsm.Visitor;
import org.squirrelframework.foundation.util.Pair;
import org.squirrelframework.foundation.util.ReflectUtils;
import org.squirrelframework.foundation.util.TypeReference;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
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
public abstract class AbstractStateMachine<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractSubject implements MutableStateMachine<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractStateMachine.class);
    
    private ImmutableState<T, S, E, C> currentState;
    
    private ImmutableState<T, S, E, C> lastState;
    
    private ImmutableState<T, S, E, C> initialState;
    
    private final Map<S, ImmutableState<T, S, E, C>> states;
    
    private StateMachineStatus status = StateMachineStatus.INITIALIZED;
    
    private boolean autoStart = true;
    
    private final LinkedList<Pair<E, C>> queuedEvents = Lists.newLinkedList();
    
    private final Map<S, S> lastActiveChildStateStore = Maps.newHashMap();
    
    private final ArrayListMultimap<S, S> parallelStatesStore = ArrayListMultimap.create();
    
    private final ActionExecutor<T, S, E, C> executor = SquirrelProvider.getInstance().newInstance(
    		new TypeReference<ActionExecutor<T, S, E, C>>(){});
    
    private E startEvent, finishEvent, terminateEvent;
    
    protected AbstractStateMachine(ImmutableState<T, S, E, C> initialState, Map<S, ImmutableState<T, S, E, C>> states) {
        this.initialState = initialState;
        this.currentState = initialState;
        this.states = Collections.unmodifiableMap(states);
    }
    
    private void processEvent(E event, C context) {
        ImmutableState<T, S, E, C> fromState = currentState;
        logger.debug("Transition from state \""+fromState+"\" on event \""+event+"\" begins.");
        Stopwatch sw = null;
        if(logger.isDebugEnabled()) {
            sw = new Stopwatch().start();
        }
        try {
            beforeTransitionBegin(fromState.getStateId(), event, context);
            fireEvent(new TransitionBeginEventImpl<T, S, E, C>(currentState.getStateId(), event, context, getThis()));
            
            executor.begin();
            TransitionResult<T, S, E, C> result = FSM.newResult(false, currentState, null);
            currentState.internalFire( FSM.newStateContext(this, currentState, event, context, result, executor) );
            executor.execute();
            
            if(result.isAccepted()) {
                lastState = currentState;
            	currentState = result.getTargetState();
            	fireEvent(new TransitionCompleteEventImpl<T, S, E, C>(fromState.getStateId(), currentState.getStateId(), 
                      event, context, getThis()));
                afterTransitionCompleted(fromState.getStateId(), currentState.getStateId(), event, context);
            } else {
            	fireEvent(new TransitionDeclinedEventImpl<T, S, E, C>(fromState.getStateId(), event, context, getThis()));
                afterTransitionDeclined(fromState.getStateId(), event, context);
            }
        } catch(Exception e) {
            fireEvent(new TransitionExceptionEventImpl<T, S, E, C>(e, fromState.getStateId(), 
                    currentState.getStateId(), event, context, getThis()));
            afterTransitionCausedException(e, fromState.getStateId(), currentState.getStateId(), event, context);
        } finally {
            if(logger.isDebugEnabled()) {
                logger.debug("Transition from state \""+fromState+"\" on event \""+event+
                        "\" tooks "+sw.stop().elapsedMillis()+"ms.");
            }
        }
    }
    
    protected int getQueuedEventSize() {
        return queuedEvents.size();
    }
    
    private void processQueuedEvents() {
        while (getQueuedEventSize() > 0) {
            Pair<E, C> eventInfo = queuedEvents.removeFirst();
            processEvent(eventInfo.first(), eventInfo.second());
        }
    }
    
    private void execute() {
        if (isIdel()) {
            try {
                status = StateMachineStatus.BUSY;
                processQueuedEvents();
            } finally {
            	if(status==StateMachineStatus.BUSY)
            		status = StateMachineStatus.IDLE;
            }
        }
    }
    
    @Override
    public void fire(E event, C context) {
        if(status==StateMachineStatus.INITIALIZED) {
            if(autoStart) {
                start(context);
            } else {
                throw new RuntimeException("The state machine is not running.");
            }
        }
        if(status==StateMachineStatus.TERMINATED) {
            throw new RuntimeException("The state machine is already terminated.");
        }
        queuedEvents.addLast(new Pair<E, C>(event, context));
        execute();
    }
    
    protected boolean isIdel() {
    	return getStatus()!=StateMachineStatus.BUSY;
    }
    
    protected void afterTransitionCausedException(Exception e, S fromState, S toState, 
            E event, C context) {
        status = StateMachineStatus.ERROR;
        logger.error("Transition from state \""+fromState+"\" to state \""+toState+
                "\" on event \""+event+"\" failed, which is caused by exception \""+e.getMessage()+"\".");
    }
    
    protected void beforeTransitionBegin(S fromState, E event, C context) {
    }
    
    protected void afterTransitionCompleted(S fromState, S toState, E event, C context) {
    }
    
    protected void afterTransitionDeclined(S fromState, E event, C context) {
    }
    
    protected void internalSetState(S state) {
        currentState = getRawStateFrom(state);
    }
    
    @Override
    public ImmutableState<T, S, E, C> getCurrentRawState() {
        return currentState;
    }
    
    @Override
    public ImmutableState<T, S, E, C> getLastRawState() {
        return lastState;
    }
    
    @Override
    public S getCurrentState() {
        return currentState.getStateId();
    }
    
    @Override
    public S getLastState() {
        return lastState.getStateId();
    }
    
    @Override
    public S getInitialState() {
        return initialState.getStateId();
    }

    @Override
    public ImmutableState<T, S, E, C> getRawStateFrom(S stateId) {
        return states.get(stateId);
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
    public void start(C context) {
    	if(isStarted()) {
            return;
        }
    	status = StateMachineStatus.IDLE;
        
    	executor.begin();
    	StateContext<T, S, E, C> stateContext = FSM.newStateContext(
    			this, getCurrentRawState(), getStartEvent(), context, null, executor);
        entryAll(initialState, stateContext);
        currentState = getCurrentRawState().enterByHistory(stateContext);
        executor.execute();
        
        execute();
        fireEvent(new StartEventImpl<T, S, E, C>(getThis()));
    }
    
    private boolean isStarted() {
        return status==StateMachineStatus.IDLE || status==StateMachineStatus.BUSY;
    }
    
    private boolean isTerminiated() {
    	return status==StateMachineStatus.TERMINATED;
    }
    
    @Override
    public StateMachineStatus getStatus() {
        return status;
    }
    
    @Override
    public S getLastActiveChildStateOf(S parentStateId) {
    	return lastActiveChildStateStore.get(parentStateId);
    }
    
    @Override
    public void setLastActiveChildState(S parentStateId, S childStateId) {
    	lastActiveChildStateStore.put(parentStateId, childStateId);
    }
    
    @Override
    public List<S> getSubStatesOn(S parentState) {
    	if(getRawStateFrom(parentState).isParallelState()) {
    		return parallelStatesStore.get(parentState);
    	} 
    	return Collections.emptyList();
    }
    
    @Override
    public void setSubState(S parentState, S subState) {
    	if(getRawStateFrom(parentState)!=null && getRawStateFrom(parentState).isParallelState()) {
    		parallelStatesStore.put(parentState, subState);
    	} else {
    		logger.warn("Cannot set sub states on none parallel state {}."+parentState);
    	}
    }
    
    @Override
    public void removeSubState(S parentState, S subState) {
    	if(getRawStateFrom(parentState)!=null && getRawStateFrom(parentState).isParallelState()) {
    		parallelStatesStore.remove(parentState, subState);
    	} else {
    		logger.warn("Cannot remove sub states on none parallel state {}."+parentState);
    	}
    }
    
    @Override
    public void removeSubStatesOn(S parentState) {
    	if(getRawStateFrom(parentState).isParallelState()) {
    		parallelStatesStore.removeAll(parentState);
    	} 
    }
    
    @Override
    public void terminate(C context) {
    	if(isTerminiated()) {
            return;
        }
        
    	executor.begin();
        StateContext<T, S, E, C> stateContext = FSM.newStateContext(
                this, getCurrentRawState(), getTerminateEvent(), context, null, executor);
        exitAll(getCurrentRawState(), stateContext);
        executor.execute();
        
        currentState = initialState;
        status = StateMachineStatus.TERMINATED;
        fireEvent(new TerminateEventImpl<T, S, E, C>(getThis()));
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
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        for(ImmutableState<T, S, E, C> state : states.values()) {
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
    
    // leverage bridge method to call the method of actual listener
    private static final Method STATEMACHINE_EVENT_METHOD = 
            ReflectUtils.getMethod(StateMachineListener.class, "stateMachineEvent", new Class<?>[]{StateMachineEvent.class});
    
    @Override
    public void addStateMachineListener(StateMachineListener<T, S, E, C> listener) {
        addListener(StateMachineEvent.class, listener, STATEMACHINE_EVENT_METHOD);
    }
    
    @Override
    public void removeStateMachineListener(StateMachineListener<T, S, E, C> listener) {
        removeListener(StateMachineEvent.class, listener, STATEMACHINE_EVENT_METHOD);
    }
    
    private static final Method START_EVENT_METHOD = 
            ReflectUtils.getMethod(StartListener.class, "started", new Class<?>[]{StartEvent.class});
    
    @Override
    public void addStartListener(StartListener<T, S, E, C> listener) {
        addListener(StartEvent.class, listener, START_EVENT_METHOD);
    }
    
    @Override
    public void removeStartListener(StartListener<T, S, E, C> listener) {
        removeListener(StartEvent.class, listener, START_EVENT_METHOD);
    }
    
    private static final Method TERMINATE_EVENT_METHOD = 
            ReflectUtils.getMethod(TerminateListener.class, "terminated", new Class<?>[]{TerminateEvent.class});

    @Override
    public void addTerminateListener(TerminateListener<T, S, E, C> listener) {
        addListener(TerminateEvent.class, listener, TERMINATE_EVENT_METHOD);
    }
    
    @Override
    public void removeTerminateListener(TerminateListener<T, S, E, C> listener) {
        removeListener(TerminateEvent.class, listener, TERMINATE_EVENT_METHOD);
    }
    
    private static final Method STATEMACHINE_EXCEPTION_EVENT_METHOD = 
            ReflectUtils.getMethod(StateMachineExceptionListener.class, 
                    "stateMachineException", new Class<?>[]{StateMachineExceptionEvent.class});

    @Override
    public void addStateMachineExceptionListener(StateMachineExceptionListener<T, S, E, C> listener) {
        addListener(StateMachineExceptionEvent.class, listener, STATEMACHINE_EXCEPTION_EVENT_METHOD);
    }
    
    @Override
    public void removeStateMachineExceptionListener(StateMachineExceptionListener<T, S, E, C> listener) {
        removeListener(StateMachineExceptionEvent.class, listener, STATEMACHINE_EXCEPTION_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_BEGIN_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionBeginListener.class, 
                    "transitionBegin", new Class<?>[]{TransitionBeginEvent.class});

    @Override
    public void addTransitionBeginListener(TransitionBeginListener<T, S, E, C> listener) {
        addListener(TransitionBeginEvent.class, listener, TRANSITION_BEGIN_EVENT_METHOD);
    }
    
    @Override
    public void removeTransitionBeginListener(TransitionBeginListener<T, S, E, C> listener) {
        removeListener(TransitionBeginEvent.class, listener, TRANSITION_BEGIN_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_COMPLETE_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionCompleteListener.class, 
                    "transitionComplete", new Class<?>[]{TransitionCompleteEvent.class});

    @Override
    public void addTransitionCompleteListener(TransitionCompleteListener<T, S, E, C> listener) {
        addListener(TransitionCompleteEvent.class, listener, TRANSITION_COMPLETE_EVENT_METHOD);
    }
    
    @Override
    public void removeTransitionCompleteListener(TransitionCompleteListener<T, S, E, C> listener) {
        removeListener(TransitionCompleteEvent.class, listener, TRANSITION_COMPLETE_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_EXCEPTION_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionExceptionListener.class, 
                    "transitionException", new Class<?>[]{TransitionExceptionEvent.class});

    @Override
    public void addTransitionExceptionListener(TransitionExceptionListener<T, S, E, C> listener) {
        addListener(TransitionExceptionEvent.class, listener, TRANSITION_EXCEPTION_EVENT_METHOD);
    }
    
    @Override
    public void removeTransitionExceptionListener(TransitionExceptionListener<T, S, E, C> listener) {
        removeListener(TransitionExceptionEvent.class, listener, TRANSITION_EXCEPTION_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_DECLINED_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionDeclinedListener.class, 
                    "transitionDeclined", new Class<?>[]{TransitionDeclinedEvent.class});

    @Override
    public void addTransitionDeclinedListener(TransitionDeclinedListener<T, S, E, C> listener) {
        addListener(TransitionDeclinedEvent.class, listener, TRANSITION_DECLINED_EVENT_METHOD);
    }
    
    @Override
    public void removeTransitionDecleindListener(TransitionDeclinedListener<T, S, E, C> listener) {
        removeListener(TransitionDeclinedEvent.class, listener, TRANSITION_DECLINED_EVENT_METHOD);
    }
    
    public void addExecActionListener(ExecActionLisenter<T, S, E, C> listener) {
    	executor.addExecActionListener(listener);
    }
	
	public void removeExecActionListener(ExecActionLisenter<T, S, E, C> listener) {
		executor.removeExecActionListener(listener);
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
}
