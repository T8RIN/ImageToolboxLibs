package com.t8rin.neural_tools.bgremover

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.awxkee.aire.Aire
import com.awxkee.aire.ResizeFunction
import com.awxkee.aire.ScaleColorSpace
import com.t8rin.neural_tools.NeuralTool
import com.t8rin.neural_tools.utils.toTensor
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object U2NetBackgroundRemover : NeuralTool() {

    private val modules = mutableMapOf<String, Module>()

    fun removeBackground(image: Bitmap): Bitmap {
        val modelPath = context.assetFilePath()
        val module = modules.getOrPut(modelPath) { Module.load(modelPath) }
        val trainedSize = 320

        val scaled = Aire.scale(
            bitmap = image,
            dstWidth = trainedSize,
            dstHeight = trainedSize,
            scaleMode = ResizeFunction.Bilinear,
            colorSpace = ScaleColorSpace.SRGB
        )

        val outputTensor = module.forward(EValue.from(scaled.toTensor()))[0].toTensor()
        val output = outputTensor.dataAsFloatArray

        val maskBmp = createBitmap(trainedSize, trainedSize)
        var i = 0
        for (y in 0 until trainedSize) {
            for (x in 0 until trainedSize) {
                val alpha = (output[i++] * 255f).roundToInt().coerceIn(0, 255)
                maskBmp[x, y] = Color.argb(alpha, 255, 255, 255)
            }
        }

        val maskScaled = Aire.scale(
            bitmap = maskBmp,
            dstWidth = image.width,
            dstHeight = image.height,
            scaleMode = ResizeFunction.Bilinear,
            colorSpace = ScaleColorSpace.SRGB
        )

        val pixels = IntArray(image.width * image.height)
        val maskPixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        maskScaled.getPixels(maskPixels, 0, image.width, 0, 0, image.width, image.height)

        for (i in pixels.indices) {
            val alpha = Color.alpha(maskPixels[i])
            val srcColor = pixels[i]
            pixels[i] =
                Color.argb(alpha, Color.red(srcColor), Color.green(srcColor), Color.blue(srcColor))
        }

        val result = createBitmap(image.width, image.height)
        result.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        result.setHasAlpha(true)

        return result
    }


    private fun Context.assetFilePath(
        assetName: String = "u2netp.pte"
    ): String {
        val file = File(filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        assets.open(assetName).use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}
