package org.squirrelframework.foundation.fsm;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.annotation.*;

public class StateMachineLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(StateMachineLogger.class);

    private Stopwatch transitionWatch;

    private Stopwatch actionWatch;

    private final StateMachine<?,?,?,?> stateMachine;

    private final String stateMachineLabel;

    public StateMachineLogger(StateMachine<?,?,?,?> stateMachine) {
        this.stateMachine = stateMachine;
        this.stateMachineLabel = stateMachine.getClass().getSimpleName()
                +"("+stateMachine.getIdentifier()+")";
    }

    public void startLogging() {
        stateMachine.addDeclarativeListener(this);
    }

    public void stopLogging() {
        stateMachine.removeDeclarativeListener(this);
    }

    @OnStateMachineStart
    public void onStateMachineStart() {
        logger.info(stateMachineLabel + ": Started.");
    }

    @OnStateMachineTerminate
    public void onStateMachineTerminate() {
        logger.info(stateMachineLabel + ": Terminated.");
    }

    @OnTransitionBegin
    public void onTransitionBegin(Object sourceState, Object event, Object context) {
        transitionWatch = Stopwatch.createStarted();
        logger.info(stateMachineLabel + ": Transition from \"" + sourceState + 
                "\" on \"" + event + "\" with context \""+context+"\" begin.");
    }
    
    @OnTransitionComplete
    public void onTransitionComplete(Object sourceState, Object targetState, Object event, Object context) {
        logger.info(stateMachineLabel + ": Transition from \"" + sourceState + 
                "\" to \"" + targetState + "\" on \"" + event
                + "\" complete which took " + transitionWatch + ".");
    }
    
    @OnTransitionDecline
    public void onTransitionDeclined(Object sourceState, Object event) {
        logger.warn(stateMachineLabel + ": Transition from \"" + sourceState + "\" on \"" + event+ "\" declined.");
    }
    
    @OnTransitionException
    public void onTransitionException(Object sourceState, Object targetState, Object event, Object context, TransitionException e) {
        Throwable expcetion = e.getTargetException(); 
        logger.error(stateMachineLabel + ": Transition from \"" + sourceState + 
                "\" to \"" + targetState + "\" on \"" + event + "\" caused exception.", expcetion);
    }
    
    @OnBeforeActionExecuted
    public void onBeforeActionExecuted(Object sourceState, Object targetState, 
            Object event, Object context, int[] mOfN, Action<?, ?, ?,?> action) {
        actionWatch = Stopwatch.createStarted();
        logger.info("Before execute method call action \""+action.name()+":"+action.weight()+"\" ("+ mOfN[0] + " of "+mOfN[1]+").");
    }
    
    @OnAfterActionExecuted
    public void onAfterActionExecuted(Object sourceState, Object targetState, 
            Object event, Object context, int[] mOfN, Action<?, ?, ?,?> action) {
        logger.info("After execute method call action \""+action.name()+":"+action.weight()+"\" which took "+actionWatch+".");
    }
    
    @OnActionExecException
    public void onActionExecException(Action<?, ?, ?,?> action, TransitionException e) {
        logger.error("Error executing method call action \""+action.name()+"\" caused by \""+e.getMessage()+"\"");
    }
}
