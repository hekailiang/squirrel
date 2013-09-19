package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.Conditions;
import org.squirrelframework.foundation.fsm.StateCompositeType;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineData;
import org.squirrelframework.foundation.fsm.TransitionResult;
import org.squirrelframework.foundation.fsm.Visitor;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;

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
class StateImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements MutableState<T, S, E, C> {
	
	private static final Logger logger = LoggerFactory.getLogger(StateImpl.class);
	
	protected final S stateId;
    
    protected final Actions<T, S, E, C> entryActions = FSM.newActions();
    
    protected final Actions<T, S, E, C> exitActions  = FSM.newActions();
    
    private LinkedListMultimap<E, ImmutableTransition<T, S, E, C>> transitions;
    
    /**
	 * The super-state of this state. Null for states with <code>level</code> equal to 1.
	 */
    private MutableState<T, S, E, C> parentState;
    
    private List<MutableState<T, S, E, C>> childStates;
    
    /**
	 * The initial child state of this state.
	 */
    private MutableState<T, S, E, C> childInitialState;
    
    /**
	 * The HistoryType of this state.
	 */
	private HistoryType historyType = HistoryType.NONE;
	
	/**
	 * The level of this state within the state hierarchy [1..maxLevel].
	 */
    private int level = 0;
    
    /**
     * Whether the state is a final state
     */
    private boolean isFinalState = false;
    
    /**
     * Composite type of child states
     */
    private StateCompositeType compositeType = StateCompositeType.SEQUENTIAL;
    
    StateImpl(S stateId) {
    	this.stateId = stateId;
    }

	@Override
    public S getStateId() {
        return stateId;
    }
    
    @Override
    public List<Action<T, S, E, C>> getEntryActions() {
        return entryActions.getAll();
    }

    @Override
    public List<Action<T, S, E, C>> getExitActions() {
        return exitActions.getAll();
    }

    @Override
    public List<ImmutableTransition<T, S, E, C>> getAllTransitions() {
    	if(transitions==null) return Collections.emptyList();
        return Lists.newArrayList(getTransitions().values());
    }

    @Override
    public List<ImmutableTransition<T, S, E, C>> getTransitions(E event) {
    	if(transitions==null) return Collections.emptyList();
    	return Lists.newArrayList(getTransitions().get(event));
    }
    
    @Override
    public void prioritizeTransitions() {
        for(E key : getTransitions().keySet()) {
            List<ImmutableTransition<T, S, E, C>> trans = transitions.get(key);
            Collections.sort(trans, new Comparator<ImmutableTransition<T, S, E, C>>() {
                @Override
                public int compare(ImmutableTransition<T, S, E, C> o1, ImmutableTransition<T, S, E, C> o2) {
                    return o2.getPriority()-o1.getPriority();
                }
            });
        }
    }
    
    @Override
    public void entry(final StateContext<T, S, E, C> stateContext) {
        for(final Action<T, S, E, C> entryAction : getEntryActions()) {
            stateContext.getExecutor().defer(entryAction, 
                    null, getStateId(), stateContext.getEvent(), 
                    stateContext.getContext(), stateContext.getStateMachine().getThis());
        }
        
        if(isParallelState()) {
            // When a parallel state group is entered, all its child states will be simultaneously entered. 
            for(ImmutableState<T, S, E, C> parallelState : getChildStates()) {
                parallelState.entry(stateContext);
                ImmutableState<T, S, E, C> subState = parallelState.enterByHistory(stateContext);
                stateContext.getStateMachineData().write().subStateFor(getStateId(), subState.getStateId());
            }
        }
        logger.debug("State \""+getStateId()+"\" entry.");
    }
    
