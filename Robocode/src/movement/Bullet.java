package movement;

import jaysRobot.Calc;

public class Bullet{
	private long shotTime;
	private double x;
	private double y;
	private double power;
	private double velocity;
	private double heading;
	
	public Bullet(long shotTime, double x, double y, double power, double velocity, double heading) {
		super();
		this.shotTime = shotTime;
		this.x = x;
		this.y = y;
		this.power = power;
		this.velocity = velocity;
		this.heading = heading;
	}
	public double getX(long time) {
		double distance = velocity * (time - this.shotTime);
		return Calc.getObjectX(this.x, this.heading, distance);
	}
	public double getY(long time) {
		double distance = velocity * (time - this.shotTime);
		return Calc.getObjectY(this.y, this.heading, distance);
	}
	public double getPower() {
		return power;
	}
	public double getVelocity() {
		return velocity;
	}
	public double getHeading() {
		return heading;
	}
	public boolean isActive(long time) {
		if (this.getX(time) < 0 || this.getY(time) < 0) return false;
		
		return true;
	}
	
}