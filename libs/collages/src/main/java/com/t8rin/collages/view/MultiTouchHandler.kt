package com.photoeditor.photoeffect.multitouch

import android.graphics.Matrix
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.view.MotionEvent

class MultiTouchHandler : Parcelable {

    // these matrices will be used to move and zoom image
    private var mMatrix = Matrix()
    private var mSavedMatrix = Matrix()

    private var mMode = NONE

    // remember some things for zooming
    private var mStart = PointF()
    private var mMid = PointF()
    private var mOldDist = 1f
    private var mD = 0f
    private var mNewRot = 0f
    private var mLastEvent: FloatArray? = null
    private var mEnableRotation = false
    private var mEnableZoom = true
    private var mEnableTranslateX = true
    private var mEnableTranslateY = true

    // for scale
    private var mScale = 1.0f
    var scaleMatrix = Matrix()
    private var mScaleSavedMatrix = Matrix()
    private var mMaxPositionOffset = -1f
    private var mOldImagePosition = PointF(0f, 0f)
    private var mCheckingPosition = PointF(0f, 0f)

    var matrix: Matrix
        get() = mMatrix
        set(matrix) {
            this.mMatrix.set(matrix)
            mSavedMatrix.set(matrix)
            scaleMatrix.reset()
            mScaleSavedMatrix.reset()
        }

    constructor()

    fun setMatrices(matrix: Matrix, scaleMatrix: Matrix) {
        this.mMatrix.set(matrix)
        mSavedMatrix.set(matrix)
        this.scaleMatrix.set(scaleMatrix)
        mScaleSavedMatrix.set(scaleMatrix)
    }

    fun reset() {
        this.mMatrix.reset()
        this.mSavedMatrix.reset()
        mMode = NONE
        mStart.set(0f, 0f)
        mMid.set(0f, 0f)
        mOldDist = 1f
        mD = 0f
        mNewRot = 0f
        mLastEvent = null
        mEnableRotation = false
        // scale
        this.scaleMatrix.reset()
        this.mScaleSavedMatrix.reset()
    }

    fun setMaxPositionOffset(maxPositionOffset: Float) {
        mMaxPositionOffset = maxPositionOffset
    }

