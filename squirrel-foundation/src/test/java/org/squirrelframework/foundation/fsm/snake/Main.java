package org.squirrelframework.foundation.fsm.snake;

import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeState;

public class Main {
    
    public static void main(String[] args) {
        StateMachineBuilder<SnakeController, SnakeState, SnakeEvent, SnakeContext> builder = 
                StateMachineBuilderFactory.create(SnakeController.class, SnakeState.class, SnakeEvent.class, SnakeContext.class);
        SnakeController controller = builder.newStateMachine(SnakeState.NEW);
        final SnakeGame snake = new SnakeGame(controller);
        controller.addTransitionCompleteListener(new StateMachine.TransitionCompleteListener<SnakeController, SnakeState, SnakeEvent, SnakeContext>() {
            @Override
            public void transitionComplete(TransitionCompleteEvent<SnakeController, SnakeState, SnakeEvent, SnakeContext> event) {
                snake.repaint();
                snake.setTitle("Greedy Snake("+event.getStateMachine().getSnakeModel().length()+"): "+
                        event.getSourceState()+"--["+event.getCause()+"]--"+event.getTargetState());
            }
        });
        snake.startGame();
    }
}
