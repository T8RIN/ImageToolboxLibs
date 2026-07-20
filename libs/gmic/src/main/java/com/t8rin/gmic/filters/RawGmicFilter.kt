package com.t8rin.gmic.filters

import android.graphics.Color
import com.t8rin.gmic.GmicFilter
import com.t8rin.gmic.model.GmicOptions

open class RawGmicFilter(
    final override val command: String,
    final override val options: GmicOptions = GmicOptions()
) : GmicFilter

/** Numeric argument accepted by a G'MIC command. */
interface GmicArgument {
    val value: Int
}

internal fun gmicCommand(
    name: String,
    vararg arguments: Any
): String = buildString {
    append(name)
    if (arguments.isNotEmpty()) {
        append(' ')
        arguments.joinTo(this, separator = ",") { argument ->
            when (argument) {
                is Boolean -> if (argument) "1" else "0"
                is GmicArgument -> argument.value.toString()
                else -> argument.toString()
            }
        }
    }
}

internal fun Int.gmicRed(): Int = Color.red(this)

internal fun Int.gmicGreen(): Int = Color.green(this)

internal fun Int.gmicBlue(): Int = Color.blue(this)

internal fun Int.gmicAlpha(): Int = Color.alpha(this)

internal fun Int.inRange(
    name: String,
    range: IntRange
): Int = apply {
    require(this in range) { "$name must be in ${range.first}..${range.last}, was $this" }
}

internal fun Float.inRange(
    name: String,
    minimum: Float,
    maximum: Float
): Float = apply {
    require(isFinite() && this in minimum..maximum) {
        "$name must be finite and in $minimum..$maximum, was $this"
    }
}
