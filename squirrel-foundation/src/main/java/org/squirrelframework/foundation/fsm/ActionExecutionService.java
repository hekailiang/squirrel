package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.Observable;
import org.squirrelframework.foundation.event.SquirrelEvent;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.util.ReflectUtils;

import java.lang.reflect.Method;

/**
 * State machine action executor. The action defined during state entry/exit and transition will be 
 * collected by action executor, and executed later together. The executor can execute actions in 
 * synchronize and synchronize manner.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface ActionExecutionService<T extends StateMachine<T, S, E, C>, S, E, C> extends Observable {
    /**
     * Begin a action execution collection in the bucket.
     */
    void begin(String bucketName);

    /**
     * Execute all the actions collected by front bucket.
     */
    void execute();

    /**
     * Reset all deferred actions and its count
     */
    void reset();

    /**
     * Set dummy execution true will cause no action being actually invoked when calling {@link ActionExecutionService#execute()}.
     *
     * @param dummyExecution
     */
    void setDummyExecution(boolean dummyExecution);

    /**
     * Add action and all the execution parameters into execution context;
     *
     * @param action activity to be executed
     * @param from source state
     * @param to target state
     * @param event activity cause
     * @param context external environment context
     * @param stateMachine state machine reference
     */
    void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine);

    /**
     * Add before action execution listener which can be used for monitoring execution
     * @param listener action execution listener
     */
    void addExecActionListener(BeforeExecActionListener<T, S, E, C> listener);

    /**
     * Remove before action execution listener
     * @param listener action execution listener
     */
    void removeExecActionListener(BeforeExecActionListener<T, S, E, C> listener);

    /**
     * Add after action execution listener which can be used for monitoring execution
     * @param listener action execution listener
     */
    void addExecActionListener(AfterExecActionListener<T, S, E, C> listener);
    
    /**
     * Remove after action execution listener
     * @param listener action execution listener
     */
    void removeExecActionListener(AfterExecActionListener<T, S, E, C> listener);

    /**
     * Add action execution exception listener which can be used for monitoring execution
     * @param listener action execution exception listener
     */
    void addExecActionExceptionListener(ExecActionExceptionListener<T, S, E, C> listener);
    
    /**
     * Remove action execution exception listener
     * @param listener action execution exception listener
     */
    void removeExecActionExceptionListener(ExecActionExceptionListener<T, S, E, C> listener);

    /**
     * Action execution event
     */
    public interface ActionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelEvent {
        Action<T, S, E, C> getExecutionTarget();
        S getFrom();
        S getTo();
        E getEvent();
        C getContext();
        T getStateMachine();
        int[] getMOfN();
    }

    public interface BeforeExecActionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends ActionEvent<T, S, E, C> {}

    /**
     * Before Action execution listener
     */
    public interface BeforeExecActionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "beforeExecute";
        public static final Method METHOD = ReflectUtils.getMethod(
                BeforeExecActionListener.class, METHOD_NAME, new Class<?>[]{BeforeExecActionEvent.class});
        void beforeExecute(BeforeExecActionEvent<T, S, E, C> event);
    }

    public interface AfterExecActionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends ActionEvent<T, S, E, C> {}
    
    /**
     * After Action execution listener
     */
    public interface AfterExecActionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "afterExecute";
        public static final Method METHOD = ReflectUtils.getMethod(
                AfterExecActionListener.class, METHOD_NAME, new Class<?>[]{AfterExecActionEvent.class});
        void afterExecute(AfterExecActionEvent<T, S, E, C> event);
    }

    public interface ExecActionExceptionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends ActionEvent<T, S, E, C> {
        TransitionException getException();
    }

    public interface ExecActionExceptionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        public static final String METHOD_NAME = "executeException";
        public static final Method METHOD = ReflectUtils.getMethod(
                ExecActionExceptionListener.class, METHOD_NAME, 
                new Class<?>[]{ExecActionExceptionEvent.class});
        void executeException(ExecActionExceptionEvent<T, S, E, C> event);
    }
}
