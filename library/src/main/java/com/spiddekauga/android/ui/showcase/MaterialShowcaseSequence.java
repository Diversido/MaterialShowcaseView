package com.spiddekauga.android.ui.showcase;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

public class MaterialShowcaseSequence implements DetachedListener, MaterialShowcase {
private static final MaterialShowcaseDisplayer mShowcaseDisplayer = MaterialShowcaseDisplayer.getInstance();
PrefsGateway mPrefsGateway;
Queue<MaterialShowcaseView> mShowcaseQueue;
Activity mActivity;
private boolean mSingleUse = false;
private ShowcaseConfig mConfig;
private int mSequencePosition = 0;
private MaterialShowcaseView mCurrentShownShowcase;

/**
 * Create a showcase sequence for single use
 * @param activity the activity to display the showcase in
 * @param sequenceId unique sequence id
 */
public MaterialShowcaseSequence(Activity activity, String sequenceId) {
	this(activity);
	setSingleUse(sequenceId);
}

/**
 * Create a showcase sequence
 * @param activity the activity to display the showcase in
 */
public MaterialShowcaseSequence(Activity activity) {
	mActivity = activity;
	mShowcaseQueue = new LinkedList<>();
}

public MaterialShowcaseSequence addSequenceItem(int contentResId, int dismissTextResId) {
	return addSequenceItem(mActivity.getString(contentResId), mActivity.getString(dismissTextResId));
}

public MaterialShowcaseSequence addSequenceItem(String content, String dismissText) {
	return addSequenceItem(null, content, dismissText);
}

public MaterialShowcaseSequence addSequenceItem(View targetView, String content, String dismissText) {
	return addSequenceItem(targetView, "", content, dismissText);
}

public MaterialShowcaseSequence addSequenceItem(View targetView, String title, String content, String dismissText) {

	MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(mActivity)
			.setTitleText(title)
			.setDismissText(dismissText)
			.setContentText(content);

	if (targetView != null) {
		builder.setTarget(targetView);
	}

	return addSequenceItem(builder.build());
}

public MaterialShowcaseSequence addSequenceItem(MaterialShowcaseView sequenceItem) {
	if (mConfig != null) {
		sequenceItem.setConfig(mConfig);
	}

	mShowcaseQueue.add(sequenceItem);
	return this;
}

public MaterialShowcaseSequence addSequenceItem(int targetViewId, int contentResId, int dismissTextResId) {
	addSequenceItem(mActivity.findViewById(targetViewId), mActivity.getString(contentResId), mActivity.getString(dismissTextResId));
	return this;
}

public MaterialShowcaseSequence addSequenceItem(int targetViewId, int titleResId, int contentResId, int dismissTextResId) {
	addSequenceItem(mActivity.findViewById(targetViewId), mActivity.getString(titleResId),
			mActivity.getString(contentResId), mActivity.getString(dismissTextResId));
	return this;
}

/**
 * Show the entire sequence
 */
public void show() {
	// Check if we've already shot our bolt and bail out if so
	if (mSingleUse) {
		if (hasFired()) {
			return;
		}

		// See if we have started this sequence before, if so then skip to the point we reached before
		// instead of showing the user everything from the showNow
		mSequencePosition = mPrefsGateway.getSequenceStatus();

		if (mSequencePosition > 0) {
			for (int i = 0; i < mSequencePosition; i++) {
				mShowcaseQueue.poll();
			}
		}
	}

	// Enqueue this
	if (mShowcaseQueue.size() > 0) {
		mShowcaseDisplayer.enqueue(this);
	}
}

@Override
public boolean isSingleUse() {
	return mSingleUse;
}

/**
 * Set the sequence as single use
 * @param sequenceId unique id of the sequence
 */
@Override
public void setSingleUse(@NonNull String sequenceId) {
	mSingleUse = true;
	mPrefsGateway = new PrefsGateway(mActivity, sequenceId);
}

/**
 * Used internally, don't call this directly! Instead call {@link #show()}
 */
@Override
public void _showNow() {
	showNextItem();
}

public boolean hasFired() {
	return mPrefsGateway.getSequenceStatus() == PrefsGateway.SEQUENCE_FINISHED;
}

private void showNextItem() {
	if (mShowcaseQueue.size() > 0 && !mActivity.isFinishing()) {
		mCurrentShownShowcase = mShowcaseQueue.remove();
		mCurrentShownShowcase.setDetachedListener(this);
		mCurrentShownShowcase._showNow();
	}
	// We've reached the end of the sequence, save the fired state
	else {
		if (mSingleUse) {
			mPrefsGateway.setFired();
		}
		mShowcaseDisplayer.onFinished(this);
	}
}

/**
 * Cancel the entire sequence
 */
public void cancel() {

	// Act like we've reached the end of the sequence
	while (mShowcaseQueue.poll() != null) {
		mSequencePosition++;
	}

	if (mPrefsGateway != null) {
		mPrefsGateway.setSequenceStatus(mSequencePosition);
		mPrefsGateway.close();
		mPrefsGateway = null;
	}

	if (mCurrentShownShowcase != null) {
		mCurrentShownShowcase.hide();
	}

	mShowcaseDisplayer.onFinished(this);
}

@Override
public void onShowcaseDetached(MaterialShowcaseView showcaseView, boolean wasDismissed) {

	mCurrentShownShowcase = null;
	showcaseView.setDetachedListener(null);

	// We're only interested if the showcase was purposefully dismissed
	if (wasDismissed) {
		// If so, update the PrefsGateway so we can potentially resume this sequence in the future
		if (mPrefsGateway != null) {
			mSequencePosition++;
			mPrefsGateway.setSequenceStatus(mSequencePosition);
		}

		showNextItem();
	}
}

/**
 * Set a config to be applied to all subsequent showcases that are added, i.e. calls {@link
 * MaterialShowcaseView#setConfig(ShowcaseConfig)}.
 * @param config configuration for all subsequent showcases
 */
public void setConfig(ShowcaseConfig config) {
	mConfig = config;
}
}
