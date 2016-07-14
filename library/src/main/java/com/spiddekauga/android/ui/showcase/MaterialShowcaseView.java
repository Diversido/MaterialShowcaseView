package com.spiddekauga.android.ui.showcase;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.spiddekauga.android.ui.showcase.target.Target;
import com.spiddekauga.android.ui.showcase.target.ViewTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a sequ
 */
public class MaterialShowcaseView extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

List<IShowcaseListener> mListeners; // external listeners who want to observe when we show and dismiss
private int mOldHeight;
private int mOldWidth;
private Bitmap mBitmap;// = new WeakReference<>(null);
private Canvas mCanvas;
private Paint mEraser;
private Target mTarget;
private CircleShape mTargetShape = null;
private boolean mWasDismissed = false;
private View mContentBox;
private TextView mTitleTextView;
private TextView mContentTextView;
private TextView mDismissButton;
private int mGravity;
private int mContentBottomMargin;
private int mContentTopMargin;
private Point mTargetLastPoint = new Point();
private Point mContentLastPoint = new Point();
private CircleShape mBackgroundShape = new CircleShape();
private Target mContentBoxTarget;
private boolean mBackgroundFullscreen = false;
private boolean mDismissOnTouch = false;
private boolean mShouldRender = false; // flag to decide when we should actually render
private boolean mRenderOverNav = false;
private int mMaskColor;
private Paint mMaskColorPaint;
private Handler mHandler;
private long mDelayInMillis = ShowcaseConfig.DEFAULT_DELAY;
private boolean mSingleUse = false; // should display only once
private PrefsGateway mPrefsGateway; // used to store state doe single use mode
private UpdateOnGlobalLayout mLayoutListener;
private IDetachedListener mDetachedListener;
private boolean mTargetTouchable = false;
private boolean mDismissOnTargetTouch = true;

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

	mMaskColor = ShowcaseConfig.mMaskColorDefault;
	setVisibility(INVISIBLE);


	View contentView = LayoutInflater.from(getContext()).inflate(R.layout.showcase_content, this, true);
	mContentBox = contentView.findViewById(R.id.content_box);
	mTitleTextView = (TextView) contentView.findViewById(R.id.tv_title);
	mContentTextView = (TextView) contentView.findViewById(R.id.tv_content);
	mDismissButton = (TextView) contentView.findViewById(R.id.tv_dismiss);
	mDismissButton.setOnClickListener(this);
	mContentBoxTarget = new ViewTarget(mContentBox);
	mBackgroundShape.setTarget(mContentBoxTarget);
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
 * Interesting drawing stuff. We draw a block of semi transparent colour to fill the whole screen
 * then we draw of transparency to create a circular "viewport" through to the underlying content
 * @param canvas
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

	// Update background radius
	if (!mBackgroundFullscreen) {
		// Content position changed
		if (!mContentLastPoint.equals((int) mContentBox.getX(), (int) mContentBox.getY())) {
			mContentLastPoint.set((int) mContentBox.getX(), (int) mContentBox.getY());

			if (mTarget != null) {
				if (useTargetAsBackgroundCenter()) {
					if (!mTargetLastPoint.equals(mTarget.getPoint())) {
						updateBackgroundRadius();
					}
					mBackgroundShape.setTarget(mTarget);
				}
				// Center background around content box
				else {
					updateBackgroundRadius();
					mBackgroundShape.setTarget(mContentBoxTarget);
				}
			} else {
				updateBackgroundRadius();
			}
		}
	}

	// save our 'old' dimensions
	mOldWidth = width;
	mOldHeight = height;

	// clear canvas
	mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

	// draw solid background
	if (!mBackgroundFullscreen && mBackgroundShape.getRadius() > 0) {
		if (mMaskColorPaint == null) {
			mMaskColorPaint = new Paint();
			mMaskColorPaint.setColor(mMaskColor);
		}
		mBackgroundShape.draw(mCanvas, mMaskColorPaint);
	} else {
		mCanvas.drawColor(mMaskColor);
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
		mTargetShape.draw(canvas, mEraser);
	}

	// Draw the bitmap on our views  canvas.
	canvas.drawBitmap(mBitmap, 0, 0, null);
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

		return targetNearBorderDistance >= mTarget.getPoint().x ||
				mTarget.getPoint().x >= width - targetNearBorderDistance ||
				targetNearBorderDistance >= mTarget.getPoint().y ||
				mTarget.getPoint().y >= height - targetNearBorderDistance;
	}

	return false;
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

