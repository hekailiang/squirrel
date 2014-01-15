package org.squirrelframework.foundation.fsm.threadsafe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.AnonymousAction;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;

public class ConcurrentEventTest {
    
    private UntypedStateMachineBuilder builder = null;
    
    @Before
    public void setUp() throws Exception {
        builder = StateMachineBuilderFactory.create(ConcurrentSimpleStateMachine.class);
    }
    
    @Test
    public void testConcurrentEvents() { 
        // test concurrent read/write/test state machine
        final CountDownLatch actionCondition = new CountDownLatch(1);
        final CountDownLatch eventCondition = new CountDownLatch(4);
        final AtomicReference<Object> testStateRef = new AtomicReference<Object>();
        final AtomicReference<Object> readStateRef = new AtomicReference<Object>();
        
        builder.transition().from("A").to("B").on("FIRST").perform(
                new AnonymousAction<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void execute(Object from, Object to, Object event,
                    Object context, UntypedStateMachine stateMachine) {
                actionCondition.countDown();
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.transition().from("B").to("C").on("SECOND");
        
        builder.transition().from("A").to("D").on("SECOND");
        builder.transition().from("C").to("E").on("SECOND");
        
        final UntypedStateMachine fsm = builder.newStateMachine("A");
        
        // thread 1 start process event "FIRST"
        new Thread(new Runnable() {
            @Override
            public void run() {
                fsm.fire("FIRST");
                eventCondition.countDown();
            }
        }).start();
        
        // thread 2 read fsm state while processing event "FIRST"
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    actionCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                readStateRef.set(fsm.getCurrentState());
                eventCondition.countDown();
            }
        }).start();
        
        // thread 3 test event "SECOND" while processing event "FIRST"
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    actionCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                testStateRef.set(fsm.test("SECOND"));
                eventCondition.countDown();
            }
        }).start();
        
        // thread 4 process event "SECOND" while processing event "FIRST"
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    actionCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fsm.fire("SECOND");
                eventCondition.countDown();
            }
        }).start();
        
        // wait for three threads finish processing events then check result
        try {
            eventCondition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Assert.assertEquals(fsm.getCurrentState(), "C");
        Assert.assertEquals(readStateRef.get(), "C");
        Assert.assertNull(testStateRef.get());
        
        Object testAgain = fsm.test("SECOND");
        Assert.assertEquals(testAgain, "E");
    }
    
}
