package com.spiddekauga.android.ui.showcase;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spiddekauga.android.ui.showcase.target.Target;
import com.spiddekauga.android.ui.showcase.target.ViewTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Show a Material showcase. Preferably use the {@link Builder} to create instances of {@link
 * MaterialShowcaseView}. Example use:
 * <pre>
 * {@code
 * new MaterialShowcaseView.Builder(this)
 * 	.setTarget(buttonView) // Show circle around target
 * 	.setTitleText("This is my title")
 * 	.setSingleUse(idOfTheShowcaseToOnlyBeShownOnce) // Set if you only want to show the showcase
 * once
 * 	.setContentText("Some text describing the feature")
 * 	.setDelay(0) // Set it to at least 300ms if your showcase calls show() in onCreate()
 * 	.setDismissText("Got it") // Will always be CAPITALIZED - When set user must click on it to continue
 * 	.setBackgroundColor(Color.RED) // You can also override R.color.material_showcase_background
 * 	.setTitleTextColor(Color.GREEN) // Default is R.color.text_color_primary
 * 	.setContentTextColor(Color.GREEN) // Default is R.color.text_color_secondary
 * 	.setDismissTextColor(Color.GREEN) // Default is R.color.text_color_secondary
 * 	.setDismissBackgroundColor(Color.WHITE) // Default is R.color.material_showcase_dismiss_background
 * 	.setRenderOverNavigationBar() // In Lollipop and later, render over navigation buttons
 * 	.setTargetTouchable(false) // If the target should be touchable, default is true
 * }
 * </pre>
 * Nothing in mandatory for the showcase to view
 */
public class MaterialShowcaseView extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

List<ShowcaseListener> mListeners = new ArrayList<>();
private int mOldHeight;
private int mOldWidth;
private Bitmap mBitmap;// = new WeakReference<>(null);
private Canvas mCanvas;
private Paint mEraser;
private Target mTarget;
private CircleShape mTargetShape = null;
private boolean mWasDismissed = false;
private LinearLayout mContentBox;
private TextView mTitleTextView;
private TextView mContentTextView;
private AppCompatButton mDismissButton;
private Point mTargetLastPoint = new Point();
private Point mContentLastPoint = new Point();
private CircleShape mBackgroundShape = new CircleShape();
private Target mContentBoxTarget;
private boolean mShouldRender = false; // flag to decide when we should actually render
private boolean mRenderOverNav = false;
private int mBackgroundColor;
private Paint mBackgroundColorPaint;
private Handler mHandler;
private long mDelayInMillis = 0;
private boolean mSingleUse = false; // should display only once
private PrefsGateway mPrefsGateway; // used to store state doe single use mode
private UpdateOnGlobalLayout mLayoutListener;
private DetachedListener mDetachedListener;
private boolean mTargetTouchable = true;
private boolean mInitialLayoutDone = false;

/**
 * Create a bare Material Showcase
 * @param context activity context we want to show the showcase in
 */
public MaterialShowcaseView(Context context) {
	super(context);
	init(context);
}

private void init(Context context) {
	ShowcaseConfig.init(context);

	setWillNotDraw(false);

	mListeners = new ArrayList<>();

	// make sure we add a global layout listener so we can adapt to changes
	mLayoutListener = new UpdateOnGlobalLayout();
	getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

	// consume touch events
	setOnTouchListener(this);

	mBackgroundColor = ShowcaseConfig.mBackgroundColorDefault;
	setVisibility(INVISIBLE);


	View contentView = LayoutInflater.from(getContext()).inflate(R.layout.showcase_content, this, true);
	mContentBox = (LinearLayout) contentView.findViewById(R.id.content_box);
	mTitleTextView = (TextView) contentView.findViewById(R.id.title);
	mContentTextView = (TextView) contentView.findViewById(R.id.content);
	mDismissButton = (AppCompatButton) contentView.findViewById(R.id.dismiss_button);
	mDismissButton.setOnClickListener(this);
	setDismissBackgroundColor(ShowcaseConfig.mDismissBackgroundColorDefault);
	mContentBoxTarget = new ViewTarget(mContentBox);
	mBackgroundShape.setTarget(mContentBoxTarget);
}

