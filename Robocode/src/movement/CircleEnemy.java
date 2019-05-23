package movement;

import java.awt.geom.Point2D;

import jaysRobot.Calc;
import jaysRobot.EnemyBot;
import jaysRobot.EnemyHandler;
import jaysRobot.JaysRobot;

public class CircleEnemy implements Movement {
	private double wallAvoidance = 0;
	private double travelDirection = 1;
	private final double ENEMY_PROXIMITY = 300;
	private final double WALL_PROXIMITY = 100;

	public void move(JaysRobot robot, EnemyHandler enemyHandler) {
		EnemyBot enemy = enemyHandler.getEnemy();
		// allow wall avoidance movements to complete
		if (avoidingWalls(robot)) {
			double centerX = robot.getBattleFieldWidth()/2;
			double centerY = robot.getBattleFieldHeight()/2;
			// get the heading
			double heading = Calc.getHeadingToObject(robot.getX(), robot.getY(), centerX, centerY);
			robot.turnTo(heading);
			robot.setAhead(100);
			wallAvoidance--;
		} else {

			if (Math.random() > 0.99) {
				this.travelDirection *= -1;
			} 

			//approach enemy if we're too far away, otherwise increase our distance
			Position enemyPosition = enemy.getPosition();
			double enemyX = enemyPosition.getX();
			double enemyY = enemyPosition.getY();
			double distance = Point2D.distance(enemyX, enemyY, robot.getX(), robot.getY());
			
			double approachAngle = distance > this.ENEMY_PROXIMITY ? ((Math.PI/180)*40) : -((Math.PI/180)*10);
			double heading = Calc.getHeadingToObject(robot.getX(), robot.getY(), enemyX, enemyY);
			robot.turnTo(heading - (Math.PI/2) + (approachAngle * travelDirection));

			robot.setAhead(100 * travelDirection);
		}
	}

	private boolean avoidingWalls(JaysRobot robot) {
		if (this.wallAvoidance > 0) {
			return true;
		} else {
			double battleFieldWidth = robot.getBattleFieldWidth();
			double battleFieldHeight = robot.getBattleFieldHeight();
			double x = robot.getX();
			double y = robot.getY();
			if (x < WALL_PROXIMITY 
					|| y < WALL_PROXIMITY
					|| x > battleFieldWidth - WALL_PROXIMITY
					|| y > battleFieldHeight - WALL_PROXIMITY) {
				this.wallAvoidance = WALL_PROXIMITY / 2;
				return true;
			}
		}
		return false;
	}
}