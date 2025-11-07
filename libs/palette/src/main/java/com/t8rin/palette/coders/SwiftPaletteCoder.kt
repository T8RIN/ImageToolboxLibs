package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALGroup
import com.t8rin.palette.PALPalette
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat

/**
 * Swift code generator (encode only)
 */
class SwiftPaletteCoder : PaletteCoder {
    private val formatter = DecimalFormat("0.0000").apply {
        decimalFormatSymbols = java.text.DecimalFormatSymbols(java.util.Locale.US)
    }

    override fun decode(input: InputStream): PALPalette {
        throw CommonError.NotImplemented()
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        fun mapColors(group: PALGroup, offset: Int): String {
            val mapped = group.colors.mapNotNull { color ->
                try {
                    val converted =
                        if (color.colorSpace == ColorSpace.RGB) color else color.converted(
                            ColorSpace.RGB
                        )
                    converted.toRgb()
                } catch (e: Exception) {
                    null
                }
            }

            if (mapped.isEmpty()) return ""

            var result = "   // Group (${group.name})\n"
            result += "   static let group$offset: [CGColor] = ["

            mapped.forEachIndexed { index, rgb ->
                if (index % 8 == 0) {
                    result += "\n     "
                }

                val rs = formatter.format(rgb.rf)
                val gs = formatter.format(rgb.gf)
                val bs = formatter.format(rgb.bf)
                val aas = formatter.format(rgb.af)
                result += " #colorLiteral(red: $rs, green: $gs, blue: $bs, alpha: $aas),"
            }

            result += "\n   ]\n\n"
            return result
        }

        var result = "struct ExportedPalettes {\n"

        palette.allGroups.forEachIndexed { index, group ->
            result += mapColors(group, index)
        }

        result += "}\n"

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

