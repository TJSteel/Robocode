package movement;

import java.awt.geom.Point2D;

public class Position {
	private long updateTime;
	private double heading;
	private double velocity;
	private double energy;
	private double x; 
	private double y;
	
	public long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	public double getHeading() {
		return heading;
	}
	public void setHeading(double heading) {
		this.heading = heading;
	}
	public double getVelocity() {
		return velocity;
	}
	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}
	public double getEnergy() {
		return energy;
	}
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getDistance(double x, double y) {
		double myX = this.getX();
		double myY = this.getY();
		return Point2D.distance(x, y, myX, myY);
	}
}
