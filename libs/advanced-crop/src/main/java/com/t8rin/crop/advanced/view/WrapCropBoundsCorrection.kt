package com.t8rin.crop.advanced.view

import android.graphics.Matrix

class WrapCropBoundsCorrection(
    @JvmField val mTargetMatrix: Matrix?,
    @JvmField val mScaleFactor: Float,
    @JvmField val mDeltaX: Float,
    @JvmField val mDeltaY: Float,
    @JvmField val mTargetImageCorners: FloatArray?
)