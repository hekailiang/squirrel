package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.collect.Lists;

public class ActionsImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Actions<T, S, E, C> {
    
    private final List<Action<T, S, E, C>> actions = Lists.newArrayList();
    
    private List<Action<T, S, E, C>> sortedActions;
    
    @Override
    public void add(Action<T, S, E, C> newAction) {
        if(newAction!=null && !actions.contains(newAction)) {
            actions.add(newAction);
        }
    }
    
    @Override
    public void addAll(List<Action<T, S, E, C>> newActions) {
        if(newActions!=null && !newActions.isEmpty()) {
            for(Action<T, S, E, C> action : newActions) {
                add(action);
            }
        }
    }

    @Override
    public List<Action<T, S, E, C>> getAll() {
        return getSortedAction();
    }
    
    private List<Action<T, S, E, C>> getSortedAction() {
        if(actions.isEmpty()) 
            return Collections.emptyList();
        
        if(sortedActions==null) {
            sortedActions = Lists.newArrayList(actions);
            Collections.sort(sortedActions, new Comparator<Action<T, S, E, C>>() {
                @Override
                public int compare(Action<T, S, E, C> o1, Action<T, S, E, C> o2) {
                    return o2.weight() - o1.weight();
                }
            });
            sortedActions = Collections.unmodifiableList(sortedActions);
        }
        return sortedActions;
    }

    @Override
    public void clear() {
        actions.clear();
        sortedActions=null;
    }
}
