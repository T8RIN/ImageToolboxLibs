package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.CommonError
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.utils.CSVParser
import com.t8rin.palette.utils.hexString
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * CSV palette coder
 */
class CSVPaletteCoder(
    private val hexFormat: ColorByteFormat = ColorByteFormat.RGB
) : PaletteCoder {
    override fun decode(input: InputStream): Palette {
        val text = input.readText()
        val records = CSVParser.parse(text)

        if (records.isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        val colors = when (records.size) {
            1 -> {
                // Single line of hex colors
                records[0].mapNotNull { field ->
                    try {
                        PaletteColor(field.trim(), ColorByteFormat.RGBA)
                    } catch (_: Throwable) {
                        null
                    }
                }
            }

            else -> {
                records.mapNotNull { record ->
                    when {
                        record.isEmpty() -> null
                        record.size == 1 -> {
                            try {
                                PaletteColor(record[0].trim(), ColorByteFormat.RGBA)
                            } catch (_: Throwable) {
                                null
                            }
                        }

                        else -> {
                            try {
                                PaletteColor(
                                    record[0].trim(),
                                    ColorByteFormat.RGBA,
                                    record[1].trim()
                                )
                            } catch (_: Throwable) {
                                null
                            }
                        }
                    }
                }
            }
        }

        if (colors.isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        return Palette(colors = colors.toMutableList())
    }

    override fun encode(palette: Palette, output: OutputStream) {
        val allColors = palette.allColors()
        var results = ""

        allColors.forEach { color ->
            results += color.hexString(hexFormat, hashmark = true, uppercase = false)
            if (color.name.isNotEmpty()) {
                results += ", ${color.name}"
            }
            results += "\n"
        }

        output.write(results.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


