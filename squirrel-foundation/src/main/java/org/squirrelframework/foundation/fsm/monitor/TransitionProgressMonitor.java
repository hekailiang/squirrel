package org.squirrelframework.foundation.fsm.monitor;

import org.squirrelframework.foundation.fsm.ActionExecutionService;
import org.squirrelframework.foundation.fsm.ActionExecutionService.ExecActionEvent;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.impl.MethodCallActionImpl;

public class TransitionProgressMonitor<T extends StateMachine<T, S, E, C>, S, E, C> implements ActionExecutionService.ExecActionLisenter<T, S, E, C> {
	@Override
    public void beforeExecute(ExecActionEvent<T, S, E, C> event) {
	    if(event.getExecutionTarget() instanceof MethodCallActionImpl) {
	    	String actionName = ((MethodCallActionImpl<T, S, E, C>)event.getExecutionTarget()).getName();
	    	System.out.println("Exection method call action \""+actionName+" \" "+ 
	    			event.getMOfN()[0] + " of "+event.getMOfN()[1]+".");
	    }
    }
}
