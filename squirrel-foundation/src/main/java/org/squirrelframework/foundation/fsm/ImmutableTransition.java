package org.squirrelframework.foundation.fsm;

import java.util.List;

import org.squirrelframework.foundation.component.SquirrelComponent;

/**
 * <p><b>Transition</b> A directed relationship between two states which represents the complete response 
 * of a state machine to an occurrence of an event of a particular type.</p>
 * 
 * <p><b>Condition</b> A constraint which must evaluate to true after the trigger occurs in order for the 
 * transition to complete.</p>
 * 
 * <p><b>Transition Action</b> An activity which is executed when performing a certain transition.</p>
 * 
 * <p><b>Trigger(Event)</b> A triggering activity that causes a transition to occur.</p>
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface ImmutableTransition<T extends StateMachine<T, S, E, C>, S, E, C>  extends Visitable<T, S, E, C>, SquirrelComponent {
    
    /**
     * @return Transition source state
     */
    ImmutableState<T, S, E, C> getSourceState();
    
    /**
     * @return Transition destination state
     */
    ImmutableState<T, S, E, C> getTargetState();
    
    /**
     * @return Transition action list
     */
    List<Action<T, S, E, C>> getActions();
    
    /**
     * Execute transition under state context
     * @param stateContext
     * @return state when transition finished
     */
    ImmutableState<T, S, E, C> transit(StateContext<T, S, E, C> stateContext);
    
    /**
     * @return Condition of the transition
     */
    Condition<C> getCondition();
    
    /**
     * @return Event that can trigger the transition
     */
    E getEvent();
    
    /**
     * @return type of transition
     */
    TransitionType getType();
    
    int getPriority();
    
    boolean isMatch(S fromState, S toState, E event);
    
    boolean isMatch(S fromState, S toState, E event, Class<?> condClazz, TransitionType type);
    
    /**
     * Notify transition when receiving event
     * @param stateContext
     */
    void internalFire(StateContext<T, S, E, C> stateContext);
    
    /**
     * Verify tranistion correctness
     */
    void verify();
}
