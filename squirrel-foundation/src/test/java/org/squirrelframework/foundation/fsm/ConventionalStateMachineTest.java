package org.squirrelframework.foundation.fsm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.squirrelframework.foundation.fsm.TestEvent.InternalA;
import static org.squirrelframework.foundation.fsm.TestEvent.ToB;
import static org.squirrelframework.foundation.fsm.TestEvent.ToC;
import static org.squirrelframework.foundation.fsm.TestEvent.ToD;
import static org.squirrelframework.foundation.fsm.TestEvent.ToEnd;
import static org.squirrelframework.foundation.fsm.TestState.A;
import static org.squirrelframework.foundation.fsm.TestState.B;
import static org.squirrelframework.foundation.fsm.TestState.C;
import static org.squirrelframework.foundation.fsm.TestState.D;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;

public class ConventionalStateMachineTest extends AbstractStateMachineTest {
    
    interface CallSequenceMonitor {
        void beforeEntryAny(TestState from, TestState to, TestEvent event, Integer context);
        void entryA(TestState from, TestState to, TestEvent event, Integer context);
        void entryB(TestState from, TestState to, TestEvent event, Integer context);
        void entryC(TestState from, TestState to, TestEvent event, Integer context);
        void entryD(TestState from, TestState to, TestEvent event, Integer context);
        void afterEntryAny(TestState from, TestState to, TestEvent event, Integer context);
        
        void transitFromAToAOnInternalA(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromAToBOnToB(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromBToCOnToC(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToDWhenCondition$1(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToDWhenCondition$2(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToD(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToD(TestState from, TestState to, TestEvent event, Integer context);
        
        void transitFromCToAnyOnToD(TestState from, TestState to, TestEvent event, Integer context);
        void onToD(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromDToFinalOnToEnd(TestState from, TestState to, TestEvent event, Integer context);
        
        void beforeExitAny(TestState from, TestState to, TestEvent event, Integer context);
        void exitA(TestState from, TestState to, TestEvent event, Integer context);
        void exitB(TestState from, TestState to, TestEvent event, Integer context);
        void exitC(TestState from, TestState to, TestEvent event, Integer context);
        void exitD(TestState from, TestState to, TestEvent event, Integer context);
        void afterExitAny(TestState from, TestState to, TestEvent event, Integer context);
        
        void beforeTransitionBegin(TestState from, TestEvent event, Integer context);
        void afterTransitionCompleted(TestState from, TestState to, TestEvent event, Integer context);
        void afterTransitionDeclined(TestState from, TestEvent event, Integer context);
        void afterTransitionCausedException(Exception e, int transitionStatus, TestState fromState, 
                TestState toState, TestEvent event, Integer context);
        
        void terminate();
    }
    
    @SuppressWarnings("serial")
    static class ConventionalStateMachineException extends RuntimeException {
    }
    
    static class ConventionalStateMachineImpl extends AbstractStateMachine<ConventionalStateMachineImpl, TestState, TestEvent, Integer> {
        
        private final CallSequenceMonitor monitor;
        
        public ConventionalStateMachineImpl(
                ImmutableState<ConventionalStateMachineImpl, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<ConventionalStateMachineImpl, TestState, TestEvent, Integer>> states,
                ConventionalStateMachineImpl parent, Class<?> type, boolean isLeaf, CallSequenceMonitor monitor) {
            super(initialState, states, parent, type, isLeaf);
            this.monitor = monitor;
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
        
        protected void beforeEntryAny(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.beforeEntryAny(from, to, event, context);
        }
        
        protected void entryA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryA(from, to, event, context);
        }
        
        protected void entryB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryB(from, to, event, context);
        }

        protected void entryC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryC(from, to, event, context);
        }

        protected void entryD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryD(from, to, event, context);
        }
        
        protected void afterEntryAny(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.afterEntryAny(from, to, event, context);
        }

        protected void transitFromAToAOnInternalA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromAToAOnInternalA(from, to, event, context);
        }

        protected void transitFromAToBOnToB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromAToBOnToB(from, to, event, context);
        }

        protected void transitFromBToCOnToC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromBToCOnToC(from, to, event, context);
        }

        protected void transitFromCToDOnToDWhenCondition$1(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromCToDOnToDWhenCondition$1(from, to, event, context);
        }
        
        protected void transitFromCToDOnToDWhenCondition$2(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromCToDOnToDWhenCondition$2(from, to, event, context);
            throw new ConventionalStateMachineException();
        }
        
        protected void transitFromCToDOnToD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromCToDOnToD(from, to, event, context);
        }
        
        protected void transitFromCToD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromCToD(from, to, event, context);
        }
        
