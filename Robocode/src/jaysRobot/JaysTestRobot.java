package jaysRobot;
import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public class JaysTestRobot extends AdvancedRobot {
	private static double MAX_FIRE_POWER = 1; // max power multiplier, should be in the range 0-1
	private static int WALL_MARGIN = 100;
	private boolean wallAvoidance = false;
	private ScannedRobotEvent closestEnemy = null;
	private long lastScanned = 0;
       	
	// this is to track the direction of the robot, this is so we can drive backwards 
	// if the shortest rotation to our bearing would be to reverse instead
	private int robotDirection = 1;
	// this is to track the direction of travel, forwards or backwards
	private int travelDirection = 1;
	
	public void run() {
		// setting radar / gun to be able to turn independently of each other
		// wiki states this is essential
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		// setting colours for my robot
        setBodyColor(new Color(255,20,147));
        setGunColor(new Color(235,0,127));
        setRadarColor(new Color(215,0,107));
        setScanColor(Color.PINK);
        setBulletColor(Color.PINK);
        
        addCustomEvents();
        
        setScanning();
        
        while (true) {
            // Initialise radar to permanently scan
        	if ( getRadarTurnRemaining() <= 0.0 ) {
        		isScanning = false;
        		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right
        	}
            execute();
        }
	}

	/**
	 * Most of our code will be here, this event is triggered when we spot another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		lastScanned = System.currentTimeMillis();
		if (closestEnemy == null) {
			closestEnemy = e;
		}
		
		// if we've scanned the enemy, update it's details
		if (e.getName().equals(closestEnemy.getName())) {
			closestEnemy = e;
		}
		
		if (e.getDistance() < closestEnemy.getDistance()) {
			closestEnemy = e;
		}
    	
    	// check that the robot you think is closest hasn't died, if it has, start attacking the newly scanned robot
//    	if (closestEnemy.getEnergy() <= 0) {
 //   		closestEnemy = e;
  //  	}
    	
    	if (e.getName().equals(closestEnemy.getName())) {
    		radarLock(e);
    		shoot(e);
    		move(e);
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

	private void setScanning() {
		if (isScanning == false) {
			isScanning = true;
			setTurnRadarRight(360);
			closestEnemy = null;
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
		
		addCustomEvent(new Condition("Gun cooldown higher than scan time") {
			@Override
			public boolean test() {
				// heat == 1 + firepower / 5 
				
				// default cooling time setting == 0.1, max is 0.7
				// max firePower gives cooling time of 1 + 3 / 5 == 1.6 / 0.1 == 16 ticks
				// min firePower gives cooling time of 1 + 1 / 5 == 1.2 / 0.1 == 12 ticks

				// any cooling time greater than 0.2 will cause this to never trigger
				
				double heat = getGunHeat();
				double coolingRate = getGunCoolingRate();
				double coolingTime = heat / coolingRate;
				
				// radar rotation = 45 degs per turn, 360 / 45 = 8 ticks per full rotation
				double radarRotationTime = 8;
				
				return (coolingTime > radarRotationTime);
			}
		});
		
	}
    
    /**
     * Moves the robot depending where the enemy is.
     * If the robot is far away, it will advance at a 20 degree angle, if we're close enough it will circle the enemy.
     * @param e the scanned robot event
     */
	private void move(ScannedRobotEvent e) {
		// allow wall avoidance movements to complete
		if (wallAvoidance == true) {
    		if (getDistanceRemaining() <= 5 && getDistanceRemaining() >= -5) {
    			wallAvoidance = false;
    		}
    	} else {
			
			double enemyProximity = 100; //how close to enemy should we get
			
			//approach enemy if we're too far away
			double approachAngle = e.getDistance() > enemyProximity ? (20 * travelDirection) : 0;
			
			doTurnRightDegrees(e.getBearing() + 90 - approachAngle);
	    	
	    	
	    	if (Math.random() > 0.95) {
	    		travelDirection *= -1;
	    	} 
	    	setAhead(100 * travelDirection);
    	}
	}
	
	/**
	 * Adjusts the gun and shoots at the enemy.
	 * If the enemy is travelling, it will shoot ahead at the location the enemy will be, should they continue to drive at the same speed and heading. 
	 * @param e the scanned robot event of the enemy to shoot at
	 */
	private void shoot(ScannedRobotEvent e) {
    	double firePower = getFirePower(e.getDistance());
    	// aim gun at the enemy
    	double myX = getX();
    	double myY = getY();
    	double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
    	double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
    	double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
    	double enemyHeading = e.getHeadingRadians();
    	double enemyVelocity = e.getVelocity();
    	 
    	 
    	double deltaTime = 0;
    	double battleFieldHeight = getBattleFieldHeight(), 
    	       battleFieldWidth = getBattleFieldWidth();
    	double predictedX = enemyX, predictedY = enemyY;
    	while((++deltaTime) * (20.0 - 3.0 * firePower) < 
    	      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
    		predictedX += Math.sin(enemyHeading) * enemyVelocity;	
    		predictedY += Math.cos(enemyHeading) * enemyVelocity;
    		if(	predictedX < 18.0 
    			|| predictedY < 18.0
    			|| predictedX > battleFieldWidth - 18.0
    			|| predictedY > battleFieldHeight - 18.0){
    			predictedX = Math.min(Math.max(18.0, predictedX), 
    	                    battleFieldWidth - 18.0);	
    			predictedY = Math.min(Math.max(18.0, predictedY), 
    	                    battleFieldHeight - 18.0);
    			break;
    		}
    	}
    	double theta = Utils.normalAbsoluteAngle(Math.atan2(
    	    predictedX - getX(), predictedY - getY()));
    	 
    	setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
    	
    	// shoot to kill if the gun is aimed and not too hot
    	if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
    		setFire(firePower);
    		setScanning();
    	}
	}

	
	/**
	 * Stops the radar spinning once a target is found, and locks onto the target
	 * @param e The robot to lock onto
	 */
	private void radarLock(ScannedRobotEvent e) {
		// basic 1v1 radar creating a permanent lock
    	setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() - getRadarHeading() + e.getBearing()));
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
    private static double getFirePower(double distance) {
    	if (distance < 200) return 3 * MAX_FIRE_POWER;
    	else if (distance < 400) return 2 * MAX_FIRE_POWER;
    	else return 1 * MAX_FIRE_POWER;
    }

    /**
     * Returns the time taken to travel a specified distance given the objects speed
     * @param distance: distance to target
     * @param speed: speed of object
     * @return double: time
     */
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