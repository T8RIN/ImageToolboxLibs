package com.t8rin.editbox

import androidx.compose.ui.geometry.Size
import kotlin.math.min

private const val NORMALIZATION_FACTOR = 500

@JvmInline
value class Pt(val value: Float) {
    fun toPx(
        size: Size
    ): Float = min(
        size.width * (value / NORMALIZATION_FACTOR),
        size.height * (value / NORMALIZATION_FACTOR)
    )

    /**
     * Add two [Pt]s together.
     */
    inline operator fun plus(other: Pt) = Pt(this.value + other.value)

    /**
     * Subtract a Pt from another one.
     */
    inline operator fun minus(other: Pt) = Pt(this.value - other.value)

    /**
     * This is the same as multiplying the Pt by -1.0.
     */
    inline operator fun unaryMinus() = Pt(-value)

    /**
     * Divide a Pt by a scalar.
     */
    inline operator fun div(other: Float): Pt = Pt(value / other)


    inline operator fun div(other: Int): Pt = Pt(value / other)

    /**
     * Divide by another Pt to get a scalar.
     */
    inline operator fun div(other: Pt): Float = value / other.value

    /**
     * Multiply a Pt by a scalar.
     */
    inline operator fun times(other: Float): Pt = Pt(value * other)


    inline operator fun times(other: Int): Pt = Pt(value * other)

    /**
     * Support comparing Dimensions with comparison operators.
     */
    inline operator fun compareTo(other: Pt) = value.compareTo(other.value)

    companion object {
        val Zero = Pt(0f)
    }
}

inline val Float.pt: Pt get() = Pt(this)
inline val Int.pt: Pt get() = Pt(this.toFloat())

inline fun Pt.coerceIn(
    min: Pt,
    max: Pt
) = Pt(value.coerceIn(min.value, max.value))