package robots;

import java.awt.Color;

import robocode.Robot;
import robocode.ScannedRobotEvent;

public class FirstRobot extends Robot {
    public void run() {
    	
		// setting colours for my robot
        setBodyColor(new Color(0,255,255));
        setGunColor(new Color(0,200,200));
        setRadarColor(new Color(0,100,100));
    	
        while (true) {
            ahead(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        fire(1);
    }
}