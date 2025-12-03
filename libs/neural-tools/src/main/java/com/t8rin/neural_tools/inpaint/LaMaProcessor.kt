package com.t8rin.neural_tools.inpaint

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.util.Log
import com.t8rin.neural_tools.NeuralTool
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.io.readByteArray
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer

object LaMaProcessor : NeuralTool() {

    private const val MODEL_DOWNLOAD_LINK =
        "https://github.com/T8RIN/ImageToolboxRemoteResources/raw/refs/heads/main/onnx/inpaint/lama/LaMa.onnx"

    private val modelFile = File(context.filesDir, MODEL_DOWNLOAD_LINK.takeLastWhile { it != '/' })

    private val client = HttpClient {
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    private var sessionHolder: OrtSession? = null
    private val session: OrtSession
        get() = sessionHolder ?: run {
            val options = OrtSession.SessionOptions().apply {
                runCatching { addNnapi() }.onFailure {
                    Log.e(
                        "LaMaProcessor",
                        "nnapi not available"
                    )
                }
                runCatching { setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT) }
                runCatching { setInterOpNumThreads(8) }
                runCatching { setIntraOpNumThreads(8) }
                runCatching { setMemoryPatternOptimization(true) }
            }
            OrtEnvironment.getEnvironment().createSession(modelFile.absolutePath, options)
        }.also { sessionHolder = it }

    private val _isDownloaded = MutableStateFlow(modelFile.exists())
    val isDownloaded: StateFlow<Boolean> = _isDownloaded

