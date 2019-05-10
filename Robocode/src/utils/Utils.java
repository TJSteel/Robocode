package utils;

public class Utils {
	/**
	 * if a bearing is not within the -pi to pi range, alters it to provide the shortest angle
	 * @param angle
	 * @return
	 */
	public static double normaliseBearing(double angle) {
		while (angle > Math.PI) {
			angle -= (Math.PI * 2);
		}
		while (angle < (-Math.PI)) {
			angle += (Math.PI * 2);
		}
		return angle;
	}
	
	/**
	 * if a heading is not within the 0 to 2pi range, alters it to provide the shortest angle
	 * @param angle
	 * @return
	 */
	public static double normaliseHeading(double angle) {
		while (angle > 2 * Math.PI)
			angle -= 2 * Math.PI;
		while (angle < 0)
			angle += 2 * Math.PI;
		return angle;
	}

	/**
	 * returns the distance between two x,y coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double getRange( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo );
		return h;	
	}
	
	/**
	 * gets the absolute bearing between to x,y coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double absbearing( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = getRange( x1,y1, x2,y2 );
		if( xo > 0 && yo > 0 )
		{
			return Math.asin( xo / h );
		}
		if( xo > 0 && yo < 0 )
		{
			return Math.PI - Math.asin( xo / h );
		}
		if( xo < 0 && yo < 0 )
		{
			return Math.PI + Math.asin( -xo / h );
		}
		if( xo < 0 && yo > 0 )
		{
			return 2.0*Math.PI - Math.asin( -xo / h );
		}
		return 0;
	}

}
