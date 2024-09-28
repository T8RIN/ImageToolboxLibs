/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.t8rin.curves;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;

public class PhotoFilterCurvesControl extends View {

    private final static int curveGranularity = 100;
    private final static int curveDataStep = 2;
    private static final float density = Resources.getSystem().getDisplayMetrics().density;
    private final static int CurvesSegmentNone = 0;
    private final static int CurvesSegmentBlacks = 1;
    private final static int CurvesSegmentShadows = 2;
    private final static int CurvesSegmentMidtones = 3;
    private final static int CurvesSegmentHighlights = 4;
    private final static int CurvesSegmentWhites = 5;
    private final static int GestureStateBegan = 1;
    private final static int GestureStateChanged = 2;
    private final static int GestureStateEnded = 3;
    private final static int GestureStateCancelled = 4;
    private final static int GestureStateFailed = 5;
    private int activeSegment = CurvesSegmentNone;
    private boolean isMoving;
    private boolean checkForMoving = true;
    private float lastX;
    private float lastY;
    private final Rect actualArea = new Rect();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintDash = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCurve = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private PhotoFilterCurvesControlDelegate delegate;
    private final CurvesToolValue curveValue;

    private int lumaCurveColor = 0xffffffff;
    private int redCurveColor = 0xffed3d4c;
    private int greenCurveColor = 0xff10ee9d;
    private int blueCurveColor = 0xff3377fb;

    public PhotoFilterCurvesControl(Context context, CurvesToolValue value) {
        super(context);
        setWillNotDraw(false);

        curveValue = value;

        paint.setColor(0x99ffffff);
        paint.setStrokeWidth(dp(1));
        paint.setStyle(Paint.Style.STROKE);

        paintDash.setColor(0x99ffffff);
        paintDash.setStrokeWidth(dp(2));
        paintDash.setStyle(Paint.Style.STROKE);

        paintCurve.setColor(lumaCurveColor);
        paintCurve.setStrokeWidth(dp(2));
        paintCurve.setStyle(Paint.Style.STROKE);

        textPaint.setColor(0xffbfbfbf);
        textPaint.setTextSize(dp(13));
    }

    public PhotoFilterCurvesControl(Context context) {
        this(context, new CurvesToolValue());
    }

    public void setColors(
            int lumaCurveColor,
            int redCurveColor,
            int greenCurveColor,
            int blueCurveColor
    ) {
        this.lumaCurveColor = lumaCurveColor;
        this.redCurveColor = redCurveColor;
        this.greenCurveColor = greenCurveColor;
        this.blueCurveColor = blueCurveColor;
        invalidate();
    }

    static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public void setDelegate(PhotoFilterCurvesControlDelegate photoFilterCurvesControlDelegate) {
        delegate = photoFilterCurvesControlDelegate;
    }

    //int bitmapX = (int) Math.ceil((viewWidth - bitmapW) / 2 + AndroidUtilities.dp(14));
    //int bitmapY = (int) Math.ceil((viewHeight - bitmapH) / 2 + AndroidUtilities.dp(14) + (Build.VERSION.SDK_INT >= 21 && !inBubbleMode ? AndroidUtilities.statusBarHeight : 0));
    //curvesControl.setActualArea(bitmapX, bitmapY - (Build.VERSION.SDK_INT >= 21 && !inBubbleMode ? AndroidUtilities.statusBarHeight : 0), width, height);
    public void setActualArea(float x, float y, float width, float height) {
        actualArea.x = x;
        actualArea.y = y;
        actualArea.width = width;
        actualArea.height = height;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN: {
                if (event.getPointerCount() == 1) {
                    if (checkForMoving && !isMoving) {
                        float locationX = event.getX();
                        float locationY = event.getY();
                        lastX = locationX;
                        lastY = locationY;
                        if (locationX >= actualArea.x && locationX <= actualArea.x + actualArea.width && locationY >= actualArea.y && locationY <= actualArea.y + actualArea.height) {
                            isMoving = true;
                        }
                        checkForMoving = false;
                        if (isMoving) {
                            handlePan(GestureStateBegan, event);
                        }
                    }
                } else {
                    if (isMoving) {
                        handlePan(GestureStateEnded, event);
                        checkForMoving = true;
                        isMoving = false;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (isMoving) {
                    handlePan(GestureStateEnded, event);
                    isMoving = false;
                }
                checkForMoving = true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isMoving) {
                    handlePan(GestureStateChanged, event);
                }
            }
        }
        return true;
    }

