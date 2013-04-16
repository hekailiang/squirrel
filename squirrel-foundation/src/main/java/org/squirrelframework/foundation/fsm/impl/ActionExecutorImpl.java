package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.squirrelframework.foundation.component.impl.AbstractSubject;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ActionExecutor;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.util.ReflectUtils;

import com.google.common.base.Preconditions;

class ActionExecutorImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractSubject implements ActionExecutor<T, S, E, C> {
	
	protected final Stack<List<ExectionContext<T, S, E, C>>> stack = new Stack<List<ExectionContext<T, S, E, C>>>();
	
	@Override
    public void begin() {
		List<ExectionContext<T, S, E, C>> executionContext = new ArrayList<ExectionContext<T, S, E, C>>();
        stack.push(executionContext);
    }

	@Override
    public void execute() {
		List<ExectionContext<T, S, E, C>> executionContexts = stack.pop();
        for (int i=0, size=executionContexts.size(); i<size; ++i) {
        	fireEvent(new ExecutorEventImpl<T, S, E, C>(i+1, size, executionContexts.get(i)));
        	executionContexts.get(i).run();
        }
    }

	@Override
    public void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
		Preconditions.checkNotNull(action);
        stack.peek().add(ExectionContext.get(action, from, to, event, context, stateMachine));
    }
	
	private static final Method EXECUTOR_EVENT_METHOD = 
            ReflectUtils.getMethod(ExecutorLisenter.class, "beforeExecute", new Class<?>[]{ExecutorEvent.class});
	
	@Override
    public void addListener(ExecutorLisenter<T, S, E, C> listener) {
		addListener(ExecutorEvent.class, listener, EXECUTOR_EVENT_METHOD);
    }
	
	@Override
	public void removeListener(ExecutorLisenter<T, S, E, C> listener) {
		removeListener(ExecutorEvent.class, listener);
	}
	
	static class ExecutorEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements ExecutorEvent<T, S, E, C> {
		private ExectionContext<T, S, E, C> executionContext;
		private int pos;
		private int size;
		
		ExecutorEventImpl(int pos, int size, ExectionContext<T, S, E, C> executionContext) {
			this.pos = pos;
			this.size = size;
			this.executionContext = executionContext;
		}

		@Override
        public Action<T, S, E, C> getExecutionTarget() {
	        return executionContext.getExecutionTarget();
        }

		@Override
        public S getFrom() {
	        return executionContext.getFrom();
        }

		@Override
        public S getTo() {
	        return executionContext.getTo();
        }

		@Override
        public E getEvent() {
	        return executionContext.getEvent();
        }

		@Override
        public C getContext() {
	        return executionContext.getContext();
        }

		@Override
        public T getStateMachine() {
	        return executionContext.getStateMachine();
        }

		@Override
        public int[] getMOfN() {
	        return new int[]{pos, size};
        }
	}
	
	static class ExectionContext<T extends StateMachine<T, S, E, C>, S, E, C> {
		private final Action<T, S, E, C> action;
		private final S from;
		private final S to;
		private final E event;
		private final C context;
		private final T stateMachine;
		
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

		public Action<T, S, E, C> getExecutionTarget() {
	        return action;
        }
		
		public S getFrom() {
	        return from;
        }

		public S getTo() {
	        return to;
        }

		public E getEvent() {
	        return event;
        }

		public C getContext() {
	        return context;
        }

		public T getStateMachine() {
	        return stateMachine;
        }
		
		public void run() {
			action.execute(from, to, event, context, stateMachine);
		}
	}
}
