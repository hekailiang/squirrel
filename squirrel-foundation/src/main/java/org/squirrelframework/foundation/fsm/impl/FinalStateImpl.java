package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;

final class FinalStateImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractState<T, S, E, C> {
    
	private static final Logger logger = LoggerFactory.getLogger(FinalStateImpl.class);
    
	FinalStateImpl(S stateId) {
	    super(stateId);
    }
	
    @Override
    public List<Action<T, S, E, C>> getEntryActions() {
        return Collections.emptyList();
    }

    @Override
    public void entry(StateContext<T, S, E, C> stateContext) {
        logger.debug("Final state entry");
        stateContext.getStateMachine().terminate();
    }
    
    @Override
    public void exit(StateContext<T, S, E, C> stateContext) {
        throw new UnsupportedOperationException("The final state should never be exited.");
    }

    @Override
    public boolean isFinal() {
        return true;
    }
}