    @Override
    public void exit(final StateContext<T, S, E, C> stateContext) {
    	if(isFinalState()) {
            return;
    	}
    	if(isParallelState()) {
    		List<ImmutableState<T, S, E, C>> subStates = getSubStatesOn(this, stateContext.getStateMachineData().read());
    		for(ImmutableState<T, S, E, C> subState : subStates) {
    			if(!subState.isFinalState()) {
    				subState.exit(stateContext);
    			}
    			subState.getParentState().exit(stateContext);
    		}
    		stateContext.getStateMachineData().write().removeSubStatesOn(getStateId());
    	}
    	
        for(final Action<T, S, E, C> exitAction : getExitActions()) {
        	stateContext.getExecutor().defer(exitAction,
        			getStateId(), null, stateContext.getEvent(), 
                    stateContext.getContext(), stateContext.getStateMachine().getThis());
        }
         
        if (getParentState() != null) {
        	// update historical state
        	if(getParentState().getHistoryType()!=HistoryType.NONE) {
        		stateContext.getStateMachineData().write().lastActiveChildStateFor(getParentState().getStateId(), getStateId());
        	}
        	if(getParentState().isRegion()) {
        		S grandParentId = getParentState().getParentState().getStateId();
        		stateContext.getStateMachineData().write().removeSubState(grandParentId, getStateId());
        	}
		}
        logger.debug("State \""+getStateId()+"\" exit.");
    }
    
    @Override
    public ImmutableState<T, S, E, C> getParentState() {
	    return parentState;
    }
    
    @Override
    public List<ImmutableState<T, S, E, C>> getChildStates() {
	    return Lists.<ImmutableState<T, S, E, C>>newArrayList(childStates);
    }
    
    @Override
    public boolean hasChildStates() {
        return childStates!=null && childStates.size()>0;
    }
    
    @Override
    public void setParentState(MutableState<T, S, E, C> parent) {
    	if(this==parent) {
    		throw new IllegalArgumentException("parent state cannot be state itself.");
    	}
		if(this.parentState==null) {
			this.parentState = parent;
			setLevel(this.parentState!=null ? this.parentState.getLevel()+1 : 1);
		} else {
			throw new RuntimeException("Cannot change state parent.");
		}
    }
    
    @Override
    public ImmutableState<T, S, E, C> getInitialState() {
	    return childInitialState;
    }

	@Override
    public void setInitialState(MutableState<T, S, E, C> childInitialState) {
		if(isParallelState()) {
			logger.warn("Ignoring attempt to set initial state of parallel state group.");
			return;
		}
	    if(this.childInitialState==null) {
	    	this.childInitialState = childInitialState;
	    } else {
	    	throw new RuntimeException("Cannot change child initial parent.");
	    }
    }

    @Override
    public ImmutableState<T, S, E, C> enterByHistory(StateContext<T, S, E, C> stateContext) {
    	if(isFinalState() || isParallelState()) // no historical info
    		return this;
    	
    	ImmutableState<T, S, E, C> result = null;
    	switch (this.historyType) {
		case NONE:
			result = enterHistoryNone(stateContext);
			break;
		case SHALLOW:
			result = enterHistoryShallow(stateContext);
			break;
		case DEEP:
			result = enterHistoryDeep(stateContext);
			break;
		default:
			throw new IllegalArgumentException("Unknown HistoryType : " + historyType);
		}
    	return result;
    }
    
    @Override
	public ImmutableState<T, S, E, C> enterDeep(StateContext<T, S, E, C> stateContext) {
		this.entry(stateContext);
		final ImmutableState<T, S, E, C> lastActiveState = getLastActiveChildStateOf(this, stateContext.getStateMachineData().read());
		return lastActiveState == null ? this : lastActiveState.enterDeep(stateContext);
	}
    
    @Override
    public ImmutableState<T, S, E, C> enterShallow(StateContext<T, S, E, C> stateContext) {
	    entry(stateContext);
	    return childInitialState!=null ? childInitialState.enterShallow(stateContext) : this;
    }
    
