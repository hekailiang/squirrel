package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.event.SquirrelEvent;

public interface ActionExecutor<T extends StateMachine<T, S, E, C>, S, E, C> {
	
	void begin();
	
	void execute();
	
	void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine);
	
	void addListener(ExecutorLisenter<T, S, E, C> listener);
	
	void removeListener(ExecutorLisenter<T, S, E, C> listener);
	
	interface ExecutorEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelEvent {
		Action<T, S, E, C> getExecutionTarget();
		S getFrom();
		S getTo();
		E getEvent();
		C getContext();
		T getStateMachine();
		int[] getMOfN();
	}
	
	interface ExecutorLisenter<T extends StateMachine<T, S, E, C>, S, E, C> {
		void beforeExecute(ExecutorEvent<T, S, E, C> event);
	}
}