        protected void transitFromCToAnyOnToD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromCToAnyOnToD(from, to, event, context);
        }
        
        protected void onToD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.onToD(from, to, event, context);
        }

        protected void transitFromDToFinalOnToEnd(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromDToFinalOnToEnd(from, to, event, context);
        }
        
        protected void beforeExitAny(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.beforeExitAny(from, to, event, context);
        }

        protected void exitA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitA(from, to, event, context);
        }

        protected void exitB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitB(from, to, event, context);
        }

        protected void exitC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitC(from, to, event, context);
        }

        protected void exitD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitD(from, to, event, context);
        }
        
        protected void afterExitAny(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.afterExitAny(from, to, event, context);
        }
        
        @Override
        public void beforeTransitionBegin(TestState from, TestEvent event, Integer context) {
            super.beforeTransitionBegin(from, event, context);
            monitor.beforeTransitionBegin(from, event, context);
        }
        
        @Override
        public void afterTransitionCompleted(TestState from, TestState to, TestEvent event, Integer context) {
            super.afterTransitionCompleted(from, to, event, context);
            monitor.afterTransitionCompleted(from, to, event, context);
        }
        
        @Override
        public void afterTransitionDeclined(TestState from, TestEvent event, Integer context) {
            super.afterTransitionDeclined(from, event, context);
            monitor.afterTransitionDeclined(from, event, context);
        }
        
        @Override
        public void afterTransitionCausedException(Exception e, int transitionStatus, TestState fromState, 
                TestState toState, TestEvent event, Integer context) {
            super.afterTransitionCausedException(e, transitionStatus, fromState, toState, event, context);
            throw new ConventionalStateMachineException();
        }
        
        @Override
        public void terminate() {
            super.terminate();
            monitor.terminate();
        }
    }
    
    @Mock
    private CallSequenceMonitor monitor;
    
    private ConventionalStateMachineImpl stateMachine;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StateMachineBuilder<ConventionalStateMachineImpl, TestState, TestEvent, Integer> builder = 
                StateMachineBuilderImpl.<ConventionalStateMachineImpl, TestState, TestEvent, Integer>
                newStateMachineBuilder(ConventionalStateMachineImpl.class, TestState.class, 
                        TestEvent.class, Integer.class, CallSequenceMonitor.class);
        builder.transition().from(A).to(B).on(ToB);
        builder.transition().within(A).on(InternalA);
        builder.transition().from(B).to(C).on(ToC);
        builder.transition().from(C).to(D).on(ToD).when(new Condition<Integer>() {
            @Override
            public boolean isSatisfied(Integer context) {
                return context!=null && context>80;
            }
        });
        builder.transition().from(C).to(D).on(ToD).when(new Condition<Integer>() {
            @Override
            public boolean isSatisfied(Integer context) {
                return context!=null && context>=60 && context<=80;
            }
        });
        builder.transition().from(D).toFinal().on(ToEnd);
        stateMachine = builder.newStateMachine(A, null, Object.class, true, monitor);
    }
    
    @Test
    public void testInternalTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).beforeExitAny(A, null, InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).exitA(A, null, InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).afterExitAny(A, null, InternalA, null);
        callSequence.verify(monitor, Mockito.times(1)).transitFromAToAOnInternalA(A, A, InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).beforeEntryAny(null, A, InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).entryA(null, A, InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).afterEntryAny(null, A, InternalA, null);
        assertThat(stateMachine.getCurrentState(), equalTo(A));
    }
    
    @Test
    public void testExternalTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeExitAny(A, null, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).exitA(A, null, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterExitAny(A, null, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).transitFromAToBOnToB(A, B, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeEntryAny(null, B, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).entryB(null, B, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterEntryAny(null, B, ToB, null);
        assertThat(stateMachine.getCurrentState(), equalTo(B));
    }
    
    @Test
    public void testDeclinedTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(ToB, null);
        stateMachine.fire(ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(B, ToB, null);
        callSequence.verify(monitor, Mockito.times(0)).exitB(B, null, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionDeclined(B, ToB, null);
        assertThat(stateMachine.getCurrentState(), equalTo(B));
    }
    
    @Test
    public void testConditionalTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(ToB, null);
        stateMachine.fire(ToC, null);
        stateMachine.fire(ToD, 50);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(C, ToD, 50);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionDeclined(C, ToD, 50);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.C));
        
        stateMachine.fire(ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(C, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).beforeExitAny(C, null, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).exitC(C, null, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).afterExitAny(C, null, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).transitFromCToDOnToDWhenCondition$1(C, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(0)).transitFromCToDOnToDWhenCondition$2(C, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).transitFromCToDOnToD(C, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).transitFromCToAnyOnToD(C, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).transitFromCToD(C, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).onToD(C, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).beforeEntryAny(null, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).entryD(null, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).afterEntryAny(null, D, ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionCompleted(C, D, ToD, 81);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.D));
    }
    
    @Test(expected=ConventionalStateMachineException.class)
    public void testTransitionWithException() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(ToB, null);
        stateMachine.fire(ToC, null);
        stateMachine.fire(ToD, 60);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(D, ToD, 60);
        callSequence.verify(monitor, Mockito.times(1)).exitD(D, null, ToD, 60);
        callSequence.verify(monitor, Mockito.times(1)).transitFromCToDOnToDWhenCondition$2(D, A, ToD, 60);
    }
    
    @Test
    public void testTransitToFinalState() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(ToB, null);
        stateMachine.fire(ToC, null);
        stateMachine.fire(ToD, 81);
        
        stateMachine.fire(ToEnd, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(D, ToEnd, null);
        callSequence.verify(monitor, Mockito.times(1)).exitD(D, null, ToEnd, null);
        callSequence.verify(monitor, Mockito.times(1)).transitFromDToFinalOnToEnd(D, null, ToEnd, null);
        callSequence.verify(monitor, Mockito.times(0)).terminate();
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionCompleted(D, null, ToEnd, null);
        
        assertThat(stateMachine.getStatus(), equalTo(StateMachineStatus.TERMINATED));
    }
}