    /**
	 * Enters this instance with history type = shallow.
	 * 
	 * @param stateContext
	 *            state context
	 * @return the entered state
	 */
	private ImmutableState<T, S, E, C> enterHistoryShallow(StateContext<T, S, E, C> stateContext) {
		final ImmutableState<T, S, E, C> lastActiveState = getLastActiveChildStateOf(this, stateContext.getStateMachineData().read());
		return lastActiveState != null ? lastActiveState.enterShallow(stateContext) : this;
	}
    
    /**
	 * Enters with history type = none.
	 * 
	 * @param stateContext
	 *            state context
	 * @return the entered state.
	 */
	private ImmutableState<T, S, E, C> enterHistoryNone(StateContext<T, S, E, C> stateContext) {
		return childInitialState != null ? childInitialState.enterShallow(stateContext) : this;
	}
	
	/**
	 * Enters this instance with history type = deep.
	 * 
	 * @param stateContext
	 *            the state context.
	 * @return the state
	 */
	private ImmutableState<T, S, E, C> enterHistoryDeep(StateContext<T, S, E, C> stateContext) {
		final ImmutableState<T, S, E, C> lastActiveState = getLastActiveChildStateOf(this, stateContext.getStateMachineData().read());
		return lastActiveState != null ? lastActiveState.enterDeep(stateContext) : this;
	}
    
	private LinkedListMultimap<E, ImmutableTransition<T, S, E, C>> getTransitions() {
    	if(transitions==null) {
    		transitions = LinkedListMultimap.create();
    	}
    	return transitions;
    }
	
    @Override
    public MutableTransition<T, S, E, C> addTransitionOn(E event) {
        MutableTransition<T, S, E, C> newTransition = FSM.newTransition();
        newTransition.setSourceState(this);
        newTransition.setEvent(event);
        getTransitions().put(event, newTransition);
        return newTransition;
    }
    
    @Override
    public void addEntryAction(Action<T, S, E, C> newAction) {
        entryActions.add(newAction);
    }
    
    @Override
    public void addEntryActions(List<Action<T, S, E, C>> newActions) {
        entryActions.addAll(newActions);
    }

    @Override
    public void addExitAction(Action<T, S, E, C> newAction) {
        exitActions.add(newAction);
    }

    @Override
    public void addExitActions(List<Action<T, S, E, C>> newActions) {
        exitActions.addAll(newActions);
    }
    
    private boolean isParentOf(ImmutableState<T, S, E, C> state) {
    	ImmutableState<T, S, E, C> parent = state.getParentState();
    	while(parent!=null) {
    		if(parent==this) 
    			return true;
    		parent=parent.getParentState();
    	}
    	return false;
    }
    