/**
 * Set the background color of the dismiss button. By default this is {@link
 * com.spiddekauga.android.ui.showcase.R.color#material_showcase_dismiss_background}
 * @param backgroundColor background color of the dismiss button
 */
public void setDismissBackgroundColor(int backgroundColor) {
	if (mDismissButton != null) {
		ColorStateList colorStateList = new ColorStateList(
				new int[][]{
						new int[]{}
				},
				new int[]{
						backgroundColor
				}
		);
		ViewCompat.setBackgroundTintList(mDismissButton, colorStateList);
	}
}

public MaterialShowcaseView(Context context, AttributeSet attrs) {
	super(context, attrs);
	init(context);
}


public MaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr) {
	super(context, attrs, defStyleAttr);
	init(context);
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public MaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
	super(context, attrs, defStyleAttr, defStyleRes);
	init(context);
}

/**
 * Static helper method for resetting single use flag
 * @param context application context
 * @param showcaseId showcase id to reset
 */
public static void resetSingleUse(Context context, String showcaseId) {
	PrefsGateway.resetShowcase(context, showcaseId);
}

/**
 * Static helper method for resetting all single use flags
 * @param context application content
 */
public static void resetAll(Context context) {
	PrefsGateway.resetAll(context);
}

/**
 * Interesting drawing stuff. We draw a block of semi transparent colour to fill the whole screen
 * then we draw of transparency to create a circular "viewport" through to the underlying content
 * @param canvas the canvas to draw on
 */
