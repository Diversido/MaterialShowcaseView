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

public static void resetAll(Context context) {
	SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().clear().apply();
}

/***
 * Used for individual showcases
 */
boolean hasFired() {
	int status = getSequenceStatus();
	return (status == SEQUENCE_FINISHED);
}

/***
 * Used for sequence showcases
 */
int getSequenceStatus() {
	return mContext
			.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.getInt(STATUS + mShowcaseId, SEQUENCE_NEVER_STARTED);

}

void setSequenceStatus(int status) {
	SharedPreferences internal = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().putInt(STATUS + mShowcaseId, status).apply();
}

void setFired() {
	setSequenceStatus(SEQUENCE_FINISHED);
}

public void resetShowcase() {
	resetShowcase(mContext, mShowcaseId);
}

static void resetShowcase(Context context, String showcaseID) {
	SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().putInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED).apply();
}

public void close() {
	mContext = null;
}
}
