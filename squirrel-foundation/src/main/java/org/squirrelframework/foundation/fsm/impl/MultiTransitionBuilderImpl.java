package org.squirrelframework.foundation.fsm.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.squirrelframework.foundation.component.SquirrelComponent;
import org.squirrelframework.foundation.fsm.*;
import org.squirrelframework.foundation.fsm.builder.*;

import java.util.List;
import java.util.Map;

/**
 * Created by kailianghe on 7/12/14.
 */
class MultiTransitionBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C> implements
        MultiTransitionBuilder<T, S, E, C>, MultiFrom<T, S, E, C>, From<T, S, E, C>, MultiTo<T, S, E, C>,
        To<T, S, E, C>, On<T, S, E, C>, Between<T, S, E, C>, And<T, S, E, C>, SquirrelComponent {

    private final List<MutableTransition<T, S, E, C>> transitions = Lists.newArrayList();

    private final List<MutableState<T, S, E, C>> sourceStates = Lists.newArrayList();

    private final List<MutableState<T, S, E, C>> targetStates = Lists.newArrayList();

    private final Map<S, MutableState<T, S, E, C>> states;

    private final int priority;

    private final ExecutionContext executionContext;

    private final TransitionType transitionType;

    MultiTransitionBuilderImpl(Map<S, MutableState<T, S, E, C>> states, TransitionType transitionType,
                               int priority, ExecutionContext executionContext) {
        this.states = states;
        this.transitionType = transitionType;
        this.priority = priority;
        this.executionContext = executionContext;
    }

    @Override
    public MultiTo<T, S, E, C> toAmong(S... stateIds) {
        for(S stateId : stateIds) {
            targetStates.add(FSM.getState(states, stateId));
        }
        return this;
    }

    @Override
    public To<T, S, E, C> to(S stateId) {
        targetStates.add(FSM.getState(states, stateId));
        return this;
    }

    @Override
    public To<T, S, E, C> toFinal(S stateId) {
        MutableState<T, S, E, C> targetState = FSM.getState(states, stateId);
        if(!targetState.isFinalState()) {
            targetState.setFinal(true);
        }
        targetStates.add(targetState);
        return this;
    }

    @Override
    public MultiFrom<T, S, E, C> from(S stateId) {
        sourceStates.add(FSM.getState(states, stateId));
        return this;
    }

    @Override
    public From<T, S, E, C> fromAmong(S... stateIds) {
        for(S stateId : stateIds) {
            sourceStates.add(FSM.getState(states, stateId));
        }
        return this;
    }

    @Override
    public Between<T, S, E, C> between(S fromStateId) {
        sourceStates.add(FSM.getState(states, fromStateId));
        return this;
    }

    @Override
    public And<T, S, E, C> and(S stateId) {
        targetStates.add(FSM.getState(states, stateId));
        return this;
    }

    @Override
    public On<T, S, E, C> onMutual(E fromEvent, E toEvent) {
        MutableTransition<T, S, E, C> forwardTransition = sourceStates.get(0).addTransitionOn(fromEvent);
        forwardTransition.setTargetState(targetStates.get(0));
        forwardTransition.setType(transitionType);
        forwardTransition.setPriority(priority);
        transitions.add(forwardTransition);

        MutableTransition<T, S, E, C> backwardTransition = targetStates.get(0).addTransitionOn(toEvent);
        backwardTransition.setTargetState(sourceStates.get(0));
        backwardTransition.setType(transitionType);
        backwardTransition.setPriority(priority);
        transitions.add(backwardTransition);
        return this;
    }

    @Override
    public On<T, S, E, C> on(E event) {
        for(MutableState<T, S, E, C> sourceState : sourceStates) {
            for(MutableState<T, S, E, C> targetState : targetStates) {
                MutableTransition<T, S, E, C> transition = sourceState.addTransitionOn(event);
                transition.setTargetState(targetState);
                transition.setType(transitionType);
                transition.setPriority(priority);
                transitions.add(transition);
            }
        }
        return this;
    }

    @Override
    public On<T, S, E, C> onEach(E... events) {
        Preconditions.checkNotNull(events);
        int eventLength = events.length;
        for(int i=0, srcStateLength=sourceStates.size(); i<srcStateLength; ++i) {
            for(int j=0, tarStateLength=targetStates.size(); j<tarStateLength; ++j) {
                int statePos = j<i ? i : j;
                int eventPos = statePos<eventLength ? statePos : eventLength-1;
                MutableTransition<T, S, E, C> transition = sourceStates.get(i).addTransitionOn(events[eventPos]);
                transition.setTargetState(targetStates.get(j));
                transition.setType(transitionType);
                transition.setPriority(priority);
                transitions.add(transition);
            }
        }
        return this;
    }

    @Override
    public When<T, S, E, C> when(Condition<C> condition) {
        for(MutableTransition<T, S, E, C> transition : transitions) {
            transition.setCondition(condition);
        }
        return this;
    }

    @Override
    public When<T, S, E, C> whenMvel(String expression) {
        for(MutableTransition<T, S, E, C> transition : transitions) {
            Condition<C> cond = FSM.newMvelCondition(expression, executionContext.getScriptManager());
            transition.setCondition(cond);
        }
        return this;
    }

    @Override
    public void perform(Action<T, S, E, C> action) {
        for(MutableTransition<T, S, E, C> transition : transitions) {
            transition.addAction(action);
        }
    }

    @Override
    public void perform(List<? extends Action<T, S, E, C>> actions) {
        int actionSize = actions.size();
        for(int i=0, transitionLength=transitions.size(); i<transitionLength; ++i) {
            int actionPos = i<actionSize ? i : actionSize-1;
            transitions.get(i).addAction(actions.get(actionPos));
        }
    }

    @Override
    public void evalMvel(String expression) {
        for(MutableTransition<T, S, E, C> transition : transitions) {
            Action<T, S, E, C> action = FSM.newMvelAction(expression, executionContext);
            transition.addAction(action);
        }
    }

    @Override
    public void callMethod(String methodName) {
        String[] methods = StringUtils.split(methodName, '|');
        int methodsLength = methods.length;

        for(int i=0, transitionLength=transitions.size(); i<transitionLength; ++i) {
            int methodPos = i<methodsLength ? i : methodsLength-1;
            String theMethodName = methods[methodPos].trim();
            if("_".equals(theMethodName)) continue;
            Action<T, S, E, C> action = FSM.newMethodCallActionProxy(theMethodName, executionContext);
            transitions.get(i).addAction(action);
        }
    }
}
