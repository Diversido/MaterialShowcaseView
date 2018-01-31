package io.blushine.android.ui.showcase;

import android.view.View;

/**
 * Create an alpha animation for a specific view
 */
class AlphaAnimation extends Animation<View> {
private float mFromAlpha;
private float mToAlpha;

AlphaAnimation(long duration, float from, float to) {
	super(duration);
	mFromAlpha = from;
	mToAlpha = to;
}

@Override
protected void onUpdate(View view) {
	float alpha = mToAlpha;
	if (getRemainingTime() > 0) {
		alpha = interpolationLinear(mFromAlpha, mToAlpha);
	}

	view.setAlpha(alpha);
}
}
