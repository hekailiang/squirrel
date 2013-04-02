package org.squirrelframework.foundation.fsm.impl;

import java.util.List;

import org.squirrelframework.foundation.component.SquirrelComponent;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.builder.EntryExitActionBuilder;

class EntryExitActionBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements 
    EntryExitActionBuilder<T, S, E, C>, SquirrelComponent {
    
    private final boolean isEntryAction;
    
    private final MutableState<T, S, E, C> state;
    
    EntryExitActionBuilderImpl(MutableState<T, S, E, C> state, boolean isEntryAction) {
        this.state = state;
        this.isEntryAction = isEntryAction;
    }

    @Override
    public void perform(Action<T, S, E, C> action) {
        if(isEntryAction) {
            state.addEntryAction(action);
        } else {
            state.addExitAction(action);
        }
    }

    @Override
    public void perform(List<Action<T, S, E, C>> actions) {
        if(isEntryAction) {
            state.addEntryActions(actions);
        } else {
            state.addExitActions(actions);
        }
    }
}
