package org.squirrelframework.foundation.fsm.snake;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JFrame;

import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeState;

public class SnakeGame extends JFrame {

    private static final long serialVersionUID = 5135000138657306646L;
    
    private SnakePanel panel;
    
    private SnakeController snakeController;
    
    private ConcurrentLinkedQueue<SnakeEvent> directionsQueue = new ConcurrentLinkedQueue<SnakeEvent>();
    
    public SnakeGame(SnakeController controller) {
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
	public void startGame() {
		for(;;) {
			long start = System.nanoTime();
			
			SnakeModel model = snakeController.getSnakeModel();
			snakeController.fire(SnakeEvent.MOVE_AHEAD, new SnakeContext(model.peekFirst(), model.getDirection()));
			if(directionsQueue.size()>0) {
			    snakeController.fire(directionsQueue.poll(), new SnakeContext(model.peekFirst(), model.getDirection()));
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
}
