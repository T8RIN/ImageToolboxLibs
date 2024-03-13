package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun NotEqual(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.NotEqual, suffixOperand)

fun foldNotEqual(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> NotEqual(acc, o2) }

fun Expression.notEqual(initializer: Expression.() -> Unit) {
    addOperand(foldNotEqual(*Expression().apply(initializer).conditions.toTypedArray()))
}