@Override
protected void onDetachedFromWindow() {
	super.onDetachedFromWindow();

	/**
	 * If we're being detached from the window without the mWasDismissed flag then we weren't purposefully dismissed
	 * Probably due to an orientation change or user backed out of activity.
	 * Ensure we reset the flag so the showcase display again.
	 */
	if (!mWasDismissed && mSingleUse && mPrefsGateway != null) {
		mPrefsGateway.resetShowcase();
	}


	notifyOnDismissed();

}

private void notifyOnDismissed() {
	if (mListeners != null) {
		for (IShowcaseListener listener : mListeners) {
			listener.onShowcaseDismissed(this);
		}

		mListeners.clear();
		mListeners = null;
	}

	/**
	 * internal listener used by sequence for storing progress within the sequence
	 */
	if (mDetachedListener != null) {
		mDetachedListener.onShowcaseDetached(this, mWasDismissed);
	}
}

@Override
public boolean onTouch(View v, MotionEvent event) {
	if (mDismissOnTouch) {
		hide();
		return true;
	}
	if (mTargetTouchable && mDismissOnTargetTouch) {
		int diffX = (mTarget.getPoint().x + (int) event.getX()) / 2;
		int diffY = (mTarget.getPoint().y + (int) event.getY()) / 2;
		int diffDistanceSq = diffX * diffX + diffY * diffY;
		if (diffDistanceSq <= ShowcaseConfig.mTargetRadiusDefaultSq) {
			hide();
			return true;
		}
	}
	return false;
}

public void hide() {
	// This flag is used to indicate to onDetachedFromWindow that the showcase view was dismissed purposefully (by the user or programmatically)
	mWasDismissed = true;

	// TODO animate
	removeFromWindow();
}

