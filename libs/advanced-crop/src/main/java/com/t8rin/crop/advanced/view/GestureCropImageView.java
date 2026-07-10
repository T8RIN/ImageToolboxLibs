package com.t8rin.crop.advanced.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.t8rin.crop.advanced.util.RotationGestureDetector;

public class GestureCropImageView extends CropImageView {

    private static final int DOUBLE_TAP_ZOOM_DURATION = 200;
    private static final float MIN_SCALE_SPAN = 10f;
    private static final float ONE_FINGER_ZOOM_SENSITIVITY = 0.004f;

    private RotationGestureDetector mRotateDetector;
    private GestureDetector mGestureDetector;

    private float mMidPntX, mMidPntY;
    private float mPreviousScaleSpan;
    private float mOneFingerZoomCenterX, mOneFingerZoomCenterY, mOneFingerZoomLastY;
    private float mDownX, mDownY, mLastTapX, mLastTapY;
    private long mDownTime, mLastTapUpTime;

    private boolean mIsRotateEnabled = true, mIsScaleEnabled = true, mIsGestureEnabled = true;
    private boolean mIsOneFingerZoomEnabled = true, mIsOneFingerZooming = false, mHasOneFingerZoomMoved = false;
    private boolean mHasMovedSinceDown = false;
    private int mDoubleTapScaleSteps = 5;
    private int mTouchSlop, mDoubleTapSlop;

    public GestureCropImageView(Context context) {
        super(context);
    }

    public GestureCropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureCropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isScaleEnabled() {
        return mIsScaleEnabled;
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        mIsScaleEnabled = scaleEnabled;
    }

    public boolean isRotateEnabled() {
        return mIsRotateEnabled;
    }

    public void setRotateEnabled(boolean rotateEnabled) {
        mIsRotateEnabled = rotateEnabled;
    }

    public boolean isGestureEnabled() {
        return mIsGestureEnabled;
    }

    public void setGestureEnabled(boolean gestureEnabled) {
        mIsGestureEnabled = gestureEnabled;
    }

    public boolean isOneFingerZoomEnabled() {
        return mIsOneFingerZoomEnabled;
    }

    public void setOneFingerZoomEnabled(boolean oneFingerZoomEnabled) {
        mIsOneFingerZoomEnabled = oneFingerZoomEnabled;
    }

    public int getDoubleTapScaleSteps() {
        return mDoubleTapScaleSteps;
    }

    public void setDoubleTapScaleSteps(int doubleTapScaleSteps) {
        mDoubleTapScaleSteps = doubleTapScaleSteps;
    }

    /**
     * If it's ACTION_DOWN event - user touches the screen and all current animation must be canceled.
     * If it's ACTION_UP event - user removed all fingers from the screen and current image position must be corrected.
     * If there are more than 2 fingers - update focal point coordinates.
     * Pass the event to the gesture detectors if those are enabled.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            cancelAllAnimations();
            mPreviousScaleSpan = 0f;
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mIsOneFingerZooming = false;
            }
        }

        if (event.getPointerCount() > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2;
            mMidPntY = (event.getY(0) + event.getY(1)) / 2;
        }

        boolean isOneFingerZoomHandled = mIsOneFingerZoomEnabled &&
                mIsScaleEnabled &&
                handleOneFingerZoomEvent(event, action);

        if (mIsGestureEnabled && !isOneFingerZoomHandled) {
            mGestureDetector.onTouchEvent(event);
        }

        if (mIsScaleEnabled && !isOneFingerZoomHandled) {
            handleScale(event);
        }

        if (mIsRotateEnabled && !isOneFingerZoomHandled) {
            mRotateDetector.onTouchEvent(event);
        }

        if (action == MotionEvent.ACTION_POINTER_UP) {
            mPreviousScaleSpan = 0f;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mPreviousScaleSpan = 0f;
            setImageToWrapCropBounds();
        }
        return true;
    }

    @Override
    protected void init() {
        super.init();
        setupGestureListeners();
    }

    /**
     * This method calculates target scale value for double tap gesture.
     * User is able to zoom the image from min scale value
     * to the max scale value with {@link #mDoubleTapScaleSteps} double taps.
     */
    protected float getDoubleTapTargetScale() {
        return getCurrentScale() * (float) Math.pow(getMaxScale() / getMinScale(), 1.0f / mDoubleTapScaleSteps);
    }

