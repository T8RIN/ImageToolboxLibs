package com.t8rin.awebp.decoder

import android.graphics.Bitmap

data class WebpFrame(
    val bitmap: Bitmap,
    val duration: Int
)