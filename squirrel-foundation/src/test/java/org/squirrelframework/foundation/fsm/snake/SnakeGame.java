package org.squirrelframework.foundation.fsm.snake;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JFrame;

import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;

public class SnakeGame extends JFrame {

    private static final long serialVersionUID = 5135000138657306646L;
    
    private SnakePanel panel;
    
    private SnakeController gameController;
    
    private SnakeModel gameModel = new SnakeModel();
    
    private ConcurrentLinkedQueue<SnakeEvent> directionsQueue = new ConcurrentLinkedQueue<SnakeEvent>();
    
    public SnakeGame(SnakeController controller) {
		super("Greedy Snake");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		
		this.gameController = controller;
		this.panel = new SnakePanel(gameController, gameModel);
		add(panel, BorderLayout.CENTER);
		
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
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
					gameController.fire(SnakeEvent.PRESS_PAUSE, gameModel);
					break;
				
				case KeyEvent.VK_ENTER:
				    directionsQueue.clear();
					gameController.fire(SnakeEvent.PRESS_START, gameModel);
					break;
				}
			}
			
		});
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
    }
    
    public SnakeModel getGameData() {
        return gameModel;
    }
    
    /**
	 * Starts the game running.
	 */
	public void startGame() {
		for(;;) {
			long start = System.nanoTime();
			
			gameController.fire(SnakeEvent.MOVE_AHEAD, gameModel);
			if(directionsQueue.size()>0) {
			    gameController.fire(directionsQueue.poll(), gameModel);
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
