package io.blushine.android.ui.showcase;

/**
 * Circular animation for a circle shape
 */
class CircularShapeAnimation extends CircularAnimation<CircleShape> {

/**
 * Circular animation for a circle shape.
 * @param duration length of the animation
 * @param startRadius start radius of the animation
 * @param endRadius end radius of the animation
 * @param algorithm what type of ease in/out algorithm we want to use for the shape
 */
public CircularShapeAnimation(long duration, int startRadius, int endRadius, Algorithm algorithm) {
	super(duration, startRadius, endRadius, algorithm);
}

@Override
protected void onUpdate(CircleShape circleShape) {
	circleShape.setRadius(getCurrentRadius());
}
}
