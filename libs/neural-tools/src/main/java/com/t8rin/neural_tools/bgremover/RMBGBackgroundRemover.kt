package com.t8rin.neural_tools.bgremover

import android.graphics.Bitmap
import com.t8rin.neural_tools.DownloadProgress
import com.t8rin.neural_tools.NeuralTool
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

object RMBGBackgroundRemover : NeuralTool() {

    private const val MODEL_DOWNLOAD_LINK =
        "https://github.com/T8RIN/ImageToolboxRemoteResources/raw/refs/heads/main/onnx/bgremove/RMBG_1.4.ort"

    private val directory: File
        get() = File(context.filesDir, "onnx").apply {
            mkdirs()
        }

    private val modelFile
        get() = File(
            directory,
            MODEL_DOWNLOAD_LINK.substringAfterLast('/')
        )

    private val _isDownloaded = MutableStateFlow(modelFile.exists())
    val isDownloaded: StateFlow<Boolean> = _isDownloaded

    fun startDownload(): Flow<DownloadProgress> = callbackFlow {
        httpClient.prepareGet(MODEL_DOWNLOAD_LINK).execute { response ->
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

    fun removeBackground(
        image: Bitmap
    ): Bitmap? {
        if (!modelFile.exists()) {
            _isDownloaded.update { false }
            return null
        }

        return U2NetBackgroundRemover.removeBackground(
            image = image,
            modelPath = modelFile.absolutePath,
            trainedSize = 1024
        )
    }

}