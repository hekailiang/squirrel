/**
 * 
 */
package org.squirrelframework.foundation.fsm;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class PerformanceTest {

    enum FSMEvent {
        ToA, ToB, ToC, ToD
    }

    @StateMachineParameters(stateType = String.class, eventType = FSMEvent.class, contextType = Integer.class)
    static class StateMachineSample extends AbstractUntypedStateMachine {
        protected StateMachineSample(ImmutableUntypedState initialState, Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
    }

    @Test(timeout = 10000)
    public void manyTransitions() {
        performTest(10000, false);
    }
    
    @Test
    public void manyTransitionWithMonitor() {
        performTest(1000, true);
    }
    
    void performTest(final int iterTimes, final boolean addPerfMonitor) {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        Action<UntypedStateMachine, Object, Object, Object> action = new AnonymousAction<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                // System.out.println(to);
            }
        };

        builder.externalTransition().from("D").to("A").on(FSMEvent.ToA).perform(action);
        builder.externalTransition().from("A").to("B").on(FSMEvent.ToB).perform(action);
        builder.externalTransition().from("B").to("C").on(FSMEvent.ToC).perform(action);
        builder.externalTransition().from("C").to("D").on(FSMEvent.ToD).perform(action);

        final UntypedStateMachine fsm1 = builder.newStateMachine("D");
        final UntypedStateMachine fsm2 = builder.newStateMachine("D");
        
        Runnable showPerfResult = null;
        if(addPerfMonitor) {
            final StateMachinePerformanceMonitor performanceMonitor = 
                    new StateMachinePerformanceMonitor(fsm1.getClass().getName());
            fsm1.addDeclarativeListener(performanceMonitor);
            fsm2.addDeclarativeListener(performanceMonitor);
            
            showPerfResult = new Runnable() {
                @Override
                public void run() {
                    fsm1.removeDeclarativeListener(performanceMonitor);
                    fsm2.removeDeclarativeListener(performanceMonitor);
                    StateMachinePerformanceModel perfModel = performanceMonitor.getPerfModel();
                    long totalTimes = 2*4*iterTimes;
                    assertEquals(perfModel.getTotalTransitionInvokedTimes(), totalTimes);
                    assertEquals(perfModel.getTotalActionInvokedTimes(), totalTimes);
                    System.out.println(perfModel);
                }
            };
        }
        
        final CountDownLatch eventCondition = new CountDownLatch(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < iterTimes; i++) {
                    fsm1.fire(FSMEvent.ToA, 10);
                    fsm1.fire(FSMEvent.ToB, 10);
                    fsm1.fire(FSMEvent.ToC, 10);
                    fsm1.fire(FSMEvent.ToD, 10);
                }
                eventCondition.countDown();
            }
        }).start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < iterTimes; i++) {
                    fsm2.fire(FSMEvent.ToA, 10);
                    fsm2.fire(FSMEvent.ToB, 10);
                    fsm2.fire(FSMEvent.ToC, 10);
                    fsm2.fire(FSMEvent.ToD, 10);
                }
                eventCondition.countDown();
            }
        }).start();
        
        try {
            eventCondition.await();
        } catch (InterruptedException e) {
        }
        
        if(showPerfResult!=null) {
            showPerfResult.run();
        }
    }
}
