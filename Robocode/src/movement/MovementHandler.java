package movement;

import java.util.*;

import jaysRobot.EnemyBot;
import jaysRobot.EnemyHandler;
import robocode.AdvancedRobot;
import utils.Utils;

public class MovementHandler
{
	private double midpointstrength;	//The strength of the gravity point in the middle of the field
	private int midpointcount;			//Number of turns since that strength was changed.
	private AdvancedRobot robot;
	private EnemyHandler enemyHandler;
	// this is to track the direction of the robot, this is so we can drive backwards 
	// if the shortest rotation to our bearing would be to reverse instead
	private int robotDirection = 1;

	public MovementHandler(AdvancedRobot robot, EnemyHandler enemyHandler) {
		this.midpointstrength = 0;
		this.midpointcount = 0;
		this.robot = robot;
		this.enemyHandler = enemyHandler;
	}
	
	public void antiGravMove(AdvancedRobot robot, EnemyHandler enemyHandler) {
		this.robot = robot;
		this.enemyHandler = enemyHandler;
		double x = robot.getX();
		double y = robot.getX();
		double battlefieldWidth = this.robot.getBattleFieldWidth();
		double battlefieldHeight = this.robot.getBattleFieldHeight();
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    GravityPoint p;
		ArrayList<EnemyBot> enemies = this.enemyHandler.getEnemies();
	    
		//cycle through all the enemies.  If they are alive, they are repulsive.  Calculate the force on us
		for (EnemyBot enemy : enemies) {
			if (enemy.isAlive()) {
				p = new GravityPoint(enemy.getX(),enemy.getY(), -1000);
		        force = p.getStrength()/Math.pow(Utils.getRange(x, y, p.getX(), p.getY()), 2);
		        //Find the bearing from the point to us
		        ang = Utils.normaliseBearing(Math.PI/2 - Math.atan2(y - p.getY(), x - p.getX())); 
		        //Add the components of this force to the total force in their respective directions
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
			}
	    }
	    
		/**The next section adds a middle point with a random (positive or negative) strength.
		The strength changes every 5 turns, and goes between -1000 and 1000.  This gives a better
		overall movement.**/
		midpointcount++;
		if (midpointcount > 5) {
			midpointcount = 0;
			midpointstrength = (Math.random() * 2000) - 1000;
		}
		p = new GravityPoint(battlefieldWidth/2, battlefieldHeight/2, midpointstrength);
		force = p.getStrength()/Math.pow(Utils.getRange(x, y, p.getX(), p.getY()), 1.5);
	    ang = Utils.normaliseBearing(Math.PI/2 - Math.atan2(y - p.getY(), x - p.getX())); 
	    xforce += Math.sin(ang) * force;
	    yforce += Math.cos(ang) * force;
	   
	    /**The following four lines add wall avoidance.  They will only affect us if the robot is close 
	    to the walls due to the force from the walls decreasing at a power 3.**/
	    xforce += 5000/Math.pow(Utils.getRange(x, y, battlefieldWidth, y), 3);
	    xforce -= 5000/Math.pow(Utils.getRange(x, y, 0, y), 3);
	    yforce += 5000/Math.pow(Utils.getRange(x, y, x, battlefieldHeight), 3);
	    yforce -= 5000/Math.pow(Utils.getRange(x, y, x, 0), 3);
	    
	    //Move in the direction of our resolved force.
	    goTo(x-xforce, y-yforce);
	}
	
	/**Move towards an x and y coordinate**/
	private void goTo(double x, double y) {
	    double dist = 20; 
	    double angle = Utils.absbearing(this.robot.getX(), this.robot.getY(), x, y);
	    this.turnTo(angle);
	    this.robot.setAhead(dist * robotDirection);
	}

	/**
	 * Turns the robot to the angle in the most efficient direction, and reverses the orientation of the robot if going backwards is faster 
	 * @param angle The angle in radians that you wish to turn to
	 */
	private void turnTo(double angle) {
		double bearing = Utils.normaliseBearing(angle - this.robot.getHeadingRadians());
		final double halfPi = Math.PI / 2;
		
		if (bearing > halfPi) { // if turning more than 90 degrees
			bearing -= Math.PI;
			robotDirection = -1;
		} else if (bearing < (-halfPi)) { // if turning more than 90 degrees
			bearing += (Math.PI);
			robotDirection = -1;
		} else {
			robotDirection = 1;
		}
		this.robot.setTurnRightRadians(bearing);
	}

	public void reverseTravelDirection() {
		robotDirection *= -1;
	}
}