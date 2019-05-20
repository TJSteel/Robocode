package jaysRobot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import movement.Bullet;
import movement.MovementHandler;
import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public class JaysRobot extends AdvancedRobot {
	private static double MAX_FIRE_POWER = 1; // max power multiplier, should be in the range 0-1
	private static boolean DEBUG = false;
	private byte scanDirection = 1;
	private int robotDirection = 1;
	private static double WALL_MARGIN = 100;
	
	private EnemyHandler enemyHandler = new EnemyHandler();
	private MovementHandler movement = new MovementHandler(this, enemyHandler);
	
	@Override
	public void run() {
		// setting radar / gun to be able to turn independently of each other
		// wiki states this is essential
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
		// setting colours for my robot
        setBodyColor(new Color(0,255,0));
        setGunColor(new Color(0,200,0));
        setRadarColor(new Color(0,100,0));
        setScanColor(Color.GREEN);
        setBulletColor(Color.GREEN);
        
        addCustomEvents();
        while (true) {
        	doScan();
        	movement.move();
        	doShoot();
        	execute();
        	if (DEBUG) System.out.println(this.enemyHandler.getEnemy().toString());
        }
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		long time = getTime();
		EnemyBot enemy = enemyHandler.getEnemy();
		if (!enemy.isIdle(time)) {
		    
		    // Set the paint color to a red half transparent color
		    g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
		 
		    // Draw a line from our robot to the scanned robot
		    g.drawLine((int)getX(), (int)getY(), enemy.getX(), enemy.getY());
		 
		    // Draw a filled square on top of the scanned robot that covers it
		    g.fillRect(enemy.getX() - 20,  enemy.getY() - 20, 40, 40);
		}
		for (Bullet b : enemyHandler.getBullets()) {
			g.drawOval((int)b.getX(time)-25, (int)b.getY(time)-25, 50, 50);
		}
	}

	@Override
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
	 * Handles the creation of our custom events, such as detecting wall proximity
	 */
	private void addCustomEvents() {
		addCustomEvent(new Condition("Custom Event") {
			@Override
			public boolean test() {
				return false;
			}
		});
	}
    
	/**
	 * Handles our custom events, such as getting close to the walls
	 */
	@Override
	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("Custom Event"))
		{
		}
	}

	private void doScan() {
		setTurnRadarRight(360*scanDirection);
	}
	
	@Override
	public void onHitRobot(HitRobotEvent e) {
		
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
    	// aim gun at the enemy
    	double myX = getX();
    	double myY = getY();
    	double distance = enemy.getDistance();
    	double firePower = getFirePower(distance);
    	double bulletSpeed = Calc.getBulletSpeed(firePower);
    	double enemyX = enemy.getX();
    	double enemyY = enemy.getY();
    	double enemyHeading = enemy.getHeadingRadians();
    	double oldEnemyHeading = enemy.getPreviousHeadingRadians();
    	double enemyHeadingChange = enemyHeading - oldEnemyHeading;
    	double enemyVelocity = enemy.getVelocity();

    	double deltaTime = 0;
    	double battleFieldHeight = getBattleFieldHeight(), 
    	       battleFieldWidth = getBattleFieldWidth();
    	double predictedX = enemyX, predictedY = enemyY;
    	while((++deltaTime) * bulletSpeed < Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
    		predictedX += Math.sin(enemyHeading) * enemyVelocity;
    		predictedY += Math.cos(enemyHeading) * enemyVelocity;
    		enemyHeading += enemyHeadingChange;
    		if(	predictedX < 0
    			|| predictedY < 0
    			|| predictedX > battleFieldWidth
    			|| predictedY > battleFieldHeight){
    	 
    			predictedX = Math.min(Math.max(0, predictedX), battleFieldWidth);	
    			predictedY = Math.min(Math.max(0, predictedY), battleFieldHeight);
    			if (DEBUG) System.out.println("deltaTime exited in if:" + deltaTime);
    			break;
    		}
    	}
		if (DEBUG) System.out.println("deltaTime exited from loop:" + deltaTime);
    	double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
    	 
    	setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
    	
    	// shoot to kill if the gun is aimed and not too hot
    	if (getGunHeat() <= firePower && Math.abs(getGunTurnRemaining()) < 5) {
    		setFire(firePower);
    	}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e) {
		super.onRobotDeath(e);
		this.enemyHandler.death(e);
	}   

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e) {
		super.onBulletHitBullet(e);
		this.enemyHandler.bulletEvent(e.getBullet());
	}	
	
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		super.onHitByBullet(e);
		this.enemyHandler.bulletEvent(e.getBullet());
	}

	@Override
	public void onStatus(StatusEvent e) {
		// TODO Auto-generated method stub
		super.onStatus(e);
	}	

	@Override
	public void onDeath(DeathEvent event) {
		super.onDeath(event);
		this.enemyHandler.clearBullets();
	}
	
    /**
     * Do a victory dance
     */
	@Override
    public void onWin(WinEvent e) {
		this.enemyHandler.clearBullets();
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

	/**Move towards an x and y coordinate**/
	public void goTo(double dX, double dY) {
		double battleFieldWidth = getBattleFieldWidth();
		double battleFieldHeight = getBattleFieldHeight();
		
		if (dX < WALL_MARGIN) dX = WALL_MARGIN;
		if (dX > battleFieldWidth-WALL_MARGIN) dX = battleFieldWidth - WALL_MARGIN;
		if (dY < WALL_MARGIN) dY = WALL_MARGIN;
		if (dY > battleFieldHeight-WALL_MARGIN) dY = battleFieldHeight - WALL_MARGIN;
		
	    double distance = Point2D.distance(this.getX(), this.getY(), dX, dY);
	    double angle = Calc.getHeadingToObject(this.getX(), this.getY(), dX, dY);
	    this.turnTo(angle);
	    if (distance < 10) distance = 10;
	    this.setAhead(distance);
	}

	@Override
	public void setAhead(double distance) {
		distance *= robotDirection;
		super.setAhead(distance);
	}
	
	/**
	 * Turns the robot to the angle in the most efficient direction, and reverses the orientation of the robot if going backwards is faster 
	 * @param angle The angle in radians that you wish to turn to
	 */
	public void turnTo(double angle) {
		double bearing = Utils.normalRelativeAngle(angle - this.getHeadingRadians());
		final double halfPi = Math.PI / 2;
		
		if (bearing > halfPi) { // if turning more than 90 degrees
			bearing -= Math.PI;
			robotDirection = -1;
		} else if (bearing < (-halfPi)) { // if turning more than 90 degrees
			bearing += (Math.PI);
			robotDirection = -1;
		} else {
			robotDirection = 1;
		}
		this.setTurnRightRadians(bearing);
	}

	public void reverseTravelDirection() {
		robotDirection *= -1;
	}	
}