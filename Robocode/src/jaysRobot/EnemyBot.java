package jaysRobot;

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
	private double energy = 0.0;
	private double previousHeadingRadians = 0.0;
	private double headingRadians = 0.0;
	private double velocity = 0.0;
	private int X = 0;
	private int Y = 0;
	private boolean alive = false;
	private double turnRate = 0.0;
	
	public void update(ScannedRobotEvent e, AdvancedRobot r) {
		this.name = e.getName();
		this.lastUpdate = this.updateTime;
		this.updateTime = e.getTime();
		this.bearingRadians = e.getBearingRadians();
		this.distance = e.getDistance();
		this.energy = e.getEnergy();
		this.previousHeadingRadians = this.headingRadians;
		this.headingRadians = e.getHeadingRadians();
		this.velocity = e.getVelocity();
		double angle = Math.toRadians((r.getHeading() + e.getBearing()) % 360);
		this.X = (int)(r.getX() + Math.sin(angle) * e.getDistance());
		this.Y = (int)(r.getY() + Math.cos(angle) * e.getDistance());
		this.alive = true;
		// calculate turn rate if the robot is alive,
		// if the robot is dead it likely doesn't have an accurate previous heading
		if (isAlive()) {
			this.turnRate = (headingRadians - previousHeadingRadians) / (this.updateTime - this.lastUpdate);
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
	public double getEnergy() {
		return energy;
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
}
