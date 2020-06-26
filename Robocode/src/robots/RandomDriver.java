package robots;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class RandomDriver extends AdvancedRobot {
	
	int travelDirection = 1;
	int turnDirection = 1;
	
    public void run() {
    	
		// setting colours for my robot
        setBodyColor(new Color(255,0,0));
        setGunColor(new Color(200,0,0));
        setRadarColor(new Color(100,0,0));
        setScanColor(Color.RED);
        setBulletColor(Color.RED);
        
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
    	
        while (true) {
        	// randomly change speed and direction
        	if (Math.random() > 0.9) {
        		turnDirection *= -1;
        		setMaxVelocity((12*Math.random())+12);
        	}
    		setTurnRadarRight(Double.POSITIVE_INFINITY); // scan permanently
            setAhead(100*travelDirection);
            setTurnLeft(180*turnDirection);
            execute();
        }
    }
    
	public void onHitWall(HitWallEvent e){
		travelDirection=-travelDirection;//reverse direction upon hitting a wall
	}

    public void onScannedRobot(ScannedRobotEvent e) {
    	
    	// if only 1 enemy, reverse radar to lock on
    	if(getOthers() == 1) {
    		setTurnRadarLeft(getRadarTurnRemaining());//lock on the radar
    	}
    	
    	double enemyBearing = e.getBearing()+getHeading(); // bearing is relative to your heading, so we'll add this.
        double gunTurn = enemyBearing-getGunHeading(); // set our gun to aim at the enemy, but subtract the angle already applied to the gun.
        gunTurn = Utils.normalRelativeAngleDegrees(gunTurn);
    	setTurnGunRight(gunTurn);
        if (Math.abs(gunTurn) < 5) {
        	setFire(1);        	
        }
        
    }
}