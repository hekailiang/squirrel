package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ActionExecutionService;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.MutableLinkedState;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTimedState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.MvelScriptManager;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineData;
import org.squirrelframework.foundation.fsm.TransitionResult;
import org.squirrelframework.foundation.fsm.TransitionType;
import org.squirrelframework.foundation.fsm.builder.DeferBoundActionBuilder;
import org.squirrelframework.foundation.fsm.builder.EntryExitActionBuilder;
import org.squirrelframework.foundation.fsm.builder.ExternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.InternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.LocalTransitionBuilder;
import org.squirrelframework.foundation.util.TypeReference;

abstract class FSM {

    static <T extends StateMachine<T, S, E, C>, S, E, C> StateContext<T, S, E, C> newStateContext(
            StateMachine<T, S, E, C> stateMachine, StateMachineData<T, S, E, C> data,
            ImmutableState<T, S, E, C> sourceState, E event, C context, 
            TransitionResult<T, S, E, C> result, ActionExecutionService<T, S, E, C> executor) {
        return new StateContextImpl<T, S, E, C>(stateMachine, data, sourceState, event, context, result, executor);
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableTransition<T, S, E, C> newTransition() {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionImpl<T, S, E, C>>() {});
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableState<T, S, E, C> newState(S stateId) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<StateImpl<T, S, E, C>>() {}, 
                new Class[] { Object.class }, new Object[] { stateId });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableLinkedState<T, S, E, C> newLinkedState(S stateId) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<LinkedStateImpl<T, S, E, C>>() {}, 
                new Class[] { Object.class }, new Object[] { stateId });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableTimedState<T, S, E, C> newTimedState(S stateId) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TimedStateImpl<T, S, E, C>>() {}, 
                new Class[] { Object.class }, new Object[] { stateId });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableState<T, S, E, C> getState(
            Map<S, MutableState<T, S, E, C>> states, S stateId) {
        MutableState<T, S, E, C> state = states.get(stateId);
        if (state == null) {
            state = FSM.newState(stateId);
            states.put(stateId, state);
        }
        return state;
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> DeferBoundActionBuilder<T, S, E, C> newDeferBoundActionBuilder(
            List<DeferBoundActionInfo<T, S, E, C>> deferBoundActionInfoList, ExecutionContext executionContext
            ) {
        return SquirrelProvider.getInstance().newInstance( new TypeReference<DeferBoundActionBuilderImpl<T, S, E, C>>(){}, 
                new Class[]{List.class, ExecutionContext.class}, new Object[]{deferBoundActionInfoList, executionContext} );
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> ExternalTransitionBuilder<T, S, E, C> newExternalTransitionBuilder(
            Map<S, MutableState<T, S, E, C>> states, int priority, ExecutionContext executionContext) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { Map.class, TransitionType.class, int.class, ExecutionContext.class }, 
                new Object[] { states, TransitionType.EXTERNAL, priority, executionContext });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> LocalTransitionBuilder<T, S, E, C> newLocalTransitionBuilder(
            Map<S, MutableState<T, S, E, C>> states, int priority, ExecutionContext executionContext) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { Map.class, TransitionType.class, int.class, ExecutionContext.class }, 
                new Object[] { states, TransitionType.LOCAL, priority, executionContext });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> InternalTransitionBuilder<T, S, E, C> newInternalTransitionBuilder(
            Map<S, MutableState<T, S, E, C>> states, int priority, ExecutionContext executionContext) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { Map.class, TransitionType.class, int.class, ExecutionContext.class }, 
                new Object[] { states, TransitionType.INTERNAL, priority, executionContext });
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> EntryExitActionBuilder<T, S, E, C> newEntryExitActionBuilder(
            MutableState<T, S, E, C> state, boolean isEntryAction, ExecutionContext executionContext) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<EntryExitActionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { MutableState.class, boolean.class, ExecutionContext.class}, 
                new Object[] { state, isEntryAction, executionContext});
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> MethodCallActionImpl<T, S, E, C> newMethodCallAction(
            Method method, int weight, ExecutionContext executionContext) {
        return SquirrelProvider.getInstance().newInstance( 
                new TypeReference<MethodCallActionImpl<T, S, E, C>>() {}, 
                new Class[] { Method.class, int.class, ExecutionContext.class }, 
                new Object[] { method, weight, executionContext } );
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> MethodCallActionProxyImpl<T, S, E, C> newMethodCallActionProxy(
            String methodName, ExecutionContext executionContext) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<MethodCallActionProxyImpl<T, S, E, C>>() {}, 
                new Class[] { String.class, ExecutionContext.class }, new Object[] { methodName, executionContext });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> Actions<T, S, E, C> newActions() {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<Actions<T, S, E, C>>() {});
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> TransitionResult<T, S, E, C> newResult(
			boolean accepted, ImmutableState<T, S, E, C> targetState, TransitionResult<T, S, E, C> parent) {
		return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionResult<T, S, E, C>>() {}).
				setAccepted(accepted).setTargetState(targetState).setParent(parent);
	}
    
    static <C> Condition<C> newMvelCondition(String expression, MvelScriptManager scriptManager) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<MvelConditionImpl<C>>() {}, 
                new Class<?>[]{String.class, MvelScriptManager.class}, new Object[]{expression, scriptManager});
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> Action<T, S, E, C> newMvelAction(
            String expression, ExecutionContext executionContext) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<MvelActionImpl<T, S, E, C>>() {}, 
                new Class<?>[]{String.class, ExecutionContext.class}, new Object[]{expression, executionContext});
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> StateMachineData<T, S, E, C> newStateMachineData(
            Map<S, ? extends ImmutableState<T, S, E, C>> states) {
        return SquirrelProvider.getInstance().newInstance( 
                new TypeReference<StateMachineData<T, S, E, C>>(){}, 
                new Class[]{Map.class}, new Object[]{states} );
    }
}
