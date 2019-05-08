package jaysRobot;

public class Game {
    
    /**
     * Returns bullet speed based on the game logic of speed = 20 - firePower * 3
     * @param power: power you're applying to the bullet
     * @return double: speed of the bullet
     */
	public static double getBulletSpeed(double firePower) {
    	return 20 - firePower * 3;
    }
}