@Override
protected void onDraw(Canvas canvas) {
	super.onDraw(canvas);

	// don't bother drawing if we're not ready
	if (!mShouldRender) {
		return;
	}

	// get current dimensions
	final int width = getMeasuredWidth();
	final int height = getMeasuredHeight();

	// don't bother drawing if there is nothing to draw on
	if (width <= 0 || height <= 0) {
		return;
	}


	// build a new canvas if needed i.e first pass or new dimensions
	if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {

		if (mBitmap != null) {
			mBitmap.recycle();
		}

		mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		mCanvas = new Canvas(mBitmap);
	}

	// Content position changed
	if (needsLayout()) {
		if (mTarget != null) {
			layoutTarget();
			updateBackgroundRadius();
			if (useTargetAsBackgroundCenter()) {
				mBackgroundShape.setTarget(mTarget);
			}
			// Center background around content box
			else {
				mBackgroundShape.setTarget(mContentBoxTarget);
			}

		} else {
			layoutFullscreen();
			updateBackgroundRadius();
		}
		mContentLastPoint.set((int) mContentBox.getX(), (int) mContentBox.getY());

		invalidate();
		if (!mInitialLayoutDone) {
			mInitialLayoutDone = true;
			return;
		}
	}
	mContentBox.setVisibility(VISIBLE);

	// save our 'old' dimensions
	mOldWidth = width;
	mOldHeight = height;

	// clear canvas
	mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

	// TODO always draw the circle even in fullscreen so we get some nice animations
	// draw background circle
	if (mTarget != null) {
		if (mBackgroundColorPaint == null) {
			mBackgroundColorPaint = new Paint();
			mBackgroundColorPaint.setColor(mBackgroundColor);
		}
		mBackgroundShape.draw(mCanvas, mBackgroundColorPaint);
	}
	// Fullscreen background
	else {
		mCanvas.drawColor(mBackgroundColor);
	}

	// Prepare eraser Paint if needed
	if (mEraser == null) {
		mEraser = new Paint();
		mEraser.setColor(0xFFFFFFFF);
		mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

	// draw (erase) shape
	if (mTargetShape != null) {
		mTargetShape.draw(mCanvas, mEraser);
	}

	// Draw the bitmap on our views  canvas.
	canvas.drawBitmap(mBitmap, 0, 0, null);
}

private boolean needsLayout() {
	return !mInitialLayoutDone ||
			!mContentLastPoint.equals((int) mContentBox.getX(), (int) mContentBox.getY()) ||
			(mTarget != null && !mTargetLastPoint.equals(mTarget.getPoint()));

}

private void layoutTarget() {
	Point targetPoint = mTarget.getPoint();

	// now figure out whether to put content above or below it
	int height = getMeasuredHeight();
	int midPoint = height / 2;
	int yPos = targetPoint.y;

	int gravity;
	int contentTopMargin;
	int contentBottomMargin;
	if (yPos > midPoint) {
		// target is in lower half of screen, we'll sit above it
		contentTopMargin = 0;
		contentBottomMargin = (height - yPos) + ShowcaseConfig.mTargetRadiusDefault;
		gravity = Gravity.BOTTOM;
	} else {
		// target is in upper half of screen, we'll sit below it
		contentTopMargin = yPos + ShowcaseConfig.mTargetRadiusDefault;
		contentBottomMargin = 0;
		gravity = Gravity.TOP;
	}

	if (mContentBox != null && mContentBox.getLayoutParams() != null) {
		LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

		boolean layoutParamsChanged = false;

		if (contentLP.bottomMargin != contentBottomMargin) {
			contentLP.bottomMargin = contentBottomMargin;
			layoutParamsChanged = true;
		}

		if (contentLP.topMargin != contentTopMargin) {
			contentLP.topMargin = contentTopMargin;
			layoutParamsChanged = true;
		}

		if (contentLP.gravity != gravity) {
			contentLP.gravity = gravity;
			layoutParamsChanged = true;
		}

		// Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
		if (layoutParamsChanged) {
			mContentBox.setLayoutParams(contentLP);
		}
	}
}

/**
 * Calculate max radius from background circle middle to content box
 */
private void updateBackgroundRadius() {
	mBackgroundShape.setRadius(0);

	// Get content location without padding
	int left = mContentBox.getLeft();
	int right = mContentBox.getRight();
	int top = mContentBox.getTop();
	int bottom = mContentBox.getBottom();

	Point[] contentPoints = new Point[4];
	contentPoints[0] = new Point(left, top);
	contentPoints[1] = new Point(left, bottom);
	contentPoints[2] = new Point(right, top);
	contentPoints[3] = new Point(right, bottom);

	Point diffPoint = new Point();
	int maxDistance = 0;
	for (int i = 0; i < contentPoints.length; i++) {
		diffPoint.set(contentPoints[i].x - mBackgroundShape.getPoint().x, contentPoints[i].y - mBackgroundShape.getPoint().y);
		int distance = (int) Math.sqrt(diffPoint.x * diffPoint.x + diffPoint.y * diffPoint.y);

		if (distance > maxDistance) {
			maxDistance = distance;
		}
	}

	mBackgroundShape.setRadius(maxDistance);
}

/**
 * Check if we should use target as background center. I.e. checks if the target is close to a
 * border or not.
 * @return true if we should use target as background center, false if we should use content box as
 * center
 */
private boolean useTargetAsBackgroundCenter() {
	if (mTarget != null) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

		int targetNearBorderDistance = ShowcaseConfig.mTargetNearBorderDistance;
		Point targetPoint = mTarget.getPoint();

		return targetNearBorderDistance >= targetPoint.x ||
				targetPoint.x >= width - targetNearBorderDistance ||
				targetNearBorderDistance >= targetPoint.y ||
				targetPoint.y >= height - targetNearBorderDistance;
	}

	return false;
}

private void layoutFullscreen() {
	if (mContentBox != null && mContentBox.getLayoutParams() != null) {
		LayoutParams layoutParams = (LayoutParams) mContentBox.getLayoutParams();

		if (layoutParams.gravity != Gravity.CENTER) {
			layoutParams.gravity = Gravity.CENTER;
			mContentBox.setLayoutParams(layoutParams);
		}
		mContentBox.setGravity(Gravity.CENTER);
	}

	if (mTitleTextView != null) {
		mTitleTextView.setGravity(Gravity.CENTER);
	}
	if (mContentTextView != null) {
		mContentTextView.setGravity(Gravity.CENTER);
	}
}

