package org.squirrelframework.foundation.fsm.snake;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    
    private ConcurrentLinkedQueue<SnakeEvent> directionsQueue = new ConcurrentLinkedQueue<SnakeEvent>();
    
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
				    directionsQueue.add(SnakeEvent.TURN_UP);
					break;

				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
				    directionsQueue.add(SnakeEvent.TURN_DOWN);
					break;
				
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
				    directionsQueue.add(SnakeEvent.TURN_LEFT);
					break;
			
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
				    directionsQueue.add(SnakeEvent.TURN_RIGHT);
					break;
				
				case KeyEvent.VK_P:
					snakeController.fire(SnakeEvent.PRESS_PAUSE, new SnakeContext(model.peekFirst(), model.getDirection()));
					break;
				
				case KeyEvent.VK_ENTER:
				    directionsQueue.clear();
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
			SnakeContext context = new SnakeContext(model.peekFirst(), model.getDirection());
			snakeController.fire(SnakeEvent.MOVE_AHEAD, context);
			if(directionsQueue.size()>0) {
			    snakeController.fire(directionsQueue.poll(), context);
			} 
			
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
				snake.setTitle("Greedy Snake("+event.getStateMachine().getSnakeModel().length()+"): "+
				        event.getSourceState()+"--["+event.getCause()+"]--"+event.getTargetState());
            }
		});
		snake.startGame();
	}
}