    @Override
    public void internalFire(StateContext<T, S, E, C> stateContext) {
    	TransitionResult<T, S, E, C> currentTransitionResult = stateContext.getResult();
    	if(isParallelState()) {
    		/**
    		 * The parallelism in the State Machine framework follows an interleaved semantics. 
    		 * All parallel operations will be executed in a single, atomic step of the event 
    		 * processing, so no event can interrupt the parallel operations. However, events 
    		 * will still be processed sequentially, since the machine itself is single threaded.
    		 * 
    		 * The child states execute in parallel in the sense that any event that is processed 
    		 * is processed in each child state independently, and each child state may take a different 
    		 * transition in response to the event. (Similarly, one child state may take a transition 
    		 * in response to an event, while another child ignores it.)
    		 */
    		for(ImmutableState<T, S, E, C> parallelState : getSubStatesOn(this, stateContext.getStateMachineData().read())) {
    			if(parallelState.isFinalState()) continue;
    			// context isolation as entering a new region
    			TransitionResult<T, S, E, C> subTransitionResult = 
    					FSM.<T, S, E, C>newResult(false, parallelState, currentTransitionResult);
    			StateContext<T, S, E, C> subStateContext = FSM.newStateContext(
    					stateContext.getStateMachine(), stateContext.getStateMachineData(), 
    					parallelState, stateContext.getEvent(), stateContext.getContext(), 
    					subTransitionResult, stateContext.getExecutor());
    			parallelState.internalFire(subStateContext); 
    			if(subTransitionResult.isDeclined()) continue;
    			
    			if(!isParentOf(subTransitionResult.getTargetState())) {
    				// child state transition exit the parallel state
    				currentTransitionResult.setTargetState(subTransitionResult.getTargetState());
    				return;
    			}
    			stateContext.getStateMachineData().write().subStateFor(getStateId(), subTransitionResult.getTargetState().getStateId());
				// TODO-hhe: fire event to notify listeners???
				if(subTransitionResult.getTargetState().isFinalState()) {
					ImmutableState<T, S, E, C> parentState = subTransitionResult.getTargetState().getParentState();
    				ImmutableState<T, S, E, C> grandParentState = parentState.getParentState();
    				AbstractStateMachine<T, S, E, C> abstractStateMachine = (AbstractStateMachine<T, S, E, C>)
                			stateContext.getStateMachine();
    				// When all of the children reach final states, the parallel state itself is considered 
            		// to be in a final state, and a completion event is generated.
            		if(grandParentState!=null && grandParentState.isParallelState()) {
            			boolean allReachedFinal = true;
            			for(ImmutableState<T, S, E, C> subState : getSubStatesOn(grandParentState, stateContext.getStateMachineData().read())) {
            				if(!subState.isFinalState()) {
            					allReachedFinal = false;
            					break;
            				}
            			}
            			if(allReachedFinal) {
            				StateContext<T, S, E, C> finishContext = FSM.newStateContext(stateContext.getStateMachine(), 
            				        stateContext.getStateMachineData(), grandParentState, abstractStateMachine.getFinishEvent(), 
            				        stateContext.getContext(), currentTransitionResult, stateContext.getExecutor());
                    		grandParentState.internalFire(finishContext);
                    		return;
            			}
    				} 
				}
    		}
    	}
    	
    	List<ImmutableTransition<T, S, E, C>> transitions = getTransitions(stateContext.getEvent());
        for(final ImmutableTransition<T, S, E, C> transition : transitions) {
        	transition.internalFire(stateContext);
        	if(currentTransitionResult.isAccepted()) {
        		ImmutableState<T, S, E, C> targetState = currentTransitionResult.getTargetState();
        		if(targetState.isFinalState() && !targetState.isRootState()) {
        			// TODO-hhe: fire event to notify listeners???
        			ImmutableState<T, S, E, C> parentState = targetState.getParentState();
    				AbstractStateMachine<T, S, E, C> abstractStateMachine = (AbstractStateMachine<T, S, E, C>)
                			stateContext.getStateMachine();
    				StateContext<T, S, E, C> finishContext = FSM.newStateContext(
    						stateContext.getStateMachine(), stateContext.getStateMachineData(),parentState, 
            				abstractStateMachine.getFinishEvent(), stateContext.getContext(), 
            				currentTransitionResult, stateContext.getExecutor());
            		parentState.internalFire(finishContext);
        		}
        		return;
        	}
        }
        
        // fire to super state
        if(currentTransitionResult.isDeclined() && getParentState()!=null && !getParentState().isRegion()) {
        	logger.debug("Internal notify the same event to parent state");
        	getParentState().internalFire(stateContext);
        }
    }
    
    @Override
    public boolean isRootState() {
	    return parentState==null;
    }
    
    @Override
    public boolean isFinalState() {
        return isFinalState;
    }
    
