package movement;

import jaysRobot.EnemyHandler;
import jaysRobot.JaysRobot;

public class MovementHandler
{
	private JaysRobot robot;
	private EnemyHandler enemyHandler;
	private AntiGravity antiGravity = new AntiGravity();
	private CircleEnemy circleEnemy = new CircleEnemy ();
	
	// this is to track the direction of the robot, this is so we can drive backwards 
	// if the shortest rotation to our bearing would be to reverse instead

	public MovementHandler(JaysRobot robot, EnemyHandler enemyHandler) {
		this.robot = robot;
		this.enemyHandler = enemyHandler;
	}

	public void move() {
		if (this.robot.getOthers() > 3) {
			antiGravity.move(this.robot, this.enemyHandler);
		} else {
			circleEnemy.move(this.robot, this.enemyHandler);
		}
	}

}