    fun startDownload(): Flow<DownloadProgress> = callbackFlow {
        client.prepareGet(MODEL_DOWNLOAD_LINK).execute { response ->
            val total = response.contentLength() ?: -1L

            val tmp = File(modelFile.parentFile, modelFile.name + ".tmp")

            val channel = response.bodyAsChannel()
            var downloaded = 0L

            FileOutputStream(tmp).use { fos ->
                try {
                    while (!channel.isClosedForRead) {
                        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!packet.exhausted()) {
                            val bytes = packet.readByteArray()
                            downloaded += bytes.size
                            fos.write(bytes)
                            trySend(
                                DownloadProgress(
                                    currentPercent = if (total > 0) downloaded.toFloat() / total else 0f,
                                    currentTotalSize = downloaded
                                )
                            )
                        }
                    }

                    tmp.renameTo(modelFile)
                    _isDownloaded.update { true }
                    close()
                } catch (e: Throwable) {
                    tmp.delete()
                    close(e)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    fun inpaint(
        image: Bitmap,
        mask: Bitmap
    ): Bitmap? = runCatching {
        if (!modelFile.exists()) {
            modelFile.delete()
            _isDownloaded.update { false }
            return null
        }

        val prepared = prepareImgAndMask(
            image = image,
            mask = if (mask.width != image.width || mask.height != image.height) {
                Bitmap.createScaledBitmap(mask, image.width, image.height, false)
            } else mask
        )
        val tensorImg = prepared.first
        val tensorMask = prepared.second
        val originalDims = prepared.third

        Log.d("LaMaProcessor", originalDims.toString())

        val inputs = mapOf("image" to tensorImg, "mask" to tensorMask)

        session.run(inputs).use { res ->
            val outValue = res[0]
            val outTensor = outValue as? OnnxTensor
                ?: throw IllegalStateException("Model output is not OnnxTensor, but ${outValue::class.simpleName}")

            val resultBitmap = outputTensorToBitmap(
                tensor = outTensor,
                originalWidth = originalDims.first,
                originalHeight = originalDims.second,
                paddedWidth = originalDims.third,
                paddedHeight = originalDims.fourth
            )

            tensorImg.close()
            tensorMask.close()
            resultBitmap
        }
    }.onFailure { Log.e("LaMaProcessor", "failure", it) }.getOrNull()

    private fun prepareImgAndMask(
        image: Bitmap,
        mask: Bitmap
    ): Triple<OnnxTensor, OnnxTensor, Quad<Int, Int, Int, Int>> {
        val (imgTensor, imgDims) = bitmapToOnnxTensor(image)
        val maskTensor = bitmapToMaskTensor(mask, imgDims)
        return Triple(imgTensor, maskTensor, imgDims)
    }

    private fun bitmapToMaskTensor(
        bitmap: Bitmap,
        originalDims: Quad<Int, Int, Int, Int>
    ): OnnxTensor {
        val env = OrtEnvironment.getEnvironment()
        val (origWidth, origHeight, paddedWidth, paddedHeight) = originalDims

        val data = FloatArray(paddedWidth * paddedHeight)

        val origPixels = IntArray(origWidth * origHeight)
        bitmap.getPixels(origPixels, 0, origWidth, 0, 0, origWidth, origHeight)

        for (y in 0 until paddedHeight) {
            val srcY = when {
                y < origHeight -> y
                else -> 2 * origHeight - y - 1
            }.coerceIn(0, origHeight - 1)

            for (x in 0 until paddedWidth) {
                val srcX = when {
                    x < origWidth -> x
                    else -> 2 * origWidth - x - 1
                }.coerceIn(0, origWidth - 1)

                val pixel = origPixels[srcY * origWidth + srcX]
                val r = (pixel shr 16) and 0xFF
                data[y * paddedWidth + x] = if (r > 0) 1f else 0f
            }
        }

        return OnnxTensor.createTensor(
            env,
            FloatBuffer.wrap(data),
            longArrayOf(1, 1, paddedHeight.toLong(), paddedWidth.toLong())
        )
    }

    private fun bitmapToOnnxTensor(
        bitmap: Bitmap
    ): Pair<OnnxTensor, Quad<Int, Int, Int, Int>> {
        val env = OrtEnvironment.getEnvironment()
        val origWidth = bitmap.width
        val origHeight = bitmap.height

        val paddedWidth = ceilModulo(origWidth)
        val paddedHeight = ceilModulo(origHeight)

        val size = 3 * paddedWidth * paddedHeight
        val data = FloatArray(size)

        val origPixels = IntArray(origWidth * origHeight)
        bitmap.getPixels(origPixels, 0, origWidth, 0, 0, origWidth, origHeight)

        val rOffset = 0
        val gOffset = paddedWidth * paddedHeight
        val bOffset = 2 * paddedWidth * paddedHeight

        for (y in 0 until paddedHeight) {
            val srcY = when {
                y < origHeight -> y
                else -> 2 * origHeight - y - 1
            }.coerceIn(0, origHeight - 1)

            for (x in 0 until paddedWidth) {
                val srcX = when {
                    x < origWidth -> x
                    else -> 2 * origWidth - x - 1
                }.coerceIn(0, origWidth - 1)

                val pixel = origPixels[srcY * origWidth + srcX]
                val r = ((pixel shr 16) and 0xFF) * (1f / 255f)
                val g = ((pixel shr 8) and 0xFF) * (1f / 255f)
                val b = (pixel and 0xFF) * (1f / 255f)

                val idx = y * paddedWidth + x
                data[rOffset + idx] = r
                data[gOffset + idx] = g
                data[bOffset + idx] = b
            }
        }

        val tensor = OnnxTensor.createTensor(
            env,
            FloatBuffer.wrap(data),
            longArrayOf(1, 3, paddedHeight.toLong(), paddedWidth.toLong())
        )

        return Pair(tensor, Quad(origWidth, origHeight, paddedWidth, paddedHeight))
    }

    private fun ceilModulo(x: Int, mod: Int = 8): Int {
        return if (x % mod == 0) {
            x
        } else {
            (x / mod + 1) * mod
        }
    }

    private fun outputTensorToBitmap(
        tensor: OnnxTensor,
        originalWidth: Int,
        originalHeight: Int,
        paddedWidth: Int,
        paddedHeight: Int
    ): Bitmap {
        val arr = tensor.floatBuffer.array()
        val pixels = IntArray(originalWidth * originalHeight)

        val rOffset = 0
        val gOffset = paddedWidth * paddedHeight
        val bOffset = 2 * paddedWidth * paddedHeight

        for (y in 0 until originalHeight) {
            for (x in 0 until originalWidth) {
                val idx = y * paddedWidth + x
                val r = arr[rOffset + idx].toInt().coerceIn(0, 255)
                val g = arr[gOffset + idx].toInt().coerceIn(0, 255)
                val b = arr[bOffset + idx].toInt().coerceIn(0, 255)

                pixels[y * originalWidth + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        return Bitmap.createBitmap(pixels, originalWidth, originalHeight, Bitmap.Config.ARGB_8888)
    }

    data class DownloadProgress(
        val currentPercent: Float,
        val currentTotalSize: Long
    )

    private data class Quad<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}