package io.blushine.android.ui.showcase;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Displays / Shows material showNow cases in order so that two can't be visible at the same time
 */
class MaterialShowcaseDisplayer {
private static MaterialShowcaseDisplayer mInstance = null;
private MaterialShowcase mCurrentShowcase = null;
private Queue<MaterialShowcase> mShowcaseQueue = new ArrayDeque<>();

/**
 * Enforces singleton pattern
 */
private MaterialShowcaseDisplayer() {
}

/**
 * Get singleton instance
 * @return get instance
 */
public static MaterialShowcaseDisplayer getInstance() {
	if (mInstance == null) {
		mInstance = new MaterialShowcaseDisplayer();
	}
	return mInstance;
}

/**
 * Add the showcase to the queue. If there are no showcases in the enqueue it will be shown
 * directly
 * @param showcaseContainer the showcase container to be queued and shown
 */
void enqueue(MaterialShowcase showcaseContainer) {
	mShowcaseQueue.add(showcaseContainer);

	if (mCurrentShowcase == null) {
		showNext();
	}
}

private void showNext() {
	mCurrentShowcase = mShowcaseQueue.poll();

	if (mCurrentShowcase != null) {
		mCurrentShowcase._showNow();
	}
}

/**
 * Call this whenever a showcase has been dismissed or completed
 * @param showcaseContainer the showcase that has been finished
 */
void onFinished(MaterialShowcase showcaseContainer) {
	if (showcaseContainer == mCurrentShowcase) {
		mCurrentShowcase = null;
		showNext();
	} else {
		mShowcaseQueue.remove(showcaseContainer);
	}
}
}
