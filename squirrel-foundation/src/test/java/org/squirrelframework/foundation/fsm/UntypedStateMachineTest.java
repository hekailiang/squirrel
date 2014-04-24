package org.squirrelframework.foundation.fsm;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.component.IdProvider;
import org.squirrelframework.foundation.fsm.StateMachine.StateMachineEvent;
import org.squirrelframework.foundation.fsm.StateMachine.StateMachineListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionBeginEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionBeginListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionEndEvent;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionEndListener;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionEvent;
import org.squirrelframework.foundation.fsm.annotation.OnAfterActionExecuted;
import org.squirrelframework.foundation.fsm.annotation.OnBeforeActionExecuted;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionBegin;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionComplete;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionDecline;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionEnd;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class UntypedStateMachineTest {
    
    enum TestEvent {
        toA, toB, toC, toD
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB", callMethod="fromAToB"),
        @Transit(from="b", to="c", on="toC"),
        @Transit(from="c", to="d", on="toD")
    })
    @StateMachineParameters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    static class UntypedStateMachineSample extends AbstractUntypedStateMachine {
        
        @Mock
        private UntypedStateMachineSample mockObject;
        
        protected void fromAToB(String from, String to, TestEvent event, Integer context) {
            mockObject.fromAToB(from, to, event, context);
        }
        
        protected void ontoB(String from, String to, TestEvent event, Integer context) {}
        
        protected void transitFromdToaOntoA(String from, String to, TestEvent event, Integer context) {
            mockObject.transitFromdToaOntoA(from, to, event, context);
        }
        
        public UntypedStateMachineSample mockedObject() {
            return mockObject;
        }
    }
    
    private UntypedStateMachineSample fsm;
    
    private UntypedStateMachineBuilder builder;
    
    @After
    public void teardown() {
        if(fsm.getStatus()!=StateMachineStatus.TERMINATED)
            fsm.terminate(null);
    }
    
    
    @Before
    public void setup() {
        builder = StateMachineBuilderFactory.create(UntypedStateMachineSample.class);
        builder.externalTransition().from("d").to("a").on(TestEvent.toA);
        fsm = builder.newUntypedStateMachine("a");
        MockitoAnnotations.initMocks(fsm);
    }
    
    @Test
    public void testUntypedStateMachine() {
        UntypedStateMachineSample mockedObject = fsm.mockedObject();
        InOrder callSequence = Mockito.inOrder(mockedObject);
        
        assertTrue(fsm.getInitialState().equals("a"));
        assertNull(fsm.getCurrentState());
        fsm.fire(TestEvent.toB, 1);
        assertTrue(fsm.getCurrentState().equals("b"));
        fsm.fire(TestEvent.toC, 2);
        assertTrue(fsm.getCurrentState().equals("c"));
        fsm.fire(TestEvent.toD, 3);
        assertTrue(fsm.getCurrentState().equals("d"));
        fsm.fire(TestEvent.toA, 4);
        assertTrue(fsm.getCurrentState().equals("a"));
        
        callSequence.verify(mockedObject, Mockito.times(1)).fromAToB("a", "b", TestEvent.toB, 1);
        callSequence.verify(mockedObject, Mockito.times(1)).transitFromdToaOntoA("d", "a", TestEvent.toA, 4);
    }
    
    @Test
    public void testTransitionEvent() {
        final AtomicInteger smCallTimes = new AtomicInteger(0);
        final AtomicInteger tCallTimes  = new AtomicInteger(0);
        final AtomicInteger teCallTimes = new AtomicInteger(0);
        final AtomicInteger tbCallTimes = new AtomicInteger(0);
        final AtomicInteger tcCallTimes = new AtomicInteger(0);
        fsm.addStateMachineListener(new StateMachineListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void stateMachineEvent(StateMachineEvent<UntypedStateMachine, Object, Object, Object> event) {
                smCallTimes.incrementAndGet();
            }
        });
        fsm.addListener(TransitionEvent.class, new StateMachineListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void stateMachineEvent(StateMachineEvent<UntypedStateMachine, Object, Object, Object> event) {
                tCallTimes.incrementAndGet();
            }
        }, "stateMachineEvent");
        fsm.addTransitionEndListener(new TransitionEndListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionEnd(TransitionEndEvent<UntypedStateMachine, Object, Object, Object> event) {
                teCallTimes.incrementAndGet();
            }
        });
        fsm.addTransitionBeginListener(new TransitionBeginListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionBegin(TransitionBeginEvent<UntypedStateMachine, Object, Object, Object> event) {
                tbCallTimes.incrementAndGet();
            }
        });
        fsm.addTransitionCompleteListener(new TransitionCompleteListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionComplete(TransitionCompleteEvent<UntypedStateMachine, Object, Object, Object> event) {
                tcCallTimes.incrementAndGet();
            }
        });
        // StateMachineStart, TransitionBegin, TransitionComplete, TransitionEnd
        fsm.fire(TestEvent.toB, 1);
        // TransitionBegin, TransitionDeclined, TransitionEnd
        fsm.fire(TestEvent.toB, 1);
        assertTrue(smCallTimes.get()==7);
        assertTrue( tCallTimes.get()==6);
        assertTrue(teCallTimes.get()==2);
        assertTrue(tbCallTimes.get()==2);
        assertTrue(tcCallTimes.get()==1);
    }
    
    static class TestListenTarget {
        final AtomicInteger teCallTimes = new AtomicInteger(0);
        final AtomicInteger tbCallTimes = new AtomicInteger(0);
        final AtomicInteger tcCallTimes = new AtomicInteger(0);
        final AtomicInteger tdCallTimes = new AtomicInteger(0);
        final AtomicInteger tcCallTimesCausedByToD = new AtomicInteger(0);
        final AtomicInteger adCallTimes = new AtomicInteger(0);
        final AtomicInteger acCallTimes = new AtomicInteger(0);
        
        @OnTransitionEnd
        @OnTransitionComplete
        public void transitionEnd() {
            teCallTimes.incrementAndGet();
        }
        
        @OnTransitionEnd
        @OnTransitionBegin
        public void transitionBegin(TestEvent event) {
            assertTrue(event==TestEvent.toB);
            tbCallTimes.incrementAndGet();
        }
        
        @OnTransitionComplete
        public void transitionComplete(String from, String to, TestEvent event, Integer context) {
            assertTrue(from.equals("a"));
            assertTrue(to.equals("b"));
            assertTrue(event==TestEvent.toB);
            assertTrue(context==1);
            tcCallTimes.incrementAndGet();
        }
        
        @OnTransitionComplete(when="event.name().equals(\"toD\")")
        public void transitionCompleteCausedByToD() {
            tcCallTimesCausedByToD.incrementAndGet();
        }
        
        @OnTransitionDecline
        public void transitionDeclined(String from, TestEvent event, Integer context) {
            assertTrue(from.equals("b"));
            assertTrue(event==TestEvent.toB);
            assertTrue(context==2);
            tdCallTimes.incrementAndGet();
        }
        
        @OnBeforeActionExecuted
        public void transitionAction(String from, String to, TestEvent event, Integer context, int[] mOfn) {
            assertTrue(from.equals("a"));
            assertTrue(to.equals("b"));
            assertTrue(event==TestEvent.toB);
            assertTrue(context==1);
            assertTrue(mOfn[0]==1||mOfn[0]==2);
            assertTrue(mOfn[1]==2);
            adCallTimes.incrementAndGet();
        }
        
        @OnAfterActionExecuted
        public void afterTransitionAction(String from, String to, TestEvent event, Integer context, int[] mOfn) {
            acCallTimes.incrementAndGet();
        }
    }
    
    @Test
    public void testTransitionDeclarativeEvent() {
        TestListenTarget listenTarget = new TestListenTarget();
        fsm.addDeclarativeListener(listenTarget);
        assertTrue(fsm.getListenerSize()==7);
        assertTrue(fsm.getExecutorListenerSize()==2);
        // StateMachineStart, TransitionBegin, TransitionComplete, TransitionEnd
        fsm.fire(TestEvent.toB, 1);
        // TransitionBegin, TransitionDeclined, TransitionEnd
        fsm.fire(TestEvent.toB, 2);
        assertTrue(listenTarget.tbCallTimes.get()==4);
        assertTrue(listenTarget.teCallTimes.get()==3);
        assertTrue(listenTarget.tcCallTimes.get()==1);
        assertTrue(listenTarget.tdCallTimes.get()==1);
        assertTrue(listenTarget.adCallTimes.get()==2);
        assertTrue(listenTarget.acCallTimes.get()==2);
        assertTrue(listenTarget.tcCallTimesCausedByToD.get()==0);
        
        fsm.removeDeclarativeListener(listenTarget);
        assertTrue(fsm.getListenerSize()==0);
        assertTrue(fsm.getExecutorListenerSize()==0);
    }
    
    @Test
    public void testAttachDuplicateDeclarativeEvent() {
        TestListenTarget listenTarget = new TestListenTarget();
        fsm.addDeclarativeListener(listenTarget);
        fsm.addDeclarativeListener(listenTarget);
        assertTrue(fsm.getListenerSize()==14);           // double called
        assertTrue(fsm.getExecutorListenerSize()==4);    // double called
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB")
    })
    static class UntypedStateMachineSample2 extends AbstractUntypedStateMachine implements ParamtersAnnotationPlaceHolder {
    }
    
    @StateMachineParameters(stateType=String.class, eventType=String.class, contextType=String.class)
    interface ParamtersAnnotationPlaceHolder extends UntypedStateMachine {}
    
    static class TestListenTarget2 {
        final AtomicInteger teCallTimes = new AtomicInteger(0);
        final AtomicInteger tbCallTimes = new AtomicInteger(0);
        final AtomicInteger tcCallTimes = new AtomicInteger(0);
        final AtomicInteger tdCallTimes = new AtomicInteger(0);
        
        @OnTransitionEnd
        public void transitionEnd(UntypedStateMachine stateMachine) {
            assertTrue(stateMachine!=null);
            teCallTimes.incrementAndGet();
        }
        
        @OnTransitionBegin
        public void transitionBegin(String from, String event) {
            assertTrue(event.equals("toB"));
            tbCallTimes.incrementAndGet();
        }
        
        @OnTransitionComplete
        public void transitionComplete(String from, String to, String event, String context) {
            assertTrue(from.equals("a"));
            assertTrue(to.equals("b"));
            assertTrue(event.equals("toB"));
            assertTrue(context.equals("1"));
            tcCallTimes.incrementAndGet();
        }
        
        @OnTransitionDecline
        public void transitionDeclined(String from, String event, String context) {
            assertTrue(from.equals("b"));
            assertTrue(event.equals("toB"));
            assertTrue(context.equals("2"));
            tdCallTimes.incrementAndGet();
        }
    }
    
    @Test
    public void testParameterValueInfer() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineSample2.class);
        UntypedStateMachineSample2 fsm2= builder.newUntypedStateMachine("a");
        
        TestListenTarget2 listenTarget = new TestListenTarget2();
        fsm2.addDeclarativeListener(listenTarget);
        assertTrue(fsm2.getListenerSize()==4);
        assertTrue(fsm2.getExecutorListenerSize()==0);
        // StateMachineStart, TransitionBegin, TransitionComplete, TransitionEnd
        fsm2.fire("toB", "1");
        // TransitionBegin, TransitionDeclined, TransitionEnd
        fsm2.fire("toB", "2");
        assertTrue(listenTarget.tbCallTimes.get()==2);
        assertTrue(listenTarget.teCallTimes.get()==2);
        assertTrue(listenTarget.tcCallTimes.get()==1);
        assertTrue(listenTarget.tdCallTimes.get()==1);
        
        fsm2.removeDeclarativeListener(listenTarget);
        assertTrue(fsm2.getListenerSize()==0);
        assertTrue(fsm2.getExecutorListenerSize()==0);
    }
    
    @Test
    public void testIgnoreExtraParam() {
        fsm = builder.newUntypedStateMachine("a", 1, 2, 3, 4);
        MockitoAnnotations.initMocks(fsm);
        fsm.fire(TestEvent.toB);
        assertTrue(fsm.getCurrentState().equals("b"));
    }
    
    @Test(expected=RuntimeException.class)
    public void testParamterTypeCheck1() {
        fsm.fire("TypeNotCorrect", 1);
    }
    
    @Test(expected=RuntimeException.class)
    public void testParamterTypeCheck2() {
        fsm.fire(TestEvent.toB, "TypeNotCorrect");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testStateMachineConfiguration() {
        fsm = builder.newUntypedStateMachine("a", 
                StateMachineConfiguration.create()
                .enableAutoStart(false)
                .setIdProvider(IdProvider.UUIDProvider.getInstance()), 
                (Object[])null);
        System.out.println("StateMachine ID: "+fsm.getIdentifier());
        fsm.fire(TestEvent.toA);
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB")
    })
    @StateMachineParameters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    static class UntypedStateMachineSampleEx1 extends AbstractUntypedStateMachine {
        final int param1;
        protected UntypedStateMachineSampleEx1(int param1) {
            this.param1 = param1;
        }
    }
    
    @Test
    public void testCreateUntypedStateMachineWithExtraSingleParams() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(
                UntypedStateMachineSampleEx1.class, int.class);
        UntypedStateMachineSampleEx1 fsmEx= builder.newUntypedStateMachine("a", 123);
        assertTrue(fsmEx.param1==123);
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB")
    })
    @StateMachineParameters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    static class UntypedStateMachineSampleEx2 extends AbstractUntypedStateMachine {
        final Integer param1;
        final String param2;
        protected UntypedStateMachineSampleEx2(Integer param1, String param2) {
            this.param1 = param1;
            this.param2 = param2;
        }
    }
    
    @Test
    public void testCreateUntypedStateMachineWithExtraTwoParams() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(
                UntypedStateMachineSampleEx2.class, Integer.class, String.class);
        UntypedStateMachineSampleEx2 fsmEx= builder.newUntypedStateMachine("a", 10, "Hello World");
        assertTrue(fsmEx.param1==10);
        assertTrue(fsmEx.param2.equals("Hello World"));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testInitialStateMachineFailBecauseMissingParam() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(
                UntypedStateMachineSampleEx2.class, Integer.class, String.class);
        builder.newUntypedStateMachine("a");
    }
    
    @Test
    public void testActionExecutionPosition() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(
                UntypedStateMachineSampleEx2.class, Integer.class, String.class);
        builder.onEntry("b").perform(new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                System.out.println("Entry B");
            }
        });
        builder.onExit("a").perform(new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                System.out.println("Exit A");
            }
        });
        builder.transition().from("a").to("b").on(TestEvent.toB).perform(new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                System.out.println("Transit from A to B");
            }
        });
        UntypedStateMachineSampleEx2 fsm = builder.newUntypedStateMachine("a", 10, "Hello World");
        final StringBuilder strBuilder = new StringBuilder();
        fsm.addDeclarativeListener(new Object() {
            @SuppressWarnings("unused")
            @OnBeforeActionExecuted
            public void onBeforeActionExecuted(int[] mOfN) {
                if(strBuilder.length()>0)
                    strBuilder.append("-");
                strBuilder.append(mOfN[0]).append(".").append(mOfN[1]);
            }
        });
        fsm.fire(TestEvent.toB);
        assertTrue("1.3-2.3-3.3".equals(strBuilder.toString()));
    }
    
    @Test
    public void testCreateUntypedStateMachineWithExtraTwoParamsExportAndImport() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(
                UntypedStateMachineSampleEx2.class, Integer.class, String.class);
        UntypedStateMachineSampleEx2 fsmEx= builder.newUntypedStateMachine("a", 10, "Hello World");
        
        UntypedStateMachineBuilder importedBuilder = 
            new UntypedStateMachineImporter().importDefinition(fsmEx.exportXMLDefinition(false));
        UntypedStateMachineSampleEx2 fsmEx2 =
            importedBuilder.newUntypedStateMachine("a", 11, "Hello World!");
        assertTrue(fsmEx2.param1==11);
        assertTrue(fsmEx2.param2.equals("Hello World!"));
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB", whenMvel="context<10", priority=12),   //  if(context<10)
        @Transit(from="a", to="c", on="toB", whenMvel="context<20", priority=11),   //  else if(context>=10 && context<20)
        @Transit(from="a", to="d", on="toB", priority=10)                           //  else ... context>=20
    })
    @StateMachineParameters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    static class UntypedStateMachineSampleEx3 extends AbstractUntypedStateMachine {
    }
    
    @Test
    public void testCreateUntypedStateMachinePrioritizedTransition() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineSampleEx3.class);
        UntypedStateMachineSampleEx3 fsmEx= builder.newUntypedStateMachine("a");
        fsmEx.fire(TestEvent.toB, 0);
        assertTrue(fsmEx.getCurrentState().equals("b"));

        fsmEx= builder.newUntypedStateMachine("a");
        fsmEx.fire(TestEvent.toB, 15);
        assertTrue(fsmEx.getCurrentState().equals("c"));
        
        fsmEx= builder.newUntypedStateMachine("a");
        fsmEx.fire(TestEvent.toB, 25);
        assertTrue(fsmEx.getCurrentState().equals("d"));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testUntypedStateMachineConfiguration() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(UntypedStateMachineSampleEx3.class);
        builder.setStateMachineConfiguration(StateMachineConfiguration.create().enableAutoStart(false));
        UntypedStateMachineSampleEx3 fsmEx= builder.newUntypedStateMachine("a");
        fsmEx.fire(TestEvent.toB, 0);
    }
}
