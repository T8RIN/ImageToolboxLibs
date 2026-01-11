@file:Suppress("UNCHECKED_CAST")

package com.t8rin.neural_tools.bgremover

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.awxkee.aire.Aire
import com.awxkee.aire.ResizeFunction
import com.awxkee.aire.ScaleColorSpace
import com.t8rin.neural_tools.NeuralTool
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import kotlin.math.roundToInt

object U2NetBackgroundRemover : NeuralTool() {

    private val env: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private val sessions = mutableMapOf<String, OrtSession>()

    fun removeBackground(
        image: Bitmap,
        modelPath: String = context.assetFilePath(),
        trainedSize: Int? = 320
    ): Bitmap {
        val session = sessions.getOrPut(modelPath) {
            env.createSession(modelPath, OrtSession.SessionOptions())
        }

        val dstWidth = trainedSize ?: image.width
        val dstHeight = trainedSize ?: image.height

        val scaled = if (trainedSize == null) {
            image
        } else {
            Aire.scale(
                bitmap = image,
                dstWidth = dstWidth,
                dstHeight = dstHeight,
                scaleMode = ResizeFunction.Bilinear,
                colorSpace = ScaleColorSpace.SRGB
            )
        }

        val input = bitmapToFloatBuffer(scaled, dstWidth, dstHeight)
        val inputName = session.inputNames.first()
        val inputTensor = OnnxTensor.createTensor(
            env,
            input,
            longArrayOf(1, 3, dstWidth.toLong(), dstHeight.toLong())
        )

        val output = session.run(mapOf(inputName to inputTensor))
        val outputArray = (output[0].value as Array<Array<Array<FloatArray>>>)[0][0]

        val maskBmp = createBitmap(dstWidth, dstHeight)
        var i = 0
        for (y in 0 until dstHeight) {
            for (x in 0 until dstWidth) {
                val alpha = (outputArray[y][x] * 255f).roundToInt().coerceIn(0, 255)
                maskBmp[x, y] = Color.argb(alpha, 255, 255, 255)
                i++
            }
        }

        val maskScaled = if (trainedSize == null) {
            maskBmp
        } else {
            Aire.scale(
                bitmap = maskBmp,
                dstWidth = image.width,
                dstHeight = image.height,
                scaleMode = ResizeFunction.Bilinear,
                colorSpace = ScaleColorSpace.SRGB
            )
        }

        val pixels = IntArray(image.width * image.height)
        val maskPixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        maskScaled.getPixels(maskPixels, 0, image.width, 0, 0, image.width, image.height)

        for (idx in pixels.indices) {
            val alpha = Color.alpha(maskPixels[idx])
            val src = pixels[idx]
            pixels[idx] =
                Color.argb(alpha, Color.red(src), Color.green(src), Color.blue(src))
        }

        val result = createBitmap(image.width, image.height)
        result.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        result.setHasAlpha(true)

        inputTensor.close()
        output.close()

        return result
    }

    private fun bitmapToFloatBuffer(
        bitmap: Bitmap,
        width: Int,
        height: Int
    ): FloatBuffer {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val buffer = FloatBuffer.allocate(3 * width * height)
        var offsetR = 0
        var offsetG = width * height
        var offsetB = 2 * width * height

        for (p in pixels) {
            buffer.put(offsetR++, Color.red(p) / 255f)
            buffer.put(offsetG++, Color.green(p) / 255f)
            buffer.put(offsetB++, Color.blue(p) / 255f)
        }

        return buffer
    }

    private fun Context.assetFilePath(
        assetName: String = "u2netp.onnx"
    ): String {
        val file = File(filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        assets.open(assetName).use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}