/**
 * Set the background color of the circle or fullscreen area.
 * @param backgroundColor the background color to use. Note that according to Material's design
 * document the opacity of the color should be 96%, 246, or F5.
 */
public void setBackgroundColor(int backgroundColor) {
	mBackgroundColor = backgroundColor;
}

@Override
protected void onDetachedFromWindow() {
	super.onDetachedFromWindow();

	// If we're being detached from the window without the mWasDismissed flag then we weren't purposefully dismissed
	// Probably due to an orientation change or user backed out of mActivity.
	// Ensure we reset the flag so the showcase display again.
	if (!mWasDismissed && mSingleUse && mPrefsGateway != null) {
		mPrefsGateway.resetShowcase();
	}

	notifyOnDismissed();
}

private void notifyOnDismissed() {
	for (ShowcaseListener listener : mListeners) {
			listener.onShowcaseDismissed(this);
		}
		mListeners.clear();

	// internal listener used by sequence for storing progress within the sequence
	if (mDetachedListener != null) {
		mDetachedListener.onShowcaseDetached(this, mWasDismissed);
		mDetachedListener = null;
	}
}

@Override
public boolean onTouch(View v, MotionEvent event) {
	if (event.getAction() == MotionEvent.ACTION_UP) {

		// Don't dismiss on touch if dismiss button is visible
		if (mDismissButton == null || mDismissButton.getVisibility() == GONE) {

			// Dismiss anywhere while in fullscreen
			if (mTarget == null) {
				hide();
			} else {
				// Try if we touched inside the target first
				if (mTargetTouchable) {
					Point targetPoint = mTarget.getPoint();
					int diffX = targetPoint.x - (int) event.getX();
					int diffY = targetPoint.y - (int) event.getY();
					int diffDistanceSq = diffX * diffX + diffY * diffY;
					if (diffDistanceSq <= ShowcaseConfig.mTargetRadiusDefaultSq) {
						hide();
						notifyOnTargetPressed();
						return false;
					}
				}

				// Test if we touched outside the background area to dismiss
				Point backgroundPoint = mBackgroundShape.getPoint();
				int diffX = backgroundPoint.x - (int) event.getX();
				int diffY = backgroundPoint.y - (int) event.getY();
				int diffDistanceSq = diffX * diffX + diffY * diffY;
				int radiusSq = mBackgroundShape.getRadius() * mBackgroundShape.getRadius();
				if (diffDistanceSq >= radiusSq) {
					hide();
				}
			}
		}
	}
	return true;
}

/**
 * Hide this showcase
 */
public void hide() {
	// This flag is used to indicate to onDetachedFromWindow that the showcase view was dismissed purposefully (by the user or programmatically)
	mWasDismissed = true;

	// TODO animate
	removeFromWindow();
}

private void notifyOnTargetPressed() {
	for (ShowcaseListener listener : mListeners) {
		listener.onTargetPressed(this);
	}
}

private void removeFromWindow() {
	if (getParent() != null && getParent() instanceof ViewGroup) {
		((ViewGroup) getParent()).removeView(this);
	}

	if (mBitmap != null) {
		mBitmap.recycle();
		mBitmap = null;
	}

	mEraser = null;
	mCanvas = null;
	mHandler = null;

	getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
	mLayoutListener = null;

	if (mPrefsGateway != null) {
		mPrefsGateway.close();
	}

	mPrefsGateway = null;
}

/**
 * Fade out the view
 */
private void fadeOut() {
	// TODO set fade out
}

/**
 * Dismiss button clicked
 * @param view button that was clicked
 */
@Override
public void onClick(View view) {
	hide();
}

/**
 * Anchor the showcase to a target button; this should be an icon button. Setting a target makes the
 * background circular, if no target is set the background will be displayed in fullscreen
 * @param target location to show the circle
 */
public void setTarget(View target) {
	setTarget(new ViewTarget(target));
}

/**
 * Anchor the showcase to a target button; this should be an icon button. Setting a target makes the
 * background circular, if no target is set the background will be displayed in fullscreen
 * @param target location to show the circle
 */
