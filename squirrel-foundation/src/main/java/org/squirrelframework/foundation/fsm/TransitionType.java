package org.squirrelframework.foundation.fsm;

/**
 * The type of transition. According to the UML specification (2.5 b1 pag. 377), state machine transition 
 * can be divided into three type.
 * 
 * @author Henry.He
 *
 */
public enum TransitionType {
    /**
     * Implies that the Transition, if triggered, occurs without exiting or entering the source State 
     * (i.e., it does not cause a state change). This means that the entry or exit condition of the source 
     * State will not be invoked. An internal Transition can be taken even if the SateMachine is in one or 
     * more Regions nested within the associated State.
     */
    INTERNAL, 
    /**
     * Implies that the Transition, if triggered, will not exit the composite (source) State, but it 
     * will exit and re-enter any state within the composite State that is in the current state configuration.
     */
    LOCAL,
    /**
     * Implies that the Transition, if triggered, will exit the composite (source) State.
     */
    EXTERNAL
}
