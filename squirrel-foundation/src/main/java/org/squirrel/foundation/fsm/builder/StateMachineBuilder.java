package org.squirrel.foundation.fsm.builder;

import org.squirrel.foundation.fsm.StateMachine;

public interface StateMachineBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {
    TransitionBuilder<T, S, E, C> transition();
    
    void defineState(S stateId);
    
    EntryExitActionBuilder<T, S, E, C> onEntry(S stateId);
    
    EntryExitActionBuilder<T, S, E, C> onExit(S stateId);
    
    T newStateMachine(S initialStateId, T parent, Class<?> type, boolean isLeaf, Object... extraParams);
}
