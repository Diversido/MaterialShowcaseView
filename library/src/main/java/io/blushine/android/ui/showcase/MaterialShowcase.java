package io.blushine.android.ui.showcase;

import android.support.annotation.NonNull;

/**
 * 'Interface' for material showcases.
 */
public interface MaterialShowcase {

/**
 * Show the showcase. If it's single use then it will only show it if it hasn't been shown before.
 * If there is another showcase active it will enqueue this showcase.
 */
void show();

/**
 * Check if it's single use
 */
boolean isSingleUse();

/**
 * Set the showcase to single use
 * @param showcaseId id of the showcase
 */
void setSingleUse(@NonNull String showcaseId);

/**
 * Used internally, don't call this directly! Instead call {@link #show()}
 */
void _showNow();

/**
 * If this showcase is a single use, check if it has fired.
 * @return true if this single use showcase has fired, always returns false if this showcase isn't
 * set as single use.
 * @see #setSingleUse(String) to set the showcase as single use
 */
boolean hasFired();
}
