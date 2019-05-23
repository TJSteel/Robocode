package jaysRobot;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import movement.Bullet;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

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
	
	public Position getPredictedPosition(double futureTime) {
		Position lastPosition = this.getLastPosition();
		Position position = this.getPosition();
		
		long time1 = lastPosition.getUpdateTime();
		double heading1 = lastPosition.getHeading();
		double x1 = lastPosition.getX();
		double y1 = lastPosition.getY();
		
		long time2 = position.getUpdateTime();
		double heading2 = position.getHeading();
		double x2 = position.getX();
		double y2 = position.getY();

		double timeTravelled = time2 - time1;
		double futureHeading = heading2;
		double futureX = 0;
		double futureY = 0;

		double headingChange = heading2 - heading1;

		if (headingChange == 0) {
			// linear target
			double xTravelled = (x2-x1) / timeTravelled;
			double yTravelled = (y2-y1) / timeTravelled;
			futureX = x2 + xTravelled * futureTime;
			futureY = y2 + yTravelled * futureTime;
		} else {
			/* 
			 * circular target, the way this will be calculated will mean we have an isosceles triangle
			 * the triangle will mark the points between position1, position2, and the center of the circle
			 * headings of the lines will be as follows
			 * lineA = position1 heading to position2
			 * lineB = center heading to position2
			 * lineC = position1 heading to center
			 */
			
			// we can start by calculating all headings
			// heading A will be the second heading, minus the difference between heading 2 and 1
			double lineAHeading = heading2 - (headingChange / 2);
			// the next 2 lines will be the known headings, plus a quarter turn towards the center of the triangle
			double lineBHeading = heading2 - (Math.PI / 2);
			double lineCHeading = heading1 + (Math.PI / 2);
			
			// now we know all headings, we can calculate the angles of the triangle
			double angleA = lineCHeading - lineAHeading;
			// unused but leaving here in comments for full understanding of how they're all calculated
			// double angleB = angleA;
			double angleC = Math.PI - (angleA*2);
			
			// now we have angles, we can calculate the lengths of the triangle, simpler because it's an isosceles triangle
			double lineALength = Point2D.distance(x1, y1, x2, y2);
			double lineBLength = lineALength*Math.sin(angleA)/Math.sin(angleC);
			// unused but leaving here in comments for full understanding of how they're all calculated
			// double lineCLength = lineBLength;
			
			// now we have all points of the triangle, we can calculate the point marking the center of the circle
			double centerX = Calc.getObjectX(x1, lineCHeading, lineBLength);
			double centerY = Calc.getObjectY(y1, lineCHeading, lineBLength);

			// now that we have the location of the objects center of rotation, we can work out where the object is at any given time in the future
			// speed we're orbiting the circle in radians per time;
			double angularVelocity = angleC / timeTravelled;
			double futureAngleTravelled = (angularVelocity * futureTime);
			futureHeading = lineBHeading + futureAngleTravelled;
			futureX = Calc.getObjectX(centerX, futureHeading, lineBLength);
			futureY = Calc.getObjectY(centerY, futureHeading, lineBLength);
		}
		
		Position futurePosition = new Position();
		futurePosition.setHeading(futureHeading);
		futurePosition.setUpdateTime((long)futureTime);
		futurePosition.setX(futureX);
		futurePosition.setY(futureY);
		
		return futurePosition;
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