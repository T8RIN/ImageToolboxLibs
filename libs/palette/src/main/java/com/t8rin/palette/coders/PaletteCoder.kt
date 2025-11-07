package com.t8rin.palette.coders

import com.t8rin.palette.PALPalette
import java.io.InputStream
import java.io.OutputStream

/**
 * Protocol for palette coders
 */
interface PaletteCoder {
    /**
     * Decode a palette from input stream
     */
    fun decode(input: InputStream): PALPalette

    /**
     * Encode a palette to output stream
     */
    fun encode(palette: PALPalette, output: OutputStream)

    /**
     * Decode a palette from byte array
     */
    fun decode(data: ByteArray): PALPalette {
        return decode(java.io.ByteArrayInputStream(data))
    }

    /**
     * Encode a palette to byte array
     */
    fun encode(palette: PALPalette): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        encode(palette, output)
        return output.toByteArray()
    }
}

inline fun <T> PaletteCoder.use(action: PaletteCoder.() -> T): Result<T> = runCatching { action() }