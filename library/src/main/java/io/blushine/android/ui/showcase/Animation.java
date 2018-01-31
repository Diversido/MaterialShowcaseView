package io.blushine.android.ui.showcase;

/**
 * Base class for animations
 * @param <AnimationObject> the object to animate
 */
abstract class Animation<AnimationObject> {
private long mStartTime = 0;
private long mDuration = -1;
private boolean mDone = false;
private long mCurrentTime = 0;

protected Animation() {

}

/**
 * Set the duration of the animation
 * @param duration length of the animation
 */
protected Animation(long duration) {
	mDuration = duration;
}

/**
 * Get the duration time
 * @return total duration time in milliseconds
 */
public long getDuration() {
	return mDuration;
}

/**
 * Duration of the animation
 * @param duration duration in milliseconds
 */
public void setDuration(long duration) {
	mDuration = duration;
}

/**
 * Call this to update the animation
 * @param animationObject the object to animate
 * @return true if the animation has been completed
 */
public final boolean update(AnimationObject animationObject) {
	if (mStartTime == 0) {
		start();
	}
	mCurrentTime = System.currentTimeMillis();
	onUpdate(animationObject);
	if (!mDone && mStartTime != 0 && getRemainingTime() == 0) {
		mDone = true;
		onEnd();
	}
	return mDone;
}

/**
 * Start the animation
 */
public void start() {
	mStartTime = System.currentTimeMillis();
	onStart();
}

/**
 * Called when we want to update the object
 * @param animationObject the object to animate
 */
protected abstract void onUpdate(AnimationObject animationObject);

/**
 * Get the remaining time of the animaiton
 * @return remaining time of the animation in milliseconds
 */
public long getRemainingTime() {
	return Math.max(mStartTime + mDuration - mCurrentTime, 0);
}

/**
 * Called when an animation has been completed
 */
protected void onEnd() {

}

/**
 * Called when the animation has been started
 */
protected void onStart() {

}

/**
 * Check if the animation is complete. Note that even if the animation is done it can still be used
 * to modify an object. Also note that if you call this before {@link }
 * @return true if the animation is complete
 */
public boolean isDone() {
	return mDone;
}

protected float interpolationLinear(float from, float to) {
	float changeValue = to - from;
	float fraction = getElapsedFraction();
	return from + changeValue * fraction;
}

/**
 * Get elapsed fraction since the start. How long through an animation we have progressed
 * @return how long through an animation we have progressed, in the range [0.0; 1.0]
 */
public float getElapsedFraction() {
	return ((float) getElapsedTime()) / ((float) mDuration);
}

/**
 * Get elapsed time since start
 * @return milliseconds since the start of the animation.
 */
public long getElapsedTime() {
	return mCurrentTime - mStartTime;
}

protected float interpolationEaseIn(float from, float to) {
	float changeValue = to - from;
	float fraction = getElapsedFraction();
	return changeValue * fraction * fraction * fraction + from;
}

protected float interpolationEaseOut(float from, float to) {
	float changeValue = to - from;
	float fraction = getElapsedFraction() - 1;
	return changeValue * (fraction * fraction * fraction + 1) + from;
}

protected float interpolationEaseInOut(float from, float to) {
	float changeValue = to - from;
	float fraction = getElapsedFraction() * 2;
	if (fraction < 1) {
		return changeValue / 2 * fraction * fraction * fraction + from;
	} else {
		fraction -= 2;
		return changeValue / 2 * (fraction * fraction * fraction + 2) + from;
	}
}
}
