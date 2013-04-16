package org.squirrelframework.foundation.fsm;

import java.util.List;

/**
 * This class will hold all the transition result including result of nested transitions.
 * 
 * @author Henry.He
 *
 * @param <T> state machine type
 * @param <S> state type
 * @param <E> event type
 * @param <C> context type
 */
public interface TransitionResult<T extends StateMachine<T, S, E, C>, S, E, C> {
	/**
	 * If any transition including all nested transitions is accepted, the parent transition is 
	 * accepted accordingly.
	 * @return true if transition is accepted; false if transition result is declined
	 */
	boolean isAccepted();
	
	/**
	 * If all transitions including all nested transitions is declined, the parent transition is
	 * declined accordingly.
	 * @return false if transition is accepted; true if transition result is declined
	 */
	boolean isDeclined();
	
	/**
	 * Set transition accepted or not.  
	 * @param accepted
	 * @return transition result
	 */
	TransitionResult<T, S, E, C> setAccepted(boolean accepted);
	
	/**
	 * @return target state of transition
	 */
	ImmutableState<T, S, E, C> getTargetState();
	
	/**
	 * Set target state of transition
	 * @param targetState
	 * @return transition result
	 */
	TransitionResult<T, S, E, C> setTargetState(ImmutableState<T, S, E, C> targetState);
	
	/**
	 * @return parent transition result
	 */
	TransitionResult<T, S, E, C> getParentResut();
	
	/**
	 * Set parent transition result
	 * @param result
	 * @return transition result
	 */
	TransitionResult<T, S, E, C> setParent(TransitionResult<T, S, E, C> result);
	
	/**
	 * @return nested transition result of current transition
	 */
	List<TransitionResult<T, S, E, C>> getSubResults();
	
	/**
	 * @return all the accepted transition result of current transition
	 */
	List<TransitionResult<T, S, E, C>> getAcceptedResults();
}
