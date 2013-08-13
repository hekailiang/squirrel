package org.squirrelframework.foundation.fsm.impl;

import java.util.List;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Actions;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.Conditions;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.MutableTransition;
import org.squirrelframework.foundation.fsm.StateContext;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.TransitionType;
import org.squirrelframework.foundation.fsm.Visitor;

class TransitionImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements MutableTransition<T, S, E, C> {
    
    private ImmutableState<T, S, E, C> sourceState;
    
    private ImmutableState<T, S, E, C> targetState;
    
    private E event;
    
    private Actions<T, S, E, C> actions = FSM.newActions();
    
    private Condition<C> condition = Conditions.always();
    
    private TransitionType type = TransitionType.EXTERNAL;
    
    private int priority;

    @Override
    public ImmutableState<T, S, E, C> getSourceState() {
        return sourceState;
    }

    @Override
    public ImmutableState<T, S, E, C> getTargetState() {
        return targetState;
    }

    @Override
    public List<Action<T, S, E, C>> getActions() {
        return actions.getAll();
    }

    @Override
    public ImmutableState<T, S, E, C> transit(final StateContext<T, S, E, C> stateContext) {
        for(final Action<T, S, E, C> action : getActions()) {
        	stateContext.getExecutor().defer(action,
        			sourceState.getStateId(), targetState.getStateId(), stateContext.getEvent(), 
                    stateContext.getContext(), stateContext.getStateMachine().getThis());
        }
        return targetState;
    }

    @Override
    public void setSourceState(ImmutableState<T, S, E, C> state) {
        this.sourceState = state;
    }

    @Override
    public void setTargetState(ImmutableState<T, S, E, C> state) {
        this.targetState = state;
    }

    @Override
    public void addAction(Action<T, S, E, C> newAction) {
        actions.add(newAction);
    }

    @Override
    public void addActions(List<Action<T, S, E, C>> newActions) {
        actions.addAll(newActions);
    }

    @Override
    public Condition<C> getCondition() {
        return condition;
    }

    @Override
    public void setCondition(Condition<C> condition) {
        this.condition = condition;
    }

    @Override
    public E getEvent() {
        return event;
    }

    @Override
    public void setEvent(E event) {
        this.event = event;
    }
    
    @Override
    public TransitionType getType() {
        return type;
    }
    
