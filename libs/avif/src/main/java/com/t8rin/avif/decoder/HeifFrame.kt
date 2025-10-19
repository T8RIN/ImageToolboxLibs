package com.t8rin.avif.decoder

import android.graphics.Bitmap

data class HeifFrame(
    val bitmap: Bitmap,
    val duration: Int
)