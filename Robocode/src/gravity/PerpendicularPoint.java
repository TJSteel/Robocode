package gravity;

import java.awt.geom.Point2D;

import jaysRobot.Calc;
import robocode.util.Utils;

/**Holds the x, y, and strength info of a gravity point**/
public class PerpendicularPoint implements GravityPoint {
	private double x;
	private double y;
	private double strength;
	private double multiplier;
	private double heading;
    
    public PerpendicularPoint(double x, double y, double strength, double multiplier, double heading) {
        this.x = x;
        this.y = y;
        this.strength = strength;
        this.multiplier = multiplier;
        this.heading = heading;
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
	public Point2D getForce(double x, double y) {
	    double force = 0;
	    double xForce = 0;
	    double yForce = 0;
	    double heading = this.heading;
	    double objectHeading = Calc.getHeadingToObject(getX(), getY(), x, y);
	    double objectBearing = objectHeading - heading;
	    objectBearing = Utils.normalRelativeAngle(objectBearing);
	    
	    if (objectBearing > Math.PI / 2) {
	    	heading += Math.PI / 2;
	    } else {
	    	heading -= Math.PI / 2;
	    }
	    Utils.normalRelativeAngle(heading);
        force = this.strength/Math.pow(Point2D.distance(x, y, this.getX(), this.getY()), this.multiplier);
        //Find the bearing from the point to us
        xForce = Math.sin(heading) * force;
        yForce = Math.cos(heading) * force;
		
		return new Point2D.Double(xForce, yForce);
	}
}
