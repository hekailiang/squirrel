package org.squirrelframework.foundation.fsm;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
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
    static class ImportParameterizedActionCase extends AbstractUntypedStateMachine {
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testImportParameterizedActionFail() {
        UntypedStateMachineBuilder builder =
                StateMachineBuilderFactory.create(ImportParameterizedActionCase.class);
        builder.externalTransition().from("a").to("b").on(TestEvent.toB).perform(new UntypedAnonymousAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, 
                    UntypedStateMachine stateMachine) {
            }
        });
        ImportParameterizedActionCase sample = builder.newUntypedStateMachine("a");

        String definition = sample.exportXMLDefinition(false);
        UntypedStateMachineBuilder importedBuilder =  new UntypedStateMachineImporter().importDefinition(definition);
    }

    @Test
    public void testImportNonParameterizedActionSuccess() {
        UntypedStateMachineBuilder builder =
                StateMachineBuilderFactory.create(ImportParameterizedActionCase.class);
        builder.externalTransition().from("b").to("c").on(TestEvent.toC)
                .whenMvel("MyCondition:::(context>1&&context<10)").perform(Action.DUMMY_ACTION);
        ImportParameterizedActionCase sample = builder.newUntypedStateMachine("a");
        String xmlDefinition = sample.exportXMLDefinition(false);
        UntypedStateMachineBuilder importedBuilder = new UntypedStateMachineImporter().importDefinition(xmlDefinition);
        ImportParameterizedActionCase importedSample = importedBuilder.newAnyStateMachine("a");
        importedSample.fire(TestEvent.toB);
        assertTrue(importedSample.getCurrentState().equals("b"));
        importedSample.fire(TestEvent.toC, 5);
        assertTrue(importedSample.getCurrentState().equals("c"));
    }

    @Test
    public void testCloneStateMachineBuilder() {
        UntypedStateMachineBuilder builder =
                StateMachineBuilderFactory.create(ImportParameterizedActionCase.class);
        ImportParameterizedActionCase orgSample = builder.newUntypedStateMachine("a");
        orgSample.fire(TestEvent.toB);

        UntypedStateMachineBuilder clonedBuilder =
                new UntypedStateMachineImporter().importDefinition(orgSample.exportXMLDefinition(false));
        // add new transition after clone a new state machine builder
        clonedBuilder.externalTransition().from("a").to("c").on(TestEvent.toC);

        ImportParameterizedActionCase newSample = clonedBuilder.newAnyStateMachine("a");
        newSample.fire(TestEvent.toC);

        assertTrue(orgSample.getCurrentState().equals("b"));
        assertTrue(newSample.getCurrentState().equals("c"));
    }

}
