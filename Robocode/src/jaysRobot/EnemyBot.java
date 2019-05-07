package jaysRobot;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class EnemyBot {

	private String name = "";
	private long lastUpdate = 0;
	private final long idleTimeout = 1000; //not good, just realised this won't timeout faster if the game speed is increased, should count ticks instead of sys time
	private double bearingRadians = 0.0;
	private double distance = 0.0;
	private double energy = 0.0;
	private double headingRadians = 0.0;
	private double velocity = 0.0;
	private int X = 0;
	private int Y = 0;
	private boolean alive = true;
	
	public void update(ScannedRobotEvent e, AdvancedRobot r, long gameTime) {
		this.name = e.getName();
		this.lastUpdate = gameTime;
		this.bearingRadians = e.getBearingRadians();
		this.distance = e.getDistance();
		this.energy = e.getEnergy();
		this.headingRadians = e.getHeadingRadians();
		this.velocity = e.getVelocity();
		double angle = Math.toRadians((r.getHeading() + e.getBearing()) % 360);
		this.X = (int)(r.getX() + Math.sin(angle) * e.getDistance());
		this.Y = (int)(r.getY() + Math.cos(angle) * e.getDistance());
		this.alive = true;
	}
	public String getName() {
		return name;
	}
	public double timeSinceLastUpdate() {
		return System.currentTimeMillis() - this.lastUpdate;
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
		return (this.energy == 0.0) || (gameTime - this.lastUpdate) > this.idleTimeout;
	}
	public void death(RobotDeathEvent e) {
		this.name = e.getName();
		this.lastUpdate = System.currentTimeMillis();
		this.bearingRadians = 0.0;
		this.distance = 0.0;
		this.energy = 0.0;
		this.headingRadians = 0.0;
		this.velocity = 0.0;
		this.X = 0;
		this.Y = 0;
		this.alive = false;
	}
	public boolean isAlive() {
		return alive;
	}
}