public void setTarget(Target target) {
	mTarget = target;

	if (mTarget != null) {
		mTargetShape = new CircleShape(ShowcaseConfig.mTargetRadiusDefault);
		mTargetShape.setTarget(target);
	}
}

/**
 * Set the title text
 * @param text text to display in the title
 */
public void setTitleText(CharSequence text) {
	if (mTitleTextView != null && !text.equals("")) {
		mTitleTextView.setText(text);
	}
}

/**
 * Set the title tex
 * @param resId string resource id of the title text
 */
public void setTitleText(@StringRes int resId) {
	if (mTitleTextView != null) {
		mTitleTextView.setText(resId);
	}
}

/**
 * Set a descriptive text for the showcase
 * @param text text to display in the content
 */
public void setContentText(CharSequence text) {
	if (mContentTextView != null) {
		mContentTextView.setText(text);
	}
}

/**
 * Set a descriptive text for the showcase
 * @param resId string resource id of the content text
 */
public void setContentText(@StringRes int resId) {
	if (mContentTextView != null) {
		mContentTextView.setText(resId);
	}
}

/**
 * Set the dismiss button text
 * @param resId string resource id of the dismiss text. Will be converted to ALL CAPS
 */
public void setDismissText(@StringRes int resId) {
	if (mDismissButton != null) {
		Resources resources = getResources();
		setDismissText(resources.getString(resId));
	}
}

/**
 * Set the dismiss button text
 * @param text text to show in the dismiss button. Will be converted to ALL CAPS
 */
public void setDismissText(CharSequence text) {
	if (mDismissButton != null) {
		mDismissButton.setText(text.toString().toUpperCase());
	}
}

/**
 * Set whether or not the target view can be touched while the showcase is visible.
 * @param targetTouchable true if the target should be touchable while the showcase is visible. True
 * by default.
 */
public void setTargetTouchable(boolean targetTouchable) {
	mTargetTouchable = targetTouchable;
}

/**
 * Remove a showcase listener
 * @param listener showcase listener to remove
 */
public void removeListener(MaterialShowcaseSequence listener) {
	mListeners.remove(listener);
}

/**
 * Listen to when the showcase is hidden and detached from the window. Used internally by {@link
 * MaterialShowcaseSequence}
 * @param detachedListener detached listener
 */
void setDetachedListener(DetachedListener detachedListener) {
	mDetachedListener = detachedListener;
}

/**
 * Set properties based on a config object. Will only set those attributes that have been set in the
 * config.
 * @param config set properties based from a configuration
 */
public void setConfig(ShowcaseConfig config) {
	if (config.isDelaySet()) {
		setDelay(config.getDelay());
	}
	if (config.isContentTextColorSet()) {
		setContentTextColor(config.getContentTextColor());
	}
	if (config.isDismissTextColorSet()) {
		setDismissTextColor(config.getDismissTextColor());
	}
	if (config.isTitleTextColorSet()) {
		setTitleTextColor(config.getTitleTextColor());
	}
	if (config.isBackgroundColorSet()) {
		setBackgroundColor(config.getBackgroundColor());
	}
	if (config.isRenderOverNavigationBarSet()) {
		setRenderOverNavigationBar(config.getRenderOverNavigationBar());
	}
	if (config.isDismissBackgroundColorSet()) {
		setDismissBackgroundColor(config.getDismissBackgroundColor());
	}
	for (ShowcaseListener showcaseListener : config.getListeners()) {
		addListener(showcaseListener);
	}
}

/**
 * Delay the showcase for X milliseconds after calling {@link #show(Activity)}
 * @param delayInMillis milliseconds to delay the showcase for after calling {@link
 * #show(Activity)}
 */
public void setDelay(long delayInMillis) {
	mDelayInMillis = delayInMillis;
}

/**
 * Set the color of the content description. By default this is {@link
 * com.spiddekauga.android.ui.showcase.R.color#text_color_secondary}
 * @param textColor color of the content description text
 */
public void setContentTextColor(int textColor) {
	if (mContentTextView != null) {
		mContentTextView.setTextColor(textColor);
	}
}

