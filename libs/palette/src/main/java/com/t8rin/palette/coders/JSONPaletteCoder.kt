package com.t8rin.palette.coders

import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
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

    override fun decode(input: InputStream): Palette {
        val text = input.bufferedReader().use { it.readText() }
        return json.decodeFromString(Palette.serializer(), text)
    }

    override fun encode(palette: Palette, output: OutputStream) {
        val text = json.encodeToString(Palette.serializer(), palette)
        output.bufferedWriter().use { it.write(text) }
    }
}


