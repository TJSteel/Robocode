package movement;

/**Holds the x, y, and strength info of a gravity point**/
public class GravityPoint {
	private double x;
	private double y;
	private double strength;
    
    public GravityPoint(double x,double y,double strength) {
        this.x = x;
        this.y = y;
        this.strength = strength;
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
}
