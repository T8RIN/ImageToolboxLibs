package com.t8rin.avif.decoder

import android.graphics.Bitmap

data class AvifFrame(
    val bitmap: Bitmap,
    val duration: Int
)