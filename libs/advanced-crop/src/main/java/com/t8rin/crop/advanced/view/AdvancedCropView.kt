package com.t8rin.crop.advanced.view

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isUnspecified
import com.t8rin.crop.advanced.R
import com.t8rin.crop.advanced.callback.CropBoundsChangeListener
import com.t8rin.crop.advanced.callback.OverlayViewChangeListener
import kotlin.math.max
import kotlin.math.roundToInt

class AdvancedCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var cropImageView: GestureCropImageView
        private set
    val overlayView: OverlayView
    internal var imageInputUri: Uri? = null
    internal var imageOutputUri: Uri? = null
    internal var onTransformationStart: (() -> Unit)? = null
    internal var onTransformationEnd: (() -> Unit)? = null
    internal var transformationTrackingEnabled = false
    private var isTransformationInProgress = false
    private var isTouchInProgress = false
    private var isRestoringState = false
    private val finishTransformation = Runnable {
        if (isTransformationInProgress) {
            isTransformationInProgress = false
            onTransformationEnd?.invoke()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.advanced_crop_view, this, true)
        cropImageView = findViewById(R.id.image_view_crop)
        overlayView = findViewById(R.id.view_overlay)

        val a = context.obtainStyledAttributes(attrs, R.styleable.AdvancedCropView)
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
        overlayView.overlayViewTouchListener =
            OverlayViewTouchListener { motionEvent -> cropImageView.onTouchEvent(motionEvent) }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (
            event.actionMasked == MotionEvent.ACTION_DOWN &&
            transformationTrackingEnabled &&
            !isRestoringState
        ) {
            isTouchInProgress = true
            removeCallbacks(finishTransformation)
            if (!isTransformationInProgress) {
                isTransformationInProgress = true
                onTransformationStart?.invoke()
            }
        }
        val handled = super.dispatchTouchEvent(event)
        if (
            event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            isTouchInProgress = false
            if (isTransformationInProgress) {
                postDelayed(finishTransformation, 300L)
            }
        }
        return handled
    }

    internal fun captureState(): AdvancedCropViewState {
        val matrixValues = FloatArray(9)
        cropImageView.currentImageMatrix.getValues(matrixValues)
        return AdvancedCropViewState(
            imageMatrixValues = matrixValues,
            cropRect = RectF(overlayView.cropViewRect),
            sourceRotationDegrees = cropImageView.sourceRotationDegrees,
            isFlippedHorizontally = cropImageView.isImageFlipHorizontally,
            viewWidth = width,
            viewHeight = height
        )
    }

    internal fun restoreState(state: AdvancedCropViewState) {
        val adjustedState = state.adjustedTo(width, height)
        isRestoringState = true
        try {
            cropImageView.apply {
                cancelAllAnimations()
                sourceRotationDegrees = adjustedState.sourceRotationDegrees
                setImageFlipHorizontally(adjustedState.isFlippedHorizontally)
            }
            overlayView.setCropViewRect(adjustedState.cropRect)
            cropImageView.apply {
                cancelAllAnimations()
                imageMatrix = Matrix().apply {
                    setValues(adjustedState.imageMatrixValues)
                }
            }
        } finally {
            isRestoringState = false
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(finishTransformation)
        isTouchInProgress = false
        isTransformationInProgress = false
        super.onDetachedFromWindow()
    }

    private val density = resources.displayMetrics.density
    private val default =
        resources.getDimension(R.dimen.advanced_crop_padding_crop_frame).roundToInt()

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
     * Method for reset state for crop image view such as rotation, scale, translation.
     * Be careful: this method recreates crop image view instance and reattaches it to layout.
     */
    fun resetCropImageView() {
        removeView(cropImageView)
        cropImageView = GestureCropImageView(context)
        setListenersToViews()
        cropImageView.setCropRect(overlayView.cropViewRect)
        addView(cropImageView, 0)
    }
}

internal fun interface OverlayViewTouchListener {
    operator fun invoke(motionEvent: MotionEvent)
}

internal data class AdvancedCropViewState(
    val imageMatrixValues: FloatArray,
    val cropRect: RectF,
    val sourceRotationDegrees: Int,
    val isFlippedHorizontally: Boolean,
    val viewWidth: Int,
    val viewHeight: Int
) {
    fun hasSameStateAs(other: AdvancedCropViewState): Boolean {
        return imageMatrixValues.contentEquals(other.imageMatrixValues) &&
                cropRect == other.cropRect &&
                sourceRotationDegrees == other.sourceRotationDegrees &&
                isFlippedHorizontally == other.isFlippedHorizontally
    }

    fun adjustedTo(width: Int, height: Int): AdvancedCropViewState {
        if (
            viewWidth <= 0 ||
            viewHeight <= 0 ||
            width <= 0 ||
            height <= 0 ||
            viewWidth == width && viewHeight == height
        ) {
            return this
        }

        val scale = minOf(
            width.toFloat() / viewWidth,
            height.toFloat() / viewHeight
        )
        val offsetX = (width - viewWidth * scale) / 2f
        val offsetY = (height - viewHeight * scale) / 2f
        val adjustedMatrixValues = imageMatrixValues.copyOf().apply {
            this[Matrix.MSCALE_X] *= scale
            this[Matrix.MSKEW_X] *= scale
            this[Matrix.MTRANS_X] = this[Matrix.MTRANS_X] * scale + offsetX
            this[Matrix.MSKEW_Y] *= scale
            this[Matrix.MSCALE_Y] *= scale
            this[Matrix.MTRANS_Y] = this[Matrix.MTRANS_Y] * scale + offsetY
        }
        val adjustedCropRect = RectF(
            cropRect.left * scale + offsetX,
            cropRect.top * scale + offsetY,
            cropRect.right * scale + offsetX,
            cropRect.bottom * scale + offsetY
        )
        return copy(
            imageMatrixValues = adjustedMatrixValues,
            cropRect = adjustedCropRect,
            viewWidth = width,
            viewHeight = height
        )
    }
}
