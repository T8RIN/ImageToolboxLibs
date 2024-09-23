/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.yalantis.ucrop.view;

import static com.yalantis.ucrop.view.CropRotationWheel.dp;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.StateSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Theme {

    public static final int ACTION_BAR_WHITE_SELECTOR_COLOR = 0x40ffffff;
    public static final int RIPPLE_MASK_CIRCLE_20DP = 1;
    public static final int RIPPLE_MASK_ALL = 2;
    public static final int RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE = 3;
    public static final int RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4;
    public static final int RIPPLE_MASK_CIRCLE_AUTO = 5;
    public static final int RIPPLE_MASK_ROUNDRECT_6DP = 7;
    private static final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static Drawable getSelectorDrawable(int color, int backgroundColor) {
        if (backgroundColor >= 0) {
            Drawable maskDrawable = new ColorDrawable(0xffffffff);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
            );
            return new RippleDrawableSafe(colorStateList, new ColorDrawable(backgroundColor), maskDrawable);
        } else {
            return createSelectorDrawable(color, 2);
        }
    }

    public static Drawable createSelectorDrawable(int color) {
        return createSelectorDrawable(color, RIPPLE_MASK_CIRCLE_20DP, -1);
    }

    public static Drawable createSelectorDrawable(int color, int maskType) {
        return createSelectorDrawable(color, maskType, -1);
    }

    public static Drawable createSelectorDrawable(int color, int maskType, int radius) {
        if (Build.VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = null;
            if ((maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == 5) && Build.VERSION.SDK_INT >= 23) {
                maskDrawable = null;
            } else if (
                    maskType == RIPPLE_MASK_CIRCLE_20DP ||
                            maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE ||
                            maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER ||
                            maskType == RIPPLE_MASK_CIRCLE_AUTO ||
                            maskType == 6 ||
                            maskType == RIPPLE_MASK_ROUNDRECT_6DP
            ) {
                maskPaint.setColor(0xffffffff);
                maskDrawable = new Drawable() {

                    RectF rect;

                    @Override
                    public void draw(Canvas canvas) {
                        Rect bounds = getBounds();
                        if (maskType == RIPPLE_MASK_ROUNDRECT_6DP) {
                            if (rect == null) {
                                rect = new RectF();
                            }
                            rect.set(bounds);
                            float rad = radius <= 0 ? dp(6) : radius;
                            canvas.drawRoundRect(rect, rad, rad, maskPaint);
                        } else {
                            int rad;
                            if (maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == 6) {
                                rad = radius <= 0 ? dp(20) : radius;
                            } else if (maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE) {
                                rad = (Math.max(bounds.width(), bounds.height()) / 2);
                            } else {
                                // RIPPLE_MASK_CIRCLE_AUTO = 5
                                // RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4
                                rad = (int) Math.ceil(Math.sqrt((bounds.left - bounds.centerX()) * (bounds.left - bounds.centerX()) + (bounds.top - bounds.centerY()) * (bounds.top - bounds.centerY())));
                            }
                            canvas.drawCircle(bounds.centerX(), bounds.centerY(), rad, maskPaint);
                        }
                    }

                    @Override
                    public void setAlpha(int alpha) {

                    }

                    @Override
                    public void setColorFilter(ColorFilter colorFilter) {

                    }

                    @Override
                    public int getOpacity() {
                        return PixelFormat.UNKNOWN;
                    }
                };
            } else if (maskType == RIPPLE_MASK_ALL) {
                maskDrawable = new ColorDrawable(0xffffffff);
            }
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
            );
            RippleDrawable rippleDrawable = new RippleDrawableSafe(colorStateList, null, maskDrawable);
            if (Build.VERSION.SDK_INT >= 23) {
                if (maskType == RIPPLE_MASK_CIRCLE_20DP) {
                    rippleDrawable.setRadius(radius <= 0 ? dp(20) : radius);
                } else if (maskType == RIPPLE_MASK_CIRCLE_AUTO) {
                    rippleDrawable.setRadius(RippleDrawable.RADIUS_AUTO);
                }
            }
            return rippleDrawable;
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0x00000000));
            return stateListDrawable;
        }
    }

    public static class RippleDrawableSafe extends RippleDrawable {
        public RippleDrawableSafe(@NonNull ColorStateList color, @Nullable Drawable content, @Nullable Drawable mask) {
            super(color, content, mask);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            try {
                super.draw(canvas);
            } catch (Exception e) {

            }
        }
    }
}
