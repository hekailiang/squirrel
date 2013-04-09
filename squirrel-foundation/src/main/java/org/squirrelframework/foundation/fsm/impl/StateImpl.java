package org.squirrelframework.foundation.fsm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;

/**
 * The state model of the state machine implementation.
 * 
 * @author Henry.He
 *
 * @param <T> The type of implemented state machine
 * @param <S> The type of implemented state
 * @param <E> The type of implemented event
 * @param <C> The type of implemented context
 */
final class StateImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractState<T, S, E, C> {
    
    StateImpl(S stateId) {
	    super(stateId);
    }

	private static final Logger logger = LoggerFactory.getLogger(StateImpl.class);
    
    @Override
    public void entry(StateContext<T, S, E, C> stateContext) {
        for(Action<T, S, E, C> entryAction : getEntryActions()) {
            entryAction.execute(null, getStateId(), stateContext.getEvent(), 
                    stateContext.getContext(), stateContext.getStateMachine());
        }
        logger.debug("State \""+getStateId()+"\" entry.");
    }
    
    @Override
    public void exit(StateContext<T, S, E, C> stateContext) {
        for(Action<T, S, E, C> exitAction : getExitActions()) {
            exitAction.execute(getStateId(), null, stateContext.getEvent(), 
                    stateContext.getContext(), stateContext.getStateMachine());
        }
        // update historical state 
        if (getParentState() != null && getParentState().getHistoryType()!=HistoryType.NONE) {
			stateContext.setLastActiveChildState(getParentState(), this);
		}
        logger.debug("State \""+getStateId()+"\" exit.");
    }
}
