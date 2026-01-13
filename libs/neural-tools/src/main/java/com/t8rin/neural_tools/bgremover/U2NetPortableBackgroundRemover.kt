@file:Suppress("UNCHECKED_CAST")

package com.t8rin.neural_tools.bgremover

import com.t8rin.neural_tools.DownloadProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.FileOutputStream

internal object U2NetPortableBackgroundRemover : GenericBackgroundRemover(
    downloadLink = "u2netp.onnx",
    trainedSize = 320
) {

    init {
        extract()
    }

    override fun startDownload(): Flow<DownloadProgress> = callbackFlow {
        extract()
        close()
    }

    private fun extract() {
        if (!modelFile.exists() || modelFile.length() <= 0) {
            context.assets.open("u2netp.onnx").use { input ->
                FileOutputStream(modelFile).use { output -> input.copyTo(output) }
            }
        }
    }
}
