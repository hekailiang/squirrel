package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.StateCompositeType;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.MutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
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
final class StateImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements MutableState<T, S, E, C> {
	
	private static final Logger logger = LoggerFactory.getLogger(StateImpl.class);
	
	private final S stateId;
    
    private final Actions<T, S, E, C> entryActions = FSM.newActions();
    
    private final Actions<T, S, E, C> exitActions  = FSM.newActions();
    
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
    public void entry(StateContext<T, S, E, C> stateContext) {
    	if(isFinalState() && isRootState()) {
    		stateContext.getStateMachine().terminateWithoutExitStates(stateContext.getContext());
    		logger.debug("Final state of state machine entry.");
    	} else if(!isFinalState()) {
    		for(Action<T, S, E, C> entryAction : getEntryActions()) {
                entryAction.execute(null, getStateId(), stateContext.getEvent(), 
                        stateContext.getContext(), stateContext.getStateMachine());
            }
    		
    		if(isParallelState()) {
    			// When a parallel state group is entered, all its child states will be simultaneously entered. 
    			for(ImmutableState<T, S, E, C> parallelState : getChildStates()) {
    				parallelState.entry(stateContext);
    				ImmutableState<T, S, E, C> subState = parallelState.enterByHistory(stateContext);
    				stateContext.getStateMachine().setSubState(getStateId(), subState.getStateId());
    			}
    		}
            logger.debug("State \""+getStateId()+"\" entry.");
    	}
    }
    
    @Override
    public void exit(StateContext<T, S, E, C> stateContext) {
    	if(isFinalState()) {
            throw new UnsupportedOperationException("The final state should never be exited.");
    	}
    	if(isParallelState()) {
    		List<ImmutableState<T, S, E, C>> subStates = stateContext.getSubStatesOn(this);
    		for(ImmutableState<T, S, E, C> subState : subStates) {
    			if(!subState.isFinalState()) {
    				subState.exit(stateContext);
    			}
    		}
    		for(ImmutableState<T, S, E, C> parallelState : childStates) {
    			parallelState.exit(stateContext);
    		}
    		stateContext.getStateMachine().removeSubStatesOn(getStateId());
    	}
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
    
    @Override
    public ImmutableState<T, S, E, C> getParentState() {
	    return parentState;
    }
    
    @Override
    public List<ImmutableState<T, S, E, C>> getChildStates() {
	    return Lists.<ImmutableState<T, S, E, C>>newArrayList(childStates);
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
		final ImmutableState<T, S, E, C> lastActiveState = stateContext.getLastActiveChildStateOf(this);
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
		final ImmutableState<T, S, E, C> lastActiveState = stateContext.getLastActiveChildStateOf(this);
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
		final ImmutableState<T, S, E, C> lastActiveState = stateContext.getLastActiveChildStateOf(this);
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
    
    @Override
    public TransitionResult<T, S, E, C> internalFire(StateContext<T, S, E, C> stateContext) {
    	TransitionResult<T, S, E, C> result = TransitionResultImpl.notAccepted();
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
    		for(ImmutableState<T, S, E, C> parallelState : stateContext.getSubStatesOn(this)) {
    			if(parallelState.isFinalState()) continue;
    			// context isolation as entering a new region
    			StateContext<T, S, E, C> subStateContext = FSM.newStateContext(stateContext.getStateMachine(), 
    					parallelState, stateContext.getEvent(), stateContext.getContext());
    			TransitionResult<T, S, E, C> subResult = parallelState.internalFire(subStateContext); 
    			if(subResult.isAccepted()) {
    				stateContext.getStateMachine().replaceSubState(getStateId(), 
    						parallelState.getStateId(), subResult.getTargetState().getStateId());
    				
    				if(subResult.getTargetState().isFinalState()) {
    					ImmutableState<T, S, E, C> parentState = subResult.getTargetState().getParentState();
        				ImmutableState<T, S, E, C> grandParentState = parentState.getParentState();
        				AbstractStateMachine<T, S, E, C> abstractStateMachine = (AbstractStateMachine<T, S, E, C>)
                    			stateContext.getStateMachine();
        				// When all of the children reach final states, the parallel state itself is considered 
                		// to be in a final state, and a completion event is generated.
                		if(grandParentState!=null && grandParentState.isParallelState()) {
                			boolean allReachedFinal = true;
                			for(ImmutableState<T, S, E, C> subState : stateContext.getSubStatesOn(grandParentState)) {
                				if(!subState.isFinalState()) {
                					allReachedFinal = false;
                					break;
                				}
                			}
                			if(allReachedFinal) {
                				StateContext<T, S, E, C> finishContext = FSM.newStateContext(stateContext.getStateMachine(), grandParentState, 
                        				abstractStateMachine.getFinishEvent(), stateContext.getContext());
                        		TransitionResult<T, S, E, C> finishResult = grandParentState.internalFire(finishContext);
                        		if(finishResult.isAccepted()) {
                        			return TransitionResultImpl.newResult(true, finishResult.getTargetState());
                        		}
                			}
        				} 
    				}
    				
    			}
    		}
    	}
    	
    	List<ImmutableTransition<T, S, E, C>> transitions = getTransitions(stateContext.getEvent());
        for(final ImmutableTransition<T, S, E, C> transition : transitions) {
        	result = transition.internalFire(stateContext);
        	if(result.isAccepted()) {
        		if(result.getTargetState().isFinalState() && !result.getTargetState().isRootState()) {
        			ImmutableState<T, S, E, C> parentState = result.getTargetState().getParentState();
    				AbstractStateMachine<T, S, E, C> abstractStateMachine = (AbstractStateMachine<T, S, E, C>)
                			stateContext.getStateMachine();
    				if(abstractStateMachine.getFinishEvent()!=null) {
                		StateContext<T, S, E, C> finishContext = FSM.newStateContext(stateContext.getStateMachine(), parentState, 
                				abstractStateMachine.getFinishEvent(), stateContext.getContext());
                		TransitionResult<T, S, E, C> finishResult = parentState.internalFire(finishContext);
                		if(finishResult.isAccepted()) 
                			result = TransitionResultImpl.newResult(true,finishResult.getTargetState());
                	} 
        		}
        		return result;
        	}
        }
        
        // fire to super state
        if(getParentState()!=null && !getParentState().isParallelState()) {
        	logger.debug("Internal notify the same event to parent state");
        	result = getParentState().internalFire(stateContext);
        }
	    return result;
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
}