    @Override
    public void setType(TransitionType type) {
        this.type = type;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    private void doTransit(ImmutableState<T, S, E, C> source, ImmutableState<T, S, E, C> target, StateContext<T, S, E, C> stateContext) {
    	if (source.getLevel() < target.getLevel() && type == TransitionType.EXTERNAL) {
    		// exit and re-enter current state for external transition to child state
    		source.exit(stateContext);
    		source.entry(stateContext);
    	}
    	doTransitInternal(source, target, stateContext);
    }
    
    /**
	 * Recursively traverses the state hierarchy, exiting states along the way, performing the action, and entering states to the target.
	 * <hr>
	 * There exist the following transition scenarios:
	 * <ul>
	 * <li>0. there is no target state (internal transition) --> handled outside this method.</li>
	 * <li>1. The source and target state are the same (self transition) --> perform the transition directly: Exit source state, perform
	 * transition actions and enter target state</li>
	 * <li>2. The target state is a direct or indirect sub-state of the source state --> perform the transition actions, then traverse the
	 * hierarchy from the source state down to the target state, entering each state along the way. No state is exited.
	 * <li>3. The source state is a sub-state of the target state --> traverse the hierarchy from the source up to the target, exiting each
	 * state along the way. Then perform transition actions. Finally enter the target state.</li>
	 * <li>4. The source and target state share the same super-state</li>
	 * <li>5. All other scenarios:
	 * <ul>
	 * <li>a. The source and target states reside at the same level in the hierarchy but do not share the same direct super-state</li>
	 * <li>b. The source state is lower in the hierarchy than the target state</li>
	 * <li>c. The target state is lower in the hierarchy than the source state</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param source the source state
	 * @param target the target state
	 * @param stateContext the state context
	 */
    private void doTransitInternal(ImmutableState<T, S, E, C> source, ImmutableState<T, S, E, C> target, StateContext<T, S, E, C> stateContext) {
		if (source == this.getTargetState()) {
			// Handles 1.
			// Handles 3. after traversing from the source to the target.
			if(type==TransitionType.LOCAL) {
				// not exit and re-enter the composite (source) state for 
				// local transition
				transit(stateContext);
			} else {
				source.exit(stateContext);
				transit(stateContext);
				getTargetState().entry(stateContext);
			}
		} else if (source == target) {
			// Handles 2. after traversing from the target to the source.
			transit(stateContext);
		} else if (source.getParentState() == target.getParentState()) {
			// Handles 4.
			// Handles 5a. after traversing the hierarchy until a common ancestor if found.
			source.exit(stateContext);
			transit(stateContext);
			target.entry(stateContext);
		} else {
			// traverses the hierarchy until one of the above scenarios is met.
			if (source.getLevel() > target.getLevel()) {
				// Handles 3.
				// Handles 5b.
				source.exit(stateContext);
				doTransitInternal(source.getParentState(), target, stateContext);
			} else if (source.getLevel() < target.getLevel()) {
				// Handles 2.
				// Handles 5c.
				doTransitInternal(source, target.getParentState(), stateContext);
				target.entry(stateContext);
			} else {
				// Handles 5a.
				source.exit(stateContext);
				doTransitInternal(source.getParentState(), target.getParentState(), stateContext);
				target.entry(stateContext);
			}
		}
	}
    
    @Override
    public void internalFire(StateContext<T, S, E, C> stateContext) {
    	if(condition.isSatisfied(stateContext.getContext())) {
    		ImmutableState<T, S, E, C> newState = stateContext.getSourceState();
        	if(type==TransitionType.INTERNAL) {
        		newState = transit(stateContext);
        	} else {
        		// exit origin states
        		unwindSubStates(stateContext.getSourceState(), stateContext);
        		// perform transition actions
        		doTransit(getSourceState(), getTargetState(), stateContext);
        		// enter new states
        		newState = getTargetState().enterByHistory(stateContext);
        	}
        	stateContext.getResult().setAccepted(true).setTargetState(newState);
    	}
    }
    
    private void unwindSubStates(ImmutableState<T, S, E, C> orgState, StateContext<T, S, E, C> stateContext) {
		for (ImmutableState<T, S, E, C> state=orgState; state!=getSourceState(); state=state.getParentState()) {
			if(state!=null) { state.exit(stateContext); }
		}
	}
    
    @Override
    public void accept(Visitor<T, S, E, C> visitor) {
        visitor.visitOnEntry(this);
        visitor.visitOnExit(this);
    }
    
    @Override
    public boolean isMatch(S fromState, S toState, E event) {
        if(toState==null && !getTargetState().isFinalState())
            return false;
        if(toState!=null && !getTargetState().isFinalState() && 
                !getTargetState().getStateId().equals(toState))
            return false;
        if(!getEvent().equals(event)) 
            return false;
        return true;
    }
    
    @Override
    public boolean isMatch(S fromState, S toState, E event, Class<?> condClazz, TransitionType type) {
        if(!isMatch(fromState, toState, event))
            return false;
        if(getCondition().getClass()!=condClazz)
            return false;
        if(!getType().equals(type))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return sourceState + "-[" + event.toString() +", "+
                condition.getClass().getSimpleName()+ "]->" + targetState;
    }

    @Override
    public void verify() {
        if(type==TransitionType.INTERNAL && sourceState!=targetState) {
            throw new RuntimeException(String.format("Internal transition source state '%s' " +
            		"and target state '%s' must be same.", sourceState, targetState));
        }
    }
}
