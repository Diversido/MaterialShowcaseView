package io.blushine.android.ui.showcase;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import io.blushine.android.ui.showcase.target.Target;

/**
 * Circular shape for target.
 */
class CircleShape {
private int mRadius = 0;
private Point mPoint = null;
private Target mTarget = null;

CircleShape() {
}

CircleShape(int radius) {
	this.mRadius = radius;
}

public Target getTarget() {
	return mTarget;
}

public void setTarget(Target target) {
	mTarget = target;
}

public int getRadius() {
	return mRadius;
}

public void setRadius(int radius) {
	this.mRadius = radius;
}

public void draw(Canvas canvas, Paint paint) {
	if (mRadius > 0) {
		Point point = getPoint();
		if (point != null) {
			canvas.drawCircle(point.x, point.y, mRadius, paint);
		}
	}
}

public Point getPoint() {
	return mTarget != null ? mTarget.getPoint() : mPoint;
}

public void setPoint(Point point) {
	mPoint = point;
}

public int getWidth() {
	return mRadius * 2;
}

public int getHeight() {
	return mRadius * 2;
}
}
