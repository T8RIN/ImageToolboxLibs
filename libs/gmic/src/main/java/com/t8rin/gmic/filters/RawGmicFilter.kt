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

private data class GmicQuotedArgument(val value: String)

internal fun gmicQuoted(value: String): Any = GmicQuotedArgument(value)

internal fun gmicPipeline(vararg commands: String): String = commands.joinToString(separator = " ")

/** Pads FFT/DCT input without stretching, then restores the original dimensions. */
internal fun withPowerOfTwoPadding(command: String): String = buildString {
    append("_gmic_android_w={w} _gmic_android_h={h} ")
    append("resize {2^ceil(log2(max(1,w)))},{2^ceil(log2(max(1,h)))},100%,100%,0,3,0.5,0.5 ")
    append(command)
    append(" crop ")
    append("{int((w-\$_gmic_android_w)/2)},{int((h-\$_gmic_android_h)/2)},")
    append("{int((w-\$_gmic_android_w)/2+\$_gmic_android_w-1)},")
    append("{int((h-\$_gmic_android_h)/2+\$_gmic_android_h-1)}")
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
                is GmicQuotedArgument -> "\"${argument.value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
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
