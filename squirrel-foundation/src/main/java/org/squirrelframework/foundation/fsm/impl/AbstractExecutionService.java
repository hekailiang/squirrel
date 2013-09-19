package org.squirrelframework.foundation.fsm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.squirrelframework.foundation.component.impl.AbstractSubject;
import org.squirrelframework.foundation.exception.ErrorCodes;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ActionExecutionService;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.base.Preconditions;

public abstract class AbstractExecutionService<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractSubject implements ActionExecutionService<T, S, E, C> {

    protected final Stack<List<ExectionContext<T, S, E, C>>> stack = new Stack<List<ExectionContext<T, S, E, C>>>();
    
    protected boolean dummyExecution = false;
    
    @Override
    public void begin() {
        List<ExectionContext<T, S, E, C>> executionContext = new ArrayList<ExectionContext<T, S, E, C>>();
        stack.push(executionContext);
    }
    
    @Override
    public void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
        Preconditions.checkNotNull(action);
        stack.peek().add(ExectionContext.get(action, from, to, event, context, stateMachine));
    }
    
    @Override
    public void execute() {
        List<ExectionContext<T, S, E, C>> executionContexts = stack.pop();
        for (int i=0, size=executionContexts.size(); i<size; ++i) {
            fireEvent(ExecActionEventImpl.get(i+1, size, executionContexts.get(i)));
            if(!dummyExecution) {
                executionContexts.get(i).run();
            }
        }
    }
    
    @Override
    public void addExecActionListener(ExecActionLisenter<T, S, E, C> listener) {
        addListener(ExecActionEvent.class, listener, ExecActionLisenter.EXECUTOR_EVENT_METHOD);
    }
    
    @Override
    public void removeExecActionListener(ExecActionLisenter<T, S, E, C> listener) {
        removeListener(ExecActionEvent.class, listener);
    }
    
    @Override
    public void setDummyExecution(boolean dummyExecution) {
        this.dummyExecution = dummyExecution;
    }
    
    static class ExecActionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements ExecActionEvent<T, S, E, C> {
        private ExectionContext<T, S, E, C> executionContext;
        private int pos;
        private int size;
        
        ExecActionEventImpl(int pos, int size, ExectionContext<T, S, E, C> executionContext) {
            this.pos = pos;
            this.size = size;
            this.executionContext = executionContext;
        }
        
        static <T extends StateMachine<T, S, E, C>, S, E, C> ExecActionEvent<T, S, E, C> get(
                int pos, int size, ExectionContext<T, S, E, C> executionContext) {
            return new ExecActionEventImpl<T, S, E, C>(pos, size, executionContext);
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
    
    static class ExectionContext<T extends StateMachine<T, S, E, C>, S, E, C> {
        final Action<T, S, E, C> action;
        final S from;
        final S to;
        final E event;
        final C context;
        final T stateMachine;
        
        private ExectionContext(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
            this.action = action;
            this.from = from;
            this.to = to;
            this.event = event;
            this.context = context;
            this.stateMachine = stateMachine;
        }
        
        static <T extends StateMachine<T, S, E, C>, S, E, C> ExectionContext<T, S, E, C> get(
                Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
            return new ExectionContext<T, S, E, C>(action, from, to, event, context, stateMachine);
        }

        public void run() {
            try {
                action.execute(from, to, event, context, stateMachine);
            } catch (Exception e) {
                // wrapper any exception into transition exception
                throw new TransitionException(e, ErrorCodes.FSM_TRANSITION_ERROR, 
                        from, to, event, context, stateMachine);
            }
        }
    }
}
