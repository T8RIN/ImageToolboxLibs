package com.t8rin.ascii.utilities

import com.t8rin.ascii.models.ASCIIMetrics

/**
 * A class to store ASCII and their luminance metrics
 */
class ASCIIConverterHelper(mappingData: Map<String, Float> = createDefaultMap()) {

    private val metrics: List<ASCIIMetrics> = buildDataForMapping(mappingData)

    companion object {
        private fun createDefaultMap(): Map<String, Float> {
            return mapOf(
                " " to 1.0f,
                "`" to 0.95f,
                "." to 0.92f,
                "," to 0.9f,
                "-" to 0.8f,
                "~" to 0.75f,
                "+" to 0.7f,
                "<" to 0.65f,
                ">" to 0.6f,
                "o" to 0.55f,
                "=" to 0.5f,
                "*" to 0.35f,
                "%" to 0.3f,
                "X" to 0.1f,
                "@" to 0.0f
            )
        }
    }

    private fun buildDataForMapping(
        mappingData: Map<String, Float>
    ): List<ASCIIMetrics> = mappingData.map { (key, value) ->
        ASCIIMetrics(
            characters = key,
            luminance = value
        )
    }.sortedByDescending { it.luminance }

    fun asciiFromLuminance(luminance: Float): String {
        var searchedIndex = 0

        for ((index, metric) in metrics.withIndex()) {
            if (luminance >= metric.luminance) {
                searchedIndex = index
                break
            }
        }

        return metrics[searchedIndex].ascii ?: ""
    }
}
