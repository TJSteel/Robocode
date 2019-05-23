package gravity;

import java.awt.geom.Point2D;

public interface ForcePoint {
	public double getX();
	public double getY();
	public double getStrength();
	public Point2D getForce(double x, double y);
}
