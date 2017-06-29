package com.gft.codejam;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.util.Utils;

import java.util.Random;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.*;

/**
 * MyFirstRobot - a sample robot by Mathew Nelson
 * <p/>
 * Moves in a seesaw motion, and spins the gun around at each end
 */
public class CylonPWO_5 extends Robot {
	static final double MIN_X = 0;
	static final double MIN_Y = 0;
	static final double MAX_X = 800;
	static final double MAX_Y = 600;
	
	static final String QUADRANT_00 = "00";
	static final String QUADRANT_01 = "01";
	static final String QUADRANT_10 = "10";
	static final String QUADRANT_11 = "11";
	
	// Maximum move distance
	double moveDistance = 100000;
	// Distance to the last enemy we saw
	double lastDistance;
	// The power of the bullet we should fire
	double firePower;
	// The name of the enemy that is closest to us
	String lastEnemy;
	int radarSweeps;

	int turnDirection = 1; // Clockwise or counterclockwise

	public void run() {
		// Set colors
		setBodyColor(Color.ORANGE);
		setGunColor(Color.BLACK);
		setRadarColor(Color.darkGray);
		// Allows the gun to turn independently of the robots movement
		setAdjustGunForRobotTurn(true);

		// Request a new target
		onRobotDeath(null);

		while (true) {
			System.out.println("X:" + this.getX());
			System.out.println("Y:" + this.getY());
			double randomNumber = Math.random();
			// Make movement difficult to predict
			if (randomNumber < 0.5) {
				turnRadarLeft(360);
				moveAhead(200, 30);
			} else {
				turnRadarLeft(360);
				turnLeft(30);
				back(200);
			}
		}
	}

	// Power selection method
	public void onScannedRobot(ScannedRobotEvent e) {

		double distanceToEnemy = e.getDistance();
		if (getEnergy() < 10)
			firePower = 0.1;
		else if (distanceToEnemy <= 200)
			firePower = 3;
		else if (distanceToEnemy <= 350)
			firePower = 2;
		else if (distanceToEnemy <= 500)
			firePower = 1;
		else
			firePower = 0.1;
		// Overall bearing of the target robot
		double targetBearing = Math.toRadians(getHeading()) + e.getBearingRadians();
		// Distance requirement
		double distanceBulletTravels = Rules.getBulletSpeed(firePower);
		double distanceTargetTravels = e.getVelocity();

		// Calculate the angle
		double shootBulletAngle = targetBearing + Math.asin(
				(distanceTargetTravels / distanceBulletTravels) * Math.sin(e.getHeadingRadians() - targetBearing));

		turnGunRight(Math.toDegrees(Utils.normalRelativeAngle(shootBulletAngle - Math.toRadians(getGunHeading()))));

		// Lock current target
		if (lastDistance > distanceToEnemy) {
			lastDistance = distanceToEnemy;
			lastEnemy = e.getName();
		}

		// Avoid taking hits, and encourage an enemy to move closer
		if ((distanceToEnemy < 600) || (radarSweeps > 5)) {
			radarSweeps = 0;
			fire(firePower);
			onRobotDeath(null);
		}
		ahead(100);
	}

	/**
	 * onHitRobot: Turn to face robot, fire hard, and ram him again!
	 */
	public void onHitRobot(HitRobotEvent e) {
		System.out.println("onHitRobot X:" + this.getX());
		System.out.println("onHitRobot Y:" + this.getY());
		if (e.getBearing() >= 0) {
			turnDirection = 1;
		} else {
			turnDirection = -1;
		}
		turnRight(e.getBearing());

		// Determine a shot that won't kill the robot...
		// We want to ram him instead for bonus points
		if (e.getEnergy() > 16) {
			fire(3);
		} else if (e.getEnergy() > 10) {
			fire(2);
		} else if (e.getEnergy() > 4) {
			fire(1);
		} else if (e.getEnergy() > 2) {
			fire(.5);
		} else if (e.getEnergy() > .4) {
			fire(.1);
		}
		ahead(40); // Ram him again!
		fire(3);
	}

