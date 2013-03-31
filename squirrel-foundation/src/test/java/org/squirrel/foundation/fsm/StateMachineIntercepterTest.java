package org.squirrel.foundation.fsm;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrel.foundation.component.SquirrelPostProcessorProvider;
import org.squirrel.foundation.component.SquirrelProvider;
import org.squirrel.foundation.fsm.annotation.Transit;
import org.squirrel.foundation.fsm.annotation.Transitions;
import org.squirrel.foundation.fsm.builder.StateMachineBuilder;
import org.squirrel.foundation.fsm.impl.AbstractStateMachine;
import org.squirrel.foundation.fsm.impl.AbstractStateMachineIntercepter;
import org.squirrel.foundation.fsm.impl.StateMachineBuilderImpl;

public class StateMachineIntercepterTest extends AbstractStateMachineTest {
    
    @Transitions({
        @Transit(from="A", to="B", on="ToB"), @Transit(from="A", to="D", on="ToD", callMethod="transitFail")
    })
    static class DeclarativeStateMachine extends AbstractStateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> {

        public DeclarativeStateMachine(
                ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer>> states,
                DeclarativeStateMachine parent, Class<?> type, boolean isLeaf) {
            super(initialState, states, parent, type, isLeaf);
        }
        
        public void transitFail(TestState from, TestState to, TestEvent event, Integer context) {
            throw new RuntimeException("fail on purpose");
        }

        @Override
        protected TestEvent getInitialEvent() {
            return null;
        }

        @Override
        protected Integer getInitialContext() {
            return 0;
        }

        @Override
        protected TestEvent getTerminateEvent() {
            return null;
        }

        @Override
        protected Integer getTerminateContext() {
            return -1;
        }
    }
    
    static class TestStateMachineIntercepter extends AbstractStateMachineIntercepter<DeclarativeStateMachine, TestState, TestEvent, Integer> {
        StateMachineIntercepter<DeclarativeStateMachine, TestState, TestEvent, Integer> delegator;
        
        public TestStateMachineIntercepter(StateMachineIntercepter<DeclarativeStateMachine, TestState, TestEvent, Integer> delegator) {
            this.delegator = delegator;
        }
        
        @Override
        public void onStart(DeclarativeStateMachine stateMachine) {
            delegator.onStart(stateMachine);
        }

        @Override
        public void onTerminate(DeclarativeStateMachine stateMachine) {
            delegator.onTerminate(stateMachine);
        }

        @Override
        public void beforeOnTransition(DeclarativeStateMachine stateMachine,
                TestState sourceState, TestEvent event, Integer context) {
            delegator.beforeOnTransition(stateMachine, sourceState, event, context);
        }

        @Override
        public void onTransitionBegin(DeclarativeStateMachine stateMachine,
                TestState sourceState, TestEvent event, Integer context) {
            delegator.onTransitionBegin(stateMachine, sourceState, event, context);
        }

        @Override
        public void onTransitionComplete(DeclarativeStateMachine stateMachine,
                TestState sourceState, TestState targetState, TestEvent event,
                Integer context) {
            delegator.onTransitionComplete(stateMachine, sourceState, targetState, event, context);
        }

        @Override
        public void onTransitionDeclined(DeclarativeStateMachine stateMachine,
                TestState sourceState, TestEvent event, Integer context) {
            delegator.onTransitionDeclined(stateMachine, sourceState, event, context);
        }

        @Override
        public void onTransitionCausedException(Exception e,
                int transitionStatus, DeclarativeStateMachine stateMachine,
                TestState sourceState, TestEvent event, Integer context) {
            delegator.onTransitionCausedException(e, transitionStatus, stateMachine, sourceState, event, context);
        }

        @Override
        public void afterOnTransition(DeclarativeStateMachine stateMachine,
                TestState sourceState, TestEvent event, Integer context) {
            delegator.afterOnTransition(stateMachine, sourceState, event, context);
        }
        
    }
    
    @Mock
    StateMachineIntercepter<DeclarativeStateMachine, TestState, TestEvent, Integer> delegator;
    
    StateMachineBuilder<DeclarativeStateMachine, TestState, TestEvent, Integer> builder;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        builder = StateMachineBuilderImpl.<DeclarativeStateMachine, TestState, TestEvent, Integer>
            newStateMachineBuilder(DeclarativeStateMachine.class, TestState.class, TestEvent.class, Integer.class);
    }
    
    @Test
    public void testTransitionCompleteIntercept() {
        InOrder callSequence = Mockito.inOrder(delegator);
        SquirrelPostProcessorProvider.getInstance().register(DeclarativeStateMachine.class, 
                SquirrelProvider.getInstance().newInstance(
                        TestStateMachineIntercepter.class, 
                        new Class<?>[]{StateMachineIntercepter.class}, 
                        new Object[]{delegator}));
        DeclarativeStateMachine stateMachine = builder.newStateMachine(TestState.A, null, Object.class, true);
        stateMachine.fire(TestEvent.ToB, null);
        stateMachine.terminate();
        callSequence.verify(delegator, Mockito.times(1)).onStart(stateMachine);
        callSequence.verify(delegator, Mockito.times(1)).beforeOnTransition(stateMachine, TestState.A, TestEvent.ToB, null);
        callSequence.verify(delegator, Mockito.times(1)).onTransitionBegin(stateMachine, TestState.A, TestEvent.ToB, null);
        callSequence.verify(delegator, Mockito.times(1)).onTransitionComplete(stateMachine, TestState.A, TestState.B, TestEvent.ToB, null);
        callSequence.verify(delegator, Mockito.times(1)).afterOnTransition(stateMachine, TestState.A, TestEvent.ToB, null);
        callSequence.verify(delegator, Mockito.times(1)).onTerminate(stateMachine);
        SquirrelPostProcessorProvider.getInstance().unregister(DeclarativeStateMachine.class);
    }
    
    @Test
    public void testTransitionDeclinedIntercept() {
        InOrder callSequence = Mockito.inOrder(delegator);
        SquirrelPostProcessorProvider.getInstance().register(DeclarativeStateMachine.class, 
                SquirrelProvider.getInstance().newInstance(
                        TestStateMachineIntercepter.class, 
                        new Class<?>[]{StateMachineIntercepter.class}, 
                        new Object[]{delegator}));
        DeclarativeStateMachine stateMachine = builder.newStateMachine(TestState.A, null, Object.class, true);
        stateMachine.fire(TestEvent.ToC, null);
        stateMachine.terminate();
        callSequence.verify(delegator, Mockito.times(1)).onStart(stateMachine);
        callSequence.verify(delegator, Mockito.times(1)).beforeOnTransition(stateMachine, TestState.A, TestEvent.ToC, null);
        callSequence.verify(delegator, Mockito.times(1)).onTransitionBegin(stateMachine, TestState.A, TestEvent.ToC, null);
        callSequence.verify(delegator, Mockito.times(1)).onTransitionDeclined(stateMachine, TestState.A, TestEvent.ToC, null);
        callSequence.verify(delegator, Mockito.times(1)).afterOnTransition(stateMachine, TestState.A, TestEvent.ToC, null);
        callSequence.verify(delegator, Mockito.times(1)).onTerminate(stateMachine);
        SquirrelPostProcessorProvider.getInstance().unregister(DeclarativeStateMachine.class);
    }
    
}
