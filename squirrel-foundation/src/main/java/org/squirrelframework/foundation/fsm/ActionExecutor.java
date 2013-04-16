package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.event.SquirrelEvent;

public interface ActionExecutor<T extends StateMachine<T, S, E, C>, S, E, C> {
	
	void begin();
	
	void execute();
	
	void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine);
	
	void addListener(ExecActionLisenter<T, S, E, C> listener);
	
	void removeListener(ExecActionLisenter<T, S, E, C> listener);
	
	interface ExecActionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelEvent {
		Action<T, S, E, C> getExecutionTarget();
		S getFrom();
		S getTo();
		E getEvent();
		C getContext();
		T getStateMachine();
		int[] getMOfN();
	}
	
	interface ExecActionLisenter<T extends StateMachine<T, S, E, C>, S, E, C> {
		void beforeExecute(ExecActionEvent<T, S, E, C> event);
	}
}
