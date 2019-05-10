package utils;

public class Utils {
	/**
	 * if a bearing is not within the -pi to pi range, alters it to provide the shortest angle
	 * @param angle
	 * @return
	 */
	public static double normaliseBearing(double angle) {
		while (angle > Math.PI)
			angle -= 2 * Math.PI;
		while (angle < -Math.PI)
			angle += 2 * Math.PI;
		return angle;
	}
	
	/**
	 * if a heading is not within the 0 to 2pi range, alters it to provide the shortest angle
	 * @param angle
	 * @return
	 */
	public static double normaliseHeading(double angle) {
		if (angle > 2*Math.PI)
			angle -= 2*Math.PI;
		if (angle < 0)
			angle += 2*Math.PI;
		return angle;
	}
}
