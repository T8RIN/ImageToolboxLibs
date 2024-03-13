package io.rektplorer64.pickerkt.builder.query

import android.os.Parcel
import android.os.Parcelable
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.builder.query.operand.Column
import io.rektplorer64.pickerkt.builder.query.operand.ContentColumn
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.builder.query.operand.Value
import io.rektplorer64.pickerkt.builder.query.operator.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*


val JsonSerializer = Json {
    serializersModule = SerializersModule {

        polymorphic(Operand::class) {
            subclass(BinaryOperatorExpression::class)
            subclass(Column::class)
            // subclass(Value.serializer(PolymorphicSerializer(Any::class)))

            // subclass(Value.serializer(ListSerializer(PolymorphicSerializer(Any::class))))
            subclass(Value.StringValue::class)
            subclass(Value.LongValue::class)
            subclass(Value.DoubleValue::class)
            subclass(Value.DoubleListValue::class)
            subclass(Value.LongListValue::class)
            subclass(Value.StringListValue::class)

            subclass(ContentColumn::class)
            subclass(Expression::class)
        }
        contextual(PickerKtConfiguration.Serializer)
        useArrayPolymorphism = true
    }
}

@Serializable
open class Expression(
    private val defaultOperator: Operator = Operator.And,
    internal val conditions: ArrayList<Operand> = arrayListOf()
) : Operand, Parcelable {

    internal open fun addOperand(operand: Operand) {
       // if (operand is Expression && operand.conditions.isNotEmpty()) {
       //     throw IllegalStateException("An Expression should have at least one condition.")
       // }
        conditions.add(operand)
    }

    infix fun Operand.equal(other: Operand) {
        addOperand(Equal(this, other))
    }

    infix fun Operand.notEqual(other: Operand) {
        addOperand(NotEqual(this, other))
    }

    infix fun Operand.and(other: Operand) {
        addOperand(And(this, other))
    }

    infix fun Operand.or(other: Operand) {
        addOperand(Or(this, other))
    }

    infix fun Operand.greaterThan(other: Operand) {
        addOperand(GreaterThan(this, other))
    }

    infix fun Operand.greaterThanOrEquals(other: Operand) {
        addOperand(GreaterThanOrEquals(this, other))
    }

    infix fun Operand.lessThan(other: Operand) {
        addOperand(LessThan(this, other))
    }

    infix fun Operand.lessThanOrEquals(other: Operand) {
        addOperand(LessThanOrEquals(this, other))
    }

    infix fun Operand.like(other: Operand) {
        addOperand(Like(this, other))
    }

    infix fun Operand.notLike(other: Operand) {
        addOperand(NotLike(this, other))
    }

    infix fun Operand.includedIn(other: Operand) {
        addOperand(In(this, other))
    }

    infix fun Operand.notIncludedIn(other: Operand) {
        addOperand(NotIn(this, other))
    }

    override fun toString(): String {
        return conditions.joinToString(separator = " ${defaultOperator.sqlOperator} ") {
            it.toString()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(JsonSerializer.encodeToString(this))
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Expression> {
        override fun createFromParcel(parcel: Parcel): Expression {
            return JsonSerializer.decodeFromString(parcel.readString() ?: "")
        }

        override fun newArray(size: Int): Array<Expression?> {
            return arrayOfNulls(size)
        }
    }
}

@Serializable
class BinaryOperatorExpression internal constructor(
    private val operator: Operator
) : Expression() {

    constructor(
        prefixOperand: Operand,
        operator: Operator,
        suffixOperand: Operand
    ) : this(operator) {
        addOperand(prefixOperand)
        addOperand(suffixOperand)
    }

    override fun toString(): String {
        return conditions.joinToString(
            separator = " ${operator.sqlOperator} ",
            prefix = "(",
            postfix = ")"
        ) {
            it.toString()
        }
    }
}

fun where(
    defaultOperator: Operator = Operator.And,
    initializer: Expression.() -> Unit
) = Expression(defaultOperator = defaultOperator).apply(initializer)

