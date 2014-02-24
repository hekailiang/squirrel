/**
 * 
 */
package org.squirrelframework.foundation.fsm;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CyclicBarrier;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;
import org.squirrelframework.foundation.fsm.threadsafe.BarrierThread;

public class PerformanceTest {

    enum FSMEvent {
        ToA, ToB, ToC, ToD
    }

    @StateMachineParameters(stateType = String.class, eventType = FSMEvent.class, contextType = Integer.class)
    static class StateMachineSample extends AbstractUntypedStateMachine {
    }

    @Test(timeout = 10000)
    public void manyTransitions() {
        performTest(10000, false, null);
    }
    
    @Test
    public void manyTransitionWithMonitor() {
        performTest(1000, true, null);
    }
    
//    @Test
//    public void comparePerfMonitorOverload() {
//        Runnable task = new Runnable() {
//            @Override
//            public void run() {
//                calculatePi(100);
//            }
//        };
//        
//        int testIter = 100;
//        for(int i=0; i<4; ++i) {
//            performTest(testIter, false, task);
//            performTest(testIter, true, task);
//        }
//        
//        int iterTimes = 20;
//        float overloadTimes=0;
//        for(int i=0; i<iterTimes; ++i) {
//            Stopwatch watch1 = new Stopwatch().start();
//            performTest(testIter, false, task);
//            long time1 = watch1.stop().elapsedMillis();
//            System.out.println("Task 1 finished in "+time1+"ms.");
//            
//            Stopwatch watch2 = new Stopwatch().start();
//            performTest(testIter, true, task);
//            long time2 = watch2.stop().elapsedMillis();
//            System.out.println("Task 2 finished in "+time2+"ms.");
//            float overloadTime = time2-time1;
//            overloadTimes+=overloadTime;
//            System.out.println("--------------------------------------");
//        }
//        System.out.println("Average overload for each transition is "+
//                String.format("%.4f", overloadTimes/iterTimes/testIter/4)+"ms.");
//    }
//    
//    private BigDecimal calculatePi(int iterTimes) {
//        BigDecimal sum = new BigDecimal(0);        // final sum
//        BigDecimal term = new BigDecimal(0);       // term without sign
//        BigDecimal sign = new BigDecimal(1.0);     // sign on each term
//
//        BigDecimal one = new BigDecimal(1.0);
//        BigDecimal two = new BigDecimal(2.0);
//
//        for (int k = 0; k < iterTimes; k++) {
//           BigDecimal count = new BigDecimal(k); 
//           //term = 1.0/(2.0*k + 1.0);
//           BigDecimal temp1 = two.multiply(count);
//           BigDecimal temp2 = temp1.add(one);
//           term = one.divide(temp2,50,BigDecimal.ROUND_FLOOR);
//           //sum = sum + sign*term;
//           BigDecimal temp3 = sign.multiply(term);
//           sum = sum.add(temp3);
//           sign = sign.negate();
//        }
//        BigDecimal pi = new BigDecimal(0);
//        BigDecimal four = new BigDecimal(4);
//        pi = sum.multiply(four);
//        return pi;
//    }
    
    void performTest(final int iterTimes, final boolean addPerfMonitor, final Runnable task) {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        UntypedAnonymousAction action = new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                if(task!=null) task.run();
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
//                    System.out.println(perfModel);
                }
            };
        }
        
        CyclicBarrier entryBarrier = new CyclicBarrier(2);
        CyclicBarrier exitBarrier = new CyclicBarrier(3);
        new BarrierThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < iterTimes; i++) {
                    fsm1.fire(FSMEvent.ToA, 10);
                    fsm1.fire(FSMEvent.ToB, 10);
                    fsm1.fire(FSMEvent.ToC, 10);
                    fsm1.fire(FSMEvent.ToD, 10);
                }
            }
        }, "Performance-Test-Thread-1", entryBarrier, exitBarrier).start();
        
        new BarrierThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < iterTimes; i++) {
                    fsm2.fire(FSMEvent.ToA, 10);
                    fsm2.fire(FSMEvent.ToB, 10);
                    fsm2.fire(FSMEvent.ToC, 10);
                    fsm2.fire(FSMEvent.ToD, 10);
                }
            }
        }, "Performance-Test-Thread-2", entryBarrier, exitBarrier).start();
        
        try {
            exitBarrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
        
        if(showPerfResult!=null) {
            showPerfResult.run();
        }
    }
}
