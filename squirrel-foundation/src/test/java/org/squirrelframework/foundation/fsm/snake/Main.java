package org.squirrelframework.foundation.fsm.snake;

import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineConfiguration;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeState;

public class Main {
    
    public static void main(String[] args) {
        final SnakeModel gameModel = new SnakeModel();
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(SnakeController.class);
        // define timed state
        builder.defineTimedState(SnakeState.UP, 0, GameConfigure.FRAME_TIME, SnakeEvent.MOVE_AHEAD, gameModel);
        builder.defineTimedState(SnakeState.DOWN, 0, GameConfigure.FRAME_TIME, SnakeEvent.MOVE_AHEAD, gameModel);
        builder.defineTimedState(SnakeState.RIGHT, 0, GameConfigure.FRAME_TIME, SnakeEvent.MOVE_AHEAD, gameModel);
        builder.defineTimedState(SnakeState.LEFT, 0, GameConfigure.FRAME_TIME, SnakeEvent.MOVE_AHEAD, gameModel);
        
        SnakeController controller = builder.newUntypedStateMachine(SnakeState.NEW);
        final SnakeGame game = new SnakeGame(controller, gameModel);
        controller.addTransitionCompleteListener(new StateMachine.TransitionCompleteListener<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void transitionComplete(TransitionCompleteEvent<UntypedStateMachine, Object, Object, Object> event) {
                game.repaint();
                game.setTitle("Greedy Snake("+gameModel.length()+"): "+
                        event.getSourceState()+"--["+event.getCause()+"]--"+event.getTargetState());
            }
        });
        controller.export();
        game.startGame();
    }
}
