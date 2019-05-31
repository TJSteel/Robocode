package safePoints;

public class SafePoint {
	private double x;
	private double y;
	private double danger;

	public SafePoint(double x, double y, double danger) {
		this.x = x;
		this.y = y;
		this.danger = danger;
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
	
	public double getDanger() {
		return danger;
	}
	
	public void setDanger(double danger) {
		this.danger = danger;
	}
	
}
