package jaysRobot;

import robocode.util.Utils;

public class Calc {
    /**
     * Returns bullet speed based on the game logic of speed = 20 - firePower * 3
     * @param power: power you're applying to the bullet
     * @return double: speed of the bullet
     */
	public static double getBulletSpeed(double firePower) {
    	return 20 - firePower * 3;
    }
	
	/**
	 * Computes the heading in radians from the robot to the object
	 * @param oX objects X coordinate
	 * @param oY objects Y coordinate
	 * @return heading to the object
	 */
	public static double getHeadingToObject(double x, double y, double oX, double oY) {
		
		if (x == oX && y == oY) {
			return 0;
		}
		double theta = Math.atan2(oY - y, oX - x);
		/* convert theta to match the game logic,
		 * theta is counter clockwise from the x axis
		 * game logic is clockwise from the y axis
		 */
		theta -= (Math.PI /2); // subtract 90 degrees worth of radians to move from x to y axis
		theta *= -1; // swap to clockwise

		// return angle between 0 and Pi * 2 (0-360 degrees)
		return Utils.normalAbsoluteAngle(theta);
	}
	
	/**
	 * Computes the X position of another object
	 * @param x
	 * @param angle
	 * @param distance
	 * @return object X
	 */
	public static double getObjectX(double x, double angle, double distance) {
		return (x + Math.sin(angle) * distance);
	}
	
	/**
	 * Computes the Y position of another object
	 * @param y
	 * @param angle
	 * @param distance
	 * @return object Y
	 */
	public static double getObjectY(double y, double angle, double distance) {
		return (y + Math.cos(angle) * distance);
	}
	
	public static int constrainValue(int value, int min, int max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}
	public static double constrainValue(double value, double min, double max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}
}
