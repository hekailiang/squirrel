package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.SquirrelComponent;

/**
 * An activity that is executed during transition happening.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface Action<T extends StateMachine<T, S, E, C>, S, E, C> extends SquirrelComponent {
    
    public static final int MAX_WEIGHT = Integer.MAX_VALUE-1;
    
    public static final int BEFORE_WEIGHT = 100;
    
    public static final int NORMAL_WEIGHT = 0;
    
    public static final int EXTENSION_WEIGHT = -10;
    
    public static final int AFTER_WEIGHT = -100;
    
    public static final int MIN_WEIGHT = Integer.MIN_VALUE+1;
    
    public static final int IGNORE_WEIGHT = Integer.MIN_VALUE;
    
    /**
     * Execute the activity.
     * 
     * @param from transition source state
     * @param to transition target state
     * @param event event that trigger the transition
     * @param context context object
     * @param stateMachine the state machine
     */
    void execute(S from, S to, E event, C context, T stateMachine);
    
    String name();
    
    int weight();
    
    boolean isAsync();
    
    long timeout();
    
    @SuppressWarnings("rawtypes")
    public final static Action DUMMY_ACTION = new AnonymousAction() {
        @Override
        public void execute(Object from, Object to, Object event, 
                Object context, StateMachine stateMachine) {
            // DO NOTHING
        }
        
        @Override
        public String name() {
            return "__DUMMY_ACTION";
        }
    };
}
