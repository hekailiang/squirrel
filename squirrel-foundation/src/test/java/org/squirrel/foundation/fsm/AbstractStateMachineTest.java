package org.squirrel.foundation.fsm;

import org.junit.BeforeClass;
import org.squirrel.foundation.fsm.Converter;
import org.squirrel.foundation.fsm.impl.ConverterProvider;

public class AbstractStateMachineTest {
    
    @BeforeClass
    public static void beforeTest() {
        ConverterProvider.getInstance().register(TestEvent.class, new Converter.EnumConverter<TestEvent>(TestEvent.class));
        ConverterProvider.getInstance().register(TestState.class, new Converter.EnumConverter<TestState>(TestState.class));
    }
    
}
