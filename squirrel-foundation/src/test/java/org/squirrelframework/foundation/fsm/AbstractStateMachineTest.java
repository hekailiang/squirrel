package org.squirrelframework.foundation.fsm;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;

public class AbstractStateMachineTest {
    
    @BeforeClass
    public static void beforeTest() {
        ConverterProvider.INSTANCE.register(TestEvent.class, new Converter.EnumConverter<TestEvent>(TestEvent.class));
        ConverterProvider.INSTANCE.register(TestState.class, new Converter.EnumConverter<TestState>(TestState.class));
    }
    
    @AfterClass
	public static void afterTest() {
		ConverterProvider.INSTANCE.clearRegistry();
	}
}
