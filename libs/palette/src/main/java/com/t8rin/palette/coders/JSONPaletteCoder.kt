package com.t8rin.palette.coders

import com.t8rin.palette.PALPalette
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * JSON palette coder
 */
class JSONPaletteCoder : PaletteCoder {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    override fun decode(input: InputStream): PALPalette {
        val text = input.bufferedReader().use { it.readText() }
        return json.decodeFromString(PALPalette.serializer(), text)
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val text = json.encodeToString(PALPalette.serializer(), palette)
        output.bufferedWriter().use { it.write(text) }
    }
}