/**
 * Set the color of the dismiss text. By default this is {@link com.spiddekauga.android.ui.showcase.R.color#text_color_secondary}
 * @param textColor color of the dismiss button text
 */
public void setDismissTextColor(int textColor) {
	if (mDismissButton != null) {
		mDismissButton.setTextColor(textColor);
	}
}

/**
 * Set the color of the title text. By default this is {@link com.spiddekauga.android.ui.showcase.R.color#text_color_primary}
 * @param textColor color of the title text
 */
private void setTitleTextColor(int textColor) {
	if (mTitleTextView != null) {
		mTitleTextView.setTextColor(textColor);
	}
}

/**
 * Render above the navigation bar. Only has some effect in Lollipop or above.
 * @param renderOverNav true to render above the navigation bar
 */
public void setRenderOverNavigationBar(boolean renderOverNav) {
	mRenderOverNav = renderOverNav;
}

/**
 * Add a showcase listener to listen to dismiss, display, and skipped events.
 * @param listener showcase listener
 */
public void addListener(ShowcaseListener listener) {
	mListeners.add(listener);
}

/**
 * If this showcase is a single use, check if it has fired.
 * @return true if this single use showcase has fired, always returns false if this showcase isn't
 * set as single use.
 * @see #setSingleUse(String) to set the showcase as single use
 */
public boolean hasFired() {
	return mPrefsGateway != null && mPrefsGateway.hasFired();
}

/**
 * Call this to only allow the showcase to be shown once for the app. There after you must reset the
 * showcase by calling either {@link #resetSingleUse()}, {@link #resetSingleUse(Context, String)},
 * or {@link #resetAll(Context)} to be able to show it again.
 * @param showcaseId
 */
public void setSingleUse(@NonNull String showcaseId) {
	mSingleUse = true;
	mPrefsGateway = new PrefsGateway(getContext(), showcaseId);
}

/**
 * Reveal the showcase view.
 * @param activity the mActivity to show the showcase in
 * @return true if the showcase was shown
 */
public boolean show(final Activity activity) {
	// if we're in single use mode and have already shot our bolt then do nothing
	if (mSingleUse) {
		if (mPrefsGateway.hasFired()) {
			notifyOnSkipped();
			return false;
		} else {
			mPrefsGateway.setFired();
		}
	}

	mHandler = new Handler();
	mHandler.postDelayed(new Runnable() {
		@Override
		public void run() {
			((ViewGroup) activity.getWindow().getDecorView()).addView(MaterialShowcaseView.this);

			hideEmptyViews();
			fixNavBarMargin();

			fadeIn();
		}
	}, mDelayInMillis);

	return true;
}

/**
 * Notify when {@link #setSingleUse(String)} is enabled and showcase has been fired before
 * @see #setSingleUse(String)
 */
private void notifyOnSkipped() {
	for (ShowcaseListener listener : mListeners) {
			listener.onShowcaseSkipped(this);
		}
	mListeners.clear();

	// internal listener used by sequence for storing progress within the sequence
	if (mDetachedListener != null) {
		mDetachedListener.onShowcaseDetached(this, mWasDismissed);
		mDetachedListener = null;
	}
}

private void hideEmptyViews() {
	if (mTitleTextView != null && mTitleTextView.getText().equals("")) {
		mTitleTextView.setVisibility(GONE);
	}
	if (mContentTextView != null && mContentTextView.getText().equals("")) {
		mContentTextView.setVisibility(GONE);
	}
	if (mDismissButton != null && mDismissButton.getText().equals("")) {
		mDismissButton.setVisibility(GONE);
	}
}

private void fixNavBarMargin() {
	// If we're on lollipop then make sure we don't draw over the nav bar
	if (!mRenderOverNav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		LayoutParams contentLP = (LayoutParams) getLayoutParams();

		if (contentLP != null) {
			int bottomMargin = getSoftButtonsBarSizePort((Activity) getContext());
			int rightMargin = getSoftButtonsBarHorizontalSizePort((Activity) getContext());

			if (contentLP.bottomMargin != bottomMargin) {
				contentLP.bottomMargin = bottomMargin;
			}
			if (contentLP.rightMargin != rightMargin) {
				contentLP.rightMargin = rightMargin;
			}
			setLayoutParams(contentLP);
		}
	}
}

