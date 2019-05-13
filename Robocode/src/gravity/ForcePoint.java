package gravity;

import java.awt.geom.Point2D;

import robocode.util.Utils;

/**Holds the x, y, and strength info of a gravity point**/
public class ForcePoint implements GravityPoint {
	private double x;
	private double y;
	private double strength;
	private double multiplier;
    
    public ForcePoint(double x, double y, double strength, double multiplier) {
        this.x = x;
        this.y = y;
        this.strength = strength;
        this.multiplier = multiplier;
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
	    double angle = 0;
	    double xForce = 0;
	    double yForce = 0;
	    
        force = this.getStrength()/Math.pow(Point2D.distance(x, y, this.getX(), this.getY()), this.multiplier);
        //Find the bearing from the point to us
        angle = Utils.normalRelativeAngle(Math.PI/2 - Math.atan2(y - this.getY(), x - this.getX())); 
        xForce = Math.sin(angle) * force;
        yForce = Math.cos(angle) * force;
		
		return new Point2D.Double(xForce, yForce);
	}
}
