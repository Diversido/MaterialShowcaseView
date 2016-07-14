package com.spiddekauga.android.ui.showcase;


public interface IShowcaseListener {
void onShowcaseDisplayed(MaterialShowcaseView showcaseView);

void onShowcaseDismissed(MaterialShowcaseView showcaseView);

/**
 * Notify when singleUse is enabled and showcase has been fired before
 */
void onShowcaseSkipped(MaterialShowcaseView showcaseView);
}
