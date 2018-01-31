package io.blushine.android.ui.showcase.target;

import android.graphics.Point;


public interface Target {
Target NONE = new Target() {
	@Override
	public Point getPoint() {
		return new Point(1000000, 1000000);
	}

	@Override
	public int getRadius() {
		return 10000;
	}

	@Override
	public int getRadiusSq() {
		return 100000000;
	}
};

/**
 * Get the center of the target
 * @return center of the target
 */
Point getPoint();

/**
 * Get the target's radius
 * @return target's radius
 */
int getRadius();

/**
 * Get the squared radius of the target. This method should be faster than {@link #getRadius()}.
 * @return target's radius squared
 */
int getRadiusSq();

}
