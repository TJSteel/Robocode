package movement;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import gravity.GravityPoint;
import gravity.ForcePoint;
import gravity.PerpendicularPoint;
import gravity.WindPoint;
import jaysRobot.EnemyBot;
import jaysRobot.EnemyHandler;
import jaysRobot.JaysRobot;

public class AntiGravity implements Movement {
	private double midPointStrength = -1000;		//The strength of the gravity point in the middle of the field
	private final double MID_POINT_MULTIPLIER = 1.5;
	private final int ENEMY_GRAVITY = -10000;
	private final double ENEMY_MULTIPLIER = 2;
	private final int WALL_GRAVITY = -5000;
	private final double WALL_MULTIPLER = 3;
	private final double BULLET_GRAVITY = -5000;
	private final double BULLET_MULTIPLIER = 3;
	private final double RANDOM_POINT_STRENGTH = 1000;
	private double randomPointCount = 11; // so that update random will change the value on first run
	private final double RANDOM_POINT_CHANGE = 100;
	private double randomPointX = 0;
	private double randomPointY = 0;
	private EnemyBot target;
	private double battlefieldWidth = 0;
	private double battlefieldHeight = 0;
	private double robotX = 0;
	private double robotY = 0;
	
	public void updateRandomPoint() {
		randomPointCount ++;
		if (randomPointCount > RANDOM_POINT_CHANGE) {
			randomPointCount = 0;
			/*
			 * it's called random point, but I'd like a little intelligence to this
			 * my idea for this is to have us aim near corner / wall,
			 * by doing this is should prevent being surrounded 
			 */
			
			final double WALL_MARGIN = 100;
			
			// start by calculating closest corner
			// initialise with top left
			double closestCorner = Point2D.distance(robotX, robotY, 0, battlefieldHeight);
			randomPointX = 0;
			randomPointY = battlefieldHeight;
			
			// top right
			if (Point2D.distance(robotX, robotY, battlefieldWidth, battlefieldHeight) < closestCorner) {
				randomPointX = battlefieldWidth;
				randomPointY = battlefieldHeight;
			}
			// bottom left
			if (Point2D.distance(robotX, robotY, 0, 0) < closestCorner) {
				randomPointX = 0;
				randomPointY = 0;
			}
			// bottom right
			if (Point2D.distance(robotX, robotY, battlefieldWidth, 0) < closestCorner) {
				randomPointX = battlefieldWidth;
				randomPointY = 0;
			}
			
			/* 
			 * now we have picked the closest corner to attract to, we will add some distance so we don't crash into it, and some random side to side movement
			 * this may put the robot outside the barrier, so we'll correct this later
			 */
			randomPointX += WALL_MARGIN;
			randomPointY += WALL_MARGIN;
			
			// now we add a random amount to each point
			randomPointX += (Math.random() * (WALL_MARGIN * 2));
			randomPointY += (Math.random() * (WALL_MARGIN * 2));
		}
	}
	