public void removeFromWindow() {
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

public void fadeOut() {
	// TODO set fade out
}

/**
 * Dismiss button clicked
 * @param v
 */
@Override
public void onClick(View v) {
	hide();
}

/**
 * Tells us about the "Target" which is the view we want to anchor to. We figure out where it is on
 * screen and (optionally) how big it is. We also figure out whether to place our content and
 * dismiss button above or below it.
 * @param target
 */
public void setTarget(Target target) {
	mTarget = target;

	// update dismiss button state
	updateDismissButton();

	if (mTarget != null) {
		mTargetShape = new CircleShape(ShowcaseConfig.mTargetRadiusDefault);

		// If we're on lollipop then make sure we don't draw over the nav bar
		if (!mRenderOverNav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			int bottomMargin = getSoftButtonsBarSizePort((Activity) getContext());
			int rightMargin = getSoftButtonsBarHorizontalSizePort((Activity) getContext());

			LayoutParams contentLP = (LayoutParams) getLayoutParams();

			if (contentLP != null) {
				if (contentLP.bottomMargin != bottomMargin) {
					contentLP.bottomMargin = bottomMargin;
				}
				if (contentLP.rightMargin != rightMargin) {
					contentLP.rightMargin = rightMargin;
				}
			}
		}

		Point targetPoint = mTarget.getPoint();

		// now figure out whether to put content above or below it
		int height = getMeasuredHeight();
		int midPoint = height / 2;
		int yPos = targetPoint.y;

		if (yPos > midPoint) {
			// target is in lower half of screen, we'll sit above it
			mContentTopMargin = 0;
			mContentBottomMargin = (height - yPos) + ShowcaseConfig.mTargetRadiusDefault;
			mGravity = Gravity.BOTTOM;
		} else {
			// target is in upper half of screen, we'll sit below it
			mContentTopMargin = yPos + ShowcaseConfig.mTargetRadiusDefault;
			mContentBottomMargin = 0;
			mGravity = Gravity.TOP;
		}
	}

	applyLayoutParams();
}

private void updateDismissButton() {
	// hide or show button
	if (mDismissButton != null) {
		if (TextUtils.isEmpty(mDismissButton.getText())) {
			mDismissButton.setVisibility(GONE);
		} else {
			mDismissButton.setVisibility(VISIBLE);
		}
	}
}

public static int getSoftButtonsBarSizePort(Activity activity) {
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

public static int getSoftButtonsBarHorizontalSizePort(Activity activity) {
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

private void applyLayoutParams() {

	if (mContentBox != null && mContentBox.getLayoutParams() != null) {
		LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

		boolean layoutParamsChanged = false;

		if (contentLP.bottomMargin != mContentBottomMargin) {
			contentLP.bottomMargin = mContentBottomMargin;
			layoutParamsChanged = true;
		}

		if (contentLP.topMargin != mContentTopMargin) {
			contentLP.topMargin = mContentTopMargin;
			layoutParamsChanged = true;
		}

		if (contentLP.gravity != mGravity) {
			contentLP.gravity = mGravity;
			layoutParamsChanged = true;
		}

		/**
		 * Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
		 */
		if (layoutParamsChanged) {
			mContentBox.setLayoutParams(contentLP);
		}
	}
}

private void setTitleText(CharSequence contentText) {
	if (mTitleTextView != null && !contentText.equals("")) {
		mContentTextView.setAlpha(0.5F);
		mTitleTextView.setText(contentText);
	}
}

private void setContentText(CharSequence contentText) {
	if (mContentTextView != null) {
		mContentTextView.setText(contentText);
	}
}

private void setDismissText(CharSequence dismissText) {
	if (mDismissButton != null) {
		mDismissButton.setText(dismissText);

		updateDismissButton();
	}
}

private void setTitleTextColor(int textColour) {
	if (mTitleTextView != null) {
		mTitleTextView.setTextColor(textColour);
	}
}

private void setDismissOnTouch(boolean dismissOnTouch) {
	mDismissOnTouch = dismissOnTouch;
}

private void setTargetTouchable(boolean targetTouchable) {
	mTargetTouchable = targetTouchable;
}

private void setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
	mDismissOnTargetTouch = dismissOnTargetTouch;
}

public void addShowcaseListener(IShowcaseListener showcaseListener) {

	if (mListeners != null) {
		mListeners.add(showcaseListener);
	}
}

public void removeShowcaseListener(MaterialShowcaseSequence showcaseListener) {

	if ((mListeners != null) && mListeners.contains(showcaseListener)) {
		mListeners.remove(showcaseListener);
	}
}

void setDetachedListener(IDetachedListener detachedListener) {
	mDetachedListener = detachedListener;
}

/**
 * Set properties based on a config object
 * @param config
 */
public void setConfig(ShowcaseConfig config) {
	setDelay(config.getDelay());
	setContentTextColor(config.getContentTextColor());
	setDismissTextColor(config.getDismissTextColor());
	setMaskColor(config.getMaskColor());
	setRenderOverNavigationBar(config.getRenderOverNavigationBar());
	setBackgroundFullscreen(config.isBackgroundFullscreen());
}

private void setDelay(long delayInMillis) {
	mDelayInMillis = delayInMillis;
}

private void setContentTextColor(int textColour) {
	if (mContentTextView != null) {
		mContentTextView.setTextColor(textColour);
	}
}

private void setDismissTextColor(int textColour) {
	if (mDismissButton != null) {
		mDismissButton.setTextColor(textColour);
	}
}

private void setMaskColor(int maskColor) {
	mMaskColor = maskColor;
}

private void setRenderOverNavigationBar(boolean mRenderOverNav) {
	this.mRenderOverNav = mRenderOverNav;
}

public boolean hasFired() {
	return mPrefsGateway.hasFired();
}

private void singleUse(String showcaseID) {
	mSingleUse = true;
	mPrefsGateway = new PrefsGateway(getContext(), showcaseID);
}

/**
 * Reveal the showcaseview. Returns a boolean telling us whether we actually did show anything
 * @param activity the activity to show the showcase in
 * @return true if the showcase was shown
 */
boolean show(final Activity activity) {
	// if we're in single use mode and have already shot our bolt then do nothing
	if (mSingleUse) {
		if (mPrefsGateway.hasFired()) {
			notifyOnSkipped();
			return false;
		} else {
			mPrefsGateway.setFired();
		}
	}

	((ViewGroup) activity.getWindow().getDecorView()).addView(this);

	setShouldRender(true);

	mHandler = new Handler();
	mHandler.postDelayed(new Runnable() {
		@Override
		public void run() {
			fadeIn();
		}
	}, mDelayInMillis);

	updateDismissButton();

	return true;
}

/**
 * Notify when {@link #singleUse(String)} is enabled and showcase has been fired before
 * @see #singleUse(String)
 */
private void notifyOnSkipped() {

	if (mListeners != null) {
		for (IShowcaseListener listener : mListeners) {
			listener.onShowcaseSkipped(this);
		}
		mListeners.clear();
		mListeners = null;
	}

	/**
	 * internal listener used by sequence for storing progress within the sequence
	 */
	if (mDetachedListener != null) {
		mDetachedListener.onShowcaseDetached(this, mWasDismissed);
	}
}

private void setShouldRender(boolean shouldRender) {
	mShouldRender = shouldRender;
}

public void fadeIn() {
	// TODO Fade in...
	setVisibility(VISIBLE);
	notifyOnDisplayed();
}

private void notifyOnDisplayed() {

	if (mListeners != null) {
		for (IShowcaseListener listener : mListeners) {
			listener.onShowcaseDisplayed(this);
		}
	}
}

public boolean isBackgroundFullscreen() {
	return mBackgroundFullscreen;
}

/**
 * Set the background to render the fullscreen.
 * @param fullscreen If set to true the background will render the entire screen. If set to false it
 * will instead render a circle around the target and content like material's design
 */
public void setBackgroundFullscreen(boolean fullscreen) {
	mBackgroundFullscreen = fullscreen;
}

public void resetSingleUse() {
	if (mSingleUse && mPrefsGateway != null) {
		mPrefsGateway.resetShowcase();
	}
}

/**
 * BUILDER CLASS Gives us a builder utility class with a fluent API for eaily configuring showcase
 * views
 */
public static class Builder {
	private static final int CIRCLE_SHAPE = 0;
	private static final int RECTANGLE_SHAPE = 1;
	private static final int NO_SHAPE = 2;
	final MaterialShowcaseView showcaseView;
	private final Activity activity;
	private boolean fullWidth = false;
	private int shapeType = CIRCLE_SHAPE;

	public Builder(Activity activity) {
		this.activity = activity;

		showcaseView = new MaterialShowcaseView(activity);
	}

	/**
	 * Set the title text shown on the ShowcaseView.
	 */
	public Builder setTarget(View target) {
		showcaseView.setTarget(new ViewTarget(target));
		return this;
	}

	/**
	 * Set the title text shown on the ShowcaseView.
	 */
	public Builder setDismissText(int resId) {
		return setDismissText(activity.getString(resId));
	}

	public Builder setDismissText(CharSequence dismissText) {
		showcaseView.setDismissText(dismissText);
		return this;
	}

	/**
	 * Set the content text shown on the ShowcaseView.
	 */
	public Builder setContentText(int resId) {
		return setContentText(activity.getString(resId));
	}

	/**
	 * Set the descriptive text shown on the ShowcaseView.
	 */
	public Builder setContentText(CharSequence text) {
		showcaseView.setContentText(text);
		return this;
	}

	/**
	 * Set the title text shown on the ShowcaseView.
	 */
	public Builder setTitleText(int resId) {
		return setTitleText(activity.getString(resId));
	}

	/**
	 * Set the descriptive text shown on the ShowcaseView as the title.
	 */
	public Builder setTitleText(CharSequence text) {
		showcaseView.setTitleText(text);
		return this;
	}

	/**
	 * Set whether or not the target view can be touched while the showcase is visible.
	 * <p>
	 * False by default.
	 */
	public Builder setTargetTouchable(boolean targetTouchable) {
		showcaseView.setTargetTouchable(targetTouchable);
		return this;
	}

	/**
	 * Set whether or not the showcase should dismiss when the target is touched.
	 * <p>
	 * True by default.
	 */
	public Builder setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
		showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch);
		return this;
	}

	public Builder setDismissOnTouch(boolean dismissOnTouch) {
		showcaseView.setDismissOnTouch(dismissOnTouch);
		return this;
	}

	public Builder setMaskColour(int maskColour) {
		showcaseView.setMaskColor(maskColour);
		return this;
	}

	public Builder setTitleTextColor(int textColour) {
		showcaseView.setTitleTextColor(textColour);
		return this;
	}

	public Builder setContentTextColor(int textColour) {
		showcaseView.setContentTextColor(textColour);
		return this;
	}

	public Builder setDismissTextColor(int textColour) {
		showcaseView.setDismissTextColor(textColour);
		return this;
	}

	public Builder setDelay(int delayInMillis) {
		showcaseView.setDelay(delayInMillis);
		return this;
	}

	/**
	 * Set the background to render the fullscreen.
	 * @param fullscreen If set to true the background will render the entire screen. If set to
	 * false it will instead render a circle around the target and content like material's design
	 */
	public void setBackgroundFullscreen(boolean fullscreen) {
		showcaseView.setBackgroundFullscreen(fullscreen);
	}

	public Builder setListener(IShowcaseListener listener) {
		showcaseView.addShowcaseListener(listener);
		return this;
	}

	public Builder singleUse(String showcaseID) {
		showcaseView.singleUse(showcaseID);
		return this;
	}

	public Builder renderOverNavigationBar() {
		// Note: This only has an effect in Lollipop or above.
		showcaseView.setRenderOverNavigationBar(true);
		return this;
	}

	public MaterialShowcaseView show() {
		build().show(activity);
		return showcaseView;
	}

	public MaterialShowcaseView build() {
		if (showcaseView.mTitleTextView != null && showcaseView.mTitleTextView.getText().equals("")) {
			showcaseView.mTitleTextView.setVisibility(GONE);
		}

		return showcaseView;
	}

}

/**
 * REDRAW LISTENER - this ensures we redraw after activity finishes laying out
 */
private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

	@Override
	public void onGlobalLayout() {
		setTarget(mTarget);
	}
}
}
