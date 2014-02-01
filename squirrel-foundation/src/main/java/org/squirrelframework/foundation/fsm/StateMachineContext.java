package org.squirrelframework.foundation.fsm;

import java.util.Stack;

public class StateMachineContext {
    
    private final Object currentInstance;
    
    private final boolean isTestEvent;
    
    private static ThreadLocal<Stack<StateMachineContext>> contextContainer = new ThreadLocal<Stack<StateMachineContext>>() {
        protected Stack<StateMachineContext> initialValue() {
            return new Stack<StateMachineContext>();
        }
    };
    
    public StateMachineContext(Object stateMachine, boolean isTestEvent) {
        this.currentInstance = stateMachine;
        this.isTestEvent = isTestEvent;
    }
    
    public static void set(Object instance) {
        set(instance, false);
    }
    
    public static void set(Object instance, boolean isTestEvent) {
        if (instance == null) {
            contextContainer.get().pop();
        } else {
            contextContainer.get().push(new StateMachineContext(instance, isTestEvent));
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T currentInstance() {
        return contextContainer.get().size()>0 ? (T) contextContainer.get().peek().currentInstance : null;
    }
    
    public static boolean isTestEvent() {
        return contextContainer.get().size()>0  ? contextContainer.get().peek().isTestEvent : false;
    }
}
