package com.t8rin.neural_tools.inpaint

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.util.Log
import com.awxkee.aire.Aire
import com.awxkee.aire.ResizeFunction
import com.awxkee.aire.ScaleColorSpace
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

    private const val TRAINED_SIZE = 512

    private const val MODEL_DOWNLOAD_LINK =
        "https://github.com/T8RIN/ImageToolboxRemoteResources/raw/refs/heads/main/onnx/inpaint/lama/LaMa.onnx"

    private val modelFile = File(context.filesDir, "LaMa.onnx")

    private val client = HttpClient {
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    private var sessionHolder: OrtSession? = null
    private val session: OrtSession
        get() = sessionHolder ?: run {
            val options = OrtSession.SessionOptions().apply {
                runCatching { addNnapi() }
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

        val inputImage = Aire.scale(
            bitmap = image,
            dstWidth = TRAINED_SIZE,
            dstHeight = TRAINED_SIZE,
            scaleMode = ResizeFunction.Lanczos3,
            colorSpace = ScaleColorSpace.LAB
        )

        val inputMask = Aire.scale(
            bitmap = mask,
            dstWidth = TRAINED_SIZE,
            dstHeight = TRAINED_SIZE,
            scaleMode = ResizeFunction.Nearest,
            colorSpace = ScaleColorSpace.SRGB
        )

        val tensorImg = bitmapToOnnxTensor(
            bitmap = inputImage
        )

        val tensorMask = bitmapToMaskTensor(
            bitmap = inputMask
        )

        val inputs = mapOf("image" to tensorImg, "mask" to tensorMask)

        session.run(inputs).use { res ->
            val outValue = res[0]
            val outTensor = outValue as? OnnxTensor
                ?: throw IllegalStateException("Model output is not OnnxTensor, but ${outValue::class.simpleName}")

            val restored = Aire.scale(
                bitmap = outputTensorToBitmap(
                    tensor = outTensor,
                    width = TRAINED_SIZE,
                    height = TRAINED_SIZE
                ),
                dstWidth = image.width,
                dstHeight = image.height,
                scaleMode = ResizeFunction.Lanczos3,
                colorSpace = ScaleColorSpace.LAB
            )

            tensorImg.close()
            tensorMask.close()
            restored
        }
    }.onFailure { Log.e("LaMaProcessor", "failure", it) }.getOrNull()

    private fun bitmapToMaskTensor(
        bitmap: Bitmap
    ): OnnxTensor {
        val env = OrtEnvironment.getEnvironment()
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val data = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            data[i] = if (r > 0) 1f else 0f
        }

        return OnnxTensor.createTensor(
            env,
            FloatBuffer.wrap(data),
            longArrayOf(1, 1, h.toLong(), w.toLong())
        )
    }

    private fun bitmapToOnnxTensor(
        bitmap: Bitmap
    ): OnnxTensor {
        val env = OrtEnvironment.getEnvironment()
        val w = bitmap.width
        val h = bitmap.height

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val size = 3 * w * h
        val data = FloatArray(size)

        val rOffset = 0
        val gOffset = w * h
        val bOffset = 2 * w * h

        var i = 0
        while (i < pixels.size) {
            val p = pixels[i]
            val r = ((p shr 16) and 0xFF) * (1f / 255f)
            val g = ((p shr 8) and 0xFF) * (1f / 255f)
            val b = (p and 0xFF) * (1f / 255f)

            data[rOffset + i] = r
            data[gOffset + i] = g
            data[bOffset + i] = b

            i++
        }

        return OnnxTensor.createTensor(
            env,
            FloatBuffer.wrap(data),
            longArrayOf(1, 3, h.toLong(), w.toLong())
        )
    }

    @Suppress("UnnecessaryVariable", "SameParameterValue")
    private fun outputTensorToBitmap(
        tensor: OnnxTensor,
        width: Int,
        height: Int
    ): Bitmap {
        val size = width * height

        val arr = tensor.floatBuffer.array()


        val rOffset = 0
        val gOffset = size
        val bOffset = size * 2

        val pixels = IntArray(size)

        var i = 0
        while (i < size) {
            val r = arr[rOffset + i].toInt().coerceIn(0, 255)
            val g = arr[gOffset + i].toInt().coerceIn(0, 255)
            val b = arr[bOffset + i].toInt().coerceIn(0, 255)

            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            i++
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    data class DownloadProgress(
        val currentPercent: Float,
        val currentTotalSize: Long
    )
}