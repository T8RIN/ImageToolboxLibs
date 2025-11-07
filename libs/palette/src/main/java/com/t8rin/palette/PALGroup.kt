package com.t8rin.palette

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A grouping of colors
 */
@Serializable
data class PALGroup(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    val colors: MutableList<PALColor> = mutableListOf()
) {
    /**
     * Generate an array of RGB-encoded hex strings for each color in the group
     */
    fun hexRGB(hashmark: Boolean = true, uppercase: Boolean = false): List<String> {
        return colors.map { color ->
            val rgb = color.toRgb()
            val r = (rgb.rf * 255).toInt().toString(16).padStart(2, '0')
            val g = (rgb.gf * 255).toInt().toString(16).padStart(2, '0')
            val b = (rgb.bf * 255).toInt().toString(16).padStart(2, '0')
            val hex = if (uppercase) {
                (if (hashmark) "#" else "") + r.uppercase() + g.uppercase() + b.uppercase()
            } else {
                (if (hashmark) "#" else "") + r + g + b
            }
            hex
        }
    }

    /**
     * Generate an array of RGBA-encoded hex strings for each color in the group
     */
    fun hexRGBA(hashmark: Boolean = true, uppercase: Boolean = false): List<String> {
        return colors.map { color ->
            val rgb = color.toRgb()
            val r = (rgb.rf * 255).toInt().toString(16).padStart(2, '0')
            val g = (rgb.gf * 255).toInt().toString(16).padStart(2, '0')
            val b = (rgb.bf * 255).toInt().toString(16).padStart(2, '0')
            val a = (rgb.af * 255).toInt().toString(16).padStart(2, '0')
            val hex = if (uppercase) {
                (if (hashmark) "#" else "") + r.uppercase() + g.uppercase() + b.uppercase() + a.uppercase()
            } else {
                (if (hashmark) "#" else "") + r + g + b + a
            }
            hex
        }
    }
}