    fun touch(event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mSavedMatrix.set(mMatrix)
                mScaleSavedMatrix.set(scaleMatrix)
                mStart.set(event.x, event.y)
                mOldImagePosition.set(mCheckingPosition.x, mCheckingPosition.y)
                mMode = DRAG
                mLastEvent = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mOldDist = spacing(event)
                if (mOldDist > 10f) {
                    mSavedMatrix.set(mMatrix)
                    mScaleSavedMatrix.set(scaleMatrix)
                    midPoint(mMid, event)
                    mMode = ZOOM
                }
                mLastEvent = FloatArray(4)
                mLastEvent!![0] = event.getX(0)
                mLastEvent!![1] = event.getX(1)
                mLastEvent!![2] = event.getY(0)
                mLastEvent!![3] = event.getY(1)
                mD = rotation(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mMode = NONE
                mLastEvent = null
            }

            MotionEvent.ACTION_MOVE -> if (mMode == DRAG) {
                mMatrix.set(mSavedMatrix)
                scaleMatrix.set(mScaleSavedMatrix)
                mCheckingPosition.set(mOldImagePosition.x, mOldImagePosition.y)

                var dx = event.x - mStart.x
                var dy = event.y - mStart.y

                mCheckingPosition.x += dx
                mCheckingPosition.y += dy
                if (!mEnableTranslateX) {
                    dx = 0f
                    if (mCheckingPosition.y > mMaxPositionOffset) {
                        dy = dy - (mCheckingPosition.y - mMaxPositionOffset)
                        mCheckingPosition.y = mMaxPositionOffset
                    } else if (mCheckingPosition.y < -mMaxPositionOffset) {
                        dy = dy - (mCheckingPosition.y + mMaxPositionOffset)
                        mCheckingPosition.y = -mMaxPositionOffset
                    }
                }

                if (!mEnableTranslateY) {
                    dy = 0f
                    if (mCheckingPosition.x > mMaxPositionOffset) {
                        dx = dx - (mCheckingPosition.x - mMaxPositionOffset)
                        mCheckingPosition.x = mMaxPositionOffset
                    } else if (mCheckingPosition.x < -mMaxPositionOffset) {
                        dx = dx - (mCheckingPosition.x + mMaxPositionOffset)
                        mCheckingPosition.x = -mMaxPositionOffset
                    }
                }

                mMatrix.postTranslate(dx, dy)
                scaleMatrix.postTranslate(dx * mScale, dy * mScale)
            } else if (mMode == ZOOM && mEnableZoom) {
                val newDist = spacing(event)
                if (newDist > 10f) {
                    mMatrix.set(mSavedMatrix)
                    scaleMatrix.set(mScaleSavedMatrix)
                    val scale = newDist / mOldDist
                    mMatrix.postScale(scale, scale, mMid.x, mMid.y)
                    scaleMatrix.postScale(scale, scale, mMid.x * mScale, mMid.y * mScale)
                }

                if (mEnableRotation && mLastEvent != null && event.pointerCount == 2) {
                    mNewRot = rotation(event)
                    midPoint(mMid, event)
                    val r = mNewRot - mD
                    mMatrix.postRotate(r, mMid.x, mMid.y)
                    scaleMatrix.postRotate(r, mMid.x * mScale, mMid.y * mScale)
                }
            }
        }
    }

    fun setScale(scale: Float) {
        mScale = scale
    }

    fun setEnableRotation(enableRotation: Boolean) {
        mEnableRotation = enableRotation
    }

    fun setEnableZoom(enableZoom: Boolean) {
        mEnableZoom = enableZoom
    }

    fun setEnableTranslateX(enableTranslateX: Boolean) {
        mEnableTranslateX = enableTranslateX
    }

    fun setEnableTranslateY(enableTranslateY: Boolean) {
        mEnableTranslateY = enableTranslateY
    }

    /**
     * Determine the space between the first two fingers
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    override fun describeContents(): Int {
        return 0
    }

    private constructor(`in`: Parcel) {
        var values = FloatArray(9)
        `in`.readFloatArray(values)
        mMatrix = Matrix()
        mMatrix.setValues(values)

        values = FloatArray(9)
        `in`.readFloatArray(values)
        mSavedMatrix = Matrix()
        mSavedMatrix.setValues(values)

        mMode = `in`.readInt()
        mStart = `in`.readParcelable(PointF::class.java.classLoader)!!
        mMid = `in`.readParcelable(PointF::class.java.classLoader)!!
        mOldDist = `in`.readFloat()
        mD = `in`.readFloat()
        mNewRot = `in`.readFloat()
        val b = BooleanArray(4)
        `in`.readBooleanArray(b)
        mEnableRotation = b[0]
        mEnableZoom = b[1]
        mEnableTranslateX = b[2]
        mEnableTranslateY = b[3]
        mScale = `in`.readFloat()

        values = FloatArray(9)
        `in`.readFloatArray(values)
        scaleMatrix = Matrix()
        scaleMatrix.setValues(values)

        values = FloatArray(9)
        `in`.readFloatArray(values)
        mScaleSavedMatrix = Matrix()
        mScaleSavedMatrix.setValues(values)

        mMaxPositionOffset = `in`.readFloat()
        mOldImagePosition = `in`.readParcelable(PointF::class.java.classLoader)!!
        mCheckingPosition = `in`.readParcelable(PointF::class.java.classLoader)!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        var values = FloatArray(9)
        mMatrix.getValues(values)
        dest.writeFloatArray(values)

        values = FloatArray(9)
        mSavedMatrix.getValues(values)
        dest.writeFloatArray(values)

        dest.writeInt(mMode)
        dest.writeParcelable(mStart, flags)
        dest.writeParcelable(mMid, flags)
        dest.writeFloat(mOldDist)
        dest.writeFloat(mD)
        dest.writeFloat(mNewRot)

        val b = booleanArrayOf(mEnableRotation, mEnableZoom, mEnableTranslateX, mEnableTranslateY)
        dest.writeBooleanArray(b)
        dest.writeFloat(mScale)

        values = FloatArray(9)
        scaleMatrix.getValues(values)
        dest.writeFloatArray(values)

        values = FloatArray(9)
        mScaleSavedMatrix.getValues(values)
        dest.writeFloatArray(values)

        dest.writeFloat(mMaxPositionOffset)
        dest.writeParcelable(mOldImagePosition, flags)
        dest.writeParcelable(mCheckingPosition, flags)
    }

    companion object CREATOR : Parcelable.Creator<MultiTouchHandler> {

        private val NONE = 0
        private val DRAG = 1
        private val ZOOM = 2

        override fun createFromParcel(parcel: Parcel): MultiTouchHandler {
            return MultiTouchHandler(parcel)
        }

        override fun newArray(size: Int): Array<MultiTouchHandler?> {
            return arrayOfNulls(size)
        }
    }
}
