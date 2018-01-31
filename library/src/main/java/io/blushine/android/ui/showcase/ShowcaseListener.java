package io.blushine.android.ui.showcase;


public interface ShowcaseListener {
/**
 * Called when the showcase is displayed.
 */
void onShowcaseDisplayed(MaterialShowcaseView showcaseView);

/**
 * Called when the showcase has been dismissed. This will always be called if the showcase has been
 * displayed
 */
void onShowcaseDismissed(MaterialShowcaseView showcaseView);

/**
 * Notify when setSingleUse is enabled and showcase has been fired before
 */
void onShowcaseSkipped(MaterialShowcaseView showcaseView);

/**
 * Called when the target was pressed. Can only be pressed if there is a target and there is no
 * hide text. The target should get the touch event too.
 */
void onTargetPressed(MaterialShowcaseView showcaseView);
}
