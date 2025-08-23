package com.t8rin.collages.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnDragListener
import android.widget.RelativeLayout
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import com.t8rin.collages.utils.ImageDecoder
import com.t8rin.collages.utils.ImageUtils
import com.t8rin.collages.utils.Handle
import com.t8rin.collages.utils.HandleUtils

@SuppressLint("ViewConstructor")
internal class FramePhotoLayout(
    context: Context,
    var mPhotoItems: List<PhotoItem>
) : RelativeLayout(context), FrameImageView.OnImageClickListener {

    private var mOnDragListener: OnDragListener = OnDragListener { v, event ->
        if (event.action == DragEvent.ACTION_DROP) {
            var target: FrameImageView? = v as FrameImageView
            val selectedView = getSelectedFrameImageView(target!!, event)
            if (selectedView != null) {
                target = selectedView
                val dragged = event.localState as FrameImageView
                var targetPath: Uri? = target.photoItem.imagePath
                var draggedPath: Uri? = dragged.photoItem.imagePath
                if (targetPath == null) targetPath = Uri.EMPTY
                if (draggedPath == null) draggedPath = Uri.EMPTY
                if (targetPath != draggedPath) target.swapImage(dragged)
            }
        }

        true
    }
    private val mItemImageViews: MutableList<FrameImageView> = ArrayList()
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mOutputScaleRatio = 1f
    private var backgroundColor: ComposeColor = ComposeColor.White
    private var onItemTapListener: ((index: Int, uri: Uri?) -> Unit)? = null

    // Handle overlay state
    private var selectedItemIndex: Int? = null
    private var activeHandle: Handle? = null
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(255, 165, 0)
        style = Paint.Style.FILL
    }
    private val handleTouchRadiusPx = 36f

    private val isNotLargeThan1Gb: Boolean
        get() {
            val memoryInfo = ImageUtils.getMemoryInfo(context)
            return memoryInfo.totalMem > 0 && memoryInfo.totalMem / 1048576.0 <= 1024
        }

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setWillNotDraw(false)
    }

    private fun getSelectedFrameImageView(
        target: FrameImageView,
        event: DragEvent
    ): FrameImageView? {
        val dragged = event.localState as FrameImageView
        val leftMargin = (mViewWidth * target.photoItem.bound.left).toInt()
        val topMargin = (mViewHeight * target.photoItem.bound.top).toInt()
        val globalX = leftMargin + event.x
        val globalY = topMargin + event.y
        for (idx in mItemImageViews.indices.reversed()) {
            val view = mItemImageViews[idx]
            val x = globalX - mViewWidth * view.photoItem.bound.left
            val y = globalY - mViewHeight * view.photoItem.bound.top
            if (view.isSelected(x, y)) {
                return if (view === dragged) {
                    null
                } else {
                    view
                }
            }
        }
        return null
    }

    fun saveInstanceState(outState: Bundle) {
        for (view in mItemImageViews)
            view.saveInstanceState(outState)
    }

    fun restoreInstanceState(savedInstanceState: Bundle) {
        for (view in mItemImageViews)
            view.restoreInstanceState(savedInstanceState)
    }

    @JvmOverloads
    fun build(
        viewWidth: Int,
        viewHeight: Int,
        outputScaleRatio: Float,
        space: Float = 0f,
        corner: Float = 0f
    ) {
        mItemImageViews.clear()
        removeAllViews()
        if (viewWidth < 1 || viewHeight < 1) {
            return
        }

        setBackgroundColor(backgroundColor.toArgb())

        //add children views
        mViewWidth = viewWidth
        mViewHeight = viewHeight
        mOutputScaleRatio = outputScaleRatio
        mItemImageViews.clear()
        //A circle view always is on top
        if (mPhotoItems.size > 4 || isNotLargeThan1Gb) {
            ImageDecoder.SAMPLER_SIZE = 1024
        } else {
            ImageDecoder.SAMPLER_SIZE = 1600
        }
        for (item in mPhotoItems) {
            val imageView = addPhotoItemView(item, mOutputScaleRatio, space, corner)
            mItemImageViews.add(imageView)
        }
    }

    fun setBackgroundColor(color: ComposeColor) {
        backgroundColor = color
        setBackgroundColor(backgroundColor.toArgb())
        invalidate()
    }

    fun setOnItemTapListener(listener: ((index: Int, uri: Uri?) -> Unit)?) {
        onItemTapListener = listener
    }

    fun setSpace(space: Float, corner: Float) {
        for (img in mItemImageViews)
            img.setSpace(space, corner)
    }

    private fun addPhotoItemView(
        item: PhotoItem,
        outputScaleRatio: Float,
        space: Float,
        corner: Float
    ): FrameImageView {
        val imageView = FrameImageView(context, item)
        val leftMargin = (mViewWidth * item.bound.left).toInt()
        val topMargin = (mViewHeight * item.bound.top).toInt()
        val frameWidth: Int = if (item.bound.right == 1f) {
            mViewWidth - leftMargin
        } else {
            (mViewWidth * item.bound.width() + 0.5f).toInt()
        }

        val frameHeight: Int = if (item.bound.bottom == 1f) {
            mViewHeight - topMargin
        } else {
            (mViewHeight * item.bound.height() + 0.5f).toInt()
        }

        imageView.init(frameWidth.toFloat(), frameHeight.toFloat(), outputScaleRatio, space, corner)
        imageView.setOnImageClickListener(this)
        imageView.setOnTouchListener { _, event ->
            // Intercept to support handle dragging overlay
            onTouchEvent(event)
        }
        if (mPhotoItems.size > 1)
            imageView.setOnDragListener(mOnDragListener)

        val params = LayoutParams(frameWidth, frameHeight)
        params.leftMargin = leftMargin
        params.topMargin = topMargin
        imageView.originalLayoutParams = params
        addView(imageView, params)
        return imageView
    }

    @Throws(OutOfMemoryError::class)
    fun createImage(): Bitmap {
        try {
            val template = Bitmap.createBitmap(
                (mOutputScaleRatio * mViewWidth).toInt(),
                (mOutputScaleRatio * mViewHeight).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(template)
            canvas.drawColor(backgroundColor.toArgb())
            for (view in mItemImageViews)
                if (view.image != null && !view.image!!.isRecycled) {
                    val left = (view.left * mOutputScaleRatio).toInt()
                    val top = (view.top * mOutputScaleRatio).toInt()
                    val width = (view.width * mOutputScaleRatio).toInt()
                    val height = (view.height * mOutputScaleRatio).toInt()
                    //draw image
                    canvas.saveLayer(
                        left.toFloat(),
                        top.toFloat(),
                        (left + width).toFloat(),
                        (top + height).toFloat(),
                        Paint()
                    )
                    canvas.translate(left.toFloat(), top.toFloat())
                    canvas.clipRect(0, 0, width, height)
                    view.drawOutputImage(canvas)
                    canvas.restore()
                }

            return template
        } catch (error: OutOfMemoryError) {
            throw error
        }

    }

    override fun onLongClickImage(view: FrameImageView) {
        if (mPhotoItems.size > 1) {
            view.tag = """x=${0f},y=${0f},path=${view.photoItem.imagePath}"""
            val item = ClipData.Item(view.tag as CharSequence)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val dragData = ClipData(view.tag.toString(), mimeTypes, item)
            val myShadow = DragShadowBuilder(view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(dragData, myShadow, view, 0)
            } else {
                @Suppress("DEPRECATION")
                view.startDrag(dragData, myShadow, view, 0)
            }
        }
    }

    override fun onDoubleClickImage(view: FrameImageView) {

    }

    override fun onSingleTapImage(view: FrameImageView) {
        onItemTapListener?.invoke(view.photoItem.index, view.photoItem.imagePath)
        selectedItemIndex = if (selectedItemIndex == view.photoItem.index) {
            activeHandle = null
            null
        } else {
            view.photoItem.index
        }
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        // draw handles for selected item
        val index = selectedItemIndex ?: return
        val itemView = mItemImageViews.firstOrNull { it.photoItem.index == index } ?: return
        val item = itemView.photoItem
        if (item.handles.isEmpty()) return
        for (handle in item.handles) {
            val dp = handle.getDraggablePoint()
            val cx = mViewWidth * dp.x
            val cy = mViewHeight * dp.y
            canvas.drawCircle(cx, cy, handleTouchRadiusPx, handlePaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val index = selectedItemIndex ?: return super.onTouchEvent(event)
        val itemView = mItemImageViews.firstOrNull { it.photoItem.index == index } ?: return super.onTouchEvent(event)
        val item = itemView.photoItem
        if (item.handles.isEmpty()) return super.onTouchEvent(event)

        // Compute coordinates in this layout's local space regardless of source view
        val screenPos = IntArray(2)
        getLocationOnScreen(screenPos)
        val globalX = event.rawX - screenPos[0]
        val globalY = event.rawY - screenPos[1]

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activeHandle = item.handles.firstOrNull { handle ->
                    val dp = handle.getDraggablePoint()
                    val hx = mViewWidth * dp.x
                    val hy = mViewHeight * dp.y
                    val dx = globalX - hx
                    val dy = globalY - hy
                    dx * dx + dy * dy <= handleTouchRadiusPx * handleTouchRadiusPx
                }
                return activeHandle != null || super.onTouchEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                val handle = activeHandle ?: return super.onTouchEvent(event)
                // restrict drag to handle direction by projecting onto normalized direction in global space
                val dirX = handle.direction.x
                val dirY = handle.direction.y
                val dirLen = kotlin.math.sqrt((dirX * dirX + dirY * dirY).toDouble()).toFloat().let { if (it == 0f) 1f else it }
                val nx = dirX / dirLen
                val ny = dirY / dirLen
                val dp = handle.getDraggablePoint()
                val hx = mViewWidth * dp.x
                val hy = mViewHeight * dp.y
                val proj = ((globalX - hx) * nx + (globalY - hy) * ny)
                val newPointX = hx + proj * nx
                val newPointY = hy + proj * ny
                val along = when {
                    kotlin.math.abs(nx) >= kotlin.math.abs(ny) -> newPointX / mViewWidth
                    else -> newPointY / mViewHeight
                }
                val prev = handle.value
                val newValue = along.coerceIn(0f, 1f)
                if (newValue == prev) return true
                // apply via HandleUtils with rollback; on success update handle position
                val applied = HandleUtils.tryDrag(handle, newValue) { updated ->
                    val view = mItemImageViews.firstOrNull { it.photoItem.index == updated.index }
                    if (view != null) {
                        val leftMargin = (mViewWidth * updated.bound.left).toInt()
                        val topMargin = (mViewHeight * updated.bound.top).toInt()
                        val frameWidth: Int = if (updated.bound.right == 1f) {
                            mViewWidth - leftMargin
                        } else {
                            (mViewWidth * updated.bound.width() + 0.5f).toInt()
                        }
                        val frameHeight: Int = if (updated.bound.bottom == 1f) {
                            mViewHeight - topMargin
                        } else {
                            (mViewHeight * updated.bound.height() + 0.5f).toInt()
                        }
                        val params = view.layoutParams as LayoutParams
                        params.leftMargin = leftMargin
                        params.topMargin = topMargin
                        params.width = frameWidth
                        params.height = frameHeight
                        view.layoutParams = params
                        view.updateFrame(frameWidth.toFloat(), frameHeight.toFloat())
                    }
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeHandle = null
                return super.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }

}
