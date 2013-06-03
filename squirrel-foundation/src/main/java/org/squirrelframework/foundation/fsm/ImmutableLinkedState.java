package org.squirrelframework.foundation.fsm;

/**
 * A linked state specifies the insertion of the specification of a submachine state machine. 
 * The state machine that contains the linked state is called the containing state machine. 
 * The same state machine may be a submachine more than once in the context of a single containing 
 * state machine.  
 * 
 * A linked state is semantically equivalent to a composite state. The regions of the submachine 
 * state machine are the regions of the composite state. The entry, exit, and behavior actions and 
 * internal transitions are defined as part of the state. Submachine state is a decomposition 
 * mechanism that allows factoring of common behaviors and their reuse.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface ImmutableLinkedState<T extends StateMachine<T, S, E, C>, S, E, C> extends ImmutableState<T, S, E, C> {
    
    /**
     * @return linked state machine
     */
    StateMachine<? extends StateMachine<?, S, E, C>, S, E, C> getLinkedStateMachine();
}
