package io.blushine.android.ui.showcase;


/**
 * Circular animation
 */
abstract class CircularAnimation<AnimationObject> extends Animation<AnimationObject> {
private int mStartRadius = 0;
private int mEndRadius = 0;
private Algorithm mAlgorithm;

/**
 * Circular animation for a circle shape.
 * @param duration length of the animation
 * @param startRadius start radius of the animation
 * @param endRadius end radius of the animation
 * @param algorithm what type of animation algorithm to use
 */
protected CircularAnimation(long duration, int startRadius, int endRadius, Algorithm algorithm) {
	super(duration);
	mStartRadius = startRadius;
	mEndRadius = endRadius;
	mAlgorithm = algorithm;
}

public int getEndRadius() {
	return mEndRadius;
}

public void setEndRadius(int endRadius) {
	mEndRadius = endRadius;
}

public int getStartRadius() {
	return mStartRadius;
}

public void setStartRadius(int startRadius) {
	mStartRadius = startRadius;
}

/**
 * Calculate and return the current radius
 * @return current radius we want the circular animation to have
 */
protected int getCurrentRadius() {
	if (getRemainingTime() <= 0) {
		return mEndRadius;
	}

	switch (mAlgorithm) {
	case EASE_IN:
		return getCurrentRadiusEaseIn();
	case EASE_OUT:
		return getCurrentRadiusEaseOut();
	case EASE_IN_OUT:
		return getCurrentRadiusEaseInOut();
	default:
		return 0;
	}
}

/**
 * Get ease in radius. Original algorithm: <a href="http://gizma.com/easing/#cub1">http://gizma.com/easing/#cub1</a>
 * @return current radius with an ease in algorithm.
 */
private int getCurrentRadiusEaseIn() {
	return (int) interpolationEaseIn(mStartRadius, mEndRadius);
}

/**
 * Get ease out radius. Original algorithm: <a href="http://gizma.com/easing/#cub2">http://gizma.com/easing/#cub2</a>
 * @return current radius with an ease out algorithm.
 */
private int getCurrentRadiusEaseOut() {
	return (int) interpolationEaseOut(mStartRadius, mEndRadius);
}

/**
 * Get ease in and out radius. Original algorithm: <a href="http://gizma.com/easing/#cub3">http://gizma.com/easing/#cub3</a>
 * @return current radius with an ease in and out algorithm.
 */
private int getCurrentRadiusEaseInOut() {
	return (int) interpolationEaseInOut(mStartRadius, mEndRadius);
}

/**
 * Which animation type to use
 */
public enum Algorithm {
	/** Slow in the beginning, fast at the end */
	EASE_IN,
	/** Fast in the beginning, slow at the end */
	EASE_OUT,
	/** Slow in the beginning and end, fast in the middle */
	EASE_IN_OUT,
}
}
