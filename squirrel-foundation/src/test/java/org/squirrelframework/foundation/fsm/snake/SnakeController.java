package org.squirrelframework.foundation.fsm.snake;

import java.awt.Point;
import java.util.Map;
import java.util.Random;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.TransitionType;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachineWithoutContext;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeEvent;
import org.squirrelframework.foundation.fsm.snake.SnakeController.SnakeState;
import org.squirrelframework.foundation.util.TypeReference;

/**
 * This is an example on how to use state machine to build a game controller. The state machine was defined in declarative manner.
 * 
 * @author Henry.He
 *
 */
@States({
	@State(name="NEW"),
	@State(name="MOVE", historyType=HistoryType.DEEP),
	@State(parent="MOVE", name="UP", initialState=true),
	@State(parent="MOVE", name="LEFT"),
	@State(parent="MOVE", name="RIGHT"),
	@State(parent="MOVE", name="DOWN"),
	@State(name="PAUSE"),
	@State(name="GAMEOVER")
})
@Transitions({
	@Transit(from="NEW", to="MOVE", on="PRESS_START", callMethod="onStart"),
    @Transit(from = "GAMEOVER", to = "MOVE", on = "PRESS_START", callMethod = "onStart"),
    @Transit(from = "MOVE", to = "GAMEOVER", on = "MOVE_AHEAD", callMethod = "onEnd"),
    @Transit(from = "UP", to = "UP", on = "MOVE_AHEAD", callMethod = "onMove", type = TransitionType.INTERNAL, when = SnakeController.ContinueRunningCondition.class),
    @Transit(from = "DOWN", to = "DOWN", on = "MOVE_AHEAD", callMethod = "onMove", type = TransitionType.INTERNAL, when = SnakeController.ContinueRunningCondition.class),
    @Transit(from = "LEFT", to = "LEFT", on = "MOVE_AHEAD", callMethod = "onMove", type = TransitionType.INTERNAL, when = SnakeController.ContinueRunningCondition.class),
    @Transit(from = "RIGHT", to = "RIGHT", on = "MOVE_AHEAD", callMethod = "onMove", type = TransitionType.INTERNAL, when = SnakeController.ContinueRunningCondition.class),
	@Transit(from="MOVE", to="PAUSE", on="PRESS_PAUSE", callMethod="onPause"),
	@Transit(from="PAUSE", to="MOVE", on="PRESS_PAUSE", callMethod="onResume"),
	@Transit(from="UP", to="LEFT", on="TURN_LEFT", callMethod="onChangeDirection"),
	@Transit(from="UP", to="RIGHT", on="TURN_RIGHT", callMethod="onChangeDirection"),
	@Transit(from="DOWN", to="LEFT", on="TURN_LEFT", callMethod="onChangeDirection"),
	@Transit(from="DOWN", to="RIGHT", on="TURN_RIGHT", callMethod="onChangeDirection"),
	@Transit(from="LEFT", to="UP", on="TURN_UP", callMethod="onChangeDirection"),
	@Transit(from="LEFT", to="DOWN", on="TURN_DOWN", callMethod="onChangeDirection"),
	@Transit(from="RIGHT", to="UP", on="TURN_UP", callMethod="onChangeDirection"),
	@Transit(from="RIGHT", to="DOWN", on="TURN_DOWN", callMethod="onChangeDirection")
})
public class SnakeController extends AbstractStateMachineWithoutContext<SnakeController, SnakeState, SnakeEvent> {
	
	public enum SnakeState {
		NEW, UP, LEFT, RIGHT, DOWN, MOVE, PAUSE, GAMEOVER
	}
	
	public enum SnakeEvent {
		PRESS_START, TURN_UP, TURN_LEFT, TURN_RIGHT, TURN_DOWN, MOVE_AHEAD, PRESS_PAUSE
	}
	
