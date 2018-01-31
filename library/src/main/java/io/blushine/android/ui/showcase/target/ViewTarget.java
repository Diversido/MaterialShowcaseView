package io.blushine.android.ui.showcase.target;

import android.app.Activity;
import android.graphics.Point;
import android.view.View;


public class ViewTarget implements Target {

private final View mView;

public ViewTarget(View view) {
	mView = view;
}

public ViewTarget(int viewId, Activity activity) {
	mView = activity.findViewById(viewId);
}

@Override
public Point getPoint() {
	int[] location = new int[2];

	mView.getLocationOnScreen(location);
	int x = location[0] + mView.getWidth() / 2;
	int y = location[1] + mView.getHeight() / 2;

	return new Point(x, y);
}

@Override
public int getRadius() {
	return (int) Math.sqrt(getRadiusSq());
}

@Override
public int getRadiusSq() {
	double width = mView.getWidth() * 0.5;
	double height = mView.getHeight() * 0.5;
	return (int) (width * width + height * height);
}
}
