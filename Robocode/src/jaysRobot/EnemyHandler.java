package jaysRobot;

import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class EnemyHandler {
	private static ArrayList<EnemyBot> enemies = new ArrayList<EnemyBot>();
	private static EnemyBot currentTarget;
	private double changeTargetMargin = 50; // when changing to closer target, only swap if the distance closer is greater than this
	
	public void update(ScannedRobotEvent e, AdvancedRobot r, long gameTime) {
		boolean enemyFound = false;
		
		// search for enemy, if exists, update entry
		for (EnemyBot enemy : EnemyHandler.enemies) {
			if (e.getName().equals(enemy.getName())) {
				enemy.update(e, r);
				enemyFound = true;
				break;
			}
		}
		// enemy wasn't found, therefore we will add the enemy
		if (!enemyFound) addEnemy(e, r);
		
		// update the currentTarget
		EnemyBot closestEnemy = getClosestEnemy();
		if ((currentTarget == null || currentTarget.isAlive() == false) && closestEnemy != null) {
			currentTarget = closestEnemy;
		} else if (closestEnemy != null) {
			if (closestEnemy.getDistance() < currentTarget.getDistance() - changeTargetMargin) {
				currentTarget = closestEnemy;
			}
		} 
	}
	private void addEnemy(ScannedRobotEvent e, AdvancedRobot r) {
		EnemyBot enemy = new EnemyBot();
		enemy.update(e, r);
		EnemyHandler.enemies.add(enemy);
	}
	public EnemyBot getClosestEnemy() {
		int enemyI = -1;
		
		for (int i = 0; i < enemies.size(); i++) {
			// we only want to update if the enemy is alive 
			if (enemies.get(i).isAlive()) {
				// if we aren't locked onto a target, or we find a closer target, update
				if (enemyI == -1 || enemies.get(i).getDistance() < enemies.get(enemyI).getDistance()) {
					enemyI = i;
				}
			}
		}
		return (enemyI == -1) ? null : enemies.get(enemyI);
	}
	public EnemyBot getEnemy() {
		return currentTarget == null ? new EnemyBot() : currentTarget;
	}
	public void death(RobotDeathEvent e) {
		for (EnemyBot enemy : enemies) {
			if (e.getName().equals(enemy.getName())) {
				enemy.death(e);
				// if current target just died, switch to closest target
				if (currentTarget.getName().equals(e.getName())) {
					currentTarget = getClosestEnemy();
				}
				return;
			}
		}
	}
}
