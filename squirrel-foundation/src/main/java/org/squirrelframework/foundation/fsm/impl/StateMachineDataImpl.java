package org.squirrelframework.foundation.fsm.impl;

import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

public class StateMachineDataImpl<T extends StateMachine<T, S, E, C>, S, E, C>
        implements StateMachineData<T, S, E, C>,
        StateMachineData.Reader<T, S, E, C>,
        StateMachineData.Writer<T, S, E, C> {

    private static final long serialVersionUID = 4102325896046410596L;

    private static final Logger logger = LoggerFactory
            .getLogger(StateMachineDataImpl.class);

    private S currentState;

    private S lastState;

    private S initialState;

    private final Map<S, S> lastActiveChildStateStore = Maps.newHashMap();

    private final ListMultimap<S, S> parallelStatesStore = ArrayListMultimap.create();

    private Class<? extends T> stateMachineType;

    private Class<S> stateType;

    private Class<E> eventType;

    private Class<C> contextType;
    
    private String identifier;

    private Object startContext;

    private final transient Map<S, ImmutableState<T, S, E, C>> states;

    private Map<S, StateMachineData.Reader<? extends StateMachine<?, S, E, C>, S, E, C>> linkStateDataStore;

    public StateMachineDataImpl(Map<S, ImmutableState<T, S, E, C>> states) {
        this.states = Collections.unmodifiableMap(states);
    }

    public StateMachineDataImpl() {
        this.states = null;
    }

    @Override
    public Map<S, ImmutableState<T, S, E, C>> originalStates() {
        if (states == null) {
            return Collections.emptyMap();
        }
        return states;
    }
    
    private void clear() {
        currentState = null;
        lastState = null;
        initialState = null;
        stateMachineType = null;
        stateType = null;
        eventType = null;
        contextType = null;
        identifier = null;
        startContext = null;
        if(lastActiveChildStateStore!=null) {
            lastActiveChildStateStore.clear();
        }
        if(parallelStatesStore!=null) {
            parallelStatesStore.clear();
        }
        if(linkStateDataStore!=null) {
            linkStateDataStore.clear();
        }
    }

    @Override
    public void dump(StateMachineData.Reader<T, S, E, C> src) {
        clear();
        
        this.write().typeOfStateMachine(src.typeOfStateMachine());
        this.write().typeOfState(src.typeOfState());
        this.write().typeOfEvent(src.typeOfEvent());
        this.write().typeOfContext(src.typeOfContext());

        this.write().identifier(src.identifier());
        this.write().currentState(src.currentState());
        this.write().lastState(src.lastState());
        this.write().initialState(src.initialState());
        // write start context of state machine
        this.write().startContext(src.startContext());

        for (S state : src.activeParentStates()) {
            S lastActiveChildState = src.lastActiveChildStateOf(state);
            if (lastActiveChildState != null) {
                this.write().lastActiveChildStateFor(state, lastActiveChildState);
            }
        }

        for (S state : src.parallelStates()) {
            List<S> subStates = src.subStatesOn(state);
            if (subStates != null && !subStates.isEmpty()) {
                for (S subState : subStates) {
                    // ignore parallel state check in subStateFor as no states
                    // for reference
                    // this.write().subStateFor(state, subState);
                    parallelStatesStore.put(state, subState);
                }
            }
        }
    }

    private Map<S, StateMachineData.Reader<? extends StateMachine<?, S, E, C>, S, E, C>> getLinkedStateData() {
        if (linkStateDataStore == null) {
            linkStateDataStore = Maps.newHashMap();
        }
        return linkStateDataStore;
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
    public void initialState(S initialStateId) {
        this.initialState = initialStateId;
    }

    @Override
    public void startContext(C context) {
        if(context!=null && context instanceof Serializable)
            startContext = context;
    }

    @Override
    public void lastActiveChildStateFor(S parentStateId, S childStateId) {
        lastActiveChildStateStore.put(parentStateId, childStateId);
    }

    @Override
    public void subStateFor(S parentStateId, S subStateId) {
        if (rawStateFrom(parentStateId) != null
                && rawStateFrom(parentStateId).isParallelState()) {
            parallelStatesStore.put(parentStateId, subStateId);
        } else {
            logger.warn("Cannot set sub states on none parallel state {}.",
                    parentStateId);
        }
    }

    @Override
    public void removeSubState(S parentStateId, S subStateId) {
        if (rawStateFrom(parentStateId) != null
                && rawStateFrom(parentStateId).isParallelState()) {
            parallelStatesStore.remove(parentStateId, subStateId);
        } else {
            logger.warn("Cannot remove sub states on none parallel state {}.",
                    parentStateId);
        }
    }

    @Override
    public void removeSubStatesOn(S parentStateId) {
        if (rawStateFrom(parentStateId).isParallelState()) {
            parallelStatesStore.removeAll(parentStateId);
        }
    }
    
    @Override
    public void identifier(String id) {
        this.identifier = id;
    }

    @Override
    public String identifier() {
        return identifier;
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
    public C startContext() {
        return (C)startContext;
    }

    @Override
    public Collection<S> activeParentStates() {
        return Collections.unmodifiableCollection(lastActiveChildStateStore.keySet());
    }

    @Override
    public List<S> subStatesOn(S parentStateId) {
        List<S> subStates = parallelStatesStore.get(parentStateId);
        return subStates != null ? Collections.unmodifiableList(subStates)
                : Collections.<S> emptyList();
    }

    @Override
    public ImmutableState<T, S, E, C> currentRawState() {
        return currentState!=null ? rawStateFrom(currentState) : null;
    }

    @Override
    public ImmutableState<T, S, E, C> lastRawState() {
        return rawStateFrom(lastState);
    }

    @Override
    public ImmutableState<T, S, E, C> rawStateFrom(S stateId) {
        if(stateId==null) return null;
        ImmutableState<T, S, E, C> rawState = originalStates().get(stateId);
        return rawState!=null ? rawState.getThis() : null;
    }

    @Override
    public ImmutableState<T, S, E, C> initialRawState() {
        return rawStateFrom(initialState());
    }

    @Override
    public Class<? extends T> typeOfStateMachine() {
        return stateMachineType;
    }

    @Override
    public Class<S> typeOfState() {
        return stateType;
    }

    @Override
    public Class<E> typeOfEvent() {
        return eventType;
    }

    @Override
    public Class<C> typeOfContext() {
        return contextType;
    }

    @Override
    public void typeOfStateMachine(Class<? extends T> stateMachineType) {
        checkState(this.stateMachineType==null);
        this.stateMachineType = stateMachineType;
    }

    @Override
    public void typeOfState(Class<S> stateClass) {
        checkState(this.stateType==null);
        this.stateType = stateClass;
    }

    @Override
    public void typeOfEvent(Class<E> eventClass) {
        checkState(this.eventType==null);
        this.eventType = eventClass;
    }

    @Override
    public void typeOfContext(Class<C> contextClass) {
        checkState(this.contextType==null);
        this.contextType = contextClass;
    }

    @Override
    public Collection<ImmutableState<T, S, E, C>> rawStates() {
        return Collections.unmodifiableCollection(originalStates().values());
    }

    @Override
    public Collection<S> states() {
        return Collections.unmodifiableCollection(originalStates().keySet());
    }

    @Override
    public Collection<S> parallelStates() {
        return Collections.unmodifiableCollection(parallelStatesStore.keySet());
    }

    @Override
    public Collection<S> linkedStates() {
        if (linkStateDataStore == null || linkStateDataStore.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(linkStateDataStore.keySet());
    }

    @Override
    public StateMachineData.Reader<? extends StateMachine<?, S, E, C>, S, E, C> linkedStateDataOf(
            S linkedState) {
        if (linkStateDataStore != null && !linkStateDataStore.isEmpty())
            return linkStateDataStore.get(linkedState);
        return null;
    }

    @Override
    public void linkedStateDataOn(S linkedState,
            StateMachineData.Reader<? extends StateMachine<?, S, E, C>, S, E, C> linkStateData) {
        getLinkedStateData().put(linkedState, linkStateData);
    }
}