    private void handlePan(int state, MotionEvent event) {
        float locationX = event.getX();
        float locationY = event.getY();

        switch (state) {
            case GestureStateBegan: {
                selectSegmentWithPoint(locationX);
                break;
            }

            case GestureStateChanged: {
                float delta = Math.min(2, (lastY - locationY) / 8.0f);

                CurvesValue curveValue = null;
                switch (this.curveValue.activeType) {
                    case CurvesToolValue.CurvesTypeLuminance:
                        curveValue = this.curveValue.luminanceCurve;
                        break;

                    case CurvesToolValue.CurvesTypeRed:
                        curveValue = this.curveValue.redCurve;
                        break;

                    case CurvesToolValue.CurvesTypeGreen:
                        curveValue = this.curveValue.greenCurve;
                        break;

                    case CurvesToolValue.CurvesTypeBlue:
                        curveValue = this.curveValue.blueCurve;
                        break;

                    default:
                        break;
                }

                switch (activeSegment) {
                    case CurvesSegmentBlacks:
                        curveValue.blacksLevel = Math.max(0, Math.min(100, curveValue.blacksLevel + delta));
                        break;

                    case CurvesSegmentShadows:
                        curveValue.shadowsLevel = Math.max(0, Math.min(100, curveValue.shadowsLevel + delta));
                        break;

                    case CurvesSegmentMidtones:
                        curveValue.midtonesLevel = Math.max(0, Math.min(100, curveValue.midtonesLevel + delta));
                        break;

                    case CurvesSegmentHighlights:
                        curveValue.highlightsLevel = Math.max(0, Math.min(100, curveValue.highlightsLevel + delta));
                        break;

                    case CurvesSegmentWhites:
                        curveValue.whitesLevel = Math.max(0, Math.min(100, curveValue.whitesLevel + delta));
                        break;

                    default:
                        break;
                }

                invalidate();

                if (delegate != null) {
                    delegate.valueChanged();
                }

                lastX = locationX;
                lastY = locationY;
            }
            break;

            case GestureStateEnded:
            case GestureStateCancelled:
            case GestureStateFailed: {
                unselectSegments();
            }
            break;

            default:
                break;
        }
    }

    private void selectSegmentWithPoint(float pointx) {
        if (activeSegment != CurvesSegmentNone) {
            return;
        }
        float segmentWidth = actualArea.width / 5.0f;
        pointx -= actualArea.x;
        activeSegment = (int) Math.floor((pointx / segmentWidth) + 1);
    }

