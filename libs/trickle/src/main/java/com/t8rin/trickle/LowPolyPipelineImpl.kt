package com.t8rin.trickle

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.t8rin.trickle.pipelines.LowPolyPipeline

internal object LowPolyPipelineImpl : LowPolyPipeline {

    override fun lowPoly(input: Bitmap, alphaOrPointCount: Float, fill: Boolean): Bitmap {
        return generate(
            input = input,
            threshold = 50,
            alphaOrPointCount = alphaOrPointCount,
            lowPoly = true,
            fill = fill
        ) ?: input
    }


    override fun sandPainting(input: Bitmap, threshold: Int, alphaOrPointCount: Float): Bitmap {
        return generateNonNative(
            input = input,
            threshold = threshold,
            alphaOrPointCount = alphaOrPointCount,
            lowPoly = false,
            fill = false
        )
    }


    private fun generateNonNative(
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
                i += 6
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

    private external fun generate(
        input: Bitmap?,
        threshold: Int,
        alphaOrPointCount: Float,
        lowPoly: Boolean,
        fill: Boolean
    ): Bitmap?

    private external fun getTriangles(
        input: Bitmap,
        threshold: Int,
        alpha_count: Float,
        lowPoly: Boolean
    ): IntArray

}