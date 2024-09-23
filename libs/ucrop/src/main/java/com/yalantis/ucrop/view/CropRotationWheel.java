package com.yalantis.ucrop.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.yalantis.ucrop.R;

import java.util.Locale;

public class CropRotationWheel extends FrameLayout {

    private static final int MAX_ANGLE = 45;
    private static final int DELTA_ANGLE = 5;
    private static final float density = Resources.getSystem().getDisplayMetrics().density;
    protected float rotation;
    private final Paint whitePaint;
    private final Paint bluePaint;
    private final ImageView rotation90Button;
    private final ImageView mirrorButton;
    private String degreesText;
    private final TextPaint degreesTextPaint;
    private final RectF tempRect;
    private float prevX;

    private RotationWheelListener rotationListener;
    private CustomHapticFeedback haptics = new CustomHapticFeedback(0, this);

    public CropRotationWheel(Context context) {
        super(context);

        tempRect = new RectF(0, 0, 0, 0);

        whitePaint = new Paint();
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setAlpha(255);
        whitePaint.setAntiAlias(true);

        bluePaint = new Paint();
        bluePaint.setStyle(Paint.Style.FILL);
        bluePaint.setColor(0xff51bdf3);
        bluePaint.setAlpha(255);
        bluePaint.setAntiAlias(true);

        mirrorButton = new ImageView(context);
        mirrorButton.setImageResource(R.drawable.msg_photo_flip);
        mirrorButton.setImageTintList(ColorStateList.valueOf(whitePaint.getColor()));
        mirrorButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR));
        mirrorButton.setScaleType(ImageView.ScaleType.CENTER);
        mirrorButton.setOnClickListener(v -> {
            rotationListener.mirror();
        });
        mirrorButton.setOnLongClickListener(v -> {
            return true;
        });
        addView(mirrorButton, createFrame(70, 64, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        rotation90Button = new ImageView(context);
        rotation90Button.setImageResource(R.drawable.msg_photo_rotate);
        rotation90Button.setImageTintList(ColorStateList.valueOf(whitePaint.getColor()));
        rotation90Button.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR));
        rotation90Button.setScaleType(ImageView.ScaleType.CENTER);
        rotation90Button.setOnClickListener(v -> {
            if (rotationListener != null) {
                rotationListener.rotate90Pressed();
            }
        });
        addView(rotation90Button, createFrame(70, 64, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        degreesTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        degreesTextPaint.setColor(Color.WHITE);
        degreesTextPaint.setTextSize(dp(14));

        setWillNotDraw(false);

        setRotation(0.0f, false);
    }

    public CropRotationWheel(Context context, int hapticsStrength) {
        this(context);
        haptics = new CustomHapticFeedback(hapticsStrength, this);
    }

    static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(MeasureSpec.makeMeasureSpec(Math.min(width, dp(400)), MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    public void setListener(RotationWheelListener listener) {
        rotationListener = listener;
    }

    public void setRotation(float rotation, boolean animated) {
        this.rotation = rotation;
        float value = rotation;
        if (Math.abs(value) < 0.1 - 0.001)
            value = Math.abs(value);
        degreesText = String.format(Locale.getDefault(), "%.1fº", value);

        invalidate();
    }

    public float getRotation() {
        return this.rotation;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        float x = ev.getX();

        if (action == MotionEvent.ACTION_DOWN) {
            prevX = x;

            if (rotationListener != null) {
                rotationListener.onStart();
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (rotationListener != null)
                rotationListener.onEnd(this.rotation);
        } else if (action == MotionEvent.ACTION_MOVE) {
            float delta = prevX - x;

            float newAngle = this.rotation + (float) (delta / density / Math.PI / 1.65f);
            newAngle = Math.max(-MAX_ANGLE, Math.min(MAX_ANGLE, newAngle));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                try {
                    if (Math.abs(newAngle - MAX_ANGLE) < 0.001f && Math.abs(this.rotation - MAX_ANGLE) >= 0.001f ||
                            Math.abs(newAngle - -MAX_ANGLE) < 0.001f && Math.abs(this.rotation - -MAX_ANGLE) >= 0.001f) {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    } else if (Math.floor(this.rotation / 2.5f) != Math.floor(newAngle / 2.5f)) {
                        haptics.performHapticFeedback(1);
                    }
                } catch (Exception ignore) {
                }
            }

            if (Math.abs(newAngle - this.rotation) > 0.001) {
                if (Math.abs(newAngle) < 0.05)
                    newAngle = 0;

                setRotation(newAngle, false);

                if (rotationListener != null) {
                    rotationListener.onChange(this.rotation);
                }

                prevX = x;
            }
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float angle = -rotation * 2;
        float delta = angle % DELTA_ANGLE;
        int segments = (int) Math.floor(angle / DELTA_ANGLE);

        for (int i = 0; i < 16; i++) {
            Paint paint = whitePaint;
            int a = i;
            if (a < segments || (a == 0 && delta < 0))
                paint = bluePaint;

            drawLine(canvas, a, delta, width, height, (a == segments || a == 0 && segments == -1), paint);

            if (i != 0) {
                a = -i;
                paint = a > segments ? bluePaint : whitePaint;
                drawLine(canvas, a, delta, width, height, a == segments + 1, paint);
            }
        }

        bluePaint.setAlpha(255);

        tempRect.left = (width - dp(2.5f)) / 2;
        tempRect.top = (height - dp(22)) / 2;
        tempRect.right = (width + dp(2.5f)) / 2;
        tempRect.bottom = (height + dp(22)) / 2;
        canvas.drawRoundRect(tempRect, dp(2), dp(2), bluePaint);

        float tx = (width - degreesTextPaint.measureText(degreesText)) / 2;
        float ty = dp(14);
        canvas.drawText(degreesText, tx, ty, degreesTextPaint);
    }

    protected void drawLine(Canvas canvas, int i, float delta, int width, int height, boolean center, Paint paint) {
        int radius = (int) (width / 2.0f - dp(70));

        float angle = 90 - (i * DELTA_ANGLE + delta);
        int val = (int) (radius * Math.cos(Math.toRadians(angle)));
        int x = width / 2 + val;

        float f = Math.abs(val) / (float) radius;
        int alpha = Math.min(255, Math.max(0, (int) ((1.0f - f * f) * 255)));

        if (center)
            paint = bluePaint;

        paint.setAlpha(alpha);

        int w = center ? 4 : 2;
        int h = center ? dp(16) : dp(12);

        canvas.drawRect(x - w / 2, (height - h) / 2, x + w / 2, (height + h) / 2, paint);
    }

    public void setCenterLineColor(int color) {
        bluePaint.setColor(color);
        invalidate();
    }

    public void setSideLineColor(int color) {
        whitePaint.setColor(color);
        mirrorButton.setImageTintList(ColorStateList.valueOf(whitePaint.getColor()));
        rotation90Button.setImageTintList(ColorStateList.valueOf(whitePaint.getColor()));
        invalidate();
    }

    public void setHapticsStrength(int hapticsStrength) {
        haptics = new CustomHapticFeedback(hapticsStrength, this);
    }

    public FrameLayout.LayoutParams createFrame(int width, int height, int gravity) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
    }

    private int getSize(float size) {
        return (int) (size < 0 ? size : dp(size));
    }


    public interface RotationWheelListener {
        void onStart();

        void onChange(float angle);

        void onEnd(float angle);

        void rotate90Pressed();

        void mirror();
    }

}
