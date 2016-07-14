package com.spiddekauga.android.ui.showcase.target;

import android.graphics.Point;


public interface Target {
Target NONE = new Target() {
	@Override
	public Point getPoint() {
		return new Point(1000000, 1000000);
	}
};

Point getPoint();
}
