package com.yalantis.ucrop.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isUnspecified
import com.yalantis.ucrop.R
import com.yalantis.ucrop.callback.CropBoundsChangeListener
import com.yalantis.ucrop.callback.OverlayViewChangeListener
import kotlin.math.max
import kotlin.math.roundToInt

class UCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var cropImageView: GestureCropImageView
        private set
    val overlayView: OverlayView

    init {
        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true)
        cropImageView = findViewById(R.id.image_view_crop)
        overlayView = findViewById(R.id.view_overlay)

        val a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView)
        overlayView.processStyledAttributes(a)
        cropImageView.processStyledAttributes(a)
        a.recycle()


        setListenersToViews()
    }

    private fun setListenersToViews() {
        cropImageView.cropBoundsChangeListener =
            CropBoundsChangeListener { cropRatio -> overlayView.setTargetAspectRatio(cropRatio) }
        overlayView.overlayViewChangeListener =
            OverlayViewChangeListener { cropRect -> cropImageView.setCropRect(cropRect) }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    private val density = resources.displayMetrics.density
    private val default = resources.getDimension(R.dimen.ucrop_padding_crop_frame).roundToInt()

    fun setPadding(
        bottomPadding: Dp,
        topPadding: Dp,
        startPadding: Dp,
        endPadding: Dp
    ) {
        val bottom = if (bottomPadding.isUnspecified) {
            default
        } else {
            (density * bottomPadding.value).roundToInt()
        }

        val top = if (topPadding.isUnspecified) {
            default
        } else {
            (density * topPadding.value).roundToInt()
        }

        val left = if (startPadding.isUnspecified) {
            default
        } else {
            (density * startPadding.value).roundToInt()
        }

        val right = if (endPadding.isUnspecified) {
            default
        } else {
            (density * endPadding.value).roundToInt()
        }

        cropImageView.setPadding(
            max(left, right),
            max(top, bottom),
            max(left, right),
            max(top, bottom)
        )
        overlayView.setPadding(
            left,
            top,
            right,
            bottom
        )
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    fun resetCropImageView() {
        removeView(cropImageView)
        cropImageView = GestureCropImageView(context)
        setListenersToViews()
        cropImageView.setCropRect(overlayView.cropViewRect)
        addView(cropImageView, 0)
    }
}