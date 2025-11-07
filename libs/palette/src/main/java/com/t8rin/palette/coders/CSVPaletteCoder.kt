package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.hexString
import com.t8rin.palette.utils.CSVParser
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * CSV palette coder
 */
class CSVPaletteCoder(
    private val hexFormat: ColorByteFormat = ColorByteFormat.RGB
) : PaletteCoder {
    override fun decode(input: InputStream): PALPalette {
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
                        PALColor(field.trim(), ColorByteFormat.RGBA)
                    } catch (e: Exception) {
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
                                PALColor(record[0].trim(), ColorByteFormat.RGBA)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        else -> {
                            try {
                                PALColor(record[0].trim(), ColorByteFormat.RGBA, record[1].trim())
                            } catch (e: Exception) {
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

        return PALPalette(colors = colors.toMutableList())
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
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


