package org.squirrelframework.foundation.fsm.atm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.SCXMLVisitor;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.UntypedStateMachineImporter;
import org.squirrelframework.foundation.fsm.atm.ATMStateMachine.ATMState;

public class ATMStateMachineTest {
    
    @AfterClass
    public static void afterTest() {
        ConverterProvider.INSTANCE.clearRegistry();
        SquirrelPostProcessorProvider.getInstance().clearRegistry();
    }
    
    private ATMStateMachine stateMachine;
    
    @Before
    public void setup() {
        StateMachineBuilder<ATMStateMachine, ATMState, String, Void> builder = StateMachineBuilderFactory.create(
                ATMStateMachine.class, ATMState.class, String.class, Void.class);
        builder.externalTransition().from(ATMState.Idle).to(ATMState.Loading).on("Connected");
        builder.externalTransition().from(ATMState.Loading).to(ATMState.Disconnected).on("ConnectionClosed");
        builder.externalTransition().from(ATMState.Loading).to(ATMState.InService).on("LoadSuccess");
        builder.externalTransition().from(ATMState.Loading).to(ATMState.OutOfService).on("LoadFail");
        builder.externalTransition().from(ATMState.OutOfService).to(ATMState.Disconnected).on("ConnectionLost");
        builder.externalTransition().from(ATMState.OutOfService).to(ATMState.InService).on("Startup");
        builder.externalTransition().from(ATMState.InService).to(ATMState.OutOfService).on("Shutdown");
        builder.externalTransition().from(ATMState.InService).to(ATMState.Disconnected).on("ConnectionLost");
        builder.externalTransition().from(ATMState.Disconnected).to(ATMState.InService).on("ConnectionRestored");
        
        stateMachine = builder.newStateMachine(ATMState.Idle);
    }
    
    @After
    public void teardown() {
        if(stateMachine!=null && stateMachine.getStatus()!=StateMachineStatus.TERMINATED) {
            stateMachine.terminate(null);
        }
    }
    
    @Test
    public void testIdelToInService() {
        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("entryIdle")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Idle)));
        
        stateMachine.fire("Connected");
        assertThat(stateMachine.consumeLog(), is(equalTo("exitIdle.transitFromIdleToLoadingOnConnected.entryLoading")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Loading)));
        
        stateMachine.fire("LoadSuccess");
        assertThat(stateMachine.consumeLog(), is(equalTo("exitLoading.transitFromLoadingToInServiceOnLoadSuccess.entryInService")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.InService)));
        
        stateMachine.fire("Shutdown");
        assertThat(stateMachine.consumeLog(), is(equalTo("exitInService.transitFromInServiceToOutOfServiceOnShutdown.entryOutOfService")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.OutOfService)));
        
        stateMachine.fire("ConnectionLost");
        assertThat(stateMachine.consumeLog(), is(equalTo("exitOutOfService.transitFromOutOfServiceToDisconnectedOnConnectionLost.entryDisconnected")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Disconnected)));
        
        stateMachine.fire("ConnectionRestored");
        assertThat(stateMachine.consumeLog(), is(equalTo("exitDisconnected.transitFromDisconnectedToInServiceOnConnectionRestored.entryInService")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.InService)));
    }
    
    @Test
    public void exportAndImportATMStateMachine() {
        SCXMLVisitor visitor = SquirrelProvider.getInstance().newInstance(SCXMLVisitor.class);
        stateMachine.accept(visitor);
        // visitor.convertSCXMLFile("ATMStateMachine", true);
        String xmlDef = visitor.getScxml(false);
        UntypedStateMachineBuilder builder = new UntypedStateMachineImporter().importDefinition(xmlDef);
        
        ATMStateMachine stateMachine = builder.newAnyStateMachine(ATMState.Idle);
        stateMachine.start();
        assertThat(stateMachine.consumeLog(), is(equalTo("entryIdle")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Idle)));
        
        stateMachine.fire("Connected");
        assertThat(stateMachine.consumeLog(), is(equalTo("exitIdle.transitFromIdleToLoadingOnConnected.entryLoading")));
        assertThat(stateMachine.getCurrentState(), is(equalTo(ATMState.Loading)));
    }

}
