package org.squirrelframework.foundation.fsm.threadsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.fsm.AnonymousAction;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineData;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.AsyncExecute;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionBegin;

@RunWith(Parameterized.class)
public class ConcurrentEventTest {
    
    private UntypedStateMachineBuilder builder = null;
    
    private static final int TIME_INTERVAL = 50;
    
    // repeat 10 times for each test case
    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[10][0]);
    }
    
    @Before
    public void setUp() throws Exception {
        builder = StateMachineBuilderFactory.create(ConcurrentSimpleStateMachine.class);
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testConcurrentEvents() { 
        // test concurrent read/write/test/dump state machine
        final CountDownLatch actionCondition = new CountDownLatch(1);
        final CountDownLatch eventCondition = new CountDownLatch(5);
        final AtomicReference<Object> testStateRef = new AtomicReference<Object>();
        final AtomicReference<Object> dumpDataRef  = new AtomicReference<Object>();
        final AtomicReference<Object> readStateRef = new AtomicReference<Object>();
        
        builder.transition().from("A").to("B").on("FIRST").perform(
                new AnonymousAction<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void execute(Object from, Object to, Object event,
                    Object context, UntypedStateMachine stateMachine) {
                actionCondition.countDown();
                try {
                    TimeUnit.MILLISECONDS.sleep(TIME_INTERVAL);
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
        }, "Test-Thread-1").start();
        
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
        }, "Test-Thread-2").start();
        
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
        }, "Test-Thread-3").start();
        
        // thread 4 dump data while processing event "FIRST"
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    actionCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dumpDataRef.set(fsm.dumpSavedData());
                eventCondition.countDown();
            }
        }, "Test-Thread-4").start();
        
        // thread 5 process event "SECOND" while processing event "FIRST"
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
        }, "Test-Thread-5").start();
        
        // wait for all threads finish processing events then check result
        try {
            eventCondition.await(1000, TimeUnit.MILLISECONDS);
            TimeUnit.MILLISECONDS.sleep(TIME_INTERVAL);
        } catch (InterruptedException e) {
            fail();
        }
        
        assertEquals(fsm.getCurrentState(), "C");
        assertEquals(readStateRef.get(), "C");
        assertEquals(testStateRef.get(), "E");
        assertNotNull(dumpDataRef.get());
        assertEquals(((StateMachineData.Reader)dumpDataRef.get()).currentState(), "C");
        
        Object testAgain = fsm.test("SECOND");
        assertEquals(testAgain, "E");
    }
    
    @Test
    @SuppressWarnings("unused")
    public void testConcurrentAddRemoveListener() {
        final CountDownLatch l1Condition = new CountDownLatch(1);
        final CountDownLatch l3Condition = new CountDownLatch(1);
        final CountDownLatch eventCondition = new CountDownLatch(2);
        
        class MockCallSequence {
            @Mock
            MockCallSequence mock;
            public void listener1() {
                mock.listener1();
            }
            public void listener2() {
                mock.listener2();
            }
            public void listener3() {
                mock.listener3();
            }
        }
        
        class Listener1 {
            MockCallSequence callSequence;
            @OnTransitionBegin
            public void onTransitionBegin() {
                l3Condition.countDown();
                try {
                    l1Condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callSequence.listener1();
            }
        }
        
        class Listener2 {
            MockCallSequence callSequence;
            @OnTransitionBegin
            public void onTransitionBegin() {
                callSequence.listener2();
            }
        }
        
        class Listener3 {
            MockCallSequence callSequence;
            @OnTransitionBegin
            public void onTransitionBegin() {
                callSequence.listener3();
            }
        }
        
        MockCallSequence callSequence = new MockCallSequence();
        final Listener1 l1 = new Listener1();
        l1.callSequence = callSequence;
        final Listener2 l2 = new Listener2();
        l2.callSequence = callSequence;
        final Listener3 l3 = new Listener3();
        l3.callSequence = callSequence;
        MockitoAnnotations.initMocks(callSequence);
        
        builder.transition().from("A").to("B").on("FIRST");
        builder.transition().from("B").to("C").on("SECOND");
        final UntypedStateMachine fsm = builder.newStateMachine("A");
        fsm.addDeclarativeListener(l1);
        fsm.addDeclarativeListener(l2);
        
        InOrder inOrder = Mockito.inOrder(callSequence.mock);
        // thread 1 fire event "FIRST"
        new Thread(new Runnable() {
            @Override
            public void run() {
                fsm.fire("FIRST");
                eventCondition.countDown();
            }
        }, "Test-Thread-1").start();
        
        // thread 2 add listener and fire event "SECOND" during thread 1 processing event "FIRST"
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    l3Condition.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fsm.addDeclarativeListener(l3);
                l1Condition.countDown();
                fsm.fire("SECOND");
                eventCondition.countDown();
            }
        }, "Test-Thread-2").start();
        
        // wait for all threads finish processing events then check result
        try {
            eventCondition.await(1000, TimeUnit.MILLISECONDS);
            TimeUnit.MILLISECONDS.sleep(TIME_INTERVAL);
        } catch (InterruptedException e) {
            fail();
        }
        
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener1();
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener2();
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener1();
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener2();
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener3();
    }
    
    @Test
    @SuppressWarnings("unused")
    public void testAnsyncDispatchEvent() {
        final CountDownLatch l1Condition = new CountDownLatch(1);
        final CountDownLatch lCondition = new CountDownLatch(2);
        final CountDownLatch eventCondition = new CountDownLatch(3);
        final AtomicReference<Thread> executorThread1 = new AtomicReference<Thread>();
        final AtomicReference<Thread> executorThread2 = new AtomicReference<Thread>();
        final AtomicReference<Thread> executorThread3 = new AtomicReference<Thread>();
        final Thread mainThread = Thread.currentThread();
        class MockCallSequence {
            @Mock
            MockCallSequence mock;
            public void listener1() {
//                System.out.println("listener1");
                mock.listener1();
            }
            public void listener2() {
//                System.out.println("listener2");
                mock.listener2();
            }
            public void listener3() {
//                System.out.println("listener3");
                mock.listener3();
            }
        }
        
        class Listener1 {
            MockCallSequence callSequence;
            @OnTransitionBegin
            public void onTransitionBegin() {
                callSequence.listener1();
                executorThread1.set(Thread.currentThread());
                lCondition.countDown();
                eventCondition.countDown();
            }
        }
        
        class Listener2 {
            MockCallSequence callSequence;
            @OnTransitionBegin
            @AsyncExecute
            public void onTransitionBegin() {
                try {
                    l1Condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callSequence.listener2();
                executorThread2.set(Thread.currentThread());
                eventCondition.countDown();
            }
        }
        
        class Listener3 {
            MockCallSequence callSequence;
            @OnTransitionBegin
            public void onTransitionBegin() {
                callSequence.listener3();
                executorThread3.set(Thread.currentThread());
                lCondition.countDown();
                eventCondition.countDown();
            }
        }
        
        MockCallSequence callSequence = new MockCallSequence();
        final Listener1 l1 = new Listener1();
        l1.callSequence = callSequence;
        final Listener2 l2 = new Listener2();
        l2.callSequence = callSequence;
        final Listener3 l3 = new Listener3();
        l3.callSequence = callSequence;
        MockitoAnnotations.initMocks(callSequence);
        
        builder.transition().from("A").to("B").on("FIRST");
        final UntypedStateMachine fsm = builder.newStateMachine("A");
        fsm.addDeclarativeListener(l1);
        fsm.addDeclarativeListener(l2);
        fsm.addDeclarativeListener(l3);
        
        InOrder inOrder = Mockito.inOrder(callSequence.mock);
        fsm.fire("FIRST");
        try {
            lCondition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        l1Condition.countDown();
        
        try {
            eventCondition.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail();
        }
        
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener1();
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener3();
        inOrder.verify(callSequence.mock, Mockito.times(1)).listener2();
        assertTrue(executorThread1.get()!=null && executorThread1.get()==mainThread);
        assertTrue(executorThread3.get()!=null && executorThread3.get()==mainThread);
        assertTrue(executorThread2.get()!=null && executorThread2.get()!=mainThread);
    }
}
