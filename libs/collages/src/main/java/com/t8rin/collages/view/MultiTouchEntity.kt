package com.photoeditor.photoeffect.multitouch

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable

abstract class MultiTouchEntity : Parcelable {

    protected var mFirstLoad = true

    var width: Int = 0
        protected set
    var height: Int = 0
        protected set

    // width/height of screen
    protected var mDisplayWidth: Int = 0
    protected var mDisplayHeight: Int = 0

    var centerX: Float = 0.toFloat()
        protected set
    var centerY: Float = 0.toFloat()
        protected set
    var scaleX: Float = 0.toFloat()
        protected set
    var scaleY: Float = 0.toFloat()
        protected set
    var angle: Float = 0.toFloat()
        protected set

    var minX: Float = 0.toFloat()
        protected set
    var maxX: Float = 0.toFloat()
        protected set
    var minY: Float = 0.toFloat()
        protected set
    var maxY: Float = 0.toFloat()
        protected set
    var isGrabAreaSelected = false
    protected var mIsLatestSelected = false

    protected var mGrabAreaX1: Float = 0.toFloat()
    protected var mGrabAreaY1: Float = 0.toFloat()
    protected var mGrabAreaX2: Float = 0.toFloat()
    protected var mGrabAreaY2: Float = 0.toFloat()

    protected var mStartMidX: Float = 0.toFloat()
    protected var mStartMidY: Float = 0.toFloat()
    protected var mUIMode = UI_MODE_ROTATE

    //detect touch
    internal var matrix = Matrix()
    internal var point = FloatArray(2)
    internal var mappedPoints: MutableList<PointF> = ArrayList()

    constructor()

    constructor(res: Resources) {
        getMetrics(res)
    }