    @Override
    public void setFinal(boolean isFinal) {
        this.isFinalState = isFinal;
    }
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        for(ImmutableTransition<T, S, E, C> transition : getAllTransitions()) {
            transition.accept(visitor);
        }
        if(childStates!=null) {
        	for (ImmutableState<T, S, E, C> childState : childStates) {
        		childState.accept(visitor);
    		}
        }
        visitor.visitOnExit(this);
    }
    
	@Override
    public int getLevel() {
	    return level;
    }

	@Override
    public void setLevel(int level) {
	    this.level = level;
	    if(childStates!=null) {
	    	for (MutableState<T, S, E, C> state : childStates) {
				state.setLevel(this.level+1);
			}
	    }
    }

	@Override
    public void addChildState(MutableState<T, S, E, C> childState) {
		if(childState!=null) {
			if(childStates==null) {
		    	childStates = Lists.newArrayList();
		    }
			if(!childStates.contains(childState))
				childStates.add(childState);
		}
    }

	@Override
    public HistoryType getHistoryType() {
	    return historyType;
    }

	@Override
    public void setHistoryType(HistoryType historyType) {
	    this.historyType = historyType;
    }
	
	@Override
    public StateCompositeType getCompositeType() {
	    return compositeType;
    }

	@Override
    public void setCompositeType(StateCompositeType compositeType) {
	    this.compositeType =compositeType;
    }
	
	@Override
    public boolean isParallelState() {
	    return compositeType==StateCompositeType.PARALLEL;
    }
	
	@Override
    public String toString() {
        return getStateId().toString();
    }

	@Override
    public boolean isRegion() {
	    return parentState!=null && parentState.isParallelState();
    }

    @Override
    public void verify() {
        if(isFinalState()) {
            if(isParallelState()) {
                throw new RuntimeException("Final state cannot be parallel state.");
            } else if(hasChildStates()) {
                throw new RuntimeException("Final state cannot have child states.");
            }
        } 
        
        // make sure that every event can only trigger one transition happen at one time
        if(transitions!=null) {
            List<ImmutableTransition<T, S, E, C>> allTransitions=transitions.values();
            for(ImmutableTransition<T, S, E, C> t : allTransitions) {
                t.verify();
                ImmutableTransition<T, S, E, C> conflictTransition = checkConflictTransitions(t, allTransitions);
                if(conflictTransition!=null) {
                    throw new RuntimeException(String.format("Tansition '%s' is conflicted with '%s'.", t, conflictTransition));
                }
            }
        }
    }
    
    public ImmutableTransition<T, S, E, C> checkConflictTransitions(ImmutableTransition<T, S, E, C> target, 
            List<ImmutableTransition<T, S, E, C>> allTransitions) {
        for(ImmutableTransition<T, S, E, C> t : allTransitions) {
            if(target==t || t.getCondition().getClass()==Conditions.Never.class) continue;
            if(t.isMatch(target.getSourceState().getStateId(), target.getTargetState().getStateId(), target.getEvent())) {
                if(t.getCondition().getClass()==Conditions.Always.class) 
                    return target;
                if(target.getCondition().getClass()==Conditions.Always.class)
                    return target;
                if(t.getCondition().getClass()==target.getCondition().getClass()) 
                    return target;
            }
        }
        return null;
    }
    
    private List<ImmutableState<T, S, E, C>> getSubStatesOn(ImmutableState<T, S, E, C> parentState, 
            StateMachineData.Reader<T, S, E, C> read) {
        List<ImmutableState<T, S, E, C>> subStates = Lists.newArrayList();
        for(S stateId : read.subStatesOn(parentState.getStateId())) {
            subStates.add(read.rawStateFrom(stateId));
        }
        return subStates;
    }
    
    private ImmutableState<T, S, E, C> getLastActiveChildStateOf(ImmutableState<T, S, E, C> parentState, 
            StateMachineData.Reader<T, S, E, C> read) {
        S childStateId = read.lastActiveChildStateOf(parentState.getStateId());
        if(childStateId!=null) {
            return read.rawStateFrom(childStateId);
        } else {
            return parentState.getInitialState();
        }
    }
}
