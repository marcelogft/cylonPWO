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
public class CylonPWO_4 extends Robot {
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

	static double lastX = 0;
	static double lastY = 0;

	public void run() {
		// Set colors
		setBodyColor(Color.ORANGE);
		setGunColor(Color.BLACK);
		setRadarColor(Color.darkGray);
		// Allows the gun to turn independently of the robots movement
		setAdjustGunForRobotTurn(true);
		
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
        setRadarColor(Color.BLACK);
        setScanColor(Color.WHITE);

		// Request a new target
		onRobotDeath(null);
		// Make movement difficult to predict
		lastX = getX();
		lastY = getY();
		while (true) {
			turnRadarRight(360);
			move();
		}
	}

	// Power selection method
	public void onScannedRobot(ScannedRobotEvent e) {
		// Lock on to our target (this time for sure)
		//we find the difference between our tank heading (getHeading()) 
		// and our radar heading (getRadarHeading()) and add the bearing to the scanned robot (e.getBearing()), like so:
		turnRadarRight(getHeading() - getRadarHeading() + e.getBearing());
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
		turnRight(e.getBearing());
		ahead(e.getDistance() + 5);
		scan(); // Might want to move ahead again!
	 
	}

	/**
	 * onHitRobot: Turn to face robot, fire hard, and ram him again!
	 */
	public void onHitRobot(HitRobotEvent e) {
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
		turnLeft(45);
		ahead(5);
	}

	// Method to handle walls
	public void onHitWall(HitWallEvent e) {
		back(250);
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

	private void move() {
		// Allows the gun to turn independently of the robots movement
		setAdjustGunForRobotTurn(true);

		double height = getBattleFieldHeight();
		double width = getBattleFieldWidth();

		double x = getX();
		double y = getY();
		double deg = getHeading(); 
		// height:600.0
		// width:800.0
		// X:782.0
		// Y:18.0
		// deg:180.0 
		System.out.println("height:" + height);
		System.out.println("width:" + width);
		System.out.println("X:" + x);
		System.out.println("Y:" + y);
		System.out.println("deg:" + deg);
		// Returns the direction that the robot's body is facing, in degrees.
		/*
		 * if (lastX == x && lastY == y) { turnLeft(180); } else {
		 
		if (width - x > 100) {
			System.out.println(" ***** Move left ");
			if (height - y > 100) {
				System.out.println(" ***** Move left UP ");
				turnLeft(-deg / 2);
				//turnRadarLeft(-deg / 2);
				//turnGunLeft(-deg / 2);
			} else {
				System.out.println(" ***** Move left DOWN ");
				if ((deg - 180) == 0) {
					System.out.println(" ***** Move left DOWN - 180");
					turnLeft(45);
					//turnRadarLeft(45);					
					//turnGunLeft(45);
				}
				if (deg < 180) {
					turnRight(180 - deg);
					//turnRadarRight(180 - deg);
					//turnRadarLeft(180 - deg);
				} else {
					turnLeft(180 - deg);
					//turnRadarLeft(180 - deg);
					//turnGunLeft(180 - deg);
				}
			}
		} else {
			if ((deg - 180) == 0) {
				System.out.println(" ***** Move right DOWN - 180");
				turnLeft(45);
				//turnRadarLeft(45);
				//turnGunLeft(45);
			}
			if (deg < 180) {
				System.out.println(" ***** Move down right ");
				turnRight(180 - deg);
				//turnRadarRight(180 - deg);
				//turnGunRight(180 - deg);
			} else {
				System.out.println(" ***** Move down left ");
				turnLeft(180 - deg);
				// turnRadarLeft(180 - deg);
				// turnGunLeft(180 - deg);
			}
		}*/
		System.out.println("  ****** MOVE ***** ");
		ahead(5);
		fire(1);
		 
	}
	/* } */
}