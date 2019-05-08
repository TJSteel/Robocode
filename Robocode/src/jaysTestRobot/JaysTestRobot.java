package jaysTestRobot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import jaysRobot.EnemyBot;
import jaysRobot.EnemyHandler;
import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public class JaysTestRobot extends AdvancedRobot {
	private static double MAX_FIRE_POWER = 1; // max power multiplier, should be in the range 0-1
	private static int WALL_MARGIN = 100;
	private boolean wallAvoidance = false;
	private double enemyProximity = 200; //how close to enemy should we get
	private byte scanDirection = 1;
	// this is to track the direction of the robot, this is so we can drive backwards 
	// if the shortest rotation to our bearing would be to reverse instead
	private int robotDirection = 1;
	// this is to track the direction of travel, forwards or backwards
	private int travelDirection = 1;
	
	private EnemyHandler enemyHandler = new EnemyHandler();
	
	public void run() {
		// setting radar / gun to be able to turn independently of each other
		// wiki states this is essential
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
		// setting colours for my robot
        setBodyColor(new Color(148,0,211)); // darkViolet
        setGunColor(new Color(139,0,139)); // darkMagenta
        setRadarColor(new Color(75,0,130)); // indigo
        setScanColor(new Color(148,0,211));
        setBulletColor(new Color(148,0,211));
        
        addCustomEvents();
        while (true) {
        	doScan();
        	doMove();
        	doShoot();
        	execute();
        	//System.out.println(this.enemyHandler.getEnemy().toString());
        }
	}
	@Override
	public void onPaint(Graphics2D g) {
		EnemyBot enemy = enemyHandler.getEnemy();
		if (!enemy.isIdle(getTime())) {
		    
		    // Set the paint color to a red half transparent color
		    g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
		 
		    // Draw a line from our robot to the scanned robot
		    g.drawLine((int)getX(), (int)getY(), enemy.getX(), enemy.getY());
		 
		    // Draw a filled square on top of the scanned robot that covers it
		    g.fillRect( enemy.getX() - 20,  enemy.getY() - 20, 40, 40);
		}
	}
	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		super.onHitByBullet(event);
		//setAllColors(Color.red);
	}
	
	/**
	 * Most of our code will be here, this event is triggered when we spot another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// we don't want to attack sentries
		if (e.isSentryRobot()) {
			return;
		}
		
		enemyHandler.update(e, this, getTime());
		
		// if there is only 1 enemy left, lock on target
		if (getOthers() == 1) {
			scanDirection *= -1;
		}
    }

	/**
	 * Handles our custom events, such as getting close to the walls
	 */
	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("Too close to wall"))
		{
			// if we're not already trying to avoid the wall, drive towards the center of the map 
			if (wallAvoidance == false) {
				// set wall avoidance true
				wallAvoidance = true;
				double centerX = getBattleFieldWidth()/2;
				double centerY = getBattleFieldHeight()/2;
		    	// get the heading
				double heading = getHeadingToObject(centerX, centerY);
				// calculate the fastest way of turning to this heading and set correct direction
				// if turning 120 degrees, you could instead turn 60 degrees and drive backwards 
				travelDirection = 1;
				doTurnRightRadians(heading);
				setAhead(100 * travelDirection * robotDirection);
			}
		}
	}

	/**
	 * Turns the robot to the heading in the most efficient direction, and reverses the orientation of the robot if going backwards is faster 
	 * @param heading The heading in radians that you wish to turn to
	 * @see doTurnRightDegrees
	 */
	private void doTurnRightRadians(double heading) {
		if (heading > Math.PI / 2) { // if turning more than 90 degrees
			heading = Math.PI - heading;
			robotDirection = -1;
		} else if (heading < (Math.PI / 2) * -1) { // if turning more than 90 degrees
			heading = (Math.PI * -1) + heading;
			robotDirection = -1;
		} else {
			robotDirection = 1;
		}
		setTurnRightRadians(heading);
	}
	
	/**
	 * Turns the robot to the heading in the most efficient direction, and reverses the orientation of the robot if going backwards is faster 
	 * @param heading The heading in degrees that you wish to turn to
	 * @see doTurnRightRadians
	 */
	private void doTurnRightDegrees(double heading) {
		doTurnRightRadians(Math.toRadians(heading));
	}
	
	/**
	 * Handles the creation of our custom events, such as detecting wall proximity
	 */
	private void addCustomEvents() {
		addCustomEvent(new Condition("Too close to wall") {
			@Override
			public boolean test() {
				return (
				// we're too close to the left wall
				(getX() <= WALL_MARGIN ||
				// or we're too close to the right wall
				getX() >= getBattleFieldWidth() - WALL_MARGIN ||
				// or we're too close to the bottom wall
				getY() <= WALL_MARGIN ||
				// or we're too close to the top wall
				getY() >= getBattleFieldHeight() - WALL_MARGIN));
			}
		});
	}
    

	private void doScan() {
		setTurnRadarRight(360*scanDirection);
	}
	
    /**
     * Moves the robot depending where the enemy is.
     * If the robot is far away, it will advance at a 20 degree angle, if we're close enough it will circle the enemy.
     */
	private void doMove() {
		EnemyBot enemy = enemyHandler.getEnemy();
		// allow wall avoidance movements to complete
		if (wallAvoidance == true) {
    		if (getDistanceRemaining() <= 5 && getDistanceRemaining() >= -5) {
    			wallAvoidance = false;
    		}
    	} else {
			
			//approach enemy if we're too far away
			double approachAngle = enemy.getDistance() > enemyProximity ? (20 * travelDirection) : 0;
			
			doTurnRightDegrees(enemy.getBearing() + 90 - approachAngle);
	    	
	    	
	    	if (Math.random() > 0.90) {
	    		travelDirection *= -1;
	    	} 
	    	setAhead(100 * travelDirection);
    	}
	}
	
	public void onHitRobot(HitRobotEvent e) {
		travelDirection *= -1;
	}
	
	/**
	 * Adjusts the gun and shoots at the enemy.
	 * If the enemy is travelling, it will shoot ahead at the location the enemy will be, should they continue to drive at the same speed and heading. 
	 */
	private void doShoot() {
		EnemyBot enemy = enemyHandler.getEnemy();
		if (enemy.isIdle(getTime())) {
			// nothing to fire at, therefore return
			return;
		}
    	double firePower = getFirePower(enemy.getDistance());
    	// aim gun at the enemy
    	double myX = getX();
    	double myY = getY();
    	double enemyX = enemy.getX();
    	double enemyY = enemy.getY();
    	double enemyHeading = enemy.getHeadingRadians();
    	double enemyVelocity = enemy.getVelocity();
    	double wallProximity = 2;
    	 
    	 
    	double deltaTime = 0;
    	double battleFieldHeight = getBattleFieldHeight(), 
    	       battleFieldWidth = getBattleFieldWidth();
    	double predictedX = enemyX, predictedY = enemyY;
    	while((++deltaTime) * getBulletSpeed(firePower) < 
    	      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
    		predictedX += Math.sin(enemyHeading) * enemyVelocity;	
    		predictedY += Math.cos(enemyHeading) * enemyVelocity;
    		if(	predictedX < wallProximity 
    			|| predictedY < wallProximity
    			|| predictedX > battleFieldWidth - wallProximity
    			|| predictedY > battleFieldHeight - wallProximity){
    			predictedX = Math.min(Math.max(wallProximity, predictedX), 
    	                    battleFieldWidth - wallProximity);	
    			predictedY = Math.min(Math.max(wallProximity, predictedY), 
    	                    battleFieldHeight - wallProximity);
    			break;
    		}
    	}
    	double theta = Utils.normalAbsoluteAngle(Math.atan2(
    	    predictedX - getX(), predictedY - getY()));
    	 
    	setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
    	
    	// shoot to kill if the gun is aimed and not too hot
    	if (getGunHeat() <= firePower && Math.abs(getGunTurnRemaining()) < 1) {
    		setFire(firePower);
    	}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e) {
		enemyHandler.death(e);
	}   

	
    /**
     * Do a victory dance
     */
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            setTurnRight(10000);
            setTurnGunRight(10000);
            setTurnRadarRight(10000);
        }
    }
    
    /**
     * Returns fire power based on how far away the enemy is
     * @param distance: the distance away from you the enemy is
     */
    private double getFirePower(double distance) {
    	double power = 0;
    	double maxPower = MAX_FIRE_POWER;
    	double energy = this.getEnergy();
    	// multiplier will reduce the amount of power as our energy depletes
    	double multiplier = energy / 100;
    	
    	
    	
    	if (distance < 200) power = 3 * maxPower;
    	else if (distance < 400) power = 2 * maxPower;
    	else power = 1 * maxPower;
    	
    	// should prevent running out of power
    	return power * multiplier;
    }

    /**
     * Returns the time taken to travel a specified distance given the objects speed
     * @param distance: distance to target
     * @param speed: speed of object
     * @return double: time
     */
    @SuppressWarnings("unused")
	private static double getTravelTime(double distance, double speed) {
    	return distance / speed;
    }
    
    /**
     * Returns bullet speed based on the game logic of speed = 20 - firePower * 3
     * @param power: power you're applying to the bullet
     * @return double: speed of the bullet
     */
	private static double getBulletSpeed(double firePower) {
    	return 20 - firePower * 3;
    }

	/**
	 * Computes the heading in radians from the robot to the object
	 * @param oX objects X coordinate
	 * @param oY objects Y coordinate
	 * @return bearing to the object
	 */
	private double getHeadingToObject(double oX, double oY) {
		double myX = getX();
		double myY = getY();
		
		if (myX == oX && myY == oY) {
			return 0;
		}
		double theta = Math.atan2(myY - oY, myX - oX);
		
		/* convert theta to match the game logic,
		 * theta is counter clockwise from the x axis
		 * game logic is clockwise from the y axis
		 */
		
		theta *= -1; // swap to clockwise
		theta -= (Math.PI /2); // subtract 90 degrees worth of radians to move from x to y axis
		
		theta -= getHeadingRadians(); //remove the current rotation to give a heading rather than a bearing
		
		// correct the radians if the number is now too low
		while (theta < Math.PI) {
			theta += Math.PI*2; // add 360 degrees of radians
		}
		
		// set up to rotate the fastest possible direction
		if (theta > Math.PI) {
			theta -= Math.PI * 2;
		}
		
		return theta;
	}

}