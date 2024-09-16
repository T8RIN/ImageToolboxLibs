package com.photoeditor.photoeffect.frame

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import android.widget.RelativeLayout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.photoeditor.photoeffect.template.PhotoItem
import com.photoeditor.photoeffect.utils.ImageDecoder
import com.photoeditor.photoeffect.utils.ImageUtils

/**
 * Created by vanhu_000 on 3/11/2016.
 */
class FramePhotoLayout(context: Context, var mPhotoItems: List<PhotoItem>) :
    RelativeLayout(context), FrameImageView.OnImageClickListener {

    private var mOnDragListener: OnDragListener = OnDragListener { v, event ->
        val dragEvent = event.action

        when (dragEvent) {
            DragEvent.ACTION_DRAG_ENTERED -> {
            }

            DragEvent.ACTION_DRAG_EXITED -> {
            }

            DragEvent.ACTION_DROP -> {
                var target: FrameImageView? = v as FrameImageView
                val selectedView = getSelectedFrameImageView(target!!, event)
                if (selectedView != null) {
                    target = selectedView
                    val dragged = event.localState as FrameImageView
                    if (target.photoItem != null && dragged.photoItem != null) {
                        var targetPath: Uri? = target.photoItem.imagePath
                        var draggedPath: Uri? = dragged.photoItem.imagePath
                        if (targetPath == null) targetPath = Uri.EMPTY
                        if (draggedPath == null) draggedPath = Uri.EMPTY
                        if (targetPath != draggedPath)
                            target.swapImage(dragged)
                    }
                }
            }
        }

        true
    }
    private val mItemImageViews: MutableList<FrameImageView> = ArrayList()
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mOutputScaleRatio = 1f
    private var mQuickActionClickListener: OnQuickActionClickListener? = null
    private var backgroundColor: Color = Color.White


    private val isNotLargeThan1Gb: Boolean
        get() {
            val memoryInfo = ImageUtils.getMemoryInfo(context)
            return memoryInfo.totalMem > 0 && memoryInfo.totalMem / 1048576.0 <= 1024
        }

    interface OnQuickActionClickListener {
        fun onEditActionClick(v: FrameImageView)

        fun onChangeActionClick(v: FrameImageView)
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
        if (mItemImageViews != null)
            for (view in mItemImageViews)
                view.saveInstanceState(outState)
    }

    fun restoreInstanceState(savedInstanceState: Bundle) {
        if (mItemImageViews != null)
            for (view in mItemImageViews)
                view.restoreInstanceState(savedInstanceState)
    }

    fun setQuickActionClickListener(quickActionClickListener: OnQuickActionClickListener) {
        mQuickActionClickListener = quickActionClickListener
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
            ImageDecoder.SAMPLER_SIZE = 256
        } else {
            ImageDecoder.SAMPLER_SIZE = 512
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
        var frameWidth = 0
        var frameHeight = 0
        if (item.bound.right == 1f) {
            frameWidth = mViewWidth - leftMargin
        } else {
            frameWidth = (mViewWidth * item.bound.width() + 0.5f).toInt()
        }

        if (item.bound.bottom == 1f) {
            frameHeight = mViewHeight - topMargin
        } else {
            frameHeight = (mViewHeight * item.bound.height() + 0.5f).toInt()
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
                        Paint(),
                        Canvas.ALL_SAVE_FLAG
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

    fun recycleImages() {
        for (view in mItemImageViews) {
            view.recycleImage()
        }
        System.gc()
    }

    override fun onLongClickImage(v: FrameImageView) {
        if (mPhotoItems.size > 1) {
            v.tag = "x=" + v.photoItem.x + ",y=" + v.photoItem.y + ",path=" + v.photoItem.imagePath
            val item = ClipData.Item(v.tag as CharSequence)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val dragData = ClipData(v.tag.toString(), mimeTypes, item)
            val myShadow = DragShadowBuilder(v)
            v.startDrag(dragData, myShadow, v, 0)
        }
    }

    override fun onDoubleClickImage(view: FrameImageView) {

    }

    companion object {
        private val TAG = FramePhotoLayout::class.java.simpleName
    }

}
