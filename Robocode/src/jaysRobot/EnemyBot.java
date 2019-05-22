package jaysRobot;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import enemyRobots.Position;
import movement.Bullet;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class EnemyBot {

	private String name = "";
	private final long idleTimeout = 50;
	private boolean alive = false;
	private double turnRate = 0.0;
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	private Position position = new Position();
	private Position lastPosition = new Position();
	
	public ArrayList<Bullet> getBullets() {
		return bullets;
	}
	public void update(ScannedRobotEvent e, AdvancedRobot r) {
		this.name = e.getName();
		this.lastPosition = position;
		this.position = new Position();
		
		this.position.setUpdateTime(e.getTime());
		this.position.setHeading(e.getHeadingRadians());
		this.position.setVelocity(e.getVelocity());
		this.position.setEnergy(e.getEnergy());
		double angle = (r.getHeadingRadians() + e.getBearingRadians()) % (Math.PI * 2); // % (Math.PI * 2) basically removes extra full rotations 
		this.position.setX(Calc.getObjectX(r.getX(), angle, e.getDistance()));
		this.position.setY(Calc.getObjectY(r.getY(), angle, e.getDistance()));
		this.alive = true;
		this.turnRate = (this.getPosition().getHeading() - this.getLastPosition().getHeading()) / (this.getPosition().getUpdateTime() - this.getLastPosition().getUpdateTime());
		this.checkEnergyDrop(r);
	}
	private void checkEnergyDrop(AdvancedRobot r) {
		double lastEnergy = this.getLastPosition().getEnergy();
		double energy = this.getPosition().getEnergy();
		double energyDrop = lastEnergy - energy;
		Position position = this.getPosition();
		double x = position.getX();
		double y = position.getY();
		if (energyDrop > 0 && energyDrop <= 3) {
			//suspect bullet was fired
			this.bullets.add(new Bullet(position.getUpdateTime(), x, y, energyDrop, Calc.getBulletSpeed(energyDrop), Calc.getHeadingToObject(x, y, r.getX(), r.getY())));
		}
	}
	public String getName() {
		return name;
	}
	public long timeSinceLastUpdate(long gameTime) {
		return gameTime - this.getPosition().getUpdateTime();
	}
	public double getTurnRate() {
		return this.turnRate;
	}
	
	public Point2D getPredictedLocation(long time) {
    	// aim gun at the enemy
		Position position = this.getPosition();
		Position lastPosition = this.getLastPosition();
		double heading = position.getHeading();
    	double headingChange = heading - lastPosition.getHeading();
    	double predictedX = position.getX();
    	double predictedY = position.getY();
    	double velocity = position.getVelocity();
    	double distance = time * velocity;
    	double turnAngle = headingChange * time; 
    	
    	/*
    	while((++deltaTime) * bulletSpeed < Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
    		predictedX += Math.sin(heading) * velocity;
    		predictedY += Math.cos(heading) * velocity;
    		heading += headingChange;
    	}
    	double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
    	*/
		return new Point2D.Double(predictedX, predictedY);
	}
	
	/**
	 * Returns true if the last scan time exceeds the idle limit 
	 * @return
	 */
	public boolean isIdle(long gameTime) {
		return (this.isAlive() == false) || (gameTime - this.getLastPosition().getUpdateTime()) > this.idleTimeout;
	}
	public void death(RobotDeathEvent e) {
		this.name = e.getName();
		Position position = this.getPosition();
		position.setUpdateTime(e.getTime());
		position.setEnergy(0.0);
		this.alive = false;
	}
	public boolean isAlive() {
		return alive;
	}

	public void hitBy(robocode.Bullet bullet) {
		/* The bullet will do (4 * power) damage if it hits another robot. If power is greater than 1, it will
		 * do an additional 2 * (power - 1) damage. You will get (3 * power) back if you hit the other robot.  
		 */
		double power = bullet.getPower(); 
		double damage = power * 4;
		if (power > 1) {
			damage += 2 * (power - 1);
		}
		this.getPosition().setEnergy(this.getPosition().getEnergy() - damage);
	}
	public void hit(robocode.Bullet bullet) {
		/* The bullet will do (4 * power) damage if it hits another robot. If power is greater than 1, it will
		 * do an additional 2 * (power - 1) damage. You will get (3 * power) back if you hit the other robot.  
		 */
		double power = bullet.getPower();
		double energyGain = power * 3;
		this.getPosition().setEnergy(this.getPosition().getEnergy() + energyGain);
	}
	public void clearBullets() {
		this.bullets.clear();
	}
	public Position getPosition() {
		return this.position;
	}
	public Position getLastPosition() {
		return this.lastPosition;
	}
}