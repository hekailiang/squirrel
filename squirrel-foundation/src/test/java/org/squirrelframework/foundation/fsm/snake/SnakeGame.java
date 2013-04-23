package org.squirrelframework.foundation.fsm.snake;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachine.TransitionCompleteEvent;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeState;

public class SnakeGame extends JFrame {

    private static final long serialVersionUID = 5135000138657306646L;
    
    private SnakePanel panel;
    
    private SnakeController snakeController;
    
    private SnakeGame(SnakeController controller) {
		super("Greedy Snake");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		
		this.snakeController = controller;
		this.panel = new SnakePanel(snakeController);
		add(panel, BorderLayout.CENTER);
		
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				SnakeModel model = snakeController.getSnakeModel();
				switch(e.getKeyCode()) {

				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					snakeController.fire(SnakeEvent.TURN_UP, new SnakeContext(model.peekFirst(), model.getDirection()));
					break;

				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					snakeController.fire(SnakeEvent.TURN_DOWN, new SnakeContext(model.peekFirst(), model.getDirection()));
					break;
				
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					snakeController.fire(SnakeEvent.TURN_LEFT, new SnakeContext(model.peekFirst(), model.getDirection()));
					break;
			
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					snakeController.fire(SnakeEvent.TURN_RIGHT, new SnakeContext(model.peekFirst(), model.getDirection()));
					break;
				
				case KeyEvent.VK_P:
					snakeController.fire(SnakeEvent.PRESS_PAUSE, new SnakeContext(model.peekFirst(), model.getDirection()));
					break;
				
				case KeyEvent.VK_ENTER:
					Point head = new Point(GameConfigure.COL_COUNT / 2, GameConfigure.ROW_COUNT / 2);
					SnakeState lastState = snakeController.getLastActiveChildStateOf(SnakeState.MOVE);
					SnakeDirection initialDirection = lastState!=null ? 
							SnakeController.getSnakeDirection(lastState) : SnakeDirection.UP;
					snakeController.fire(SnakeEvent.PRESS_START, new SnakeContext(head, initialDirection));
					break;
				}
			}
			
		});
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
    }
    
    /**
	 * Starts the game running.
	 */
	private void startGame() {
		for(;;) {
			long start = System.nanoTime();
			
			SnakeModel model = snakeController.getSnakeModel();
			snakeController.fire(SnakeEvent.MOVE_AHEAD, new SnakeContext(model.peekFirst(), model.getDirection()));
			
			long delta = (System.nanoTime() - start) / 1000000L;
			if(delta < GameConfigure.FRAME_TIME) {
				try {
					Thread.sleep(GameConfigure.FRAME_TIME - delta);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
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
            }
		});
		snake.startGame();
	}

}
