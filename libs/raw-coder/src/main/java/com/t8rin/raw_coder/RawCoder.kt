package com.t8rin.raw_coder

import android.graphics.Bitmap
import java.io.File

interface RawCoder {
    fun probe(file: File): RawInfo?
    fun decode(file: File, options: RawDecodeOptions = RawDecodeOptions()): RawImage?
}

object LibRawCoder : RawCoder {

    override fun probe(file: File): RawInfo? = LibRawBridge.open(file)?.use { session ->
        session.info()
    }

    override fun decode(file: File, options: RawDecodeOptions): RawImage? {
        return LibRawBridge.open(file)?.use { session ->
            val info = session.info() ?: return@use null
            val preview = options.mode == RawDecodeMode.EmbeddedPreview && session.unpackThumbnail()
            if (!preview) {
                if (!session.unpack()) return@use null
                if (!session.process(
                        options.developSettings,
                        options.developSettings.halfSize,
                        false
                    )
                ) {
                    return@use null
                }
            }
            val output = session.output(Bitmap.Config.ARGB_8888) ?: return@use null
            RawImage(
                output.bitmap,
                preview,
                output.orientation.takeIf { it != 0 } ?: info.orientation
            )
        }
    }

}