    protected fun getMetrics(res: Resources) {
        val metrics = res.displayMetrics
        mDisplayWidth = if (res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            Math
                .max(metrics.widthPixels, metrics.heightPixels)
        else
            Math.min(
                metrics.widthPixels, metrics.heightPixels
            )
        mDisplayHeight = if (res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            Math
                .min(metrics.widthPixels, metrics.heightPixels)
        else
            Math.max(
                metrics.widthPixels, metrics.heightPixels
            )
    }

    /**
     * Set the position and scale of an image in screen coordinates
     */
    fun setPos(newImgPosAndScale: MultiTouchController.PositionAndScale): Boolean {
        val newScaleX: Float
        val newScaleY: Float

        if (mUIMode and UI_MODE_ANISOTROPIC_SCALE != 0) {
            newScaleX = newImgPosAndScale.getScaleX()
        } else {
            newScaleX = newImgPosAndScale.getScale()
        }

        if (mUIMode and UI_MODE_ANISOTROPIC_SCALE != 0) {
            newScaleY = newImgPosAndScale.getScaleY()
        } else {
            newScaleY = newImgPosAndScale.getScale()
        }

        return setPos(
            newImgPosAndScale.xOff, newImgPosAndScale.yOff,
            newScaleX, newScaleY, newImgPosAndScale.getAngle()
        )
    }

    /**
     * Set the position and scale of an image in screen coordinates
     */
    protected fun setPos(
        centerX: Float, centerY: Float, scaleX: Float,
        scaleY: Float, angle: Float
    ): Boolean {
        val size = calculateHalfDrawableSize(scaleX, scaleY)
        val ws = size[0]//(mWidth / 2) * scaleX;
        val hs = size[1]//(mHeight / 2) * scaleY;

        minX = centerX - ws
        minY = centerY - hs
        maxX = centerX + ws
        maxY = centerY + hs

        mGrabAreaX1 = maxX - GRAB_AREA_SIZE
        mGrabAreaY1 = maxY - GRAB_AREA_SIZE
        mGrabAreaX2 = maxX
        mGrabAreaY2 = maxY

        this.centerX = centerX
        this.centerY = centerY
        this.scaleX = scaleX
        this.scaleY = scaleY
        this.angle = angle

        return true
    }

    protected fun calculateHalfDrawableSize(
        scaleX: Float,
        scaleY: Float
    ): FloatArray {
        val size = FloatArray(2)
        val ws = width / 2 * scaleX
        val hs = height / 2 * scaleY
        size[0] = ws
        size[1] = hs
        return size
    }


    @Deprecated(" Return whether or not the given screen coords are inside this image")
    fun containsPoint(touchX: Float, touchY: Float): Boolean {
        return touchX >= minX && touchX <= maxX && touchY >= minY && touchY <= maxY
    }

    /**
     * Return whether or not the given screen coords are inside this image
     */
    fun contain(touchX: Float, touchY: Float): Boolean {
        val dx = (maxX + minX) / 2
        val dy = (maxY + minY) / 2

        matrix.reset()
        matrix.setRotate(angle * 180.0f / Math.PI.toFloat(), dx, dy)
        //draw mapped points
        mappedPoints.clear()
        point[0] = minX
        point[1] = minY
        matrix.mapPoints(point)
        mappedPoints.add(PointF(point[0], point[1]))

        point[0] = maxX
        point[1] = minY
        matrix.mapPoints(point)
        mappedPoints.add(PointF(point[0], point[1]))

        point[0] = maxX
        point[1] = maxY
        matrix.mapPoints(point)
        mappedPoints.add(PointF(point[0], point[1]))

        point[0] = minX
        point[1] = maxY
        matrix.mapPoints(point)
        mappedPoints.add(PointF(point[0], point[1]))
        return contains(mappedPoints, PointF(touchX, touchY))
    }

    fun grabAreaContainsPoint(touchX: Float, touchY: Float): Boolean {
        return (touchX >= mGrabAreaX1 && touchX <= mGrabAreaX2
                && touchY >= mGrabAreaY1 && touchY <= mGrabAreaY2)
    }

    fun reload(context: Context) {
        mFirstLoad = false // Let the load know properties have changed so
        // reload those,
        // don't go back and start with defaults
        load(context, centerX, centerY)
    }

    abstract fun draw(canvas: Canvas)

    abstract fun load(context: Context, startMidX: Float, startMidY: Float)

    abstract fun load(context: Context)

    abstract fun unload()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeBooleanArray(booleanArrayOf(mFirstLoad, isGrabAreaSelected, mIsLatestSelected))
        dest.writeInt(width)
        dest.writeInt(height)
        dest.writeInt(mDisplayWidth)
        dest.writeInt(mDisplayHeight)
        dest.writeFloat(centerX)
        dest.writeFloat(centerY)
        dest.writeFloat(scaleX)
        dest.writeFloat(scaleY)
        dest.writeFloat(angle)
        dest.writeFloat(minX)
        dest.writeFloat(maxX)
        dest.writeFloat(minY)
        dest.writeFloat(maxY)
        dest.writeFloat(mGrabAreaX1)
        dest.writeFloat(mGrabAreaY1)
        dest.writeFloat(mGrabAreaX2)
        dest.writeFloat(mGrabAreaY2)
        dest.writeFloat(mStartMidX)
        dest.writeFloat(mStartMidY)
        dest.writeInt(mUIMode)
    }

    open fun readFromParcel(`in`: Parcel) {
        val `val` = BooleanArray(3)
        `in`.readBooleanArray(`val`)
        mFirstLoad = `val`[0]
        isGrabAreaSelected = `val`[1]
        mIsLatestSelected = `val`[2]
        width = `in`.readInt()
        height = `in`.readInt()
        mDisplayWidth = `in`.readInt()
        mDisplayHeight = `in`.readInt()
        centerX = `in`.readFloat()
        centerY = `in`.readFloat()
        scaleX = `in`.readFloat()
        scaleY = `in`.readFloat()
        angle = `in`.readFloat()
        minX = `in`.readFloat()
        maxX = `in`.readFloat()
        minY = `in`.readFloat()
        maxY = `in`.readFloat()
        mGrabAreaX1 = `in`.readFloat()
        mGrabAreaY1 = `in`.readFloat()
        mGrabAreaX2 = `in`.readFloat()
        mGrabAreaY2 = `in`.readFloat()
        mStartMidX = `in`.readFloat()
        mStartMidY = `in`.readFloat()
        mUIMode = `in`.readInt()
    }

    companion object {

        // area of the entity that can be scaled/rotated
        // using single touch (grows from bottom right)
        protected val GRAB_AREA_SIZE = 40

        private val UI_MODE_ROTATE = 1
        private val UI_MODE_ANISOTROPIC_SCALE = 2

        /**
         * Return true if the given point is contained inside the boundary.
         * See: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
         *
         * @param test The point to check
         * @return true if the point is inside the boundary, false otherwise
         */
        fun contains(points: List<PointF>, test: PointF): Boolean {
            var i: Int
            var j: Int
            var result = false
            i = 0
            j = points.size - 1
            while (i < points.size) {
                if (points[i].y > test.y != points[j].y > test.y && test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x) {
                    result = !result
                }
                j = i++
            }
            return result
        }
    }
}
