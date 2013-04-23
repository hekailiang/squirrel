package org.squirrelframework.foundation.fsm.snake;

import java.awt.Point;

public class SnakeContext {
	
	private Point snakePos;
	
	private SnakeDirection direction;
	
	public SnakeContext(Point snakePos, SnakeDirection direction) {
		this.snakePos = snakePos;
		this.direction = direction;
	}
	
	public Point getSnakePos() {
		return snakePos;
	}
	
	public SnakeDirection getDirection() {
		return direction;
	}
}
