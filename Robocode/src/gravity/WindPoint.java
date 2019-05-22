package gravity;

import java.awt.geom.Point2D;

/**Holds the x, y, and strength info of a gravity point**/
public class WindPoint implements ForcePoint {
	private double x;
	private double y;
	private double strength;
	private double multiplier;
	private double heading;
    
    public WindPoint(double x, double y, double strength, double multiplier, double heading) {
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
	    
        force = this.strength/Math.pow(Point2D.distance(x, y, this.getX(), this.getY()), this.multiplier);
        //Find the bearing from the point to us
        xForce = Math.sin(this.heading) * force;
        yForce = Math.cos(this.heading) * force;
		
		return new Point2D.Double(xForce, yForce);
	}
}
