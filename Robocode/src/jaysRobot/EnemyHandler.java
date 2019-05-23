package jaysRobot;

import java.util.ArrayList;

import movement.Bullet;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class EnemyHandler {
	private static ArrayList<EnemyBot> enemies = new ArrayList<EnemyBot>();
	private static EnemyBot currentTarget;
	private double changeTargetMargin = 50; // when changing to closer target, only swap if the distance closer is less than this
	
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
		EnemyBot closestEnemy = getClosestEnemy(r);
		if ((currentTarget == null || currentTarget.isAlive() == false) && closestEnemy != null) {
			currentTarget = closestEnemy;
		} else if (closestEnemy != null) {
			if (closestEnemy.getPosition().getDistance(r.getX(), r.getY()) < currentTarget.getPosition().getDistance(r.getX(), r.getY()) - changeTargetMargin) {
				currentTarget = closestEnemy;
			}
		} 
	}
	private void addEnemy(ScannedRobotEvent e, AdvancedRobot r) {
		EnemyBot enemy = new EnemyBot();
		enemy.update(e, r);
		EnemyHandler.enemies.add(enemy);
	}
	public EnemyBot getClosestEnemy(AdvancedRobot r) {
		int enemyI = -1;
		
		for (int i = 0; i < enemies.size(); i++) {
			// we only want to update if the enemy is alive 
			if (enemies.get(i).isAlive()) {
				// if we aren't locked onto a target, or we find a closer target, update
				if (enemyI == -1 || enemies.get(i).getPosition().getDistance(r.getX(), r.getY()) < enemies.get(enemyI).getPosition().getDistance(r.getX(), r.getY())) {
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
				return;
			}
		}
	}
	public ArrayList<EnemyBot> getEnemies(){
		return EnemyHandler.enemies;
	}
	public void bulletEvent(robocode.Bullet bullet) {
		String victim = bullet.getVictim();
		String attacker = bullet.getName();
		
		if (victim != null) {
			for (EnemyBot enemy : EnemyHandler.enemies) {
				if (enemy.getName().equals(victim)) {
					enemy.hitBy(bullet);
				}
				if (enemy.getName().equals(attacker)) {
					enemy.hit(bullet);
				}
			}
		}
	}
	public ArrayList<Bullet> getBullets(){
		ArrayList<Bullet> bullets = new ArrayList<Bullet>();
		for (EnemyBot enemy : this.getEnemies()) {
			for (Bullet b : enemy.getBullets()) {
				bullets.add(b);
			}
		}
		return bullets;
	}
	public void clearBullets() {
		for (EnemyBot enemy : EnemyHandler.enemies) {
			enemy.clearBullets();
		}
	}
}
