package org.squirrelframework.foundation.fsm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

public class DefaultInitialStateTest {
    private StateMachineBuilder<DefaultInitialStateMachine, TestState, TestEvent, Void> builder;

    @Before
    public void setUp() throws Exception {
        builder = StateMachineBuilderFactory.create(DefaultInitialStateMachine.class, TestState.class, TestEvent.class, Void.class);
        builder.defineDefaultInitialState(TestState.A);
        builder.transition().from(TestState.A).to(TestState.B).on(TestEvent.ToB);
        builder.transition().from(TestState.B).to(TestState.C).on(TestEvent.ToC);
    }

    @Test
    public void defineInitialState() {
        // define the initial state by builder.newStateMachine
        DefaultInitialStateMachine initWithA = builder.newStateMachine(TestState.A);
        Assert.assertEquals(TestState.A, initWithA.getInitialState());
        initWithA.fire(TestEvent.ToB);
        Assert.assertEquals(TestState.B, initWithA.getCurrentState());

        // define the initial state by builder.defineInitialStateId
        DefaultInitialStateMachine initWithDefault = builder.newStateMachine(null);
        Assert.assertEquals(TestState.A, initWithDefault.getInitialState());
        initWithDefault.fire(TestEvent.ToB);
        Assert.assertEquals(TestState.B, initWithDefault.getCurrentState());
    }

    @Test
    public void exportAndImport() {
        DefaultInitialStateMachine stateMachine = builder.newStateMachine(null);
        String sqrlScxml = stateMachine.exportXMLDefinition(true);
        System.out.println(sqrlScxml);

        UntypedStateMachineBuilder builder = new UntypedStateMachineImporter().importDefinition(sqrlScxml);
        DefaultInitialStateMachine recover = builder.newAnyStateMachine(null);
        Assert.assertEquals(TestState.A, recover.getInitialState());
    }

    static class DefaultInitialStateMachine extends AbstractStateMachine<DefaultInitialStateMachine, TestState, TestEvent, Void> {
    }
}
