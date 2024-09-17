package com.t8rin.collages.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import android.widget.RelativeLayout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.t8rin.collages.utils.ImageDecoder
import com.t8rin.collages.utils.ImageUtils

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
    private var backgroundColor: Color = Color.White

    private val isNotLargeThan1Gb: Boolean
        get() {
            val memoryInfo = ImageUtils.getMemoryInfo(context)
            return memoryInfo.totalMem > 0 && memoryInfo.totalMem / 1048576.0 <= 1024
        }

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
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
            ImageDecoder.SAMPLER_SIZE = 512
        } else {
            ImageDecoder.SAMPLER_SIZE = 1024
        }
        for (item in mPhotoItems) {
            val imageView = addPhotoItemView(item, mOutputScaleRatio, space, corner)
            mItemImageViews.add(imageView)
        }
    }

    fun setBackgroundColor(color: Color) {
        backgroundColor = color
        setBackgroundColor(backgroundColor.toArgb())
        invalidate()
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

}
