package com.t8rin.gmic

import android.graphics.Bitmap
import com.t8rin.gmic.model.GmicAlphaMode
import com.t8rin.gmic.model.GmicException
import com.t8rin.gmic.model.GmicOptions

data object Gmic {
    const val VERSION: String = "4.0.2"

    init {
        System.loadLibrary("gmic-android")
    }

    fun run(
        input: Bitmap,
        filter: GmicFilter
    ): Bitmap = run(
        input = input,
        command = filter.command,
        options = filter.options
    )

    fun run(
        input: Bitmap,
        command: String,
        options: GmicOptions = GmicOptions()
    ): Bitmap {
        require(!input.isRecycled) { "Input bitmap is recycled" }
        require(command.isNotBlank()) { "G'MIC command must not be blank" }

        val nativeInput = if (input.config == Bitmap.Config.ARGB_8888) {
            input
        } else {
            input.copy(Bitmap.Config.ARGB_8888, false)
                ?: throw GmicException("Unable to copy input bitmap to ARGB_8888")
        }

        return nativeRun(
            input = nativeInput,
            command = command,
            preserveAlpha = options.alphaMode == GmicAlphaMode.Preserve,
            outputIndex = options.outputIndex
        )
    }

    private external fun nativeRun(
        input: Bitmap,
        command: String,
        preserveAlpha: Boolean,
        outputIndex: Int
    ): Bitmap
}
