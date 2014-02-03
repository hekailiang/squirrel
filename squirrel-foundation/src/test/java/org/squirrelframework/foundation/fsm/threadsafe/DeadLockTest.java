package org.squirrelframework.foundation.fsm.threadsafe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.AnonymousAction;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;

public class DeadLockTest {
    
    private UntypedStateMachine[] getTestingFSMInstances() {
        UntypedStateMachineBuilder builder1 = StateMachineBuilderFactory.create(ConcurrentSimpleStateMachine.class);
        builder1.transition().from("A").to("B").on("FIRST").perform(
                new AnonymousAction<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void execute(Object from, Object to, Object event,
                    Object context, UntypedStateMachine stateMachine) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UntypedStateMachine fsm2 = (UntypedStateMachine)context;
                System.out.println("fsm2 current state: "+fsm2.getCurrentState());
            }
        });
        
        UntypedStateMachineBuilder builder2 = StateMachineBuilderFactory.create(ConcurrentSimpleStateMachine.class);
        builder2.transition().from("C").to("D").on("SECOND").perform(
                new AnonymousAction<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void execute(Object from, Object to, Object event,
                    Object context, UntypedStateMachine stateMachine) {
                UntypedStateMachine fsm1 = (UntypedStateMachine)context;
                System.out.println("fsm1 current state: "+fsm1.getCurrentState());
            }
        });
        
        final UntypedStateMachine fsm1 = builder1.newStateMachine("A");
        fsm1.start();
        final UntypedStateMachine fsm2 = builder2.newStateMachine("C");
        fsm2.start();
        
        return new UntypedStateMachine[]{fsm1, fsm2};
    }
    
    @Test
    public void testDeadLockProblem() {
        final UntypedStateMachine[] fsm = getTestingFSMInstances();
        final CountDownLatch eventCondition = new CountDownLatch(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                fsm[0].fire("FIRST", fsm[1]);
                eventCondition.countDown();
            }
        }, "Test-Thread-1").start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                fsm[1].fire("SECOND", fsm[0]);
                eventCondition.countDown();
            }
        }, "Test-Thread-2").start();
        
        try {
            boolean isJobDone = eventCondition.await(1000, TimeUnit.MILLISECONDS);
            Assert.assertTrue(isJobDone==false); // due to dead lock, job cannot be done properly
        } catch (InterruptedException e) {
        }
    }
    
    @Test(timeout=100)
    public void testDeadLockFixed() {
        final UntypedStateMachine[] fsm = getTestingFSMInstances();
        final CountDownLatch eventCondition = new CountDownLatch(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (fsm[0]) {
                    synchronized (fsm[1]) {
                        fsm[0].fire("FIRST", fsm[1]);
                    }
                }
                eventCondition.countDown();
            }
        }, "Test-Thread-1").start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (fsm[0]) {
                    synchronized (fsm[1]) {
                        fsm[1].fire("SECOND", fsm[0]);
                    }
                }
                eventCondition.countDown();
            }
        }, "Test-Thread-2").start();
        
        try {
            boolean isJobDone = eventCondition.await(50, TimeUnit.MILLISECONDS);
            Assert.assertTrue(isJobDone);
        } catch (InterruptedException e) {
        }
    }

}