/**
 * Fade in the showcase
 */
private void fadeIn() {
	// TODO Fade in...
	setShouldRender(true);
	setVisibility(VISIBLE);
	mContentBox.setVisibility(INVISIBLE);
	notifyOnDisplayed();
}

private static int getSoftButtonsBarSizePort(Activity activity) {
	// getRealMetrics is only available with API 17 and +
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int usableHeight = metrics.heightPixels;
		activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
		int realHeight = metrics.heightPixels;
		if (realHeight > usableHeight) {
			return realHeight - usableHeight;
		} else {
			return 0;
		}
	}
	return 0;
}

private static int getSoftButtonsBarHorizontalSizePort(Activity activity) {
	// getRealMetrics is only available with API 17 and +
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int usableWidth = metrics.widthPixels;
		activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
		int realWidth = metrics.widthPixels;
		if (realWidth > usableWidth) {
			return realWidth - usableWidth;
		} else {
			return 0;
		}
	}
	return 0;
}

private void setShouldRender(boolean shouldRender) {
	mShouldRender = shouldRender;
}

private void notifyOnDisplayed() {
	for (ShowcaseListener listener : mListeners) {
		listener.onShowcaseDisplayed(this);
	}
}

/**
 * Set content box layout params
 * @param gravity gravity of the content box
 * @param bottomMargin bottom margin (usually from the target)
 * @param topMargin top margin (usually from target)
 */
private void applyLayoutParams(int gravity, int bottomMargin, int topMargin) {

}

/**
 * Reset this showcase so that it can be shown again. Only has an effect if this is a single use
 * showcase, i.e. that {@link #setSingleUse(String)} has been called.
 */
public void resetSingleUse() {
	if (mSingleUse && mPrefsGateway != null) {
		mPrefsGateway.resetShowcase();
	}
}

/**
 * Gives us a builder utility class with a fluent API for eaily configuring showcase
 * views
 */
public static class Builder {
	final MaterialShowcaseView mShowcaseView;
	private final Activity mActivity;

	/**
	 * Create the builder
	 * @param activity the activity to show the showcase in
	 */
	public Builder(Activity activity) {
		mActivity = activity;
		mShowcaseView = new MaterialShowcaseView(activity);
	}

	/**
	 * Anchor the showcase to a target button; this should be an icon button. Setting a target makes
	 * the background circular, if no target is set the background will be displayed in fullscreen
	 * @param target location to show the circle
	 */
	public Builder setTarget(View target) {
		mShowcaseView.setTarget(target);
		return this;
	}

	/**
	 * Anchor the showcase to a target button; this should be an icon button. Setting a target makes
	 * the background circular, if no target is set the background will be displayed in fullscreen
	 * @param target location to show the circle
	 */
	public Builder setTarget(Target target) {
		mShowcaseView.setTarget(target);
		return this;
	}

	/**
	 * Set the dismiss button text
	 * @param resId string resource id of the dismiss text
	 */
	public Builder setDismissText(@StringRes int resId) {
		mShowcaseView.setDismissText(resId);
		return this;
	}

	/**
	 * Set the dismiss button text
	 * @param text text to show in the dismiss button
	 */
	public Builder setDismissText(CharSequence text) {
		if (text != null) {
			mShowcaseView.setDismissText(text);
		}
		return this;
	}

	/**
	 * Set a descriptive text for the showcase
	 * @param resId string resource id of the title text
	 */
	public Builder setContentText(@StringRes int resId) {
		return setContentText(mActivity.getString(resId));
	}

	/**
	 * Set a descriptive text for the showcase
	 * @param text text to display in the content
	 */
	public Builder setContentText(CharSequence text) {
		if (text != null) {
			mShowcaseView.setContentText(text);
		}
		return this;
	}

	/**
	 * Set the title tex
	 * @param resId string resource id of the title text
	 */
	public Builder setTitleText(@StringRes int resId) {
		mShowcaseView.setTitleText(resId);
		return this;
	}

