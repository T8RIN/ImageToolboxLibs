package com.t8rin.curves.utils

import android.graphics.Bitmap

internal val Bitmap.aspectRatio: Float get() = width / height.toFloat()

internal val Bitmap.safeAspectRatio: Float
    get() = aspectRatio
        .coerceAtLeast(0.005f)
        .coerceAtMost(1000f)