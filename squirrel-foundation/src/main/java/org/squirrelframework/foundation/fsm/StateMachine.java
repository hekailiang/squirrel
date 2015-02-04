package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.Observable;
import org.squirrelframework.foundation.event.SquirrelEvent;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.ActionExecutionService.BeforeExecActionListener;
import org.squirrelframework.foundation.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Interface for finite state machine.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface StateMachine<T extends StateMachine<T, S, E, C>, S, E, C> extends Visitable, Observable {
    
    /**
     * Fires the specified event
     * @param event the event
     * @param context external context
     */
    void fire(E event, C context);
    
    /**
     * Fires event with context immediately, if current state machine is busy, the next processing event 
     * is this event.
     * @param event the event
     * @param context external context
     */
    void fireImmediate(E event, C context);
    
    /**
     * Test transition result under circumstance
     * @param event test event
     * @param context text context
     * @return test transition result
     */
    S test(E event, C context);
    
    /**
     * Fire event
     * @param event
     */
    void fire(E event);
    
    /**
     * Fire event immediately
     * @param event
     */
    void fireImmediate(E event);
    
    /**
     * Test event
     * @param event
     */
    S test(E event);
    
    /**
     * @param event test event
     * @return true is passed in event is acceptable otherwise return false
     */
    boolean canAccept(E event);
    
    /**
     * Start state machine under external context
     * @param context external context
     */
    void start(C context);
    
    /**
     * Start state machine without context
     */
    void start();
    
    /**
     * Terminate state machine under external context
     * @param context external context
     */
    void terminate(C context);
    
    /**
     * Terminate state machine without context
     */
    void terminate();
    
    /**
     * @return current status of state machine
     */
    StateMachineStatus getStatus();
    
    /**
     * @return type-safe state machine instance
     */
    T getThis();
    
    /**
     * @return current state id of state machine
     */
    S getCurrentState();
    
    /**
     * @return last active state id of state machine
     */
    S getLastState();

    /**
     * @return id of state machine initial state
     */
    S getInitialState();
    
    /**
     * @param parentStateId id of parent state
     * @return last active child state of the parent state
     */
    S getLastActiveChildStateOf(S parentStateId);
    
    /**
     * @param parentStateId
     * @return sub state of parallel state
     */
    List<S> getSubStatesOn(S parentStateId);
    
    /**
     * @return current raw state of state machine
     */
    ImmutableState<T, S, E, C> getCurrentRawState();
    
    /**
     * @return last active raw state of state machine
     */
    ImmutableState<T, S, E, C> getLastRawState();
    
    /**
     * @return initial raw state of state machine
     */
    ImmutableState<T, S, E, C> getInitialRawState();
    
    ImmutableState<T, S, E, C> getRawStateFrom(S stateId);
    
    Collection<S> getAllStates();
    
    Collection<ImmutableState<T, S, E, C>> getAllRawStates();
    
    /**
     * Dump current state machine data. This operation can only be done when state machine status is 
     * {@link StateMachineStatus#IDLE}, otherwise null will be returned.
     * 
     * @return dumped state machine data reader
     */
    StateMachineData.Reader<T, S, E, C> dumpSavedData();
    
    /**
     * Load saved data for current state machine. The operation can only be done when state machine 
     * status is {@link StateMachineStatus#INITIALIZED} or {@link StateMachineStatus#TERMINATED}.
     * 
     * @param savedData provided saved data
     * @return true if load saved data success otherwise false
     */
    boolean loadSavedData(StateMachineData.Reader<T, S, E, C> savedData);
    
    /**
     * @return whether state machine is context sensitive
     */
    @Deprecated
    boolean isContextSensitive();
    
    Class<C> typeOfContext();
    
    Class<E> typeOfEvent();
    
    Class<S> typeOfState();
    
    TransitionException getLastException();
    
    String getIdentifier();
    
    String getDescription();
    
    String exportXMLDefinition(boolean beautifyXml);
    
    void addDeclarativeListener(Object listener);
    
    void removeDeclarativeListener(Object listener);
    
    boolean isRemoteMonitorEnabled();
    
    boolean isStarted();
    
    boolean isTerminated();
    
    boolean isError();
    
    interface StateMachineListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "stateMachineEvent";
        // leverage bridge method to call the method of actual listener
        public static final Method METHOD = ReflectUtils.getMethod(
                StateMachineListener.class, METHOD_NAME, new Class<?>[]{StateMachineEvent.class});
        
        void stateMachineEvent(StateMachineEvent<T, S, E, C> event);
    }
    interface StateMachineEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelEvent {
        T getStateMachine();
    }
    void addStateMachineListener(StateMachineListener<T, S, E, C> listener);
    void removeStateMachineListener(StateMachineListener<T, S, E, C> listener);
    
    interface StartListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "started";
        public static final Method METHOD = ReflectUtils.getMethod(
                StartListener.class, METHOD_NAME, new Class<?>[]{StartEvent.class});
        
        void started(StartEvent<T, S, E, C> event);
    }
    interface StartEvent <T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {}
    void addStartListener(StartListener<T, S, E, C> listener);
    void removeStartListener(StartListener<T, S, E, C> listener);
    
    interface TerminateListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "terminated";
        public static final Method METHOD = ReflectUtils.getMethod(
                TerminateListener.class, METHOD_NAME, new Class<?>[]{TerminateEvent.class});
        
        void terminated(TerminateEvent<T, S, E, C> event);
    }
    interface TerminateEvent <T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {}
    void addTerminateListener(TerminateListener<T, S, E, C> listener);
    void removeTerminateListener(TerminateListener<T, S, E, C> listener);
    
    interface StateMachineExceptionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "stateMachineException";
        public static final Method METHOD = ReflectUtils.getMethod(
                StateMachineExceptionListener.class, METHOD_NAME, new Class<?>[]{StateMachineExceptionEvent.class});
        
        void stateMachineException(StateMachineExceptionEvent<T, S, E, C> event);
    }
    interface StateMachineExceptionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {
        Exception getException();
    }
    void addStateMachineExceptionListener(StateMachineExceptionListener<T, S, E, C> listener);
    void removeStateMachineExceptionListener(StateMachineExceptionListener<T, S, E, C> listener);
    
    interface TransitionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {
        S getSourceState();
        E getCause();
        C getContext();
    }
    
    interface TransitionBeginListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "transitionBegin";
        public static final Method METHOD = ReflectUtils.getMethod(
                TransitionBeginListener.class, METHOD_NAME, new Class<?>[]{TransitionBeginEvent.class});
        
        void transitionBegin(TransitionBeginEvent<T, S, E, C> event);
    }
    interface TransitionBeginEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {}
    void addTransitionBeginListener(TransitionBeginListener<T, S, E, C> listener);
    void removeTransitionBeginListener(TransitionBeginListener<T, S, E, C> listener);
    
    interface TransitionCompleteListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "transitionComplete";
        public static final Method METHOD = ReflectUtils.getMethod(
                TransitionCompleteListener.class, METHOD_NAME, new Class<?>[]{TransitionCompleteEvent.class});
        
        void transitionComplete(TransitionCompleteEvent<T, S, E, C> event);
    }
    interface TransitionCompleteEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {
        S getTargetState();
    }
    void addTransitionCompleteListener(TransitionCompleteListener<T, S, E, C> listener);
    void removeTransitionCompleteListener(TransitionCompleteListener<T, S, E, C> listener);
    
    interface TransitionExceptionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "transitionException";
        public static final Method METHOD = ReflectUtils.getMethod(
                TransitionExceptionListener.class, METHOD_NAME, new Class<?>[]{TransitionExceptionEvent.class});
        
        void transitionException(TransitionExceptionEvent<T, S, E, C> event);
    }
    interface TransitionExceptionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {
        S getTargetState();
        TransitionException getException();
    }
    void addTransitionExceptionListener(TransitionExceptionListener<T, S, E, C> listener);
    void removeTransitionExceptionListener(TransitionExceptionListener<T, S, E, C> listener);
    
    interface TransitionDeclinedListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "transitionDeclined";
        public static final Method METHOD = ReflectUtils.getMethod(
                TransitionDeclinedListener.class, METHOD_NAME, new Class<?>[]{TransitionDeclinedEvent.class});
        
        void transitionDeclined(TransitionDeclinedEvent<T, S, E, C> event);
    }
    interface TransitionDeclinedEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {}
    void addTransitionDeclinedListener(TransitionDeclinedListener<T, S, E, C> listener);
    void removeTransitionDeclinedListener(TransitionDeclinedListener<T, S, E, C> listener);
    @Deprecated
    void removeTransitionDecleindListener(TransitionDeclinedListener<T, S, E, C> listener);
    
    interface TransitionEndListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "transitionEnd";
        public static final Method METHOD = ReflectUtils.getMethod(
                TransitionEndListener.class, METHOD_NAME, new Class<?>[]{TransitionEndEvent.class});
        
        void transitionEnd(TransitionEndEvent<T, S, E, C> event);
    }
    interface TransitionEndEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {
        S getTargetState();
    }
    void addTransitionEndListener(TransitionEndListener<T, S, E, C> listener);
    void removeTransitionEndListener(TransitionEndListener<T, S, E, C> listener);
    
    void addExecActionListener(BeforeExecActionListener<T, S, E, C> listener);
    void removeExecActionListener(BeforeExecActionListener<T, S, E, C> listener);
}
