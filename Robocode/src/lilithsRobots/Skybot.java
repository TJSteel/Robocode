package lilithsRobots;

import java.awt.Color;

import robocode.*;
import robocode.util.Utils;
 
public class Skybot extends AdvancedRobot {
    public void run() {
		// setting radar / gun to be able to turn independently of each other
		// wiki states this is essential
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		// setting colours for my robot
        setBodyColor(new Color(63, 255, 239));
        setGunColor(new Color(164, 252, 245));
        setRadarColor(new Color(255, 255, 255));
        setScanColor(new Color(63, 255, 239));
        setBulletColor(new Color(63, 255, 239));
        
        turnRadarRight(Double.POSITIVE_INFINITY);
        
        //while (true) {
        //}
    }
 
    public void onScannedRobot(ScannedRobotEvent e) {
    	setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
        setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() - getGunHeading() + e.getBearing()));
        setFire(3);
    }
}