	public static class ContinueRunningCondition implements Condition<SnakeController> {
		@Override
        public boolean isSatisfied(SnakeController context) {
			Point nextPoint = computeNextPoint(context.getSnakeModel().peekFirst(), context.getSnakeModel().getDirection());
			boolean insideBorder = nextPoint.x >= 0 && nextPoint.x < GameConfigure.COL_COUNT && 
					nextPoint.y >= 0 && nextPoint.y < GameConfigure.ROW_COUNT;
			boolean bodyNotCollapsed = context.getSnakeModel().getSnakePoints().contains(nextPoint)==false;
			return insideBorder && bodyNotCollapsed;
        }
	}
	
	private SnakeModel snakeModel = new SnakeModel();
	
	private Random random = new Random();
	
	protected SnakeController (
            ImmutableState<SnakeController, SnakeState, SnakeEvent, SnakeController> initialState,
            Map<SnakeState, ImmutableState<SnakeController, SnakeState, SnakeEvent, SnakeController>> states) {
	    super(initialState, states);
    }
	
	protected void onStart(SnakeState from, SnakeState to, SnakeEvent event) {
		snakeModel.clear();
		Point startPoint = new Point(GameConfigure.COL_COUNT / 2, GameConfigure.ROW_COUNT / 2);
		snakeModel.push(startPoint);
		snakeModel.setDirection(getSnakeModel().getDirection());
		
		// generate random fruit point
		snakeModel.spawnFruit(getNextFruitIndex());
	}
	
	private int getNextFruitIndex() {
		return random.nextInt(GameConfigure.COL_COUNT * GameConfigure.ROW_COUNT - snakeModel.length());
	}
	
	protected void onMove(SnakeState from, SnakeState to, SnakeEvent event) {
		Point nextSnakePoint = computeNextPoint(getSnakeModel().peekFirst(), getSnakeModel().getDirection());
		if (nextSnakePoint.equals(snakeModel.getFruitPos())) {
			snakeModel.spawnFruit(getNextFruitIndex());
		} else if (snakeModel.length()>=GameConfigure.MIN_SNAKE_LENGTH) {
			snakeModel.removeLast();
		}
		snakeModel.push(nextSnakePoint);
	}
	
	protected void onPause(SnakeState from, SnakeState to, SnakeEvent event) {
		
	}
	
	protected void onResume(SnakeState from, SnakeState to, SnakeEvent event) {
		
	}
	
	protected void onChangeDirection(SnakeState from, SnakeState to, SnakeEvent event) {
	    snakeModel.setDirection(getSnakeDirection(to));
	}
	
	public static SnakeDirection getSnakeDirection(SnakeState state) {
		switch (state) {
		case DOWN:
			return SnakeDirection.DOWN;
			
		case LEFT:
			return SnakeDirection.LEFT;
			
		case RIGHT:
			return SnakeDirection.RIGHT;
		}
		return SnakeDirection.UP;
	}
	
	protected void onEnd(SnakeState from, SnakeState to, SnakeEvent event) {
		
	}
	
	public SnakeModel getSnakeModel() {
		return snakeModel;
	}
	
	public static Point computeNextPoint(Point snakePos, SnakeDirection direction) {
		Point nextPoint=new Point(snakePos);
		switch (direction) {
		case UP:
			nextPoint.y--;
			break;

		case DOWN:
			nextPoint.y++;
			break;

		case LEFT:
			nextPoint.x--;
			break;

		case RIGHT:
			nextPoint.x++;
			break;
		}
		return nextPoint;
	}
	
	public void export() {
	    // export snake game state machine
	    DotVisitor<SnakeController, SnakeState, SnakeEvent, SnakeController> visitor = SquirrelProvider.getInstance().newInstance(
                new TypeReference<DotVisitor<SnakeController, SnakeState, SnakeEvent, SnakeController>>() {} );
        this.accept(visitor);
        visitor.convertDotFile("SnakeStateMachine");
	}
}
