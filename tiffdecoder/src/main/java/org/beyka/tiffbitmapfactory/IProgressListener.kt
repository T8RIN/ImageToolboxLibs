package org.beyka.tiffbitmapfactory

/**
 * Created by beyka on 4/26/17.
 */
fun interface IProgressListener {
    fun reportProgress(processedPixels: Long, totalPixels: Long)
}
