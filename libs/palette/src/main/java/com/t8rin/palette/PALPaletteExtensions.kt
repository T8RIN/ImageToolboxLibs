package com.t8rin.palette

import com.t8rin.palette.coders.PaletteCoder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Extension functions for PALPalette
 */

/**
 * Load a palette from a file using a coder
 */
fun PALPalette.Companion.load(file: File, coder: PaletteCoder): PALPalette {
    return FileInputStream(file).use { input ->
        coder.decode(input)
    }
}

/**
 * Save a palette to a file using a coder
 */
fun PALPalette.save(file: File, coder: PaletteCoder) {
    FileOutputStream(file).use { output ->
        coder.encode(this, output)
    }
}

/**
 * Export palette to byte array using a coder
 */
fun PALPalette.export(coder: PaletteCoder): ByteArray {
    return coder.encode(this)
}


