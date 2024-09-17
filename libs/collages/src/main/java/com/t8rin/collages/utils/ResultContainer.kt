package com.t8rin.collages.utils

import android.graphics.Bitmap
import android.net.Uri

object ResultContainer {

    private val mDecodedImageMap = HashMap<Uri, Bitmap>()

    fun putImage(key: Uri, bitmap: Bitmap) {
        if (mDecodedImageMap.size > 10) {
            mDecodedImageMap.remove(mDecodedImageMap.keys.first())
        }
        mDecodedImageMap[key] = bitmap
    }

    fun getImage(key: Uri): Bitmap? {
        return mDecodedImageMap[key]
    }

}
