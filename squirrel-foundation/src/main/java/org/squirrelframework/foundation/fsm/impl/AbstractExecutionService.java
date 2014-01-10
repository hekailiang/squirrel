package org.squirrelframework.foundation.fsm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.squirrelframework.foundation.component.impl.AbstractSubject;
import org.squirrelframework.foundation.exception.ErrorCodes;
import org.squirrelframework.foundation.exception.SquirrelRuntimeException;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ActionExecutionService;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.base.Preconditions;

public abstract class AbstractExecutionService<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractSubject implements ActionExecutionService<T, S, E, C> {

    protected final Stack<List<ActionContext<T, S, E, C>>> stack = new Stack<List<ActionContext<T, S, E, C>>>();
    
    protected boolean dummyExecution = false;
    
    @Override
    public void begin() {
        List<ActionContext<T, S, E, C>> actionContext = new ArrayList<ActionContext<T, S, E, C>>();
        stack.push(actionContext);
    }
    
    @Override
    public void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
        Preconditions.checkNotNull(action);
        stack.peek().add(ActionContext.get(action, from, to, event, context, stateMachine));
    }
    
    @Override
    public void execute() {
        List<ActionContext<T, S, E, C>> actionContexts = stack.pop();
        for (int i=0, size=actionContexts.size(); i<size; ++i) {
            if(!dummyExecution) {
                ActionContext<T, S, E, C> actionContext = actionContexts.get(i);
                try {
                    ((AbstractStateMachine<T, S, E, C>)actionContext.stateMachine).beforeActionInvoked(
                            actionContext.from, actionContext.to, actionContext.event, actionContext.context);
                    fireEvent(ExecActionEventImpl.get(i+1, size, actionContext));
                    actionContext.run();
                } catch (Exception e) {
                    Throwable t = (e instanceof SquirrelRuntimeException) ?
                            ((SquirrelRuntimeException)e).getTargetException() : e;
                    // wrap any exception into transition exception
                    TransitionException te = new TransitionException(t, ErrorCodes.FSM_TRANSITION_ERROR, 
                            new Object[]{actionContext.from, actionContext.to, actionContext.event, 
                            actionContext.context, actionContext.action.name(), e.getMessage()});
                    fireEvent(new ExecActionExceptionEventImpl<T, S, E, C>(te, i+1, size, actionContext));
                    throw te;
                } 
            }
        }
    }
    
    @Override
    public void addExecActionListener(ExecActionListener<T, S, E, C> listener) {
        addListener(ExecActionEvent.class, listener, ExecActionListener.METHOD);
    }
    
    @Override
    public void removeExecActionListener(ExecActionListener<T, S, E, C> listener) {
        removeListener(ExecActionEvent.class, listener);
    }
    
    @Override
    public void addExecActionExceptionListener(ExecActionExceptionListener<T, S, E, C> listener) {
        addListener(ExecActionExceptionEvent.class, listener, ExecActionExceptionListener.METHOD);
    }
    
    @Override
    public void removeExecActionExceptionListener(ExecActionExceptionListener<T, S, E, C> listener) {
        removeListener(ExecActionExceptionEvent.class, listener);
    }
    
    @Override
    public void setDummyExecution(boolean dummyExecution) {
        this.dummyExecution = dummyExecution;
    }
    
    static class ExecActionExceptionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
        extends AbstractExecActionEvent<T, S, E, C> implements ExecActionExceptionEvent<T, S, E, C> {
        
        private final TransitionException e;

        ExecActionExceptionEventImpl(TransitionException e, int pos, int size, ActionContext<T, S, E, C> actionContext) {
            super(pos, size, actionContext);
            this.e = e;
        }

        @Override
        public TransitionException getException() {
            return e;
        }
        
    }
    
    static class ExecActionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
        extends AbstractExecActionEvent<T, S, E, C> implements ExecActionEvent<T, S, E, C> {
        
        ExecActionEventImpl(int pos, int size, ActionContext<T, S, E, C> actionContext) {
            super(pos, size, actionContext);
        }

        static <T extends StateMachine<T, S, E, C>, S, E, C> ExecActionEvent<T, S, E, C> get(
                int pos, int size, ActionContext<T, S, E, C> actionContext) {
            return new ExecActionEventImpl<T, S, E, C>(pos, size, actionContext);
        }
    }
    
    static abstract class AbstractExecActionEvent<T extends StateMachine<T, S, E, C>, S, E, C> implements ActionEvent<T, S, E, C> {
        private ActionContext<T, S, E, C> executionContext;
        private int pos;
        private int size;
        
        AbstractExecActionEvent(int pos, int size, ActionContext<T, S, E, C> actionContext) {
            this.pos = pos;
            this.size = size;
            this.executionContext = actionContext;
        }
        
        @Override
        public Action<T, S, E, C> getExecutionTarget() {
            return executionContext.action;
        }

        @Override
        public S getFrom() {
            return executionContext.from;
        }

        @Override
        public S getTo() {
            return executionContext.to;
        }

        @Override
        public E getEvent() {
            return executionContext.event;
        }

        @Override
        public C getContext() {
            return executionContext.context;
        }

        @Override
        public T getStateMachine() {
            return executionContext.stateMachine;
        }

        @Override
        public int[] getMOfN() {
            return new int[]{pos, size};
        }
    }
    
    static class ActionContext<T extends StateMachine<T, S, E, C>, S, E, C> {
        final Action<T, S, E, C> action;
        final S from;
        final S to;
        final E event;
        final C context;
        final T stateMachine;
        
        private ActionContext(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
            this.action = action;
            this.from = from;
            this.to = to;
            this.event = event;
            this.context = context;
            this.stateMachine = stateMachine;
        }
        
        static <T extends StateMachine<T, S, E, C>, S, E, C> ActionContext<T, S, E, C> get(
                Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
            return new ActionContext<T, S, E, C>(action, from, to, event, context, stateMachine);
        }

        void run() {
            action.execute(from, to, event, context, stateMachine);
        }
    }
}
