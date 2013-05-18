package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.StateMachine;

import com.google.common.collect.Lists;

public class ActionsImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements Actions<T, S, E, C> {
    
    private int afterActionCounter = 0;
    
    private int beforeActionCounter = 0;
    
    private LinkedList<Action<T, S, E, C>> actions;
    
    @Override
    public void add(Action<T, S, E, C> newAction) {
        if(actions==null) actions = Lists.newLinkedList();
        if(newAction!=null && !actions.contains(newAction)) {
            if(newAction instanceof MethodCallActionImpl) {
                // sort method call action by its name
                String name = MethodCallActionImpl.class.cast(newAction).getName();
                if(name.startsWith("before")) {
                    actions.add(beforeActionCounter++, newAction);
                } else if(name.startsWith("after")) {
                    actions.add(actions.size()-afterActionCounter++, newAction);
                } else {
                    actions.add(actions.size()-afterActionCounter, newAction);
                }
            } else {
                actions.add(actions.size()-afterActionCounter, newAction);
            }
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
        return actions==null ? Collections.<Action<T, S, E, C>>emptyList() : Collections.unmodifiableList(actions);
    }

    @Override
    public void clear() {
        actions.clear();
        afterActionCounter = 0;
        beforeActionCounter = 0;
    }
}
