package org.squirrelframework.foundation.fsm;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.squirrelframework.foundation.exception.SquirrelRuntimeException;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class StateMachineImporterTest {
    
    enum TestEvent {
        toA, toB, toC, toD
    }
    
    @Transitions({
        @Transit(from="a", to="b", on="toB")
    })
    @StateMachineParameters(stateType=String.class, eventType=TestEvent.class, contextType=Integer.class)
    class ImportParameterizedActionCase extends AbstractUntypedStateMachine {
    }
    
    @Test(expected=SquirrelRuntimeException.class)
    @SuppressWarnings("unused")
    public void testImportParameterizedActionFail() {
        final Integer param1 = 10;
        final String param2 = "Hello World!";
        UntypedStateMachineBuilder builder = 
                StateMachineBuilderFactory.create(ImportParameterizedActionCase.class);
        builder.externalTransition().from("a").to("b").on(TestEvent.toB).perform(new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, 
                    UntypedStateMachine stateMachine) {
            }
        });
        ImportParameterizedActionCase sample = builder.newUntypedStateMachine("a");
        
        UntypedStateMachineBuilder importedBuilder = 
                new UntypedStateMachineImporter().importDefinition(sample.exportXMLDefinition(false));
    }
    
    @Test(expected=SquirrelRuntimeException.class)
    @SuppressWarnings("unused")
    public void testImportParameterizedActionSuccess() {
        final Integer param1 = 10;
        final String param2 = "Hello World!";
        UntypedStateMachineBuilder builder = 
                StateMachineBuilderFactory.create(ImportParameterizedActionCase.class);
        builder.externalTransition().from("a").to("b").on(TestEvent.toB).perform(new UntypedAnonymousAction() {
            {
                // user need to make sure there always no-args constructor exits
            }
            @Override
            public void execute(Object from, Object to, Object event, Object context, 
                    UntypedStateMachine stateMachine) {
            }
        });
        ImportParameterizedActionCase sample = builder.newUntypedStateMachine("a");
        
        UntypedStateMachineBuilder importedBuilder = 
                new UntypedStateMachineImporter().importDefinition(sample.exportXMLDefinition(false));
        ImportParameterizedActionCase importedSample = importedBuilder.newAnyStateMachine("a");
        importedSample.fire(TestEvent.toB);
        assertTrue(importedSample.getCurrentState().equals("a"));
    }

}
