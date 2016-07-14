package com.spiddekauga.android.ui.showcase;

import android.content.Context;
import android.content.SharedPreferences;

class PrefsGateway {

private static final String PREFS_NAME = "material_showcaseview_prefs";
private static final String STATUS = "status_";
public static int SEQUENCE_NEVER_STARTED = 0;
public static int SEQUENCE_FINISHED = -1;
private String showcaseID = null;
private Context context;

public PrefsGateway(Context context, String showcaseID) {
	this.context = context;
	this.showcaseID = showcaseID;
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
	return context
			.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.getInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED);

}

void setSequenceStatus(int status) {
	SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().putInt(STATUS + showcaseID, status).apply();
}

void setFired() {
	setSequenceStatus(SEQUENCE_FINISHED);
}

public void resetShowcase() {
	resetShowcase(context, showcaseID);
}

static void resetShowcase(Context context, String showcaseID) {
	SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	internal.edit().putInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED).apply();
}

public void close() {
	context = null;
}
}
