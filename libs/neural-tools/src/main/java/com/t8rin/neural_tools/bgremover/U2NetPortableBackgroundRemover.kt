@file:Suppress("UNCHECKED_CAST")

package com.t8rin.neural_tools.bgremover

import android.graphics.Bitmap
import com.t8rin.neural_tools.DownloadProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import java.io.FileOutputStream

internal object U2NetPortableBackgroundRemover : GenericBackgroundRemover(
    downloadLink = "u2netp.onnx",
    trainedSize = 320
) {

    init {
        extract()
    }

    override fun startDownload(forced: Boolean): Flow<DownloadProgress> = callbackFlow {
        extract()
        close()
    }

    override fun removeBackground(image: Bitmap, modelPath: String, trainedSize: Int?): Bitmap? {
        extract()
        return super.removeBackground(
            image = image,
            modelPath = modelPath,
            trainedSize = trainedSize
        )
    }

    override fun checkModel(): Boolean {
        extract()
        return super.checkModel()
    }

    private fun extract() {
        if (!modelFile.exists() || modelFile.length() <= 0) {
            context.assets.open("u2netp.onnx").use { input ->
                FileOutputStream(modelFile).use { output -> input.copyTo(output) }
            }
        }
        _isDownloaded.update { true }
    }
}
