package org.squirrelframework.foundation.fsm.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineData;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;

public class StateMachineDataImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
implements StateMachineData<T, S, E, C>, StateMachineData.Reader<T, S, E, C>, StateMachineData.Writer<T, S, E, C> {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineDataImpl.class);
    
    private S currentState;
    
    private S lastState;
    
    private S initialState;
    
    private final Map<S, S> lastActiveChildStateStore = Maps.newHashMap();
    
    private final ArrayListMultimap<S, S> parallelStatesStore = ArrayListMultimap.create();
    
    private Class<? extends T> stateMachineType;
    
    private Class<S> stateType;
    
    private Class<E> eventType;
    
    private Class<C> contextType;
    
    private final transient Map<S, ImmutableState<T, S, E, C>> states;
    
    public StateMachineDataImpl(Map<S, ImmutableState<T, S, E, C>> states) {
        this.states = Collections.unmodifiableMap(states);
    }
    
    @Override
    public void dump(StateMachineData<T, S, E, C> src) {
        this.setTypeOfStateMachine(src.getTypeOfStateMachine());
        this.setTypeOfState(src.getTypeOfState());
        this.setTypeOfEvent(src.getTypeOfEvent());
        this.setTypeOfContext(src.getTypeOfContext());
        
        this.write().currentState(src.read().currentState());
        this.write().lastState(src.read().lastState());
        this.write().initalState(src.read().initialState());
        
        for(S state : src.getStates()) {
            S lastActiveChildState = src.read().lastActiveChildStateOf(state);
            if(lastActiveChildState!=null) {
                this.write().lastActiveChildStateFor(state, lastActiveChildState);
            }
        }
        
        for(S state : src.read().parallelStates()) {
            List<S> subStates = src.read().subStatesOn(state);
            if(subStates!=null && !subStates.isEmpty()) {
                for(S subState : subStates) {
                    this.write().subStateFor(state, subState);
                }
            }
        }
    }
    
    @Override
    public StateMachineData.Reader<T, S, E, C> read() {
        return this;
    }

    @Override
    public StateMachineData.Writer<T, S, E, C> write() {
        return this;
    }

    @Override
    public void currentState(S currentStateId) {
        this.currentState = currentStateId;
    }

    @Override
    public void lastState(S lastStateId) {
        this.lastState = lastStateId;
    }

    @Override
    public void initalState(S initialStateId) {
        this.initialState = initialStateId;
    }

    @Override
    public void lastActiveChildStateFor(S parentStateId, S childStateId) {
        lastActiveChildStateStore.put(parentStateId, childStateId);
    }

    @Override
    public void subStateFor(S parentStateId, S subStateId) {
        if(getRawStateFrom(parentStateId)!=null && getRawStateFrom(parentStateId).isParallelState()) {
            parallelStatesStore.put(parentStateId, subStateId);
        } else {
            logger.warn("Cannot set sub states on none parallel state {}."+parentStateId);
        }
    }
    
    @Override
    public void removeSubState(S parentStateId, S subStateId) {
        if(getRawStateFrom(parentStateId)!=null && getRawStateFrom(parentStateId).isParallelState()) {
            parallelStatesStore.remove(parentStateId, subStateId);
        } else {
            logger.warn("Cannot remove sub states on none parallel state {}."+parentStateId);
        }
    }
    
    @Override
    public void removeSubStatesOn(S parentStateId) {
        if(getRawStateFrom(parentStateId).isParallelState()) {
            parallelStatesStore.removeAll(parentStateId);
        } 
    }

    @Override
    public S currentState() {
        return currentState;
    }

    @Override
    public S lastState() {
        return lastState;
    }

    @Override
    public S initialState() {
        return initialState;
    }

    @Override
    public S lastActiveChildStateOf(S parentStateId) {
        return lastActiveChildStateStore.get(parentStateId);
    }
    
    @Override
    public List<S> subStatesOn(S parentStateId) {
        if(getRawStateFrom(parentStateId).isParallelState()) {
            return parallelStatesStore.get(parentStateId);
        } 
        return Collections.emptyList();
    }
    
    @Override
    public ImmutableState<T, S, E, C> currentRawState() {
        return getRawStateFrom(currentState);
    }

    @Override
    public ImmutableState<T, S, E, C> lastRawState() {
        return getRawStateFrom(lastState);
    }
    
    @Override
    public ImmutableState<T, S, E, C> getRawStateFrom(S stateId) {
        return states.get(stateId);
    }
    
    @Override
    public ImmutableState<T, S, E, C> initialRawState() {
        return getRawStateFrom(initialState);
    }

    @Override
    public Class<? extends T> getTypeOfStateMachine() {
        return stateMachineType;
    }

    @Override
    public Class<S> getTypeOfState() {
        return stateType;
    }

    @Override
    public Class<E> getTypeOfEvent() {
        return eventType;
    }

    @Override
    public Class<C> getTypeOfContext() {
        return contextType;
    }
    
    @Override
    public void setTypeOfStateMachine(Class<? extends T> stateMachineType) {
        this.stateMachineType = stateMachineType;
    }
    
    @Override
    public void setTypeOfState(Class<S> stateClass) {
        this.stateType = stateClass;
    }
    
    @Override
    public void setTypeOfEvent(Class<E> eventClass) {
        this.eventType = eventClass;
    }
    
    @Override
    public void setTypeOfContext(Class<C> contextClass) {
        this.contextType = contextClass;
    }
    
    @Override
    public Collection<ImmutableState<T, S, E, C>> getRawStates() {
        return states.values();
    }

    @Override
    public Collection<S> getStates() {
        return states.keySet();
    }

    @Override
    public Collection<S> parallelStates() {
        return parallelStatesStore.keySet();
    }
}
