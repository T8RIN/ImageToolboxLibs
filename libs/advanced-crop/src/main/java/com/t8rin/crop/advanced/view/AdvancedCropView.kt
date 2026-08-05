package com.t8rin.crop.advanced.view

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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
    internal var layoutStateProvider: (() -> AdvancedCropViewState?)? = null
    internal var transformationTrackingEnabled = false
    private var isTransformationInProgress = false
    private var isTouchInProgress = false
    private var isRestoringState = false
    private var isLayoutRestorePending = false
    private var sizeChangeRestoreVersion = 0
    private var pendingSizeChangeState: AdvancedCropViewState? = null
    private val finishTransformation = Runnable {
        if (isTransformationInProgress) {
            isTransformationInProgress = false
            onTransformationEnd?.invoke()
        }
    }

    internal val restoringState: Boolean
        get() = isRestoringState || isLayoutRestorePending

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

    internal fun captureState(
        viewWidth: Int = width,
        viewHeight: Int = height
    ): AdvancedCropViewState {
        if (viewWidth == width && viewHeight == height) {
            pendingSizeChangeState?.let { return it }
        }
        return captureCurrentState(viewWidth, viewHeight)
    }

    private fun captureCurrentState(
        viewWidth: Int,
        viewHeight: Int
    ): AdvancedCropViewState {
        val matrixValues = FloatArray(9)
        cropImageView.currentImageMatrix.getValues(matrixValues)
        val overlayCropRect = RectF(overlayView.cropViewRect)
        return AdvancedCropViewState(
            imageMatrixValues = matrixValues,
            overlayCropRect = overlayCropRect,
            overlayBounds = overlaySafeBounds(viewWidth, viewHeight),
            imageCropRect = overlayCropRect.toImageCropRect(),
            sourceRotationDegrees = cropImageView.sourceRotationDegrees,
            isFlippedHorizontally = cropImageView.isImageFlipHorizontally,
            viewWidth = viewWidth,
            viewHeight = viewHeight
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val stateBeforeSizeChange = if (
            oldw > 0 &&
            oldh > 0 &&
            w > 0 &&
            h > 0 &&
            !isRestoringState &&
            overlayView.cropViewRect.width() > 0f &&
            overlayView.cropViewRect.height() > 0f
        ) {
            // onSizeChanged is invoked after this View receives the new width/height, but the
            // children still contain the old crop geometry. Capture that actual geometry with
            // explicit old bounds: the externally stored snapshot can be stale while a gesture
            // or an image rotation is still being committed.
            captureCurrentState(viewWidth = oldw, viewHeight = oldh)
        } else {
            null
        }
        super.onSizeChanged(w, h, oldw, oldh)

        pendingSizeChangeState = stateBeforeSizeChange
        val hasValidSizeChange = oldw > 0 && oldh > 0 && w > 0 && h > 0
        if (hasValidSizeChange && stateBeforeSizeChange != null) {
            // Keep the previous frame on screen until the new geometry is restored. Making the
            // view transparent here removes the default-frame flash but creates a visible blink.
            isLayoutRestorePending = true
        } else if (!hasValidSizeChange) {
            isLayoutRestorePending = false
        }
        val restoreVersion = ++sizeChangeRestoreVersion
        stateBeforeSizeChange?.takeIf { hasValidSizeChange }?.let { state ->
            // A parent layout-change callback runs after both child views have laid themselves
            // out. Restoring here prevents the children from exposing their default crop matrix
            // for a frame, which can still happen when restoration is deferred to pre-draw.
            addOnLayoutChangeListener(
                object : OnLayoutChangeListener {
                    override fun onLayoutChange(
                        view: View,
                        left: Int,
                        top: Int,
                        right: Int,
                        bottom: Int,
                        oldLeft: Int,
                        oldTop: Int,
                        oldRight: Int,
                        oldBottom: Int
                    ) {
                        removeOnLayoutChangeListener(this)
                        if (
                            restoreVersion == sizeChangeRestoreVersion &&
                            width == w &&
                            height == h
                        ) {
                            restoreState(state)
                            isLayoutRestorePending = false
                        }
                    }
                }
            )
        }
    }

    internal fun restoreState(state: AdvancedCropViewState) {
        pendingSizeChangeState = null
        sizeChangeRestoreVersion++
        val safeOverlayBounds = overlaySafeBounds()
        val canRestoreExactOverlay =
            state.overlayBounds == safeOverlayBounds &&
                state.overlayCropRect.width() > 0f &&
                state.overlayCropRect.height() > 0f &&
                safeOverlayBounds.contains(state.overlayCropRect)
        val targetOverlayCropRect = if (canRestoreExactOverlay) {
            RectF(state.overlayCropRect)
        } else {
            state.overlayCropRect.mappedFrom(
                sourceBounds = state.overlayBounds,
                targetBounds = safeOverlayBounds
            ) ?: fittedOverlayCropRect(
                state.overlayCropRect.width() /
                        state.overlayCropRect.height().coerceAtLeast(1f)
            )
        }
        val targetImageCropRect = targetOverlayCropRect.toImageCropRect()
        val adjustedState = state.adjustedTo(
            width = width,
            height = height,
            targetOverlayCropRect = targetOverlayCropRect,
            targetImageCropRect = targetImageCropRect
        )
        isRestoringState = true
        try {
            cropImageView.apply {
                cancelAllAnimations()
                sourceRotationDegrees = adjustedState.sourceRotationDegrees
                setImageFlipHorizontally(adjustedState.isFlippedHorizontally)
            }
            overlayView.setCropViewRect(adjustedState.overlayCropRect)
            cropImageView.apply {
                cancelAllAnimations()
                imageMatrix = Matrix().apply {
                    setValues(adjustedState.imageMatrixValues)
                }
                setImageToWrapCropBounds(false)
            }
        } finally {
            isRestoringState = false
        }
    }

    private fun overlaySafeBounds(
        viewWidth: Int = width,
        viewHeight: Int = height
    ) = RectF(
        overlayView.paddingLeft.toFloat(),
        overlayView.paddingTop.toFloat(),
        (viewWidth - overlayView.paddingRight).toFloat(),
        (viewHeight - overlayView.paddingBottom).toFloat()
    )

    private fun fittedOverlayCropRect(aspectRatio: Float): RectF {
        val bounds = overlaySafeBounds()
        if (
            bounds.width() <= 0f ||
            bounds.height() <= 0f ||
            !aspectRatio.isFinite() ||
            aspectRatio <= 0f
        ) {
            return RectF(overlayView.cropViewRect)
        }

        var cropWidth = bounds.width()
        var cropHeight = cropWidth / aspectRatio
        if (cropHeight > bounds.height()) {
            cropHeight = bounds.height()
            cropWidth = cropHeight * aspectRatio
        }
        return RectF(
            bounds.centerX() - cropWidth / 2f,
            bounds.centerY() - cropHeight / 2f,
            bounds.centerX() + cropWidth / 2f,
            bounds.centerY() + cropHeight / 2f
        )
    }

    private fun RectF.mappedFrom(
        sourceBounds: RectF,
        targetBounds: RectF
    ): RectF? {
        if (
            width() <= 0f ||
            height() <= 0f ||
            sourceBounds.width() <= 0f ||
            sourceBounds.height() <= 0f ||
            targetBounds.width() <= 0f ||
            targetBounds.height() <= 0f ||
            !sourceBounds.contains(this)
        ) {
            return null
        }

        val aspectRatio = width() / height()
        if (!aspectRatio.isFinite() || aspectRatio <= 0f) return null

        // Preserve the frame width relative to the container. Scaling by the largest fitted
        // frame makes a tall crop collapse on portrait -> landscape because the fitted frame's
        // width is constrained by the shorter landscape height.
        val widthFraction = (width() / sourceBounds.width()).coerceIn(0f, 1f)
        val targetWidth = minOf(
            targetBounds.width() * widthFraction,
            targetBounds.height() * aspectRatio
        )
        val targetHeight = targetWidth / aspectRatio

        val centerXFraction = (centerX() - sourceBounds.left) / sourceBounds.width()
        val centerYFraction = (centerY() - sourceBounds.top) / sourceBounds.height()
        val targetCenterX = targetBounds.left + targetBounds.width() * centerXFraction
        val targetCenterY = targetBounds.top + targetBounds.height() * centerYFraction
        val targetLeft = (targetCenterX - targetWidth / 2f).coerceIn(
            targetBounds.left,
            targetBounds.right - targetWidth
        )
        val targetTop = (targetCenterY - targetHeight / 2f).coerceIn(
            targetBounds.top,
            targetBounds.bottom - targetHeight
        )

        return RectF(
            targetLeft,
            targetTop,
            targetLeft + targetWidth,
            targetTop + targetHeight
        )
    }

    private fun RectF.toImageCropRect() = RectF(
        left - cropImageView.paddingLeft,
        top - cropImageView.paddingTop,
        right - cropImageView.paddingRight,
        bottom - cropImageView.paddingBottom
    )

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
    val overlayCropRect: RectF,
    val overlayBounds: RectF,
    val imageCropRect: RectF,
    val sourceRotationDegrees: Int,
    val isFlippedHorizontally: Boolean,
    val viewWidth: Int,
    val viewHeight: Int
) {
    fun hasSameStateAs(other: AdvancedCropViewState): Boolean {
        return imageMatrixValues.contentEquals(other.imageMatrixValues) &&
                overlayCropRect == other.overlayCropRect &&
                imageCropRect == other.imageCropRect &&
                sourceRotationDegrees == other.sourceRotationDegrees &&
                isFlippedHorizontally == other.isFlippedHorizontally
    }

    fun adjustedTo(
        width: Int,
        height: Int,
        targetOverlayCropRect: RectF,
        targetImageCropRect: RectF
    ): AdvancedCropViewState {
        if (
            viewWidth <= 0 ||
            viewHeight <= 0 ||
            width <= 0 ||
            height <= 0 ||
            imageCropRect.width() <= 0f ||
            imageCropRect.height() <= 0f ||
            targetImageCropRect.width() <= 0f ||
            targetImageCropRect.height() <= 0f ||
            !targetOverlayCropRect.width().isFinite() ||
            !targetOverlayCropRect.height().isFinite()
        ) {
            return this
        }
        if (
            viewWidth == width &&
            viewHeight == height &&
            overlayCropRect == targetOverlayCropRect &&
            imageCropRect == targetImageCropRect
        ) {
            return this
        }

        val scale = minOf(
            targetImageCropRect.width() / imageCropRect.width(),
            targetImageCropRect.height() / imageCropRect.height()
        )
        val offsetX = targetImageCropRect.centerX() - imageCropRect.centerX() * scale
        val offsetY = targetImageCropRect.centerY() - imageCropRect.centerY() * scale
        val adjustedMatrixValues = imageMatrixValues.copyOf().apply {
            this[Matrix.MSCALE_X] *= scale
            this[Matrix.MSKEW_X] *= scale
            this[Matrix.MTRANS_X] = this[Matrix.MTRANS_X] * scale + offsetX
            this[Matrix.MSKEW_Y] *= scale
            this[Matrix.MSCALE_Y] *= scale
            this[Matrix.MTRANS_Y] = this[Matrix.MTRANS_Y] * scale + offsetY
        }
        return copy(
            imageMatrixValues = adjustedMatrixValues,
            overlayCropRect = RectF(targetOverlayCropRect),
            imageCropRect = RectF(targetImageCropRect),
            viewWidth = width,
            viewHeight = height
        )
    }
}
