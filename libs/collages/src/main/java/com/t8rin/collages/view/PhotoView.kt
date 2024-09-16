package com.photoeditor.photoeffect.multitouch


import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.photoeditor.photoeffect.frame.FrameTouch
import com.photoeditor.photoeffect.frame.OnFrameTouchListener
import com.photoeditor.photoeffect.utils.ImageDecoder
import com.t8rin.collages.R

class PhotoView(context: Context, attrs: AttributeSet?, defStyle: Int) :
    View(context, attrs, defStyle), MultiTouchController.MultiTouchObjectCanvas<MultiTouchEntity> {

    // original code
    var imageEntities: ArrayList<MultiTouchEntity>? = ArrayList()

    private val multiTouchController = MultiTouchController(this)

    private val currTouchPoint = MultiTouchController.PointInfo()

    private val mShowDebugInfo = false

    private var mUIMode = UI_MODE_ROTATE

    // --

    private val mLinePaintTouchPointCircle = Paint()

    private val wdth: Int = 0
    private val hight: Int = 0
    private var displayWidth: Int = 0
    private var displayHeight: Int = 0

    // Hung NV add onDoubleClick listener
    private var mCurrentSelectedObject: MultiTouchEntity? = null
    private var mSelectedCount = 0
    private var mSelectedTime = System.currentTimeMillis()
    private var mClickListener: OnDoubleClickListener? = null
    var photoBackgroundUri: Uri? = null
        private set
    private var mOldX = 0f
    private var mOldY = 0f
    private var mTouchAreaInterval = 10f

    // //////////////////////////
    private var mFrameTouchListener: OnFrameTouchListener? = null
    private var mTouchedObject: MultiTouchEntity? = null
    // ---------------------------------------------------------------------------------------------------

    constructor(context: Context) : this(context, null) {

        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {

        init(context)
    }

    init {

        init(context)
    }

    private fun init(context: Context) {
        val res = context.resources

        mLinePaintTouchPointCircle.color = Color.YELLOW
        mLinePaintTouchPointCircle.strokeWidth = 5f
        mLinePaintTouchPointCircle.style = Style.STROKE
        mLinePaintTouchPointCircle.isAntiAlias = true

        val metrics = res.displayMetrics
        this.displayWidth =
            if (res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                Math
                    .max(metrics.widthPixels, metrics.heightPixels)
            else
                Math.min(
                    metrics.widthPixels, metrics.heightPixels
                )
        this.displayHeight =
            if (res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                Math
                    .min(metrics.widthPixels, metrics.heightPixels)
            else
                Math.max(
                    metrics.widthPixels, metrics.heightPixels
                )
        // photocollage
        mTouchAreaInterval = res.getDimension(R.dimen.touch_area_interval)

    }

    /**
     * Called by activity's onResume() method to load the images
     */
    fun loadImages(context: Context) {
        if (imageEntities == null) {
            return
        }

        val n = imageEntities!!.size
        for (i in 0 until n) {
            // float cx = SCREEN_MARGIN
            // + (float) (Math.random() * (displayWidth - 2 * SCREEN_MARGIN));
            // float cy = SCREEN_MARGIN
            // + (float) (Math.random() * (displayHeight - 2 * SCREEN_MARGIN));
            imageEntities!![i].load(context)// , cx, cy);
        }

        cleanImages()
    }

    private fun cleanImages() {
        if (imageEntities == null) {
            return
        }

        val entityList = ArrayList<MultiTouchEntity>()
        for (entity in imageEntities!!)
            if (!(entity as ImageEntity).isNull) {
                entityList.add(entity)
            }

        imageEntities!!.clear()
        imageEntities!!.addAll(entityList)
    }

    fun addImageEntity(entity: MultiTouchEntity) {
        if (imageEntities == null) {
            return
        }

        if (imageEntities!!.size > 0) {
            if (imageEntities!![0] is ImageEntity && entity is ImageEntity) {
                val img = imageEntities!![0] as ImageEntity
                entity.borderColor = img.borderColor
                entity.isDrawImageBorder = img
                    .isDrawImageBorder
            }
        }

        imageEntities!!.add(entity)
        entity.load(
            context, ((width - entity.width) / 2).toFloat(),
            ((height - entity.height) / 2).toFloat()
        )
        invalidate()
    }

    fun clearAllImageEntities() {
        if (imageEntities == null) {
            return
        }

        unloadImages()
        imageEntities!!.clear()
        invalidate()
    }

    fun removeImageEntity(entity: MultiTouchEntity) {
        if (imageEntities == null) {
            return
        }

        imageEntities!!.remove(entity)
        invalidate()
    }

    fun setOnDoubleClickListener(listener: OnDoubleClickListener) {
        this.mClickListener = listener
    }

    fun setFrameTouchListener(frameTouchListener: OnFrameTouchListener) {
        mFrameTouchListener = frameTouchListener
    }

    fun setBorderColor(color: Int) {
        if (imageEntities == null) {
            return
        }

        val n = imageEntities!!.size
        for (i in 0 until n) {
            if (imageEntities!![i] is ImageEntity) {
                (imageEntities!![i] as ImageEntity).borderColor = color
            }
        }

        invalidate()
    }

    fun setBorderSize(borderSize: Float) {
        if (imageEntities == null) {
            return
        }

        val n = imageEntities!!.size
        for (i in 0 until n) {
            if (imageEntities!![i] is ImageEntity) {
                (imageEntities!![i] as ImageEntity).setBorderSize(borderSize)
            }
        }

        invalidate()
    }

    fun setDrawImageBound(drawImageBorder: Boolean) {
        if (imageEntities == null) {
            return
        }

        val n = imageEntities!!.size
        for (i in 0 until n) {
            if (imageEntities!![i] is ImageEntity) {
                (imageEntities!![i] as ImageEntity)
                    .isDrawImageBorder = drawImageBorder
            }
        }

        invalidate()
    }

    fun setDrawShadow(drawShadow: Boolean) {
        if (imageEntities == null) {
            return
        }

        val n = imageEntities!!.size
        for (i in 0 until n) {
            if (imageEntities!![i] is ImageEntity) {
                (imageEntities!![i] as ImageEntity).setDrawShadow(drawShadow)
            }
        }

        invalidate()
    }

    fun setShadowSize(shadowSize: Int) {
        if (imageEntities == null) {
            return
        }

        val n = imageEntities!!.size
        for (i in 0 until n) {
            if (imageEntities!![i] is ImageEntity) {
                (imageEntities!![i] as ImageEntity).setShadowSize(shadowSize)
            }
        }

        invalidate()
    }

    fun setPhotoBackground(photoBackgroundUri: Uri) {
        // destroy old background first
        destroyBackground()
        // set new background
        this.photoBackgroundUri = photoBackgroundUri
        if (this.photoBackgroundUri != null) {
            val d = ImageDecoder.decodeUriToDrawable(
                context,
                photoBackgroundUri
            )
            if (Build.VERSION.SDK_INT >= 16) {
                background = d
            } else {
                setBackgroundDrawable(d)
            }
        } else {
            if (Build.VERSION.SDK_INT >= 16) {
                background = null
            } else {
                setBackgroundDrawable(null)
            }
        }

    }

    fun destroyBackground() {
        val d = background
        if (d != null && d is BitmapDrawable) {
            var bm: Bitmap? = d.bitmap
            if (bm != null && !bm.isRecycled) {
                bm.recycle()
            }
            bm = null
        }

        if (Build.VERSION.SDK_INT >= 16) {
            background = null
        } else {
            setBackgroundDrawable(null)
        }

        photoBackgroundUri = null
    }

    /**
     * Called by activity's onPause() method to free memory used for loading the
     * images
     */
    fun unloadImages() {
        val n = imageEntities!!.size
        for (i in 0 until n)
            imageEntities!![i].unload()
    }

    // ---------------------------------------------------------------------------------------------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageEntities == null) {
            return
        }

        val n = imageEntities!!.size
        for (i in 0 until n)
            imageEntities!![i].draw(canvas)
        if (mShowDebugInfo)
            drawMultitouchDebugMarks(canvas)
    }

    // ---------------------------------------------------------------------------------------------------
    fun getImage(outputScale: Float): Bitmap? {
        if (imageEntities == null) {
            return null
        }

        val result = Bitmap.createBitmap(
            (width * outputScale).toInt(), (height * outputScale).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        val bgDrawable = background
        if (bgDrawable != null && bgDrawable is BitmapDrawable) {
            val bitmap = bgDrawable.bitmap
            if (bitmap != null) {
                canvas.drawBitmap(
                    bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    Rect(0, 0, result.width, result.height),
                    Paint(Paint.ANTI_ALIAS_FLAG)
                )
            }
        }

        val n = imageEntities!!.size
        for (i in 0 until n) {
            val entity = imageEntities!![i]
            if (entity is ImageEntity) {
                entity.draw(canvas, outputScale)
            } else {
                entity.draw(canvas)
            }
        }

        return result
    }

    fun trackballClicked() {
        mUIMode = (mUIMode + 1) % 3
        invalidate()
    }

    private fun drawMultitouchDebugMarks(canvas: Canvas) {
        if (currTouchPoint.isDown) {
            val xs = currTouchPoint.xs
            val ys = currTouchPoint.ys
            val pressures = currTouchPoint.pressures
            val numPoints = Math.min(currTouchPoint.numTouchPoints, 2)
            for (i in 0 until numPoints)
                canvas.drawCircle(
                    xs[i], ys[i], 50 + pressures[i] * 80,
                    mLinePaintTouchPointCircle
                )
            if (numPoints == 2)
                canvas.drawLine(
                    xs[0], ys[0], xs[1], ys[1],
                    mLinePaintTouchPointCircle
                )
        }
    }

    // ---------------------------------------------------------------------------------------------------

    /**
     * Pass touch events to the MT controller
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var b = false
        var processed = false
        if (mFrameTouchListener != null
            && mFrameTouchListener is FrameTouch
            && (mFrameTouchListener as FrameTouch).isImageFrameMoving
        ) {
            if (mFrameTouchListener != null && mTouchedObject == null) {
                mFrameTouchListener!!.onFrameTouch(event)
                processed = true
            } else {
                b = multiTouchController.onTouchEvent(event)
            }
        } else {
            b = multiTouchController.onTouchEvent(event)
            if (mFrameTouchListener != null && mTouchedObject == null) {
                mFrameTouchListener!!.onFrameTouch(event)
                processed = true
            }
        }


        return b
    }

    /**
     * Get the image that is under the single-touch point, or return null
     * (canceling the drag op) if none
     */
    override fun getDraggableObjectAtPoint(pt: MultiTouchController.PointInfo): MultiTouchEntity? {
        val x = pt.x
        val y = pt.y
        val n = imageEntities!!.size
        for (i in n - 1 downTo 0) {
            val im = imageEntities!![i] as ImageEntity
            if (im.contain(x, y))
                return im
        }
        return null
    }

    /**
     * Select an object for dragging. Called whenever an object is found to be
     * under the point (non-null is returned by getDraggableObjectAtPoint()) and
     * a drag operation is starting. Called with null when drag op ends.
     */

    override fun selectObject(img: MultiTouchEntity, touchPoint: MultiTouchController.PointInfo) {
        currTouchPoint.set(touchPoint)
        mTouchedObject = img
        if (img != null) {
            // Move image to the top of the stack when selected
            imageEntities!!.remove(img)
            imageEntities!!.add(img)
            // is double click ?
            if (!touchPoint.isMultiTouch && touchPoint.isDown) {
                val currentTime = System.currentTimeMillis()
                if (mCurrentSelectedObject !== img) {
                    mCurrentSelectedObject = img
                    mSelectedCount = 1
                    mOldX = touchPoint.x
                    mOldY = touchPoint.y
                } else {
                    if (currentTime - mSelectedTime < DOUBLE_CLICK_TIME_INTERVAL) {
                        val x = touchPoint.x
                        val y = touchPoint.y
                        if (mOldX + mTouchAreaInterval > x
                            && mOldX - mTouchAreaInterval < x
                            && mOldY + mTouchAreaInterval > y
                            && mOldY - mTouchAreaInterval < y
                        ) {
                            mSelectedCount++
                        } else {
                            mOldX = x
                            mOldY = y
                        }
                    } else {
                        mOldX = touchPoint.x
                        mOldY = touchPoint.y
                    }
                    // Double click
                    if (mSelectedCount == 2) {
                        if (mClickListener != null) {
                            mClickListener!!.onPhotoViewDoubleClick(this, img)
                        }
                        mCurrentSelectedObject = null
                        mSelectedCount = 0
                        mOldX = 0f
                        mOldY = 0f
                    }
                }
                mSelectedTime = currentTime
            }
        } else {
            // Called with img == null when drag stops.
        }
        invalidate()
    }

    /**
     * Get the current position and scale of the selected image. Called whenever
     * a drag starts or is reset.
     */
    override fun getPositionAndScale(
        img: MultiTouchEntity,
        objPosAndScaleOut: MultiTouchController.PositionAndScale
    ) {
        // FIXME affine-izem (and fix the fact that the anisotropic_scale part
        // requires averaging the two scale factors)
        objPosAndScaleOut.set(
            img.centerX, img.centerY,
            mUIMode and UI_MODE_ANISOTROPIC_SCALE == 0,
            (img.scaleX + img.scaleY) / 2,
            mUIMode and UI_MODE_ANISOTROPIC_SCALE != 0, img.scaleX,
            img.scaleY, mUIMode and UI_MODE_ROTATE != 0,
            img.angle
        )
    }

    /**
     * Set the position and scale of the dragged/stretched image.
     */
    override fun setPositionAndScale(
        img: MultiTouchEntity,
        newImgPosAndScale: MultiTouchController.PositionAndScale,
        touchPoint: MultiTouchController.PointInfo
    ): Boolean {
        currTouchPoint.set(touchPoint)
        val ok = (img as ImageEntity).setPos(newImgPosAndScale)
        if (ok)
            invalidate()
        return ok
    }

    override fun pointInObjectGrabArea(
        pt: MultiTouchController.PointInfo,
        img: MultiTouchEntity
    ): Boolean {
        return false
    }

    companion object {
        private val DOUBLE_CLICK_TIME_INTERVAL: Long = 700

        private val UI_MODE_ROTATE = 1
        private val UI_MODE_ANISOTROPIC_SCALE = 2

        private val SCREEN_MARGIN = 100f
    }
}
