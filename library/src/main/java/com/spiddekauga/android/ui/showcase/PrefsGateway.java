package com.spiddekauga.android.ui.showcase;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

class PrefsGateway {

static final int SEQUENCE_NEVER_STARTED = 0;
static final int SEQUENCE_FINISHED = -1;
private static final String PREFS_NAME = "material_showcaseview_prefs";
private static final String STATUS = "status_";
private String mShowcaseId;
private Context mContext;

public PrefsGateway(Context context, @NonNull String showcaseId) {
	mContext = context;
	mShowcaseId = showcaseId;
}

/**
 * Reset all showcases
 * @param context current context in the activity
 */
static void resetAll(Context context) {
	SharedPreferences internal = getPrefs(context);
	internal.edit().clear().apply();
}

private static SharedPreferences getPrefs(Context context) {
	return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
}

/**
 * Set the showcase as fired
 * @param context application context
 * @param showcaseId the showcase to set as fired
 */
static void setFired(Context context, String showcaseId) {
	setSequenceStatus(context, showcaseId, SEQUENCE_FINISHED);
}

static void setSequenceStatus(Context context, @NonNull String showcaseId, int position) {
	SharedPreferences internal = getPrefs(context);
	internal.edit().putInt(STATUS + showcaseId, position).apply();
}

/***
 * Check if an individual showcase has fired
 * @return true if an individual showcase has fired
 */
boolean hasFired() {
	return hasFired(mContext, mShowcaseId);
}

/**
 * Static helper to check if a showcase has been fired or not
 * @param context application context
 * @param showcaseId the showcase to check if it has been fired or not
 * @return true if the showcase has been fired, false otherwise
 */
static boolean hasFired(Context context, @NonNull String showcaseId) {
	int status = getSequenceStatus(context, showcaseId);
	return (status == SEQUENCE_FINISHED);
}

/**
 * Check if a sequence showcase has fired and how many showcases it has fired in that case
 * @return number of showcases fired, {@link #SEQUENCE_NEVER_STARTED} if it hasn't started,or {@link #SEQUENCE_FINISHED}
 * the entire sequence has finished.
 */
static int getSequenceStatus(Context context, @NonNull String showcaseId) {
	return getPrefs(context)
			.getInt(STATUS + showcaseId, SEQUENCE_NEVER_STARTED);
}

/**
 * Check if a sequence showcase has fired and how many showcases it has fired in that case
 * @return number of showcases fired, {@link #SEQUENCE_NEVER_STARTED} if it hasn't started,or {@link
 * #SEQUENCE_FINISHED} the entire sequence has finished.
 */
int getSequenceStatus() {
	return getSequenceStatus(mContext, mShowcaseId);
}

/**
 * Update the number of showcases that has been fired in a sequence
 * @param position how many showcases has been fired in the sequence
 */
void setSequenceStatus(int position) {
	setSequenceStatus(mContext, mShowcaseId, position);
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
static void resetShowcase(Context context, @NonNull String showcaseId) {
	SharedPreferences internal = getPrefs(context);
	internal.edit().putInt(STATUS + showcaseId, SEQUENCE_NEVER_STARTED).apply();
}

public void close() {
	mContext = null;
}
}
