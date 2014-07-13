package org.squirrelframework.foundation.fsm.impl;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;

import java.util.List;

public class DeferBoundActionInfo<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    private List<? extends Action<T, S, E, C>> actions;
    
    private S from;
    
    private S to;
    
    private E event;
    
    DeferBoundActionInfo(S from, S to, E event) {
        this.from = from;
        this.to = to;
        this.event = event;
    }
    
    public boolean  isFromStateMatch(S from) {
        return this.from==null || this.from.equals(from);
    }
    
    public boolean  isToStateMatch(S to) {
        return this.to==null || this.to.equals(to);
    }
    
    public boolean  isEventStateMatch(E event) {
        return this.event==null || this.event.equals(event);
    }
    
    public List<? extends Action<T, S, E, C>> getActions() {
        return actions;
    }
    
    public void setActions(List<? extends Action<T, S, E, C>> actions) {
        this.actions = actions;
    }

}
