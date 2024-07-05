/*
 *  Copyright 2016 xyzxqs (xyzxqs@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.xyzxqs.xlowpoly

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/**
 * Created by xyzxqs(xyzxqs@gmail.com) on 9/21/16.
 */
object LowPoly {
    /**
     * generate lowpoly picture
     *
     * @param input             src Bitmap
     * @param alphaOrPointCount (0.0, 1) or [1, max)
     * @param fill              fill ? Paint.Style.FILL : Paint.Style.STROKE
     * @return out Bitmap
     */
    fun lowPoly(input: Bitmap, alphaOrPointCount: Float, fill: Boolean): Bitmap {
        return generateNonNative(
            input = input,
            threshold = 50,
            alphaOrPointCount = alphaOrPointCount,
            lowPoly = true,
            fill = fill
        )
    }

    /**
     * generate sandpainting
     *
     * @param input             src Bitmap
     * @param threshold         recommend（30, 90）
     * @param alphaOrPointCount (0.0, 1) or [1, max)
     * @return out Bitmap
     */
    fun sandPainting(input: Bitmap, threshold: Int, alphaOrPointCount: Float): Bitmap {
        return generateNonNative(
            input = input,
            threshold = threshold,
            alphaOrPointCount = alphaOrPointCount,
            lowPoly = false,
            fill = false
        )
    }


    fun generateNonNative(
        input: Bitmap,
        threshold: Int,
        alphaOrPointCount: Float,
        lowPoly: Boolean,
        fill: Boolean
    ): Bitmap {
        val newImage = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newImage)
        val paint = Paint()
        paint.isAntiAlias = false
        paint.style = if (fill) Paint.Style.FILL else Paint.Style.STROKE
        var x1: Int
        var x2: Int
        var x3: Int
        var y1: Int
        var y2: Int
        var y3: Int

        val triangles = getTriangles(input, threshold, alphaOrPointCount, lowPoly)
        if (lowPoly) {
            val path = Path()
            var i = 0
            while (i + 5 < triangles.size) {
                x1 = triangles[i]
                y1 = triangles[i + 1]
                x2 = triangles[i + 2]
                y2 = triangles[i + 3]
                x3 = triangles[i + 4]
                y3 = triangles[i + 5]

                val color = input.getPixel((x1 + x2 + x3) / 3, (y1 + y2 + y3) / 3)
                path.rewind()
                path.moveTo(x1.toFloat(), y1.toFloat())
                path.lineTo(x2.toFloat(), y2.toFloat())
                path.lineTo(x3.toFloat(), y3.toFloat())
                path.close()
                paint.color = color
                canvas.drawPath(path, paint)
                i = i + 6
            }
        } else {
            var i = 0
            val n = triangles.size
            while (i + 1 < n) {
                x1 = triangles[i]
                y1 = triangles[i + 1]
                val color = input.getPixel(x1, y1)
                paint.color = color
                canvas.drawCircle(x1.toFloat(), y1.toFloat(), 1f, paint)
                i += 2
            }
        }
        return newImage
    }

    external fun generate(
        input: Bitmap?,
        threshold: Int,
        alphaOrPointCount: Float,
        lowPoly: Boolean,
        fill: Boolean
    ): Bitmap?

    init {
        System.loadLibrary("lowpoly-lib")
    }

    private external fun getTriangles(
        input: Bitmap,
        threshold: Int,
        alpha_count: Float,
        lowPoly: Boolean
    ): IntArray
}
