package org.squirrelframework.foundation.fsm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.squirrelframework.foundation.component.SquirrelInstanceProvider;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.AnonymousAction;
import org.squirrelframework.foundation.fsm.ImmutableLinkedState;
import org.squirrelframework.foundation.fsm.MutableLinkedState;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineContext;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionDeclinedEvent;
import org.squirrelframework.foundation.fsm.StateMachineStatus;

import com.google.common.collect.Maps;

class LinkedStateImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends StateImpl<T, S, E, C> 
    implements ImmutableLinkedState<T, S, E, C>, MutableLinkedState<T, S, E, C> {
    
    class DeclineEventHandler<M> implements StateMachine.TransitionDeclinedListener<T, S, E, C> {
        
        private StateContext<T, S, E, C> orgStateContext;
        
        DeclineEventHandler(StateContext<T, S, E, C> orgStateContext) {
            this.orgStateContext = orgStateContext;
        }
        
        @Override
        public void transitionDeclined(TransitionDeclinedEvent<T, S, E, C> event) {
            LinkedStateImpl.super.internalFire(orgStateContext);
        }
    }
    
    private SquirrelInstanceProvider<? extends StateMachine<?, S, E, C>> provider;
    
    private Map<String, StateMachine<? extends StateMachine<?, S, E, C>, S, E, C>> 
        linkedStateMachineInstances = Maps.newConcurrentMap();
    
    private Action<T, S, E, C> lastEntryAction = new AnonymousAction<T, S, E, C>() {
        @Override
        public void execute(S from, S to, E event, C context, T stateMachine) {
            StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachine = 
                    getLinkedStateMachine(stateMachine);
            linkedStateMachine.start(context);
        }

        @Override
        public String name() {
            return "__LINK_STATE_ENTRY_ACTION";
        }
    };
    
    private Action<T, S, E, C> firstExitAction = new AnonymousAction<T, S, E, C>() {
        @Override
        public void execute(S from, S to, E event, C context, T stateMachine) {
            StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachine =
                    linkedStateMachineInstances.remove(getKey(stateMachine));
            if(linkedStateMachine!=null) {
                linkedStateMachine.terminate(context);
            }
        }

        @Override
        public String name() {
            return "__LINK_STATE_EXIT_ACTION";
        }
    };

    LinkedStateImpl(S stateId) {
        super(stateId);
    }

    @Override
    public void setLinkedStateMachineProvider(
            SquirrelInstanceProvider<? extends StateMachine<?, S, E, C>> provider) {
        this.provider = provider;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" }) // TODO-hhe: check type safety
    @Override
    public void internalFire(StateContext<T, S, E, C> stateContext) {
        StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> stateMachine = 
                linkedStateMachineInstances.get(getKey(stateContext.getStateMachine().getThis()));
        if(stateMachine.getStatus()==StateMachineStatus.TERMINATED) {
            // if linked state machine entered its final state, then outside state will process event, 
            super.internalFire(stateContext);
        } else {
            // otherwise the linked state machine will try to process event first and only handle event 
            // to outside state when event was declined by linked state machine.
            DeclineEventHandler declinedEventHandler = new DeclineEventHandler(stateContext);
            try {
                // add declined event listener
                stateMachine.addTransitionDeclinedListener(declinedEventHandler);
                // set child(linked) state machine context
                StateMachineContext.set(stateMachine.getThis(), StateMachineContext.isTestEvent());
                // delegate the event to linked state machine process
                stateMachine.fire(stateContext.getEvent(), stateContext.getContext());
            } finally {
                StateMachineContext.set(null);
                // remove declined event listener
                stateMachine.removeTransitionDecleindListener(declinedEventHandler);
            }
        }
    }

    @Override
    public StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> getLinkedStateMachine(T stateMachine) {
        String key = getKey(stateMachine);
        StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachine = 
                linkedStateMachineInstances.get(key);
        if(linkedStateMachine==null) {
            linkedStateMachine = provider.get();
            linkedStateMachineInstances.put(key, linkedStateMachine);
        }
        return linkedStateMachine;
    }
    
    @Override
    public List<Action<T, S, E, C>> getEntryActions() {
        List<Action<T, S, E, C>> actions = new ArrayList<Action<T, S, E, C>>();
        actions.addAll(entryActions.getAll());
        actions.add(lastEntryAction);
        return Collections.unmodifiableList(actions);
    }

    @Override
    public List<Action<T, S, E, C>> getExitActions() {
        List<Action<T, S, E, C>> actions = new ArrayList<Action<T, S, E, C>>();
        actions.add(firstExitAction);
        actions.addAll(exitActions.getAll());
        return Collections.unmodifiableList(actions);
    }
    
    @Override
    public void verify() {
        if(provider==null) {
            throw new IllegalStateException("Linked state machine provider cannot be null.");
        }
        
        if(isParallelState() || hasChildStates()) {
            throw new IllegalStateException("Linked state cannot be parallel state or has any child states.");
        }
        super.verify();
    }

}