	public void move(JaysRobot robot, EnemyHandler enemyHandler) {
		robotX = robot.getX();
		robotY = robot.getY();
		long time = robot.getTime();
		battlefieldWidth = robot.getBattleFieldWidth();
		battlefieldHeight = robot.getBattleFieldHeight();
   		double xForce = 0;
	    double yForce = 0;
	    ArrayList<ForcePoint> gravityPoints = new ArrayList<ForcePoint>();
		ArrayList<EnemyBot> enemies = enemyHandler.getEnemies();

		//cycle through all the enemies. If they are alive, add gravity points
		for (EnemyBot enemy : enemies) {
			if (enemy.isAlive()) {
				Position enemyPosition = enemy.getPosition();
				gravityPoints.add(new GravityPoint(enemyPosition.getX(),enemyPosition.getY(), this.ENEMY_GRAVITY, this.ENEMY_MULTIPLIER));
				gravityPoints.add(new PerpendicularPoint(enemyPosition.getX(),enemyPosition.getY(), this.ENEMY_GRAVITY, this.ENEMY_MULTIPLIER, enemyPosition.getHeading()));
			}
	    }
		
		// check distance to target enemy, if too far away, set attraction point
		target = enemyHandler.getEnemy();
		Position targetPosition = target.getPosition();
		if (target.getPosition().getDistance(robot.getX(), robot.getY()) > 1000) {
			gravityPoints.add(new GravityPoint(targetPosition.getX(), targetPosition.getY(), -this.ENEMY_GRAVITY, 1));
			gravityPoints.add(new PerpendicularPoint(targetPosition.getX(),targetPosition.getY(), this.ENEMY_GRAVITY, this.ENEMY_MULTIPLIER, targetPosition.getHeading()));
		}
		
		for (Bullet bullet : enemyHandler.getBullets()) {
			if (bullet.isActive(time)) {
				double bulletX = bullet.getX(time);
				double bulletY = bullet.getY(time);
				double distance = Point2D.distance(robotX, robotY, bulletX, bulletY);
				double velocity = bullet.getVelocity();
				long timeWhenHit = (long) (distance / velocity);
				timeWhenHit += time; 
				
				// where it is now
				gravityPoints.add(new PerpendicularPoint(bullet.getX(time), bullet.getY(time), this.BULLET_GRAVITY, this.BULLET_MULTIPLIER, bullet.getHeading()));
				
				// where it will hit
				gravityPoints.add(new PerpendicularPoint(bullet.getX(timeWhenHit), bullet.getY(timeWhenHit), this.BULLET_GRAVITY, this.BULLET_MULTIPLIER, bullet.getHeading()));
			}
		}
		
		// add point in center of map to push
		gravityPoints.add(new GravityPoint(battlefieldWidth/2, battlefieldHeight/2, this.midPointStrength, this.MID_POINT_MULTIPLIER));

		// add points for the walls
		// Top Wall
		gravityPoints.add(new WindPoint(battlefieldWidth/2, battlefieldHeight, this.WALL_GRAVITY, this.WALL_MULTIPLER, Math.PI));
		// Top Right Wall
		gravityPoints.add(new WindPoint(battlefieldWidth, battlefieldHeight, this.WALL_GRAVITY, this.WALL_MULTIPLER, (Math.PI/4)*5));
		// Right Wall
		gravityPoints.add(new WindPoint(battlefieldWidth, battlefieldHeight/2, this.WALL_GRAVITY, this.WALL_MULTIPLER, (Math.PI/2)*3));
		// Bottom Right Wall
		gravityPoints.add(new WindPoint(battlefieldWidth, 0, this.WALL_GRAVITY, this.WALL_MULTIPLER, (Math.PI/4)*7));
		// Bottom Wall
		gravityPoints.add(new WindPoint(battlefieldWidth/2, 0, this.WALL_GRAVITY, this.WALL_MULTIPLER, 0));
		// Left Bottom Wall
		gravityPoints.add(new WindPoint(0, 0, this.WALL_GRAVITY, this.WALL_MULTIPLER, Math.PI/4));
		// Left Wall
		gravityPoints.add(new WindPoint(0, battlefieldHeight/2, this.WALL_GRAVITY, this.WALL_MULTIPLER, Math.PI/2));
		// Top Left Wall
		gravityPoints.add(new WindPoint(0, battlefieldHeight, this.WALL_GRAVITY, this.WALL_MULTIPLER, (Math.PI/4)*3));
		
		updateRandomPoint();
		gravityPoints.add(new GravityPoint(this.randomPointX, this.randomPointY, this.RANDOM_POINT_STRENGTH, 1));
		
		
		for (ForcePoint gp : gravityPoints) {
			Point2D force = gp.getForce(robotX, robotY); 
			xForce += force.getX();
			yForce += force.getY();
		}

	    //Move in the direction of our resolved force.
	    robot.goTo(robotX-xForce, robotY-yForce);
	}

}
