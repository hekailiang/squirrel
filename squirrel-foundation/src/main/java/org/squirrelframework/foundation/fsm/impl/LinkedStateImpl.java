package org.squirrelframework.foundation.fsm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ImmutableLinkedState;
import org.squirrelframework.foundation.fsm.MutableLinkedState;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionDeclinedEvent;
import org.squirrelframework.foundation.fsm.StateMachineStatus;

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
    
    private StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachine;
    
    private Action<T, S, E, C> lastEntryAction = new Action<T, S, E, C>() {
        @Override
        public void execute(S from, S to, E event, C context, T stateMachine) {
            linkedStateMachine.start(context);
        }
    };
    
    private Action<T, S, E, C> firstExitAction = new Action<T, S, E, C>() {
        @Override
        public void execute(S from, S to, E event, C context, T stateMachine) {
            linkedStateMachine.terminate(context);
        }
    };

    LinkedStateImpl(S stateId) {
        super(stateId);
    }

    @Override
    public void setLinkedStateMachine(StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateMachine) {
        this.linkedStateMachine = linkedStateMachine;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" }) // TODO-hhe: check type safety
    @Override
    public void internalFire(StateContext<T, S, E, C> stateContext) {
        if(linkedStateMachine.getStatus()==StateMachineStatus.TERMINATED) {
            // if linked state machine entered its final state, then outside state will process event, 
            super.internalFire(stateContext);
        } else {
            // otherwise the linked state machine will try to process event first and only handle event 
            // to outside state when event was declined by linked state machine.
            DeclineEventHandler declinedEventHandler = new DeclineEventHandler(stateContext);
            // add declined event listener
            linkedStateMachine.addTransitionDeclinedListener(declinedEventHandler);
            
            // delegate the event to linked state machine process
            linkedStateMachine.fire(stateContext.getEvent(), stateContext.getContext());
            
            // remove declined event listener
            linkedStateMachine.removeTransitionDecleindListener(declinedEventHandler);
        }
    }

    @Override
    public StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> getLinkedStateMachine() {
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
        if(linkedStateMachine==null) {
            throw new RuntimeException("Linked state machine cannot be null.");
        }
        
        if(isParallelState() || hasChildStates()) {
            throw new RuntimeException("Linked state cannot be parallel state or has any child states.");
        }
        super.verify();
    }

}
