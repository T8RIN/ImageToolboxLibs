package io.rektplorer64.pickerkt.builder.query.operand

import io.rektplorer64.pickerkt.builder.query.operand.Operand
import kotlinx.serialization.Serializable

@Serializable
sealed class Value : Operand {

    override fun toString(): String = when (this) {
        is DoubleValue -> this.value.toString()
        is LongValue -> this.value.toString()
        is DoubleListValue -> this.value.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        ) {
            it.toString()
        }
        is LongListValue -> this.value.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        ) {
            it.toString()
        }
        is StringListValue -> this.value.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )
        is StringValue -> this.value
    }


    @Serializable
    class StringValue(val value: String) : Value()

    @Serializable
    class LongValue(val value: Long) : Value()

    @Serializable
    class DoubleValue(val value: Double) : Value()

    @Serializable
    class DoubleListValue(val value: List<Double>) : Value()

    @Serializable
    class LongListValue(val value: List<Long>) : Value()

    @Serializable
    class StringListValue(val value: List<String>) : Value()
}

fun valueOf(value: String) = Value.StringValue(value)
fun valueOf(value: Long) = Value.LongValue(value)
fun valueOf(value: Double) = Value.DoubleValue(value)
fun valueOf(value: Int) = Value.LongValue(value.toLong())

fun valueOf(values: List<Double>) = Value.DoubleListValue(values)
fun valueOf(values: List<Long>) = Value.LongListValue(values)
@JvmName("valueInt")
fun valueOf(values: List<Int>) = Value.LongListValue(values.map { it.toLong() })
fun valueOf(values: List<String>) = Value.StringListValue(values)