	/**
	 * We were hit! Turn perpendicular to the bullet, so our seesaw might avoid
	 * a future shot.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		moveAhead(150, 45);
	}

	// Method to handle walls
	public void onHitWall(HitWallEvent e) {
		System.out.println("onHitWall X:" + this.getX());
		System.out.println("onHitWall Y:" + this.getY());
		Random r = new Random();
		int Low = 50;
		int High = 100;
		int Result = r.nextInt(High - Low) + Low;
		moveAhead(Result, 180);
	}

	// Robot death method
	public void onRobotDeath(RobotDeathEvent e) {
		lastDistance = 100000;
	}

	/**
	 * onWin: Do a victory dance
	 */
	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {
			turnRight(45);
			turnLeft(45);
		}
	}
	
	private void moveAhead(double distance, double degrees) {
		double x = getX();
		double y = getY();
		String quadrant = calculateQuadrant();
		
		if (quadrant.equals(QUADRANT_00)) {
			if (degrees > 90 && degrees < 360) {
				degrees = checkMoveInQuadrant00(distance, degrees);
			}
		} else if (quadrant.equals(QUADRANT_01)) {
			if (degrees > 0 && degrees < 270) {
//				degrees = checkMoveInQuadrant01(distance, degrees);
			}
		} else if (quadrant.equals(QUADRANT_10)) {
			if (degrees < 90 || degrees > 180) {
//				degrees = checkMoveInQuadrant10(distance, degrees);
			}
		} else if (quadrant.equals(QUADRANT_11)) {
			if (degrees < 180 || degrees > 270) {
//				degrees = checkMoveInQuadrant11(distance, degrees);
			}
		}
		
		double heading = getHeading();
		
		if (360 - heading > 180) {
			turnRight(degrees);
		} else {
			turnLeft(degrees);
		}
		
		ahead(distance);
	}
	
	private double checkMoveInQuadrant00(double distance, double degrees) {
		double result = -1;
		double a, b;
		double angleA, angleB;
		double x = getX();
		double y = getY();
		
		if (degrees == 180) {
			if (x-distance >= MIN_X) {
				result = degrees;
			} else {
				result = degrees - 90;
			}
		} else if (degrees == 270) {
			if (y-distance >= MIN_Y) {
				result = degrees;
			} else {
				result = degrees + 90;
			}
		} else if (degrees < 180) {
			angleB = 180 - degrees;
			b = getSide(angleB, distance);
			angleA = 180 - 45 - angleB;
			a = getSide(angleA, distance);
			
			if (x-a >= MIN_X) {
				result = degrees;
			} else {
				result = degrees - 90;
			}
		} else if (degrees < 270) {
			angleB = degrees - 180;
			b = getSide(angleB, distance);
			angleA = 180 - 45 - angleB;
			a = getSide(angleA, distance);
			
			if (x-a >= MIN_X && y-b >= MIN_Y) {
				result = degrees;
			} else {
				result = degrees + 90;
			}
		} else if (degrees < 360) {
			angleB = degrees - 270;
			b = getSide(angleB, distance);
			angleA = 180 - 45 - angleB;
			a = getSide(angleA, distance);
			
			if (y-a >= MIN_Y) {
				result = degrees;
			} else {
				result = degrees + 90;
			}
		}
		
		return result;
	}

	private String calculateQuadrant() {
		String quadrant = null;
		double x = getX();
		double y = getY();
		
		if (x <= MAX_X/2 && y <= MAX_Y/2) {
			quadrant = QUADRANT_00;
		}
		
		if (x <= MAX_X/2 && y > MAX_Y/2) {
			quadrant = QUADRANT_01;
		}
		
		if (x > MAX_X/2 && y <= MAX_Y/2) {
			quadrant = QUADRANT_10;
		}
		
		if (x > MAX_X/2 && y > MAX_Y/2) {
			quadrant = QUADRANT_11;
		}
		
		return quadrant;
	}
	
	private double getSide(double angle, double distance) {
		return Math.sin(angle) * distance/Math.sin(45);
	}
}