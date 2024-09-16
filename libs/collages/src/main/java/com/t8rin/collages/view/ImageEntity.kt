package com.photoeditor.photoeffect.multitouch

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

import com.photoeditor.photoeffect.utils.ImageDecoder
import com.t8rin.collages.R

internal class ImageEntity : MultiTouchEntity {
    private var mInitScaleFactor = 0.25

    @Transient
    var drawable: Drawable? = null
        private set

    var imageUri: Uri? = null
        private set
    var resourceId = -1
        private set
    var isDrawImageBorder = false
    var borderColor = Color.GREEN
    private var mBorderSize = 3f
    private var mBoundRect: RectF? = RectF()
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var isSticker = true
    private val mGradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, intArrayOf(Color.TRANSPARENT, Color.GRAY)
    )
    private var mDrawShadow = false
    private var mShadowSize = 0

    val isNull: Boolean
        get() = !(imageUri != null || resourceId > 0)

    constructor(resourceId: Int, res: Resources) : super(res) {
        this.resourceId = resourceId
        imageUri = null
        loadConfigs(res)
    }

    constructor(image: Uri, res: Resources) : super(res) {
        imageUri = image
        resourceId = -1
        loadConfigs(res)
    }

    constructor(e: ImageEntity, res: Resources) : super(res) {
        drawable = e.drawable
        resourceId = e.resourceId
        scaleX = e.scaleX
        scaleY = e.scaleY
        centerX = e.centerX
        centerY = e.centerY
        angle = e.angle
        imageUri = e.imageUri
        loadConfigs(res)
    }

    protected fun loadConfigs(res: Resources) {
        mBorderSize = res.getDimension(R.dimen.image_border_size)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mBorderSize
        mPaint.isAntiAlias = true
        mPaint.isDither = true
    }

    fun setShadowSize(shadowSize: Int) {
        mShadowSize = shadowSize
    }

    fun setDrawShadow(drawShadow: Boolean) {
        mDrawShadow = drawShadow
    }

    fun setInitScaleFactor(initScaleFactor: Double) {
        mInitScaleFactor = initScaleFactor
    }

    fun setBorderSize(borderSize: Float) {
        mBorderSize = borderSize
        mPaint.strokeWidth = mBorderSize
    }

    fun setImageUri(context: Context, imageUri: Uri) {
        unload()
        this.imageUri = imageUri
        load(context)
    }

    override fun draw(canvas: Canvas) {
        draw(canvas, 1f)
    }

    fun draw(canvas: Canvas, scale: Float) {
        canvas.save()
        if (drawable == null) {
            return
        }

        if (drawable is BitmapDrawable) {
            val bm = (drawable as BitmapDrawable).bitmap
            if (bm == null || bm.isRecycled) {
                return
            }
        }

        val dx = scale * (maxX + minX) / 2
        val dy = scale * (maxY + minY) / 2

        drawable!!.setBounds(
            (scale * minX).toInt(),
            (scale * minY).toInt(),
            (scale * maxX).toInt(),
            (scale * maxY).toInt()
        )

        canvas.translate(dx, dy)
        canvas.rotate(angle * 180.0f / Math.PI.toFloat())
        canvas.translate(-dx, -dy)

        if (mDrawShadow && !isSticker && mShadowSize > 1) {
            drawShadow(canvas, scale)
        }

        drawable!!.draw(canvas)
        // Draw bound
        //        if (mDrawImageBorder && !mSticker) {
        //            mPaint.setStyle(Paint.Style.STROKE);
        //            mPaint.setStrokeWidth(mBorderSize);
        //            mPaint.setColor(mBorderColor);
        //            mBoundRect.set(scale * mMinX, scale * mMinY, scale * mMaxX, scale * mMaxY);
        //            canvas.drawRect(mBoundRect, mPaint);
        //        }

        canvas.restore()
    }

    private fun drawShadow(canvas: Canvas, scale: Float) {
        mGradientDrawable.setBounds(
            (scale * (minX + mShadowSize)).toInt(),
            (scale * (minY + mShadowSize)).toInt(),
            (scale * (maxX + mShadowSize)).toInt(),
            (scale * (maxY + mShadowSize)).toInt()
        )
        mGradientDrawable.cornerRadius = 5f
        mGradientDrawable.draw(canvas)
    }

    /**
     * Called by activity's onPause() method to free memory used for loading the
     * images
     */
    override fun unload() {
        if (drawable is BitmapDrawable && drawable != null) {
            var bm: Bitmap? = (drawable as BitmapDrawable).bitmap
            if (bm != null && !bm.isRecycled) {
                bm.recycle()
            }
            bm = null
        }

        this.drawable = null
    }

    override fun load(context: Context) {
        val res = context.resources
        getMetrics(res)

        if (drawable == null) {
            drawable = createDrawableFromPrimaryInfo(context)
        }
        if (drawable == null) {
            if (imageUri != null) {
                resetPrimaryInfo()
            }
            return
        }

        width = drawable!!.intrinsicWidth
        height = drawable!!.intrinsicHeight

        setPos(centerX, centerY, scaleX, scaleY, angle)
    }

    /**
     * Called by activity's onResume() method to load the images
     */
    fun load(
        context: Context, startMidX: Float, startMidY: Float,
        startAngle: Float
    ) {
        val res = context.resources
        getMetrics(res)

        mStartMidX = startMidX
        mStartMidY = startMidY
        if (drawable == null) {
            drawable = createDrawableFromPrimaryInfo(context)
        }

        if (drawable == null) {
            if (imageUri != null) {
                resetPrimaryInfo()
            }
            return
        }

        width = drawable!!.intrinsicWidth
        height = drawable!!.intrinsicHeight

        val centerX: Float
        val centerY: Float
        val scaleX: Float
        val scaleY: Float
        // float angle;
        if (mFirstLoad) {
            centerX = startMidX
            centerY = startMidY

            val scaleFactor = (Math
                .min(mDisplayWidth, mDisplayHeight) / Math.max(
                width,
                height
            ).toFloat() * mInitScaleFactor).toFloat()
            scaleY = scaleFactor
            scaleX = scaleY
            angle = startAngle

            mFirstLoad = false
        } else {
            centerX = this.centerX
            centerY = this.centerY
            scaleX = this.scaleX
            scaleY = this.scaleY
            // angle = mAngle;
        }
        setPos(centerX, centerY, scaleX, scaleY, angle)
    }

    override fun load(context: Context, startMidX: Float, startMidY: Float) {
        load(context, startMidX, startMidY, 0f)
    }

    protected fun createDrawableFromPrimaryInfo(context: Context): Drawable? {
        val res = context.resources
        var drawable: Drawable? = null
        if (imageUri != null) {
            drawable = ImageDecoder.decodeUriToDrawable(context, imageUri!!)
        } else if (resourceId > 0) {
            drawable = res.getDrawable(resourceId)
        }

        return drawable
    }

    protected fun resetPrimaryInfo() {
        imageUri = null
        resourceId = -1
    }

    // parcelable
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeDouble(mInitScaleFactor)
        dest.writeParcelable(imageUri, flags)
        dest.writeInt(resourceId)
        dest.writeBooleanArray(booleanArrayOf(isDrawImageBorder, isSticker))
        dest.writeInt(borderColor)
        dest.writeFloat(mBorderSize)
        dest.writeParcelable(mBoundRect, flags)
    }

    override fun readFromParcel(`in`: Parcel) {
        super.readFromParcel(`in`)
        mInitScaleFactor = `in`.readDouble()
        imageUri = `in`.readParcelable(Uri::class.java.classLoader)
        resourceId = `in`.readInt()
        val `val` = BooleanArray(2)
        `in`.readBooleanArray(`val`)
        isDrawImageBorder = `val`[0]
        isSticker = `val`[1]
        borderColor = `in`.readInt()
        mBorderSize = `in`.readFloat()
        mBoundRect = `in`.readParcelable(RectF::class.java.classLoader)
    }

    protected constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageEntity> {
        override fun createFromParcel(parcel: Parcel): ImageEntity {
            return ImageEntity(parcel)
        }

        override fun newArray(size: Int): Array<ImageEntity?> {
            return arrayOfNulls(size)
        }
    }
}
