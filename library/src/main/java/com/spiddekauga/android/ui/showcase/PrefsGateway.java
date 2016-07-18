package com.spiddekauga.android.ui.showcase;

import android.content.Context;
import android.content.SharedPreferences;

class PrefsGateway {

static final int SEQUENCE_NEVER_STARTED = 0;
static final int SEQUENCE_FINISHED = -1;
private static final String PREFS_NAME = "material_showcaseview_prefs";
private static final String STATUS = "status_";
private String mShowcaseId;
private Context mContext;

public PrefsGateway(Context context, String showcaseId) {
	mContext = context;
	mShowcaseId = showcaseId;
}

/**
 * Reset all showcases
 * @param context current context in the activity
 */
public static void resetAll(Context context) {
	SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().clear().apply();
}

/***
 * Check if an individual showcase has fired
 * @return true if an individual showcase has fired
 */
boolean hasFired() {
	int status = getSequenceStatus();
	return (status == SEQUENCE_FINISHED);
}

/***
 * Check if a sequence showcase has fired and how many showcases it has fired in that case
 * @return number of showcases fired, {@link #SEQUENCE_NEVER_STARTED} if it hasn't started,or {@link #SEQUENCE_FINISHED}
 * the entire sequence has finished.
 */
int getSequenceStatus() {
	return mContext
			.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.getInt(STATUS + mShowcaseId, SEQUENCE_NEVER_STARTED);

}

/**
 * Update the number of showcases that has been fired in a sequence
 * @param position how many showcases has been fired in the sequence
 */
void setSequenceStatus(int position) {
	SharedPreferences internal = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().putInt(STATUS + mShowcaseId, position).apply();
}

/**
 * Sets the showcase or sequence as fired
 */
void setFired() {
	setSequenceStatus(SEQUENCE_FINISHED);
}

/**
 * Reset the showcase or sequence,
 */
public void resetShowcase() {
	resetShowcase(mContext, mShowcaseId);
}

/**
 * Reset a specific showcase
 * @param context context for getting the preferences
 * @param showcaseId the showcase to reset
 */
static void resetShowcase(Context context, String showcaseId) {
	SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().putInt(STATUS + showcaseId, SEQUENCE_NEVER_STARTED).apply();
}

public void close() {
	mContext = null;
}
}
