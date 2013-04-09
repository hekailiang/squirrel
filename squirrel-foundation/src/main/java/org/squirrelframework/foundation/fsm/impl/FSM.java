package org.squirrelframework.foundation.fsm.impl;

import java.lang.reflect.Method;
import java.util.Map;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.TransitionType;
import org.squirrelframework.foundation.fsm.builder.EntryExitActionBuilder;
import org.squirrelframework.foundation.fsm.builder.ExternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.InternalTransitionBuilder;
import org.squirrelframework.foundation.fsm.builder.LocalTransitionBuilder;
import org.squirrelframework.foundation.util.TypeReference;

final class FSM {

    private FSM() {
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> StateContext<T, S, E, C> newStateContext(
            T stateMachine, ImmutableState<T, S, E, C> sourceState, E event, C context) {
        return new StateContextImpl<T, S, E, C>(stateMachine, sourceState, event, context);
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableTransition<T, S, E, C> newTransition() {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionImpl<T, S, E, C>>() {});
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableState<T, S, E, C> newState(S stateId) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<StateImpl<T, S, E, C>>() {}, 
                new Class[] { Object.class }, new Object[] { stateId });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> MutableState<T, S, E, C> newFinalState(S stateId) {
    	return SquirrelProvider.getInstance().newInstance(new TypeReference<FinalStateImpl<T, S, E, C>>() {}, 
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

    static <T extends StateMachine<T, S, E, C>, S, E, C> ExternalTransitionBuilder<T, S, E, C> newExternalTransitionBuilder(
            Map<S, MutableState<T, S, E, C>> states) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { Map.class, TransitionType.class }, new Object[] { states, TransitionType.EXTERNAL });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> LocalTransitionBuilder<T, S, E, C> newLocalTransitionBuilder(
            Map<S, MutableState<T, S, E, C>> states) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { Map.class, TransitionType.class }, new Object[] { states, TransitionType.LOCAL });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> InternalTransitionBuilder<T, S, E, C> newInternalTransitionBuilder(
            Map<S, MutableState<T, S, E, C>> states) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<TransitionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { Map.class, TransitionType.class }, new Object[] { states, TransitionType.INTERNAL });
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> EntryExitActionBuilder<T, S, E, C> newEntryExitActionBuilder(
            MutableState<T, S, E, C> state, boolean isEntryAction) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<EntryExitActionBuilderImpl<T, S, E, C>>() {}, 
                new Class[] { MutableState.class, boolean.class }, new Object[] { state, isEntryAction });
    }

    static <T extends StateMachine<T, S, E, C>, S, E, C> MethodCallActionImpl<T, S, E, C> newMethodCallAction(
            Method method) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<MethodCallActionImpl<T, S, E, C>>() {}, 
                new Class[] { Method.class }, new Object[] { method });
    }
    
    static <T extends StateMachine<T, S, E, C>, S, E, C> Actions<T, S, E, C> newActions() {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<Actions<T, S, E, C>>() {});
    }
}
