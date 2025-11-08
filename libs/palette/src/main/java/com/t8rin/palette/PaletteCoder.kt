package com.t8rin.palette

import android.content.Context
import android.net.Uri
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Protocol for palette coders
 */
interface PaletteCoder {
    /**
     * Decode a palette from input stream
     */
    fun decode(input: InputStream): Palette

    /**
     * Encode a palette to output stream
     */
    fun encode(palette: Palette, output: OutputStream)
}

/**
 * Decode a palette from byte array
 */
fun PaletteCoder.decode(data: ByteArray): Palette = decode(ByteArrayInputStream(data).buffered())

/**
 * Encode a palette to byte array
 */
fun PaletteCoder.encode(palette: Palette): ByteArray = ByteArrayOutputStream().use {
    encode(palette, it)
    it.toByteArray()
}

inline fun <T> PaletteCoder.use(action: PaletteCoder.() -> T): Result<T> = runCatching { action() }

fun PaletteCoder.decode(uri: Uri, context: Context) =
    decode(context.contentResolver.openInputStream(uri)!!)