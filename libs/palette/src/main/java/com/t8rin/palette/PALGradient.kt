package com.t8rin.palette

import com.t8rin.palette.PALGradient.Stop
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A gradient object represents a single gradient within a gradient file
 */
@Serializable
data class PALGradient(
    val id: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var stops: MutableList<Stop> = mutableListOf(),
    var transparencyStops: MutableList<TransparencyStop>? = null
) {
    /**
     * The minimum position value for a stop within the gradient
     */
    val minValue: Double
        get() = if (stops.isEmpty()) 0.0 else stops.minOfOrNull { it.position } ?: 0.0

    /**
     * The maximum position value for a stop within the gradient
     */
    val maxValue: Double
        get() = if (stops.isEmpty()) 0.0 else stops.maxOfOrNull { it.position } ?: 0.0

    /**
     * The colors defined in the gradient
     */
    val colors: List<PALColor>
        get() = stops.map { it.color }

    /**
     * Create a new palette containing the colors in the order of the color stops
     */
    fun toPalette(): PALPalette {
        val sorted = sorted()
        return PALPalette(
            colors = sorted.stops.map { it.color }.toMutableList(),
            name = name ?: ""
        )
    }

    /**
     * Return a gradient with the stops sorted ascending by position
     */
    fun sorted(): PALGradient {
        return copy(
            stops = stops.sortedBy { it.position }.toMutableList(),
            transparencyStops = transparencyStops?.sortedBy { it.position }?.toMutableList()
        )
    }

    /**
     * Normalize the stops within the gradient to 0.0 -> 1.0 range
     */
    fun normalized(): PALGradient {
        if (stops.isEmpty()) {
            return PALGradient(colors = emptyList(), name = name)
        }

        val minVal = minValue
        val maxVal = maxValue
        val range = maxVal - minVal

        if (range == 0.0) {
            throw GradientError.CannotNormalize()
        }

        val normalizedStops = stops.sortedBy { it.position }.map { stop ->
            val shifted = stop.position - minVal
            val scaled = shifted / range
            Stop(position = scaled, color = stop.color)
        }

        val normalizedTransparency = transparencyStops?.sortedBy { it.position }?.map { tStop ->
            val shifted = tStop.position - minVal
            val scaled = shifted / range
            TransparencyStop(position = scaled, value = tStop.value, midpoint = tStop.midpoint)
        }

        return PALGradient(
            stops = normalizedStops.toMutableList(),
            transparencyStops = normalizedTransparency?.toMutableList(),
            name = name
        )
    }

    /**
     * Return the color at a given fractional position within the gradient
     */
    fun color(at: Double): PALColor {
        val clampedT = at.coerceIn(0.0, 1.0)
        val sorted = sorted()

        if (sorted.stops.isEmpty()) {
            throw GradientError.NotEnoughStops()
        }
        if (sorted.stops.size == 1) {
            return sorted.stops[0].color
        }

        // Find the two stops to interpolate between
        val stops = sorted.stops
        for (i in 0 until stops.size - 1) {
            val stop1 = stops[i]
            val stop2 = stops[i + 1]

            if (clampedT >= stop1.position && clampedT <= stop2.position) {
                val localT = if (stop2.position != stop1.position) {
                    (clampedT - stop1.position) / (stop2.position - stop1.position)
                } else {
                    0.0
                }

                val rgb1 = stop1.color.toRgb()
                val rgb2 = stop2.color.toRgb()

                return PALColor.rgb(
                    r = rgb1.rf + (rgb2.rf - rgb1.rf) * localT,
                    g = rgb1.gf + (rgb2.gf - rgb1.gf) * localT,
                    b = rgb1.bf + (rgb2.bf - rgb1.bf) * localT,
                    a = rgb1.af + (rgb2.af - rgb1.af) * localT
                )
            }
        }

        // If we're outside the range, return the closest stop
        return if (clampedT <= stops.first().position) {
            stops.first().color
        } else {
            stops.last().color
        }
    }

    /**
     * Return colors at fractional values across the gradient
     */
    fun colors(at: List<Double>): List<PALColor> {
        return at.map { color(it) }
    }

    /**
     * Return evenly spaced colors across this gradient
     */
    fun colors(count: Int): List<PALColor> {
        if (count <= 0) return emptyList()
        if (count == 1) return listOf(color(0.0))

        return (0 until count).map { i ->
            val t = if (count > 1) i / (count - 1.0) else 0.0
            color(t)
        }
    }

    /**
     * A color stop within the gradient
     */
    @Serializable
    data class Stop(
        val id: String = UUID.randomUUID().toString(),
        var position: Double,
        var color: PALColor
    ) {
        fun matchesColorAndPosition(other: Stop?): Boolean {
            return other != null && position == other.position && color == other.color
        }
    }

    /**
     * A transparency stop
     */
    @Serializable
    data class TransparencyStop(
        val id: String = UUID.randomUUID().toString(),
        var position: Double,
        var value: Double,
        var midpoint: Double = 0.5
    ) {
        init {
            position = position.coerceIn(0.0, 1.0)
            value = value.coerceIn(0.0, 1.0)
            midpoint = midpoint.coerceIn(0.0, 1.0)
        }

        fun matchesOpacityPositionAndMidpoint(other: TransparencyStop?): Boolean {
            return other != null &&
                    position == other.position &&
                    midpoint == other.midpoint &&
                    value == other.value
        }
    }
}

/**
 * Extension function to convert palette to gradient
 */
fun PALPalette.toGradient(name: String? = null): PALGradient {
    return PALGradient(colors = this.colors, name = name ?: this.name)
}


/**
 * Create an evenly spaced gradient from an array of colors
 */
@JvmName("PALGradient1")
fun PALGradient(colors: List<PALColor>, name: String? = null) = PALGradient(
    name = name,
    stops = colors.mapIndexed { index, color ->
        val position = if (colors.size > 1) index / (colors.size - 1.0) else 0.0
        Stop(position = position, color = color)
    }.toMutableList()
)

/**
 * Create a gradient from colors and positions
 */
@JvmName("PALGradient2")
fun PALGradient(
    colors: List<PALColor>,
    positions: List<Double>,
    name: String? = null
) = PALGradient(
    name = name,
    stops = colors.zip(positions).map { (color, position) ->
        Stop(position = position, color = color)
    }.toMutableList()
)

/**
 * Create a gradient from an array of position:color tuples
 */
@JvmName("PALGradient3")
fun PALGradient(
    colorPositions: List<Pair<Double, PALColor>>,
    name: String? = null
) = PALGradient(
    name = name,
    stops = colorPositions.map { (position, color) ->
        Stop(position = position, color = color)
    }.toMutableList()
)

/**
 * Create an evenly-spaced gradient from the global colors of a palette
 */
@JvmName("PALGradient4")
fun PALGradient(palette: PALPalette, name: String? = null) = PALGradient(
    colors = palette.colors,
    name = name ?: palette.name
)

/**
 * Create an evenly-spaced gradient from a color group
 */
@JvmName("PALGradient5")
fun PALGradient(group: PALGroup, name: String? = null) = PALGradient(
    colors = group.colors,
    name = name ?: group.name
)
