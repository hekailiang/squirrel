package org.squirrel.foundation.fsm;

import org.squirrel.foundation.data.HierarchyItem;

public interface StateMachine<T extends StateMachine<T, S, E, C>, S, E, C> extends HierarchyItem<T, Object>, Visitable<T, S, E, C> {
    
    public static final int TRANSITION_NOT_STARTED = -1;
    
    public static final int TRANSITION_INITIALIED = 0;
    
    public static final int TRANSITION_STARTED = 1;
    
    public static final int CHILD_STATES_EXITED = 2;
    
    public static final int STATE_EXITED = 3;
    
    public static final int TRANSITION_DONE = 4;
    
    public static final int STATE_ENTERED = 5;
    
    public static final int TRANSITION_FINALIZED = 6;
    
    void fire(E event, C context);
    
    S getCurrentState();
    
    ImmutableState<T, S, E, C> getRawStateFrom(S state);
    
    ImmutableState<T, S, E, C> getCurrentRawState();
    
    S getInitialState();
    
    void start();
    
    void terminate();
    
    StateMachineStatus getStatus();
    
    interface StateMachineListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void stateMachineEvent(StateMachineEvent<T, S, E, C> event);
    }
    interface StateMachineEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends ItemEvent<T, Object> {
        T getStateMachine();
    }
    void addListener(StateMachineListener<T, S, E, C> listener);
    void removeListener(StateMachineListener<T, S, E, C> listener);
    
    interface StartListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void started(StartEvent<T, S, E, C> event);
    }
    interface StartEvent <T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {}
    void addListener(StartListener<T, S, E, C> listener);
    void removeListener(StartListener<T, S, E, C> listener);
    
    interface TerminateListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void terminated(TerminateEvent<T, S, E, C> event);
    }
    interface TerminateEvent <T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {}
    void addListener(TerminateListener<T, S, E, C> listener);
    void removeListener(TerminateListener<T, S, E, C> listener);
    
    interface StateMachineExceptionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void stateMachineException(StateMachineExceptionEvent<T, S, E, C> event);
    }
    interface StateMachineExceptionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {
        Exception getException();
    }
    void addListener(StateMachineExceptionListener<T, S, E, C> listener);
    void removeListener(StateMachineExceptionListener<T, S, E, C> listener);
    
    interface TransitionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends StateMachineEvent<T, S, E, C> {
        S getSourceState();
        E getCause();
        C getContext();
    }
    
    interface TransitionBeginListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionBegin(TransitionBeginEvent<T, S, E, C> event);
    }
    interface TransitionBeginEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {}
    void addListener(TransitionBeginListener<T, S, E, C> listener);
    void removeListener(TransitionBeginListener<T, S, E, C> listener);
    
    interface TransitionCompleteListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionComplete(TransitionCompleteEvent<T, S, E, C> event);
    }
    interface TransitionCompleteEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {
        S getTargetState();
    }
    void addListener(TransitionCompleteListener<T, S, E, C> listener);
    void removeListener(TransitionCompleteListener<T, S, E, C> listener);
    
    interface TransitionExceptionListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionException(TransitionExceptionEvent<T, S, E, C> event);
    }
    interface TransitionExceptionEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {
        S getTargetState();
        Exception getException();
        int getTransitionStatus();
    }
    void addListener(TransitionExceptionListener<T, S, E, C> listener);
    void removeListener(TransitionExceptionListener<T, S, E, C> listener);
    
    interface TransitionDeclinedListener<T extends StateMachine<T, S, E, C>, S, E, C> {
        void transitionDeclined(TransitionDeclinedEvent<T, S, E, C> event);
    }
    interface TransitionDeclinedEvent<T extends StateMachine<T, S, E, C>, S, E, C> extends TransitionEvent<T, S, E, C> {}
    void addListener(TransitionDeclinedListener<T, S, E, C> listener);
    void removeListener(TransitionDeclinedListener<T, S, E, C> listener);
}
