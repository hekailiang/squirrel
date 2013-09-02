package org.squirrelframework.foundation.fsm.atm;

import java.util.Map;

import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.atm.ATMStateMachine.ATMState;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachineWithoutContext;

public class ATMStateMachine extends AbstractStateMachineWithoutContext<ATMStateMachine, ATMState, String> {
    
    enum ATMState {
        Idle, Loading, OutOfService, Disconnected, InService
    }
    
    private StringBuilder logger = new StringBuilder();
    
    protected ATMStateMachine(
            ImmutableState<ATMStateMachine, ATMState, String, Void> initialState,
            Map<ATMState, ImmutableState<ATMStateMachine, ATMState, String, Void>> states) {
        super(initialState, states);
    }
    
    public void entryIdle(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("entryIdle");
    }
    
    public void exitIdle(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("exitIdle");
    }
    
    public void transitFromIdleToLoadingOnConnected(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromIdleToLoadingOnConnected");
    }
    
    public void entryLoading(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("entryLoading");
    }
    
    public void exitLoading(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("exitLoading");
    }
    
    public void transitFromLoadingToInServiceOnLoadSuccess(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromLoadingToInServiceOnLoadSuccess");
    }
    
    public void transitFromLoadingToOutOfServiceOnLoadFail(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromLoadingToOutOfServiceOnLoadFail");
    }
    
    public void transitFromLoadingToDisconnectedOnConnectionClosed(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromLoadingToDisconnectedOnConnectionClosed");
    }
    
    public void entryOutOfService(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("entryOutOfService");
    }
    
    public void transitFromOutOfServiceToDisconnectedOnConnectionLost(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromOutOfServiceToDisconnectedOnConnectionLost");
    }
    
    public void transitFromOutOfServiceToInServiceOnStartup(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromOutOfServiceToInServiceOnStartup");
    }
    
    public void exitOutOfService(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("exitOutOfService");
    }
    
    public void entryDisconnected(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("entryDisconnected");
    }
    
    public void transitFromDisconnectedToInServiceOnConnectionRestored(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromDisconnectedToInServiceOnConnectionRestored");
    }
    
    public void exitDisconnected(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("exitDisconnected");
    }
    
    public void entryInService(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("entryInService");
    }
    
    public void transitFromInServiceToOutOfServiceOnShutdown(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromInServiceToOutOfServiceOnShutdown");
    }
    
    public void transitFromInServiceToDisconnectedOnConnectionLost(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("transitFromInServiceToDisconnectedOnConnectionLost");
    }
    
    public void exitInService(ATMState from, ATMState to, String event) {
        addOptionalDot();
        logger.append("exitInService");
    }
    
    private void addOptionalDot() {
        if (logger.length() > 0) {
            logger.append('.');
        }
    }
    
    public String consumeLog() {
        final String result = logger.toString();
        logger = new StringBuilder();
        return result;
    }
}
