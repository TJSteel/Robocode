package jaysRobot;

import java.util.ArrayList;

import movement.Bullet;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class EnemyBot {

	private String name = "";
	private long lastUpdate = 0;
	private long updateTime = 0;
	private final long idleTimeout = 50; //not good, just realised this won't timeout faster if the game speed is increased, should count ticks instead of sys time
	private double bearingRadians = 0.0;
	private double distance = 0.0;
	private double lastEnergy = 0.0;
	private double energy = 0.0;
	private double previousHeadingRadians = 0.0;
	private double headingRadians = 0.0;
	private double velocity = 0.0;
	private int X = 0;
	private int Y = 0;
	private boolean alive = false;
	private double turnRate = 0.0;
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	public ArrayList<Bullet> getBullets() {
		return bullets;
	}
	public void update(ScannedRobotEvent e, AdvancedRobot r) {
		this.name = e.getName();
		this.lastUpdate = this.updateTime;
		this.updateTime = e.getTime();
		this.bearingRadians = e.getBearingRadians();
		this.distance = e.getDistance();
		this.lastEnergy = this.energy;
		this.energy = e.getEnergy();
		this.previousHeadingRadians = this.headingRadians;
		this.headingRadians = e.getHeadingRadians();
		this.velocity = e.getVelocity();
		double angle = (r.getHeadingRadians() + bearingRadians) % (Math.PI * 2); // % (Math.PI * 2) basically removes extra full rotations 
		this.X = (int) Calc.getObjectX(r.getX(), angle, e.getDistance());
		this.Y = (int) Calc.getObjectY(r.getY(), angle, e.getDistance());
		this.alive = true;
		this.turnRate = (headingRadians - previousHeadingRadians) / (this.updateTime - this.lastUpdate);
		
		this.checkEnergyDrop(r);

	}
	private void checkEnergyDrop(AdvancedRobot r) {
		double lastEnergy = this.getLastEnergy();
		double energy = this.getEnergy();
		double energyDrop = lastEnergy - energy;
		double x = this.getX();
		double y = this.getY();
		if (energyDrop > 0 && energyDrop <= 3) {
			//suspect bullet was fired
			this.bullets.add(new Bullet(this.updateTime, x, y, energyDrop, Calc.getBulletSpeed(energyDrop), Calc.getHeadingToObject(x, y, r.getX(), r.getY())));
			
		}
	}
	public String getName() {
		return name;
	}
	public long timeSinceLastUpdate(long gameTime) {
		return gameTime - this.updateTime;
	}
	public double getBearingRadians() {
		return bearingRadians;
	}
	public double getBearing() {
		return Math.toDegrees(bearingRadians);
	}
	public double getDistance() {
		return distance;
	}
	public double getLastEnergy() {
		return lastEnergy;
	}
	public double getEnergy() {
		return energy;
	}
	public double getPreviousHeadingRadians() {
		return previousHeadingRadians;
	}
	public double getHeadingRadians() {
		return headingRadians;
	}
	public double getHeading() {
		return Math.toDegrees(headingRadians);
	}
	public double getVelocity() {
		return velocity;
	}
	public int getX() {
		return X;
	}
	public int getY() {
		return Y;
	}
	public double getTurnRate() {
		return this.turnRate;
	}
	/**
	 * Returns true if the last scan time exceeds the idle limit 
	 * @return
	 */
	public boolean isIdle(long gameTime) {
		return (this.isAlive() == false) || (gameTime - this.lastUpdate) > this.idleTimeout;
	}
	public void death(RobotDeathEvent e) {
		this.name = e.getName();
		this.lastUpdate = e.getTime();
		this.energy = 0.0;
		this.alive = false;
	}
	public boolean isAlive() {
		return alive;
	}
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("\nEnemy Details");
		str.append("\nname: " + this.getName());
		str.append("\nidleTimeout: " + this.idleTimeout);
		str.append("\nlastUpdate: " + this.lastUpdate);
		str.append("\nbearingRadians: " + this.getBearingRadians());
		str.append("\ndistance: " + this.getDistance());
		str.append("\nenergy: " + this.getEnergy());
		str.append("\nheadingRadians: " + this.getHeadingRadians());
		str.append("\nvelocity: " + this.getVelocity());
		str.append("\nx: " + this.getX());
		str.append("\ny: " + this.getY());
		str.append("\nalive: " + this.isAlive());
		return str.toString();
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
		this.energy -= damage;
	}
	public void clearBullets() {
		this.bullets.clear();
	}
}