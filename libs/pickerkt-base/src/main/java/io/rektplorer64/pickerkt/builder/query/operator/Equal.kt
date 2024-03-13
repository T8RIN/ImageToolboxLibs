package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun Equal(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.Equal, suffixOperand)

fun foldEqual(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> Equal(acc, o2) }

fun Expression.equal(initializer: Expression.() -> Unit) {
    addOperand(foldEqual(*Expression().apply(initializer).conditions.toTypedArray()))
}