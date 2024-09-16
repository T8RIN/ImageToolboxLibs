package com.photoeditor.photoeffect.frame

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.photoeditor.photoeffect.multitouch.MultiTouchHandler
import com.photoeditor.photoeffect.template.PhotoItem
import com.photoeditor.photoeffect.utils.ImageDecoder
import com.photoeditor.photoeffect.utils.ImageUtils
import com.photoeditor.photoeffect.utils.ResultContainer
import com.t8rin.collages.R
import com.t8rin.collages.utils.GeometryUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrameImageView(context: Context, val photoItem: PhotoItem) :
    androidx.appcompat.widget.AppCompatImageView(context) {

    private val mGestureDetector: GestureDetector
    private var mTouchHandler: MultiTouchHandler? = null
    var image: Bitmap? = null
    private val mPaint: Paint
    private val mImageMatrix: Matrix
    private val mScaleMatrix: Matrix
    var viewWidth: Float = 0.toFloat()
        private set
    var viewHeight: Float = 0.toFloat()
        private set
    private var mOutputScale = 1f
    private var mOnImageClickListener: OnImageClickListener? = null
    private var mOriginalLayoutParams: RelativeLayout.LayoutParams? = null
    private var mEnableTouch = true
    var corner = 0f
        private set
    var space = 0f
        private set
    private val mPath = Path()
    private val mBackgroundPath = Path()
    private val mPolygon = ArrayList<PointF>()
    private val mPathRect = Rect(0, 0, 0, 0)
    private var mSelected = true
    private val mConvertedPoints = ArrayList<PointF>()

    private var mBackgroundColor = Color.WHITE

    //Clear area
    private val mClearPath = Path()
    private val mConvertedClearPoints = ArrayList<PointF>()

    var originalLayoutParams: RelativeLayout.LayoutParams
        get() {
            if (mOriginalLayoutParams != null) {
                val params = RelativeLayout.LayoutParams(
                    mOriginalLayoutParams!!.width,
                    mOriginalLayoutParams!!.height
                )
                params.leftMargin = mOriginalLayoutParams!!.leftMargin
                params.topMargin = mOriginalLayoutParams!!.topMargin
                return params
            } else {
                return layoutParams as RelativeLayout.LayoutParams
            }
        }
        set(originalLayoutParams) {
            mOriginalLayoutParams =
                RelativeLayout.LayoutParams(originalLayoutParams.width, originalLayoutParams.height)
            mOriginalLayoutParams!!.leftMargin = originalLayoutParams.leftMargin
            mOriginalLayoutParams!!.topMargin = originalLayoutParams.topMargin
        }

    val centerPolygon: PointF?
        get() {
            if (mPolygon != null && mPolygon.size > 0) {
                val result = PointF()
                for (p in mPolygon) {
                    result.x += p.x
                    result.y += p.y
                }
                result.x = result.x / mPolygon.size
                result.y = result.y / mPolygon.size
                return result
            } else {
                return null
            }
        }

    interface OnImageClickListener {
        fun onLongClickImage(view: FrameImageView)

        fun onDoubleClickImage(view: FrameImageView)
    }

    init {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            if (photoItem.imagePath != null && photoItem.imagePath!!.toString().length > 0) {
                image = ResultContainer.getInstance().getImage(photoItem.imagePath!!)
                if (image == null || image!!.isRecycled) {
                    try {
                        image = ImageDecoder.decodeFileToBitmap(context, photoItem.imagePath!!)
                    } catch (err: OutOfMemoryError) {
                        if (context is Activity) {
                            context.runOnUiThread {
                                try {
                                    Toast.makeText(
                                        context.applicationContext,
                                        context.getString(R.string.waring_out_of_memory),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        }
                    }

                    ResultContainer.getInstance().putImage(photoItem.imagePath!!, image!!)

                } else {
                }
            }
        }

        mPaint = Paint()
        mPaint.isFilterBitmap = true
        mPaint.isAntiAlias = true
        scaleType = ScaleType.MATRIX
        setLayerType(View.LAYER_TYPE_SOFTWARE, mPaint)
        mImageMatrix = Matrix()
        mScaleMatrix = Matrix()

        mGestureDetector =
            GestureDetector(getContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    if (mOnImageClickListener != null) {
                        mOnImageClickListener!!.onLongClickImage(this@FrameImageView)
                    }
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (mOnImageClickListener != null) {
                        mOnImageClickListener!!.onDoubleClickImage(this@FrameImageView)
                    }
                    return true
                }
            })
    }

    fun saveInstanceState(outState: Bundle) {
        val index = photoItem.index
        var values = FloatArray(9)
        mImageMatrix.getValues(values)
        outState.putFloatArray("mImageMatrix_$index", values)
        values = FloatArray(9)
        mScaleMatrix.getValues(values)
        outState.putFloatArray("mScaleMatrix_$index", values)
        outState.putFloat("mViewWidth_$index", viewWidth)
        outState.putFloat("mViewHeight_$index", viewHeight)
        outState.putFloat("mOutputScale_$index", mOutputScale)
        outState.putFloat("mCorner_$index", corner)
        outState.putFloat("mSpace_$index", space)
        outState.putInt("mBackgroundColor_$index", mBackgroundColor)
    }

    /**
     * Called after init() function
     *
     * @param savedInstanceState
     */
    fun restoreInstanceState(savedInstanceState: Bundle) {
        val index = photoItem.index
        var values = savedInstanceState.getFloatArray("mImageMatrix_$index")
        if (values != null) {
            mImageMatrix.setValues(values)
        }
        values = savedInstanceState.getFloatArray("mScaleMatrix_$index")
        if (values != null) {
            mScaleMatrix.setValues(values)
        }
        viewWidth = savedInstanceState.getFloat("mViewWidth_$index", 1f)
        viewHeight = savedInstanceState.getFloat("mViewHeight_$index", 1f)
        mOutputScale = savedInstanceState.getFloat("mOutputScale_$index", 1f)
        corner = savedInstanceState.getFloat("mCorner_$index", 0f)
        space = savedInstanceState.getFloat("mSpace_$index", 0f)
        mBackgroundColor = savedInstanceState.getInt("mBackgroundColor_$index", Color.WHITE)
        mTouchHandler!!.setMatrices(mImageMatrix, mScaleMatrix)
        mTouchHandler!!.setScale(mOutputScale)
        setSpace(space, corner)
    }

    fun swapImage(view: FrameImageView) {
        if (image != null && view.image != null) {
            val temp = view.image
            view.image = image
            image = temp

            val tmpPath = view.photoItem.imagePath
            view.photoItem.imagePath = photoItem.imagePath
            photoItem.imagePath = tmpPath
            resetImageMatrix()
            view.resetImageMatrix()
        }
    }

    fun setOnImageClickListener(onImageClickListener: OnImageClickListener) {
        mOnImageClickListener = onImageClickListener
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        invalidate()
    }

    override fun getImageMatrix(): Matrix {
        return mImageMatrix
    }

    @JvmOverloads
    fun init(
        viewWidth: Float,
        viewHeight: Float,
        scale: Float,
        space: Float = 0f,
        corner: Float = 0f
    ) {
        this.viewWidth = viewWidth
        this.viewHeight = viewHeight
        mOutputScale = scale
        this.space = space
        this.corner = corner

        if (image != null) {
            mImageMatrix.set(
                ImageUtils.createMatrixToDrawImageInCenterView(
                    viewWidth,
                    viewHeight,
                    image!!.width.toFloat(),
                    image!!.height.toFloat()
                )
            )
            mScaleMatrix.set(
                ImageUtils.createMatrixToDrawImageInCenterView(
                    scale * viewWidth,
                    scale * viewHeight,
                    image!!.width.toFloat(),
                    image!!.height.toFloat()
                )
            )
        }

        mTouchHandler = MultiTouchHandler()
        mTouchHandler!!.setMatrices(mImageMatrix, mScaleMatrix)
        mTouchHandler!!.setScale(scale)
        mTouchHandler!!.setEnableRotation(true)

        setSpace(this.space, this.corner)
    }

    fun setSpace(space: Float, corner: Float) {
        this.space = space
        this.corner = corner
        setSpace(
            viewWidth, viewHeight, photoItem,
            mConvertedPoints, mConvertedClearPoints,
            mPath, mClearPath, mBackgroundPath, mPolygon, mPathRect, space, corner
        )
        invalidate()
    }

    fun resetImageMatrix() {
        mImageMatrix.set(
            ImageUtils.createMatrixToDrawImageInCenterView(
                viewWidth,
                viewHeight,
                image!!.width.toFloat(),
                image!!.height.toFloat()
            )
        )
        mScaleMatrix.set(
            ImageUtils.createMatrixToDrawImageInCenterView(
                mOutputScale * viewWidth,
                mOutputScale * viewHeight,
                image!!.width.toFloat(),
                image!!.height.toFloat()
            )
        )
        mTouchHandler!!.setMatrices(mImageMatrix, mScaleMatrix)
        invalidate()
    }

    fun clearMainImage() {
        photoItem.imagePath = null
        recycleImage()
        invalidate()
    }

    fun recycleImage() {
        if (image != null) {
            image!!.recycle()
            image = null
            System.gc()
        }
    }

    private fun drawCenterLine(canvas: Canvas) {
        val paint = Paint()
        paint.strokeWidth = 5f
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        canvas.drawLine(0f, viewHeight / 2, viewWidth, viewHeight / 2, paint)
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
    }

    fun isSelected(x: Float, y: Float): Boolean {
        return GeometryUtils.contains(mPolygon, PointF(x, y))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawImage(
            canvas, mPath, mPaint, mPathRect, image, mImageMatrix,
            width.toFloat(), height.toFloat(), mBackgroundColor, mBackgroundPath,
            mClearPath, mPolygon
        )
    }

    fun drawOutputImage(canvas: Canvas) {
        val viewWidth = this.viewWidth * mOutputScale
        val viewHeight = this.viewHeight * mOutputScale
        val path = Path()
        val clearPath = Path()
        val backgroundPath = Path()
        val pathRect = Rect()
        val polygon = ArrayList<PointF>()
        setSpace(
            viewWidth, viewHeight, photoItem, ArrayList(),
            ArrayList(), path, clearPath, backgroundPath, polygon, pathRect,
            space * mOutputScale, corner * mOutputScale
        )
        drawImage(
            canvas, path, mPaint, pathRect, image, mScaleMatrix,
            viewWidth, viewHeight, mBackgroundColor, backgroundPath, clearPath, polygon
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mEnableTouch) {
            return super.onTouchEvent(event)
        } else {
            if (event.action == MotionEvent.ACTION_DOWN) {
                mSelected = GeometryUtils.contains(mPolygon, PointF(event.x, event.y))
            }

            if (mSelected) {
                if (event.action == MotionEvent.ACTION_UP) {
                    mSelected = false
                }

                mGestureDetector.onTouchEvent(event)
                if (mTouchHandler != null && image != null && !image!!.isRecycled) {
                    mTouchHandler!!.touch(event)
                    mImageMatrix.set(mTouchHandler!!.matrix)
                    mScaleMatrix.set(mTouchHandler!!.scaleMatrix)
                    invalidate()
                }
                return true
            } else {
                return super.onTouchEvent(event)
            }
        }
    }

    fun setEnableTouch(enableTouch: Boolean) {
        mEnableTouch = enableTouch
    }

    companion object {
        private val TAG = FrameImageView::class.java.simpleName

        private fun setSpace(
            viewWidth: Float, viewHeight: Float, photoItem: PhotoItem,
            convertedPoints: MutableList<PointF>,
            convertedClearPoints: MutableList<PointF>,
            path: Path,
            clearPath: Path,
            backgroundPath: Path,
            polygon: MutableList<PointF>,
            pathRect: Rect,
            space: Float, corner: Float
        ) {
            if (photoItem.pointList != null && convertedPoints.isEmpty()) {
                for (p in photoItem.pointList) {
                    val convertedPoint = PointF(p.x * viewWidth, p.y * viewHeight)
                    convertedPoints.add(convertedPoint)
                    if (photoItem.shrinkMap != null) {

                        photoItem.shrinkMap!![convertedPoint] = photoItem.shrinkMap!![p]!!

                    }
                }
            }

            if (photoItem.clearAreaPoints != null && photoItem.clearAreaPoints!!.size > 0) {
                clearPath.reset()
                if (convertedClearPoints.isEmpty())
                    for (p in photoItem.clearAreaPoints!!) {
                        convertedClearPoints.add(PointF(p.x * viewWidth, p.y * viewHeight))
                    }
                GeometryUtils.createPathWithCircleCorner(clearPath, convertedClearPoints, corner)
            } else if (photoItem.clearPath != null) {
                clearPath.reset()
                buildRealClearPath(viewWidth, viewHeight, photoItem, clearPath, corner)
            }

            if (photoItem.path != null) {
                buildRealPath(viewWidth, viewHeight, photoItem, path, space, corner)
                polygon.clear()
            } else {
                val shrunkPoints: List<PointF>
                if (photoItem.shrinkMethod == PhotoItem.SHRINK_METHOD_3_3) {
                    val centerPointIdx = findCenterPointIndex(photoItem)
                    shrunkPoints = GeometryUtils.shrinkPathCollage_3_3(
                        convertedPoints,
                        centerPointIdx,
                        space,
                        photoItem.bound
                    )
                } else if (photoItem.shrinkMethod == PhotoItem.SHRINK_METHOD_USING_MAP && photoItem.shrinkMap != null) {
                    shrunkPoints = GeometryUtils.shrinkPathCollageUsingMap(
                        convertedPoints,
                        space,
                        photoItem.shrinkMap!!
                    )
                } else if (photoItem.shrinkMethod == PhotoItem.SHRINK_METHOD_COMMON && photoItem.shrinkMap != null) {
                    shrunkPoints =
                        GeometryUtils.commonShrinkPath(
                            convertedPoints,
                            space,
                            photoItem.shrinkMap!!
                        )
                } else {
                    if (photoItem.disableShrink) {
                        shrunkPoints =
                            GeometryUtils.shrinkPath(convertedPoints, 0f, photoItem.bound)
                    } else {
                        shrunkPoints =
                            GeometryUtils.shrinkPath(convertedPoints, space, photoItem.bound)
                    }
                }
                polygon.clear()
                polygon.addAll(shrunkPoints)
                GeometryUtils.createPathWithCircleCorner(path, shrunkPoints, corner)
                if (photoItem.hasBackground) {
                    backgroundPath.reset()
                    GeometryUtils.createPathWithCircleCorner(
                        backgroundPath,
                        convertedPoints,
                        corner
                    )
                }
            }

            pathRect.set(0, 0, 0, 0)
        }

        private fun findCenterPointIndex(photoItem: PhotoItem): Int {
            var centerPointIdx = 0
            if (photoItem.bound.left == 0f && photoItem.bound.top == 0f) {
                var minX = 1f
                for (idx in photoItem.pointList.indices) {
                    val p = photoItem.pointList[idx]
                    if (p.x > 0 && p.x < 1 && p.y > 0 && p.y < 1 && p.x < minX) {
                        centerPointIdx = idx
                        minX = p.x
                    }
                }
            } else {
                var maxX = 0f
                for (idx in photoItem.pointList.indices) {
                    val p = photoItem.pointList[idx]
                    if (p.x > 0 && p.x < 1 && p.y > 0 && p.y < 1 && p.x > maxX) {
                        centerPointIdx = idx
                        maxX = p.x
                    }
                }
            }

            return centerPointIdx
        }

        private fun buildRealPath(
            viewWidth: Float, viewHeight: Float,
            photoItem: PhotoItem, outPath: Path,
            space: Float, corner: Float
        ) {
            var space = space
            if (photoItem.path != null) {
                val rect = RectF()
                photoItem.path!!.computeBounds(rect, true)
                val pathWidthPixels = rect.width()
                val pathHeightPixels = rect.height()
                space = 2 * space
                outPath.set(photoItem.path!!)
                val m = Matrix()
                var ratioX = 1f
                var ratioY = 1f
                if (photoItem.fitBound) {
                    ratioX =
                        photoItem.pathScaleRatio * (viewWidth * photoItem.pathRatioBound!!.width() - 2 * space) / pathWidthPixels
                    ratioY =
                        photoItem.pathScaleRatio * (viewHeight * photoItem.pathRatioBound!!.height() - 2 * space) / pathHeightPixels
                } else {
                    val ratio = Math.min(
                        photoItem.pathScaleRatio * (viewHeight - 2 * space) / pathHeightPixels,
                        photoItem.pathScaleRatio * (viewWidth - 2 * space) / pathWidthPixels
                    )
                    ratioX = ratio
                    ratioY = ratio
                }
                m.postScale(ratioX, ratioY)
                outPath.transform(m)
                val bound = RectF()
                if (photoItem.cornerMethod == PhotoItem.CORNER_METHOD_3_6) {
                    outPath.computeBounds(bound, true)
                    GeometryUtils.createRegularPolygonPath(
                        outPath,
                        Math.min(bound.width(), bound.height()),
                        6,
                        corner
                    )
                    outPath.computeBounds(bound, true)
                } else if (photoItem.cornerMethod == PhotoItem.CORNER_METHOD_3_13) {
                    outPath.computeBounds(bound, true)
                    GeometryUtils.createRectanglePath(
                        outPath,
                        bound.width(),
                        bound.height(),
                        corner
                    )
                    outPath.computeBounds(bound, true)
                } else {
                    outPath.computeBounds(bound, true)
                }

                var x = 0f
                var y = 0f
                if (photoItem.shrinkMethod == PhotoItem.SHRINK_METHOD_3_6 || photoItem.shrinkMethod == PhotoItem.SHRINK_METHOD_3_8) {
                    x = viewWidth / 2 - bound.width() / 2
                    y = viewHeight / 2 - bound.height() / 2
                    m.reset()
                    m.postTranslate(x, y)
                    outPath.transform(m)
                } else {
                    if (photoItem.pathAlignParentRight) {
                        x =
                            photoItem.pathRatioBound!!.right * viewWidth - bound.width() - space / ratioX
                        y = photoItem.pathRatioBound!!.top * viewHeight + space / ratioY
                    } else {
                        x = photoItem.pathRatioBound!!.left * viewWidth + space / ratioX
                        y = photoItem.pathRatioBound!!.top * viewHeight + space / ratioY
                    }

                    if (photoItem.pathInCenterHorizontal) {
                        x = viewWidth / 2.0f - bound.width() / 2.0f
                    }

                    if (photoItem.pathInCenterVertical) {
                        y = viewHeight / 2.0f - bound.height() / 2.0f
                    }

                    m.reset()
                    m.postTranslate(x, y)
                    outPath.transform(m)
                }
            }
        }

        private fun buildRealClearPath(
            viewWidth: Float,
            viewHeight: Float,
            photoItem: PhotoItem,
            clearPath: Path,
            corner: Float
        ): Path? {
            if (photoItem.clearPath != null) {
                val rect = RectF()
                photoItem.clearPath!!.computeBounds(rect, true)
                val clearPathWidthPixels = rect.width()
                val clearPathHeightPixels = rect.height()

                clearPath.set(photoItem.clearPath!!)
                val m = Matrix()
                var ratioX = 1f
                var ratioY = 1f
                if (photoItem.fitBound) {
                    ratioX =
                        photoItem.clearPathScaleRatio * viewWidth * photoItem.clearPathRatioBound!!.width() / clearPathWidthPixels
                    ratioY =
                        photoItem.clearPathScaleRatio * viewHeight * photoItem.clearPathRatioBound!!.height() / clearPathHeightPixels
                } else {
                    val ratio = Math.min(
                        photoItem.clearPathScaleRatio * viewHeight / clearPathHeightPixels,
                        photoItem.clearPathScaleRatio * viewWidth / clearPathWidthPixels
                    )
                    ratioX = ratio
                    ratioY = ratio
                }
                m.postScale(ratioX, ratioY)
                clearPath.transform(m)
                val bound = RectF()
                if (photoItem.cornerMethod == PhotoItem.CORNER_METHOD_3_6) {
                    clearPath.computeBounds(bound, true)
                    GeometryUtils.createRegularPolygonPath(
                        clearPath,
                        Math.min(bound.width(), bound.height()),
                        6,
                        corner
                    )
                    clearPath.computeBounds(bound, true)
                } else if (photoItem.cornerMethod == PhotoItem.CORNER_METHOD_3_13) {
                    clearPath.computeBounds(bound, true)
                    GeometryUtils.createRectanglePath(
                        clearPath,
                        bound.width(),
                        bound.height(),
                        corner
                    )
                    clearPath.computeBounds(bound, true)
                } else {
                    clearPath.computeBounds(bound, true)
                }

                var x = 0f
                var y = 0f
                if (photoItem.shrinkMethod == PhotoItem.SHRINK_METHOD_3_6) {
                    if (photoItem.clearPathRatioBound!!.left > 0) {
                        x = viewWidth - bound.width() / 2
                    } else {
                        x = -bound.width() / 2
                    }
                    y = viewHeight / 2 - bound.height() / 2
                } else {
                    if (photoItem.centerInClearBound) {
                        x =
                            photoItem.clearPathRatioBound!!.left * viewWidth + (viewWidth / 2 - bound.width() / 2)
                        y =
                            photoItem.clearPathRatioBound!!.top * viewHeight + (viewHeight / 2 - bound.height() / 2)
                    } else {
                        x = photoItem.clearPathRatioBound!!.left * viewWidth
                        y = photoItem.clearPathRatioBound!!.top * viewHeight
                        if (photoItem.clearPathInCenterHorizontal) {
                            x = viewWidth / 2.0f - bound.width() / 2.0f
                        }
                        if (photoItem.clearPathInCenterVertical) {
                            y = viewHeight / 2.0f - bound.height() / 2.0f
                        }
                    }
                }

                m.reset()
                m.postTranslate(x, y)
                clearPath.transform(m)
                return clearPath
            } else {
                return null
            }
        }

        private fun drawImage(
            canvas: Canvas,
            path: Path,
            paint: Paint,
            pathRect: Rect,
            image: Bitmap?,
            imageMatrix: Matrix,
            viewWidth: Float,
            viewHeight: Float,
            color: Int,
            backgroundPath: Path?,
            clearPath: Path?,
            touchPolygon: MutableList<PointF>?
        ) {
            if (image != null && !image.isRecycled) {
                canvas.drawBitmap(image, imageMatrix, paint)
            }
            //clip outside
            if (pathRect.left == pathRect.right) {
                canvas.save()
                canvas.clipPath(path)
                pathRect.set(canvas.clipBounds)
                canvas.restore()
            }

            canvas.save()
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawARGB(0x00, 0x00, 0x00, 0x00)
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, viewWidth, pathRect.top.toFloat(), paint)
            canvas.drawRect(0f, 0f, pathRect.left.toFloat(), viewHeight, paint)
            canvas.drawRect(pathRect.right.toFloat(), 0f, viewWidth, viewHeight, paint)
            canvas.drawRect(0f, pathRect.bottom.toFloat(), viewWidth, viewHeight, paint)
            paint.xfermode = null
            canvas.restore()
            //clip inside
            canvas.save()
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawARGB(0x00, 0x00, 0x00, 0x00)
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            val currentFillType = path.fillType
            path.fillType = Path.FillType.INVERSE_WINDING
            canvas.drawPath(path, paint)
            paint.xfermode = null
            canvas.restore()
            path.fillType = currentFillType
            //clear area
            if (clearPath != null) {
                canvas.save()
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                canvas.drawARGB(0x00, 0x00, 0x00, 0x00)
                paint.color = Color.BLACK
                paint.style = Paint.Style.FILL
                canvas.drawPath(clearPath, paint)
                paint.xfermode = null
                canvas.restore()
            }
            //draw out side
            if (backgroundPath != null) {
                canvas.save()
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                canvas.drawARGB(0x00, 0x00, 0x00, 0x00)
                paint.color = color
                paint.style = Paint.Style.FILL
                canvas.drawPath(backgroundPath, paint)
                paint.xfermode = null
                canvas.restore()
            }
            //touch polygon
            if (touchPolygon != null && touchPolygon.isEmpty()) {
                touchPolygon.add(PointF(pathRect.left.toFloat(), pathRect.top.toFloat()))
                touchPolygon.add(PointF(pathRect.right.toFloat(), pathRect.top.toFloat()))
                touchPolygon.add(PointF(pathRect.right.toFloat(), pathRect.bottom.toFloat()))
                touchPolygon.add(PointF(pathRect.left.toFloat(), pathRect.bottom.toFloat()))
            }
        }
    }
}
