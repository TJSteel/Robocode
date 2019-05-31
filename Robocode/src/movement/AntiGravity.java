package movement;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import gravity.GravityPoint;
import gravity.ForcePoint;
import gravity.PerpendicularPoint;
import gravity.WindPoint;
import jaysRobot.Calc;
import jaysRobot.EnemyBot;
import jaysRobot.EnemyHandler;
import jaysRobot.JaysRobot;
import robocode.AdvancedRobot;
import safePoints.SafePoint;

public class AntiGravity implements Movement {
	private final int ENEMY_GRAVITY = -500;
	private final int WALL_GRAVITY = -5000;
	private final double BULLET_GRAVITY = -50000;
	private final double SAFE_POINT_STRENGTH = 5000;
	private SafePoint safePoint = new SafePoint(0,0,Double.POSITIVE_INFINITY);
	private EnemyBot target;
	private double battlefieldWidth = 0;
	private double battlefieldHeight = 0;
	private final double SAFE_DISTANCE = 100;
	private EnemyHandler enemyHandler = new EnemyHandler();
	private AdvancedRobot robot = new AdvancedRobot();
	private final double ENEMY_DANGER = 10;
	
	public void updateSafePoint() {
		safePoint = new SafePoint(0,0,Double.POSITIVE_INFINITY);

		// I will start by generating a point in each corner and then set it's danger level based on the closeness of each enemy;
		
		ArrayList<SafePoint> safePoints = new ArrayList<SafePoint>();
		
		safePoints.add(new SafePoint(0 + SAFE_DISTANCE, battlefieldHeight - SAFE_DISTANCE, 0)); // top left corner
		safePoints.add(new SafePoint(battlefieldWidth - SAFE_DISTANCE, battlefieldHeight - SAFE_DISTANCE, 0)); // top right corner
		safePoints.add(new SafePoint(0 + SAFE_DISTANCE, 0 + SAFE_DISTANCE, 0)); // bottom left corner
		safePoints.add(new SafePoint(battlefieldWidth - SAFE_DISTANCE, 0 + SAFE_DISTANCE, 0)); // bottom right corner			
		
		// for each safe point, calculate the distance to each enemy and update the danger according to their proximity
		for (SafePoint p : safePoints) {
			for (EnemyBot e : this.enemyHandler.getEnemies()) {
				double danger = 0;
				double enemyDistance = e.getPosition().getDistance(p.getX(), p.getY());
				
				danger = ENEMY_DANGER / enemyDistance;
				
				p.setDanger(p.getDanger() + danger);
			}
			if (p.getDanger() < safePoint.getDanger()) safePoint = p;
		}
		System.out.println("Safe point is at " + safePoint.getX() + ", " + safePoint.getY());
		// move the point back inside the map and away from the wall
		safePoint.setX(Calc.constrainValue(safePoint.getX(), 0 + SAFE_DISTANCE, battlefieldWidth - SAFE_DISTANCE));
		safePoint.setY(Calc.constrainValue(safePoint.getY(), 0 + SAFE_DISTANCE, battlefieldHeight - SAFE_DISTANCE));
	}
	
	public void move(JaysRobot robot, EnemyHandler enemyHandler) {
		// update global variables so that other functions have access to this
		this.robot = robot;
		this.enemyHandler = enemyHandler;
		double robotX = robot.getX();
		double robotY = robot.getY();
		long time = robot.getTime();
		battlefieldWidth = robot.getBattleFieldWidth();
		battlefieldHeight = robot.getBattleFieldHeight();
   		double xForce = 0;
	    double yForce = 0;
	    ArrayList<ForcePoint> forcePoints = new ArrayList<ForcePoint>();
		ArrayList<EnemyBot> enemies = enemyHandler.getEnemies();

		// get the target robot
		target = enemyHandler.getEnemy();

		//cycle through all the enemies. If they are alive, add gravity points
		for (EnemyBot enemy : enemies) {
			if (enemy.isAlive()) {
				if (enemy.getName().equals(target.getName()) == false){
					Position enemyPosition = enemy.getPosition();
					forcePoints.add(new GravityPoint(enemyPosition.getX(),enemyPosition.getY(), this.ENEMY_GRAVITY, 0));
					//forcePoints.add(new PerpendicularPoint(enemyPosition.getX(),enemyPosition.getY(), this.ENEMY_GRAVITY, enemyPosition.getHeading()));
				}
			}
	    }
		
		// setup force points for the target
		Position targetPosition = target.getPosition();
		//forcePoints.add(new GravityPoint(targetPosition.getX(), targetPosition.getY(), -this.ENEMY_GRAVITY, SAFE_DISTANCE*5));
		forcePoints.add(new PerpendicularPoint(targetPosition.getX(),targetPosition.getY(), this.ENEMY_GRAVITY, targetPosition.getHeading()));
		
		for (Bullet bullet : enemyHandler.getBullets()) {
			if (bullet.isActive(time)) {
				double bulletX = bullet.getX(time);
				double bulletY = bullet.getY(time);
				double distance = Point2D.distance(robotX, robotY, bulletX, bulletY);
				double velocity = bullet.getVelocity();
				long timeWhenHit = (long) (distance / velocity);
				timeWhenHit += time; 
				
				// where it is now
				//forcePoints.add(new PerpendicularPoint(bullet.getX(time), bullet.getY(time), this.BULLET_GRAVITY, bullet.getHeading()));
				
				// where it will hit
				//forcePoints.add(new PerpendicularPoint(bullet.getX(timeWhenHit), bullet.getY(timeWhenHit), this.BULLET_GRAVITY, bullet.getHeading()));
			}
		}
		/*
		// add points for the walls
		// Top Wall
		forcePoints.add(new WindPoint(battlefieldWidth/2, battlefieldHeight, this.WALL_GRAVITY, Math.PI));
		// Top Right Wall
		forcePoints.add(new WindPoint(battlefieldWidth, battlefieldHeight, this.WALL_GRAVITY, (Math.PI/4)*5));
		// Right Wall
		forcePoints.add(new WindPoint(battlefieldWidth, battlefieldHeight/2, this.WALL_GRAVITY, (Math.PI/2)*3));
		// Bottom Right Wall
		forcePoints.add(new WindPoint(battlefieldWidth, 0, this.WALL_GRAVITY, (Math.PI/4)*7));
		// Bottom Wall
		forcePoints.add(new WindPoint(battlefieldWidth/2, 0, this.WALL_GRAVITY, 0));
		// Left Bottom Wall
		forcePoints.add(new WindPoint(0, 0, this.WALL_GRAVITY, Math.PI/4));
		// Left Wall
		forcePoints.add(new WindPoint(0, battlefieldHeight/2, this.WALL_GRAVITY, Math.PI/2));
		// Top Left Wall
		forcePoints.add(new WindPoint(0, battlefieldHeight, this.WALL_GRAVITY, (Math.PI/4)*3));
		*/
		updateSafePoint();
		//forcePoints.add(new GravityPoint(this.safePoint.getX(), this.safePoint.getY(), this.SAFE_POINT_STRENGTH, SAFE_DISTANCE*2));
		
		
		for (ForcePoint fp : forcePoints) {
			Point2D force = fp.getForce(robotX, robotY); 
			xForce += force.getX();
			yForce += force.getY();
		}

	    //Move in the direction of our resolved force.
	    robot.goTo(robotX-xForce, robotY-yForce);
	}

}
