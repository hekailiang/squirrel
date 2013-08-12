package org.squirrelframework.foundation.fsm.snake;

import java.awt.Point;
import java.util.LinkedList;

import com.google.common.collect.Lists;

public class SnakeModel {
	
	private LinkedList<Point> snakePoints = Lists.newLinkedList();
	
	private Point fruitPoint;
	
	private SnakeDirection direction = SnakeDirection.UP;
	
	public Point peekFirst() {
		return snakePoints.peekFirst();
	}
	
	public Point peekLast() {
		return snakePoints.peekLast();
	}
	
	public void push(Point newPoint) {
		snakePoints.push(newPoint);
	}
	
	public Point removeFirst() {
		return snakePoints.removeFirst();
	}
	
	public Point removeLast() {
		return snakePoints.removeLast();
	}
	
	public void clear() {
		snakePoints.clear();
	}
	
	public int length() {
		return snakePoints.size();
	}
	
	public LinkedList<Point> getSnakePoints() {
		return Lists.newLinkedList(snakePoints);
	}
	
	public SnakeDirection getDirection() {
		return direction;
	}
	
	public void setDirection(SnakeDirection direction) {
		this.direction = direction;
	}
	
	public Point getFruitPos() {
		return fruitPoint;
	}
	
	public boolean isBody(int x, int y) {
		return snakePoints.contains(new Point(x, y));
	}
	
	public void spawnFruit(int index) {
		int freeFound = -1;
		for(int x = 0; x < GameConfigure.COL_COUNT; x++) {
			for(int y = 0; y < GameConfigure.ROW_COUNT; y++) {
				if(!isBody(x, y) && ++freeFound==index) {
					fruitPoint = new Point(x, y);
					break;
				}
			}
		}
	}
}
