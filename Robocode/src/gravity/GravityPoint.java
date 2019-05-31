package gravity;

import java.awt.geom.Point2D;

import jaysRobot.Calc;
import robocode.util.Utils;

/**Holds the x, y, and strength info of a gravity point**/
public class GravityPoint implements ForcePoint {
	private double x;
	private double y;
	private double strength;
	private double deadZone;
    
    public GravityPoint(double x, double y, double strength, double deadZone) {
        this.x = x;
        this.y = y;
        this.strength = strength;
        this.deadZone = deadZone;
    }
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getStrength() {
		return strength;
	}
	public double getDeadZone() {
		return deadZone;
	}
	public Point2D getForce(double x, double y) {
		
		double forceX = this.getX();
		double forceY = this.getY();
		
		// start by checking the distance, if it's inside the dead zone, return 0 force
		double distance = Point2D.distance(x, y, forceX, forceY);
		if (distance < this.getDeadZone()) return new Point2D.Double(0, 0);
		
		
		
        double force = this.getStrength()/distance;
        //Find the heading from the point to us
        double angle = Utils.normalRelativeAngle(Calc.getHeadingToObject(forceX, forceY, x, y));
        double xForce = Calc.getObjectX(forceX, angle, force);
        double yForce = Calc.getObjectY(forceY, angle, force);
		
		return new Point2D.Double(xForce - forceX, yForce - forceY);
	}
}
