package io.rektplorer64.pickerkt.common.unit

import kotlinx.serialization.Serializable
import java.text.StringCharacterIterator
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sign

@JvmInline
@Serializable
value class Byte(val value: Long) : Comparable<Byte> {

    operator fun plus(other: Byte): Byte {
        return Byte(value = this.value + other.value)
    }

    operator fun minus(other: Byte): Byte {
        return Byte(value = this.value - other.value)
    }

    operator fun div(other: Byte): Byte {
        return Byte(value = this.value / other.value)
    }

    operator fun times(other: Byte): Byte {
        return Byte(value = this.value * other.value)
    }


    operator fun plus(other: Int): Byte {
        return Byte(value = this.value + other)
    }

    operator fun minus(other: Int): Byte {
        return Byte(value = this.value - other)
    }

    operator fun div(other: Int): Byte {
        return Byte(value = this.value / other)
    }

    operator fun times(other: Int): Byte {
        return Byte(value = this.value * other)
    }


    operator fun plus(other: Long): Byte {
        return Byte(value = this.value + other)
    }

    operator fun minus(other: Long): Byte {
        return Byte(value = this.value - other)
    }

    operator fun div(other: Long): Byte {
        return Byte(value = this.value / other)
    }

    operator fun times(other: Long): Byte {
        return Byte(value = this.value * other)
    }


    operator fun plus(other: Double): Byte {
        return Byte(value = (this.value + other).roundToLong())
    }

    operator fun minus(other: Double): Byte {
        return Byte(value = (this.value - other).roundToLong())
    }

    operator fun div(other: Double): Byte {
        return Byte(value = (this.value / other).roundToLong())
    }

    operator fun times(other: Double): Byte {
        return Byte(value = (this.value * other).roundToLong())
    }


    operator fun plus(other: Float): Byte {
        return Byte(value = (this.value + other).roundToLong())
    }

    operator fun minus(other: Float): Byte {
        return Byte(value = (this.value - other).roundToLong())
    }

    operator fun div(other: Float): Byte {
        return Byte(value = (this.value / other).roundToLong())
    }

    operator fun times(other: Float): Byte {
        return Byte(value = (this.value * other).roundToLong())
    }


    override fun compareTo(other: Byte): Int {
        return this.value.compareTo(other.value)
    }

    override fun toString(): String {
        return "$value byte" + (if (value > 1) "" else "s")
    }
}

fun Int.asByteUnit(): Byte = Byte(this.toLong())

fun Long.asByteUnit(): Byte = Byte(this)

fun Double.asByteUnit(): Byte = Byte(this.roundToLong())

fun Byte.toKilobyte(): Double = value / 2e10

fun Byte.toMegabyte(): Double = value / 2e20

fun Byte.toGigabyte(): Double = value / 2e30

fun Byte.toTerabyte(): Double = value / 2e40

fun Byte.toPetabyte(): Double = value / 2e50

/**
 * Converts raw [Byte] count in [Long] to a human-readable [String]
 *
 * **Reference:** [Stackoverflow](https://stackoverflow.com/a/3758880)
 */
fun Byte.formatAsHumanReadableString(): String {
    val absB = if (value == Long.MIN_VALUE) Long.MAX_VALUE else abs(value)
    if (absB < 1024) {
        return "$value B"
    }

    var value = absB
    val ci = StringCharacterIterator("KMGTPE")
    var i = 40

    // Why use 0xfffccccccccccccL?
    // Because that is the point at which one should transition from PB to EB.
    // Think of it like this: 0xfffccccccccccccL is to 250 what 999,950,000 is to 109.
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci.next()
        i -= 10
    }

    value *= value.sign.toLong()
    return String.format("%.1f %ciB", value / 1024.0, ci.current())
}