	/**
	 * Set the title text
	 * @param text text to display in the title
	 */
	public Builder setTitleText(CharSequence text) {
		if (text != null) {
			mShowcaseView.setTitleText(text);
		}
		return this;
	}

	/**
	 * Set whether or not the target view can be touched while the showcase is visible.
	 * @param targetTouchable true if the target should be touchable while the showcase is visible. True by default.
	 */
	public Builder setTargetTouchable(boolean targetTouchable) {
		mShowcaseView.setTargetTouchable(targetTouchable);
		return this;
	}

	/**
	 * Set properties based on a config object. Will only set those attributes that have been set in the config.
	 * @param config set properties based from a configuration
	 */
	public void setConfig(ShowcaseConfig config) {
		mShowcaseView.setConfig(config);
	}

	public Builder setBackgroundColor(int backgroundColor) {
		mShowcaseView.setBackgroundColor(backgroundColor);
		return this;
	}

	/**
	 * Set the color of the title text. By default this is {@link com.spiddekauga.android.ui.showcase.R.color#text_color_primary}
	 * @param textColor color of the title text
	 */
	public Builder setTitleTextColor(int textColor) {
		mShowcaseView.setTitleTextColor(textColor);
		return this;
	}

	/**
	 * Set the color of the content description. By default this is {@link
	 * com.spiddekauga.android.ui.showcase.R.color#text_color_secondary}
	 * @param textColor color of the content description text
	 */
	public Builder setContentTextColor(int textColor) {
		mShowcaseView.setContentTextColor(textColor);
		return this;
	}

	/**
	 * Set the color of the dismiss text. By default this is {@link com.spiddekauga.android.ui.showcase.R.color#text_color_secondary}
	 * @param textColor color of the dismiss button text
	 */
	public Builder setDismissTextColor(int textColor) {
		mShowcaseView.setDismissTextColor(textColor);
		return this;
	}

	/**
	 * Set the background color of the dismiss button. By default this is {@link
	 * com.spiddekauga.android.ui.showcase.R.color#material_showcase_dismiss_background}
	 * @param backgroundColor background color of the dismiss button
	 */
	public Builder setDismissBackgroundColor(int backgroundColor) {
		mShowcaseView.setDismissBackgroundColor(backgroundColor);
		return this;
	}

	/**
	 * Delay the showcase for X milliseconds after calling {@link #show(Activity)}
	 * @param delayInMillis milliseconds to delay the showcase for after calling {@link
	 * #show(Activity)}
	 */
	public Builder setDelay(int delayInMillis) {
		mShowcaseView.setDelay(delayInMillis);
		return this;
	}

	/**
	 * Add a showcase listener to listen to dismiss, display, and skipped events.
	 * @param listener showcase listener
	 */
	public Builder addListener(ShowcaseListener listener) {
		mShowcaseView.addListener(listener);
		return this;
	}

	/**
	 * Call this to only allow the showcase to be shown once for the app. There after you must reset
	 * the showcase by calling either {@link #resetSingleUse()}, {@link #resetSingleUse(Context,
	 * String)}, or {@link #resetAll(Context)} to be able to show it again.
	 * @param showcaseId
	 */
	public Builder setSingleUse(@NonNull String showcaseId) {
		mShowcaseView.setSingleUse(showcaseId);
		return this;
	}

	/**
	 * Render above the navigation bar. Only has an effect in Lollipop or above.
	 */
	public Builder renderOverNavigationBar() {
		mShowcaseView.setRenderOverNavigationBar(true);
		return this;
	}

	/**
	 * Build and show the showcase
	 * @return created showcase view
	 */
	public MaterialShowcaseView show() {
		build().show(mActivity);
		return mShowcaseView;
	}

	/**
	 * Build the showcase
	 * @return created showcase view
	 */
	public MaterialShowcaseView build() {
		return mShowcaseView;
	}
}

/**
 * REDRAW LISTENER - this ensures we redraw after mActivity finishes laying out
 */
private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

	@Override
	public void onGlobalLayout() {
		setTarget(mTarget);
	}
}
}
