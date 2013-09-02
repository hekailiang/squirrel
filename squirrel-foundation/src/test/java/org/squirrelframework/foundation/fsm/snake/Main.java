package org.squirrelframework.foundation.fsm.snake;

import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeState;

public class Main {
    
    public static void main(String[] args) {
        StateMachineBuilder<SnakeController, SnakeState, SnakeEvent, SnakeModel> builder = 
                StateMachineBuilderFactory.create(SnakeController.class, SnakeState.class, SnakeEvent.class, SnakeModel.class);
        SnakeController controller = builder.newStateMachine(SnakeState.NEW);
        final SnakeGame game = new SnakeGame(controller);
        controller.addTransitionCompleteListener(new StateMachine.TransitionCompleteListener<SnakeController, SnakeState, SnakeEvent, SnakeModel>() {
            @Override
            public void transitionComplete(TransitionCompleteEvent<SnakeController, SnakeState, SnakeEvent, SnakeModel> event) {
                game.repaint();
                game.setTitle("Greedy Snake("+game.getGameData().length()+"): "+
                        event.getSourceState()+"--["+event.getCause()+"]--"+event.getTargetState());
            }
        });
        game.startGame();
    }
}
