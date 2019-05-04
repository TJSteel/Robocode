package jaysRobot;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class EnemyBot {

	private String name;
	private double bearingRadians;
	private double distance;
	private double energy;
	private double headingRadians;
	private double velocity;
	private int X;
	private int Y;

	public EnemyBot() {
		this.reset();
	}
	
	public void update(ScannedRobotEvent e, AdvancedRobot r) {
		this.name = e.getName();
		this.bearingRadians = e.getBearingRadians();
		this.distance = e.getDistance();
		this.energy = e.getEnergy();
		this.headingRadians = e.getHeadingRadians();
		this.velocity = e.getVelocity();
		
		double angle = Math.toRadians((r.getHeading() + e.getBearing()) % 360);
		this.X = (int)(r.getX() + Math.sin(angle) * e.getDistance());
		this.Y = (int)(r.getY() + Math.cos(angle) * e.getDistance());
	}
	
	public void reset() {
		this.name = "";
		this.bearingRadians = 0.0;
		this.distance = 0.0;
		this.energy = 0.0;
		this.headingRadians = 0.0;
		this.velocity = 0.0;
		this.X = 0;
		this.Y = 0;
	}
	
	public boolean none() {
		return this.name.equals("");
	}
	
	
	public String getName() {
		return name;
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
}
