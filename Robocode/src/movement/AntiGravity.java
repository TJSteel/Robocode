package movement;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import gravity.ForcePoint;
import gravity.GravityPoint;
import gravity.PerpendicularPoint;
import gravity.WindPoint;
import jaysRobot.EnemyBot;
import jaysRobot.EnemyHandler;
import jaysRobot.JaysRobot;

public class AntiGravity {
	private double midPointStrength = -1000;		//The strength of the gravity point in the middle of the field
	private final double midPointMultiplier = 1.5;
	private final int enemyGravity = -10000;
	private final double enemyMultiplier = 2;
	private final int wallGravity = -5000;
	private final double wallMultiplier = 3;
	private final double bulletGravity = -5000;
	private final double bulletMultiplier = 3;
	private final double randomPointStrength = 500;
	private double randomPointCount = 11; // so that update random will change the value on first run
	private final double randomPointChange = 50;
	private double randomPointX = 0;
	private double randomPointY = 0;
	private EnemyBot target;
	
	public void updateRandomPoint() {
		
		randomPointCount ++;
		if (randomPointCount > randomPointChange) {
			randomPointCount = 0;
			randomPointX = 0 + (int)(Math.random() * ((1200 - 0) + 1));
			randomPointY = 0 + (int)(Math.random() * ((1200 - 0) + 1));
		}
	}
	
	public void move(JaysRobot robot, EnemyHandler enemyHandler) {
		double x = robot.getX();
		double y = robot.getY();
		long time = robot.getTime();
		double battlefieldWidth = robot.getBattleFieldWidth();
		double battlefieldHeight = robot.getBattleFieldHeight();
   		double xForce = 0;
	    double yForce = 0;
	    ArrayList<GravityPoint> gravityPoints = new ArrayList<GravityPoint>();
		ArrayList<EnemyBot> enemies = enemyHandler.getEnemies();

		//cycle through all the enemies. If they are alive, add gravity points
		for (EnemyBot enemy : enemies) {
			if (enemy.isAlive()) {
				gravityPoints.add(new ForcePoint(enemy.getX(),enemy.getY(), this.enemyGravity, this.enemyMultiplier));
				gravityPoints.add(new PerpendicularPoint(enemy.getX(),enemy.getY(), this.enemyGravity, this.enemyMultiplier, enemy.getHeading()));
			}
	    }
		
		// check distance to target enemy, if too far away, set attraction point
		target = enemyHandler.getEnemy();
		if (target.getDistance() > 1000) {
			gravityPoints.add(new ForcePoint(target.getX(), target.getY(), -this.enemyGravity, 1));
			gravityPoints.add(new PerpendicularPoint(target.getX(),target.getY(), this.enemyGravity, this.enemyMultiplier, target.getHeading()));
		}
		
		for (Bullet bullet : enemyHandler.getBullets()) {
			if (bullet.isActive(time)) {
				double bulletX = bullet.getX(time);
				double bulletY = bullet.getY(time);
				double distance = Point2D.distance(x, y, bulletX, bulletY);
				double velocity = bullet.getVelocity();
				long timeWhenHit = (long) (distance / velocity);
				timeWhenHit += time; 
				
				// where it is now
				gravityPoints.add(new PerpendicularPoint(bullet.getX(time), bullet.getY(time), this.bulletGravity, this.bulletMultiplier, bullet.getHeading()));
				
				// where it will hit
				gravityPoints.add(new PerpendicularPoint(bullet.getX(timeWhenHit), bullet.getY(timeWhenHit), this.bulletGravity, this.bulletMultiplier, bullet.getHeading()));
			}
		}
		
		// add point in center of map to push
		gravityPoints.add(new ForcePoint(battlefieldWidth/2, battlefieldHeight/2, this.midPointStrength, this.midPointMultiplier));

		// add points for the walls
		// Top Wall
		gravityPoints.add(new WindPoint(battlefieldWidth/2, battlefieldHeight, this.wallGravity, this.wallMultiplier, Math.PI));
		// Top Right Wall
		gravityPoints.add(new WindPoint(battlefieldWidth, battlefieldHeight, this.wallGravity, this.wallMultiplier, (Math.PI/4)*5));
		// Right Wall
		gravityPoints.add(new WindPoint(battlefieldWidth, battlefieldHeight/2, this.wallGravity, this.wallMultiplier, (Math.PI/2)*3));
		// Bottom Right Wall
		gravityPoints.add(new WindPoint(battlefieldWidth, 0, this.wallGravity, this.wallMultiplier, (Math.PI/4)*7));
		// Bottom Wall
		gravityPoints.add(new WindPoint(battlefieldWidth/2, 0, this.wallGravity, this.wallMultiplier, 0));
		// Left Bottom Wall
		gravityPoints.add(new WindPoint(0, 0, this.wallGravity, this.wallMultiplier, Math.PI/4));
		// Left Wall
		gravityPoints.add(new WindPoint(0, battlefieldHeight/2, this.wallGravity, this.wallMultiplier, Math.PI/2));
		// Top Left Wall
		gravityPoints.add(new WindPoint(0, battlefieldHeight, this.wallGravity, this.wallMultiplier, (Math.PI/4)*3));
		
		updateRandomPoint();
		gravityPoints.add(new ForcePoint(this.randomPointX, this.randomPointY, this.randomPointStrength, 1));
		
		
		for (GravityPoint gp : gravityPoints) {
			Point2D force = gp.getForce(x, y); 
			xForce += force.getX();
			yForce += force.getY();
		}

	    //Move in the direction of our resolved force.
	    robot.goTo(x-xForce, y-yForce);
	}

}
