package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.data.impl.AbstractHierarchyItem;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.TransitionResult;
import org.squirrelframework.foundation.fsm.Visitor;
import org.squirrelframework.foundation.util.Pair;
import org.squirrelframework.foundation.util.ReflectUtils;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

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
public abstract class AbstractStateMachine<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractHierarchyItem<T, Object> implements StateMachine<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractStateMachine.class);
    
    private ImmutableState<T, S, E, C> currentState;
    
    private ImmutableState<T, S, E, C> initialState;
    
    private final Map<S, ImmutableState<T, S, E, C>> states;
    
    private StateMachineStatus status = StateMachineStatus.INITIALIZED;
    
    private boolean autoStart = true;
    
    private final LinkedList<Pair<E, C>> queuedEvents = Lists.newLinkedList();
    
    public AbstractStateMachine(ImmutableState<T, S, E, C> initialState, Map<S, ImmutableState<T, S, E, C>> states, 
            T parent, Class<?> type, boolean isLeaf) {
        super(parent, type, isLeaf);
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
            fireEvent(new TransitionBeginEventImpl<T, S, E, C>(currentState.getStateId(), event, context, getCurrent()));
            
            TransitionResult<T, S, E, C> result = currentState.internalFire( 
            		FSM.newStateContext(getCurrent(), currentState, event, context) );
            
            if(result.isAccepted()) {
            	currentState = result.getTargetState();
            	fireEvent(new TransitionCompleteEventImpl<T, S, E, C>(fromState.getStateId(), currentState.getStateId(), 
                      event, context, getCurrent()));
                afterTransitionCompleted(fromState.getStateId(), currentState.getStateId(), event, context);
            } else {
            	fireEvent(new TransitionDeclinedEventImpl<T, S, E, C>(fromState.getStateId(), event, context, getCurrent()));
                afterTransitionDeclined(fromState.getStateId(), event, context);
            }
        } catch(Exception e) {
            fireEvent(new TransitionExceptionEventImpl<T, S, E, C>(e, fromState.getStateId(), 
                    currentState.getStateId(), event, context, getCurrent()));
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
            AbstractStateMachine<T, S, E, C> originParent = 
                    (AbstractStateMachine<T, S, E, C>)getParent();
            try {
                status = StateMachineStatus.BUSY;
                processQueuedEvents();
            } finally {
                status = StateMachineStatus.IDLE;
            }
            
            if(currentState.isFinal()) 
                terminateNow();
            
            // execute new events which was fired to parent during child processing events
            if(originParent!=null && originParent.getQueuedEventSize()>0) {
                originParent.execute();
            }
        }
    }
    
    @Override
    public void fire(E event, C context) {
        if(status==StateMachineStatus.INITIALIZED) {
            if(autoStart) {
                start();
            } else {
                throw new RuntimeException("The state machine is not running.");
            }
        }
        queuedEvents.addLast(new Pair<E, C>(event, context));
        execute();
    }
    
    protected boolean isIdel() {
        if(getStatus()==StateMachineStatus.BUSY)
            return false;
        
        for(StateMachine<T, S, E, C> s : getAllRawChildren(true, false)) {
            if(s.getStatus()==StateMachineStatus.BUSY) 
                return false;
        } 
        return true;
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
        // TODO-hhe: when exception happened during parent state machine handle the event, the exception 
        // handling order would be parent handle first then child. Is this what we expected?
        if(getParent()!=null) 
            getParent().fire(event, context);
    }
    
    protected void internalSetState(S state) {
        currentState = getRawStateFrom(state);
    }
    
    @Override
    public ImmutableState<T, S, E, C> getCurrentRawState() {
        return currentState;
    }
    
    @Override
    public S getCurrentState() {
        return currentState.getStateId();
    }
    
    @Override
    public S getInitialState() {
        return initialState.getStateId();
    }

    @Override
    public ImmutableState<T, S, E, C> getRawStateFrom(S stateId) {
        return states.get(stateId);
    }
    
    @Override
    public void start() {
        startNow();
        
        StateContext<T, S, E, C> stateContext = 
                FSM.newStateContext(getCurrent(), getCurrentRawState(), getInitialEvent(), getInitialContext());
        getCurrentRawState().entry(stateContext);
        
        nonRecursiveStartChildren(getCurrent(), getInitialEvent(), getInitialContext());
    }
    
    protected void startNow() {
        if(isStarted()) {
            return;
        }
        status = StateMachineStatus.IDLE;
        fireEvent(new StartEventImpl<T, S, E, C>(getCurrent()));
    }
    
    @SuppressWarnings("rawtypes")
    private void nonRecursiveStartChildren(T parent, E event, C context) {
        if(parent.getCurrentRawState().isFinal()) 
            return;
        
        for(T stateMachine : parent.getAllChildren(false, false)) {
            ((AbstractStateMachine)stateMachine).startNow();
            StateContext<T, S, E, C> stateContext = 
                    FSM.newStateContext(stateMachine, stateMachine.getCurrentRawState(), event, context);
            stateMachine.getCurrentRawState().entry(stateContext);
        }
    }
    
    private boolean isStarted() {
        return status==StateMachineStatus.IDLE || status==StateMachineStatus.BUSY;
    }
    
    @Override
    public StateMachineStatus getStatus() {
        return status;
    }

    @Override
    public void terminate() {
        // terminate children first then parent
        nonRecursiveTerminateChildren(getCurrent(), getTerminateEvent(), getTerminateContext());
        
        StateContext<T, S, E, C> stateContext = 
                FSM.newStateContext(getCurrent(), getCurrentRawState(), getTerminateEvent(), getTerminateContext());
        getCurrentRawState().exit(stateContext);
        
        terminateNow();
    }
    
    protected void terminateNow() {
        if(status!=StateMachineStatus.IDLE) {
            return;
        }
        // reset current state to state machine initial state
        currentState = initialState;
        status = StateMachineStatus.TERMINATED;
        fireEvent(new TerminateEventImpl<T, S, E, C>(getCurrent()));
        
        if(getParent()!=null) {
            detach();
        }
    }
    
    /**
     * Terminate all the child state machine without recursion. The same reason(event and context) that caused 
     * the child state machine terminated will be used for each child state machine exiting its current state. 
     * @param parent the parent state machine
     * @param event the reason caused child state machine closed.
     * @param context the context when terminating all child state machines.
     */
    @SuppressWarnings("rawtypes")
    private void nonRecursiveTerminateChildren(T parent, E event, C context) {
        if(!parent.hasChildren())
            return;
        
        for(T stateMachine : parent.getAllChildren(false, true)) {
            StateContext<T, S, E, C> stateContext = 
                    FSM.newStateContext(stateMachine, stateMachine.getCurrentRawState(), event, context);
            stateMachine.getCurrentRawState().exit(stateContext);
            ((AbstractStateMachine)stateMachine).terminateNow();
        }
    }
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        for(ImmutableState<T, S, E, C> state : states.values()) {
            state.accept(visitor);
        }
        visitor.visitOnExit(this);
    }
    
    abstract protected E getInitialEvent();
    
    abstract protected C getInitialContext();
    
    abstract protected E getTerminateEvent();
    
    abstract protected C getTerminateContext();
    
    // leverage bridge method to call the method of actual listener
    private static final Method STATEMACHINE_EVENT_METHOD = 
            ReflectUtils.getMethod(StateMachineListener.class, "stateMachineEvent", new Class<?>[]{StateMachineEvent.class});
    
    @Override
    public void addListener(StateMachineListener<T, S, E, C> listener) {
        addListener(StateMachineEvent.class, listener, STATEMACHINE_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(StateMachineListener<T, S, E, C> listener) {
        removeListener(StateMachineEvent.class, listener, STATEMACHINE_EVENT_METHOD);
    }
    
    private static final Method START_EVENT_METHOD = 
            ReflectUtils.getMethod(StartListener.class, "started", new Class<?>[]{StartEvent.class});
    
    @Override
    public void addListener(StartListener<T, S, E, C> listener) {
        addListener(StartEvent.class, listener, START_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(StartListener<T, S, E, C> listener) {
        removeListener(StartEvent.class, listener, START_EVENT_METHOD);
    }
    
    private static final Method TERMINATE_EVENT_METHOD = 
            ReflectUtils.getMethod(TerminateListener.class, "terminated", new Class<?>[]{TerminateEvent.class});

    @Override
    public void addListener(TerminateListener<T, S, E, C> listener) {
        addListener(TerminateEvent.class, listener, TERMINATE_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(TerminateListener<T, S, E, C> listener) {
        removeListener(TerminateEvent.class, listener, TERMINATE_EVENT_METHOD);
    }
    
    private static final Method STATEMACHINE_EXCEPTION_EVENT_METHOD = 
            ReflectUtils.getMethod(StateMachineExceptionListener.class, 
                    "stateMachineException", new Class<?>[]{StateMachineExceptionEvent.class});

    @Override
    public void addListener(StateMachineExceptionListener<T, S, E, C> listener) {
        addListener(StateMachineExceptionEvent.class, listener, STATEMACHINE_EXCEPTION_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(StateMachineExceptionListener<T, S, E, C> listener) {
        removeListener(StateMachineExceptionEvent.class, listener, STATEMACHINE_EXCEPTION_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_BEGIN_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionBeginListener.class, 
                    "transitionBegin", new Class<?>[]{TransitionBeginEvent.class});

    @Override
    public void addListener(TransitionBeginListener<T, S, E, C> listener) {
        addListener(TransitionBeginEvent.class, listener, TRANSITION_BEGIN_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(TransitionBeginListener<T, S, E, C> listener) {
        removeListener(TransitionBeginEvent.class, listener, TRANSITION_BEGIN_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_COMPLETE_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionCompleteListener.class, 
                    "transitionComplete", new Class<?>[]{TransitionCompleteEvent.class});

    @Override
    public void addListener(TransitionCompleteListener<T, S, E, C> listener) {
        addListener(TransitionCompleteEvent.class, listener, TRANSITION_COMPLETE_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(TransitionCompleteListener<T, S, E, C> listener) {
        removeListener(TransitionCompleteEvent.class, listener, TRANSITION_COMPLETE_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_EXCEPTION_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionExceptionListener.class, 
                    "transitionException", new Class<?>[]{TransitionExceptionEvent.class});

    @Override
    public void addListener(TransitionExceptionListener<T, S, E, C> listener) {
        addListener(TransitionExceptionEvent.class, listener, TRANSITION_EXCEPTION_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(TransitionExceptionListener<T, S, E, C> listener) {
        removeListener(TransitionExceptionEvent.class, listener, TRANSITION_EXCEPTION_EVENT_METHOD);
    }
    
    private static final Method TRANSITION_DECLINED_EVENT_METHOD = 
            ReflectUtils.getMethod(TransitionDeclinedListener.class, 
                    "transitionDeclined", new Class<?>[]{TransitionDeclinedEvent.class});

    @Override
    public void addListener(TransitionDeclinedListener<T, S, E, C> listener) {
        addListener(TransitionDeclinedEvent.class, listener, TRANSITION_DECLINED_EVENT_METHOD);
    }
    
    @Override
    public void removeListener(TransitionDeclinedListener<T, S, E, C> listener) {
        removeListener(TransitionDeclinedEvent.class, listener, TRANSITION_DECLINED_EVENT_METHOD);
    }
    
    public static abstract class AbstractStateMachineEvent<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractItemEvent<T, Object> implements StateMachine.StateMachineEvent<T, S, E, C> {
        public AbstractStateMachineEvent(T source) {
            super(source);
        }
        
        @Override
        public T getStateMachine() {
            return getSourceItem();
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