    private void unselectSegments() {
        if (activeSegment == CurvesSegmentNone) {
            return;
        }
        activeSegment = CurvesSegmentNone;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float segmentWidth = actualArea.width / 5.0f;

        for (int i = 0; i < 4; i++) {
            canvas.drawLine(actualArea.x + segmentWidth + i * segmentWidth, actualArea.y, actualArea.x + segmentWidth + i * segmentWidth, actualArea.y + actualArea.height, paint);
        }

        canvas.drawLine(actualArea.x, actualArea.y + actualArea.height, actualArea.x + actualArea.width, actualArea.y, paintDash);

        CurvesValue curvesValue = null;
        switch (curveValue.activeType) {
            case CurvesToolValue.CurvesTypeLuminance:
                paintCurve.setColor(lumaCurveColor);
                curvesValue = curveValue.luminanceCurve;
                break;

            case CurvesToolValue.CurvesTypeRed:
                paintCurve.setColor(redCurveColor);
                curvesValue = curveValue.redCurve;
                break;

            case CurvesToolValue.CurvesTypeGreen:
                paintCurve.setColor(greenCurveColor);
                curvesValue = curveValue.greenCurve;
                break;

            case CurvesToolValue.CurvesTypeBlue:
                paintCurve.setColor(blueCurveColor);
                curvesValue = curveValue.blueCurve;
                break;

            default:
                break;
        }

        for (int a = 0; a < 5; a++) {
            String str;
            switch (a) {
                case 0:
                    str = String.format(Locale.US, "%.2f", curvesValue.blacksLevel / 100.0f);
                    break;
                case 1:
                    str = String.format(Locale.US, "%.2f", curvesValue.shadowsLevel / 100.0f);
                    break;
                case 2:
                    str = String.format(Locale.US, "%.2f", curvesValue.midtonesLevel / 100.0f);
                    break;
                case 3:
                    str = String.format(Locale.US, "%.2f", curvesValue.highlightsLevel / 100.0f);
                    break;
                case 4:
                    str = String.format(Locale.US, "%.2f", curvesValue.whitesLevel / 100.0f);
                    break;
                default:
                    str = "";
                    break;
            }
            float width = textPaint.measureText(str);
            canvas.drawText(str, actualArea.x + (segmentWidth - width) / 2 + segmentWidth * a, actualArea.y + actualArea.height - dp(4), textPaint);
        }

        float[] points = curvesValue.interpolateCurve();
        invalidate();
        path.reset();
        for (int a = 0; a < points.length / 2; a++) {
            if (a == 0) {
                path.moveTo(actualArea.x + points[a * 2] * actualArea.width, actualArea.y + (1.0f - points[a * 2 + 1]) * actualArea.height);
            } else {
                path.lineTo(actualArea.x + points[a * 2] * actualArea.width, actualArea.y + (1.0f - points[a * 2 + 1]) * actualArea.height);
            }
        }

        canvas.drawPath(path, paintCurve);
    }

    public interface PhotoFilterCurvesControlDelegate {
        void valueChanged();
    }

    public static class CurvesValue {

        public float blacksLevel = 0.0f;
        public float shadowsLevel = 25.0f;
        public float midtonesLevel = 50.0f;
        public float highlightsLevel = 75.0f;
        public float whitesLevel = 100.0f;

        public float previousBlacksLevel = 0.0f;
        public float previousShadowsLevel = 25.0f;
        public float previousMidtonesLevel = 50.0f;
        public float previousHighlightsLevel = 75.0f;
        public float previousWhitesLevel = 100.0f;

        public float[] cachedDataPoints;

        public float[] getDataPoints() {
            if (cachedDataPoints == null) {
                interpolateCurve();
            }
            return cachedDataPoints;
        }

        public void saveValues() {
            previousBlacksLevel = blacksLevel;
            previousShadowsLevel = shadowsLevel;
            previousMidtonesLevel = midtonesLevel;
            previousHighlightsLevel = highlightsLevel;
            previousWhitesLevel = whitesLevel;
        }

        public void restoreValues() {
            blacksLevel = previousBlacksLevel;
            shadowsLevel = previousShadowsLevel;
            midtonesLevel = previousMidtonesLevel;
            highlightsLevel = previousHighlightsLevel;
            whitesLevel = previousWhitesLevel;
            interpolateCurve();
        }

