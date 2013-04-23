package org.squirrelframework.foundation.fsm.snake;

import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeState;

public class Main {
    
    public static void main(String[] args) {
        ConverterProvider.INSTANCE.register(SnakeEvent.class, new Converter.EnumConverter<SnakeEvent>(SnakeEvent.class));
        ConverterProvider.INSTANCE.register(SnakeState.class, new Converter.EnumConverter<SnakeState>(SnakeState.class));
        
        StateMachineBuilder<SnakeController, SnakeState, SnakeEvent, SnakeContext> builder = 
                StateMachineBuilderImpl.newStateMachineBuilder(SnakeController.class, SnakeState.class, SnakeEvent.class, SnakeContext.class);
        SnakeController controller = builder.newStateMachine(SnakeState.NEW);
        final SnakeGame snake = new SnakeGame(controller);
        controller.addListener(new StateMachine.TransitionCompleteListener<SnakeController, SnakeState, SnakeEvent, SnakeContext>() {
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
