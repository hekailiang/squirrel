package org.squirrelframework.foundation.component;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.component.Heartbeat;
import org.squirrelframework.foundation.component.SquirrelProvider;

public class HeartBeatTest {
    
    interface MethodCalls {
        void method1();
        void method2();
        void method3();
        void method4();
    }
    
    @Mock 
    MethodCalls methodCalls;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHeartBeat() {
        InOrder callSequence = Mockito.inOrder(methodCalls);
        Heartbeat hb = SquirrelProvider.getInstance().newInstance(Heartbeat.class);
        hb.begin();
        hb.defer(new Runnable() {
            @Override
            public void run() {
                methodCalls.method1();
            }
        });
        hb.defer(new Runnable() {
            @Override
            public void run() {
                methodCalls.method3();
            }
        });
        callSequence.verify(methodCalls, Mockito.times(0)).method1();
        callSequence.verify(methodCalls, Mockito.times(0)).method2();
        callSequence.verify(methodCalls, Mockito.times(0)).method3();
        callSequence.verify(methodCalls, Mockito.times(0)).method4();
        hb.begin();
        hb.defer(new Runnable() {
            @Override
            public void run() {
                methodCalls.method2();
            }
        });
        hb.defer(new Runnable() {
            @Override
            public void run() {
                methodCalls.method4();
            }
        });
        callSequence.verify(methodCalls, Mockito.times(0)).method1();
        callSequence.verify(methodCalls, Mockito.times(0)).method2();
        callSequence.verify(methodCalls, Mockito.times(0)).method3();
        callSequence.verify(methodCalls, Mockito.times(0)).method4();
        hb.execute();
        callSequence.verify(methodCalls, Mockito.times(0)).method1();
        callSequence.verify(methodCalls, Mockito.times(1)).method2();
        callSequence.verify(methodCalls, Mockito.times(0)).method3();
        callSequence.verify(methodCalls, Mockito.times(1)).method4();
        hb.execute();
        callSequence.verify(methodCalls, Mockito.times(1)).method1();
        callSequence.verify(methodCalls, Mockito.times(0)).method2();
        callSequence.verify(methodCalls, Mockito.times(1)).method3();
        callSequence.verify(methodCalls, Mockito.times(0)).method4();
    }
}