        public float[] interpolateCurve() {
            float[] points = new float[]{
                    -0.001f, blacksLevel / 100.0f,
                    0.0f, blacksLevel / 100.0f,
                    0.25f, shadowsLevel / 100.0f,
                    0.5f, midtonesLevel / 100.0f,
                    0.75f, highlightsLevel / 100.0f,
                    1f, whitesLevel / 100.0f,
                    1.001f, whitesLevel / 100.0f
            };

            ArrayList<Float> dataPoints = new ArrayList<>(100);
            ArrayList<Float> interpolatedPoints = new ArrayList<>(100);

            interpolatedPoints.add(points[0]);
            interpolatedPoints.add(points[1]);

            for (int index = 1; index < points.length / 2 - 2; index++) {
                float point0x = points[(index - 1) * 2];
                float point0y = points[(index - 1) * 2 + 1];
                float point1x = points[(index) * 2];
                float point1y = points[(index) * 2 + 1];
                float point2x = points[(index + 1) * 2];
                float point2y = points[(index + 1) * 2 + 1];
                float point3x = points[(index + 2) * 2];
                float point3y = points[(index + 2) * 2 + 1];


                for (int i = 1; i < curveGranularity; i++) {
                    float t = (float) i * (1.0f / (float) curveGranularity);
                    float tt = t * t;
                    float ttt = tt * t;

                    float pix = 0.5f * (2 * point1x + (point2x - point0x) * t + (2 * point0x - 5 * point1x + 4 * point2x - point3x) * tt + (3 * point1x - point0x - 3 * point2x + point3x) * ttt);
                    float piy = 0.5f * (2 * point1y + (point2y - point0y) * t + (2 * point0y - 5 * point1y + 4 * point2y - point3y) * tt + (3 * point1y - point0y - 3 * point2y + point3y) * ttt);

                    piy = Math.max(0, Math.min(1, piy));

                    if (pix > point0x) {
                        interpolatedPoints.add(pix);
                        interpolatedPoints.add(piy);
                    }

                    if ((i - 1) % curveDataStep == 0) {
                        dataPoints.add(piy);
                    }
                }
                interpolatedPoints.add(point2x);
                interpolatedPoints.add(point2y);
            }
            interpolatedPoints.add(points[12]);
            interpolatedPoints.add(points[13]);

            cachedDataPoints = new float[dataPoints.size()];
            for (int a = 0; a < cachedDataPoints.length; a++) {
                cachedDataPoints[a] = dataPoints.get(a);
            }
            float[] retValue = new float[interpolatedPoints.size()];
            for (int a = 0; a < retValue.length; a++) {
                retValue[a] = interpolatedPoints.get(a);
            }
            return retValue;
        }

        public boolean isDefault() {
            return Math.abs(blacksLevel - 0) < 0.00001 && Math.abs(shadowsLevel - 25) < 0.00001 && Math.abs(midtonesLevel - 50) < 0.00001 && Math.abs(highlightsLevel - 75) < 0.00001 && Math.abs(whitesLevel - 100) < 0.00001;
        }
    }

    public static class CurvesToolValue {

        public final static int CurvesTypeLuminance = 0;
        public final static int CurvesTypeRed = 1;
        public final static int CurvesTypeGreen = 2;
        public final static int CurvesTypeBlue = 3;
        public CurvesValue luminanceCurve = new CurvesValue();
        public CurvesValue redCurve = new CurvesValue();
        public CurvesValue greenCurve = new CurvesValue();
        public CurvesValue blueCurve = new CurvesValue();
        public ByteBuffer curveBuffer;
        public int activeType = CurvesTypeLuminance;

        public CurvesToolValue() {
            curveBuffer = ByteBuffer.allocateDirect(200 * 4);
            curveBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        public void fillBuffer() {
            curveBuffer.position(0);
            float[] luminanceCurveData = luminanceCurve.getDataPoints();
            float[] redCurveData = redCurve.getDataPoints();
            float[] greenCurveData = greenCurve.getDataPoints();
            float[] blueCurveData = blueCurve.getDataPoints();
            for (int a = 0; a < 200; a++) {
                curveBuffer.put((byte) (redCurveData[a] * 255));
                curveBuffer.put((byte) (greenCurveData[a] * 255));
                curveBuffer.put((byte) (blueCurveData[a] * 255));
                curveBuffer.put((byte) (luminanceCurveData[a] * 255));
            }
            curveBuffer.position(0);
        }

        public boolean shouldBeSkipped() {
            return luminanceCurve.isDefault() && redCurve.isDefault() && greenCurve.isDefault() && blueCurve.isDefault();
        }
    }
}