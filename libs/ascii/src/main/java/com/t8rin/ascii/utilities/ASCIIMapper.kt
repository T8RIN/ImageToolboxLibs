package com.t8rin.ascii.utilities

import com.t8rin.ascii.models.ASCIIMetrics

/**
 * A class to store ASCII and their luminance metrics
 */
private class ASCIIMapperImpl(
    mappingData: GradientMap = Gradient.NORMAL.toMap()
) : ASCIIMapper {

    private val metrics: List<ASCIIMetrics> = buildDataForMapping(mappingData)

    private fun buildDataForMapping(
        mappingData: GradientMap
    ): List<ASCIIMetrics> = mappingData.map { (key, value) ->
        ASCIIMetrics(
            ascii = key,
            luminance = value
        )
    }.sortedByDescending { it.luminance }

    override fun mapToAscii(luminance: Float): String {
        var searchedIndex = 0

        for ((index, metric) in metrics.withIndex()) {
            if (luminance >= metric.luminance) {
                searchedIndex = index
                break
            }
        }

        return metrics[searchedIndex].ascii
    }
}

fun interface ASCIIMapper {
    fun mapToAscii(luminance: Float): String
}

fun GradientMap.toMapper(): ASCIIMapper = ASCIIMapperImpl(this)

fun ASCIIMapper(map: GradientMap = Gradient.NORMAL.toMap()) = map.toMapper()

@JvmInline
value class Gradient(val value: String) {

    fun toMap(): GradientMap {
        val length = value.length
        return value.mapIndexed { index, char ->
            char.toString() to (1.0f - index.toFloat() / (length - 1).coerceAtLeast(1))
        }.toMap()
    }

    companion object {
        val NORMAL = Gradient(".:-=+*#%@")
        val NORMAL2 = Gradient(".:-=+*#%@")
        val ARROWS = Gradient("↖←↙↓↘→↗↑")
        val OLD = Gradient("░▒▓█")
        val EXTENDED_HIGH = Gradient(".:-~=+*^><)(][}{#%@")
        val MINIMAL = Gradient(".-+#")
        val MATH = Gradient("π√∞≈≠=÷×-+")
        val NUMERICAL = Gradient("7132546980")
    }
}

fun String.toGradientMap() = Gradient(this).toMap()

typealias GradientMap = Map<String, Float>