    private void setupGestureListeners() {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(), null, true);
        mRotateDetector = new RotationGestureDetector(new RotateListener());

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mDoubleTapSlop = configuration.getScaledDoubleTapSlop();
    }

    private void handleScale(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            mPreviousScaleSpan = 0f;
            return;
        }

        float currentSpan = getScaleSpan(event);
        if (currentSpan < MIN_SCALE_SPAN) {
            return;
        }

        if (mPreviousScaleSpan >= MIN_SCALE_SPAN) {
            postScale(currentSpan / mPreviousScaleSpan, mMidPntX, mMidPntY);
        }
        mPreviousScaleSpan = currentSpan;
    }

    private float getScaleSpan(MotionEvent event) {
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private boolean handleOneFingerZoomEvent(MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                mDownTime = event.getEventTime();
                mHasMovedSinceDown = false;
                if (isSecondTap(event)) {
                    startOneFingerZoom(event);
                    mLastTapUpTime = 0L;
                    return true;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (mIsOneFingerZooming) {
                    handleOneFingerZoom(event);
                    return true;
                }
                if (!mHasMovedSinceDown && isMovedPastSlop(event.getX(), event.getY(), mDownX, mDownY, mTouchSlop)) {
                    mHasMovedSinceDown = true;
                }
                return false;
            case MotionEvent.ACTION_UP:
                if (mIsOneFingerZooming) {
                    finishOneFingerZoom(event);
                    return true;
                }
                if (!mHasMovedSinceDown &&
                        event.getEventTime() - mDownTime <= ViewConfiguration.getLongPressTimeout()) {
                    mLastTapUpTime = event.getEventTime();
                    mLastTapX = event.getX();
                    mLastTapY = event.getY();
                } else {
                    mLastTapUpTime = 0L;
                }
                return false;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_CANCEL:
                mIsOneFingerZooming = false;
                mLastTapUpTime = 0L;
                return false;
            default:
                return false;
        }
    }

    private class RotateListener extends RotationGestureDetector.SimpleOnRotationGestureListener {

        @Override
        public boolean onRotation(RotationGestureDetector rotationDetector) {
            postRotate(rotationDetector.getAngle(), mMidPntX, mMidPntY);
            return true;
        }

    }

    private boolean isSecondTap(MotionEvent event) {
        long timeSinceLastTap = event.getEventTime() - mLastTapUpTime;
        return mLastTapUpTime > 0L &&
                timeSinceLastTap <= ViewConfiguration.getDoubleTapTimeout() &&
                !isMovedPastSlop(event.getX(), event.getY(), mLastTapX, mLastTapY, mDoubleTapSlop);
    }

    private boolean isMovedPastSlop(float x, float y, float startX, float startY, int slop) {
        float dx = x - startX;
        float dy = y - startY;
        return dx * dx + dy * dy > slop * slop;
    }

    private void startOneFingerZoom(MotionEvent event) {
        mIsOneFingerZooming = true;
        mHasOneFingerZoomMoved = false;
        mOneFingerZoomCenterX = event.getX();
        mOneFingerZoomCenterY = event.getY();
        mOneFingerZoomLastY = event.getY();
    }

    private void handleOneFingerZoom(MotionEvent event) {
        if (event.getPointerCount() != 1) {
            mIsOneFingerZooming = false;
            return;
        }

        float deltaY = event.getY() - mOneFingerZoomLastY;
        if (deltaY != 0f) {
            postScale(
                    Math.max(0.01f, 1f + deltaY * ONE_FINGER_ZOOM_SENSITIVITY),
                    mOneFingerZoomCenterX,
                    mOneFingerZoomCenterY
            );
            mOneFingerZoomLastY = event.getY();
            mHasOneFingerZoomMoved = true;
        }
    }

    private void finishOneFingerZoom(MotionEvent event) {
        if (!mHasOneFingerZoomMoved) {
            zoomImageToPosition(
                    getDoubleTapTargetScale(),
                    event.getX(),
                    event.getY(),
                    DOUBLE_TAP_ZOOM_DURATION
            );
        }
        mIsOneFingerZooming = false;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            zoomImageToPosition(getDoubleTapTargetScale(), e.getX(), e.getY(), DOUBLE_TAP_ZOOM_DURATION);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mIsOneFingerZooming) {
                return true;
            }
            postTranslate(-distanceX, -distanceY);
            return true;
        }

    }

}
