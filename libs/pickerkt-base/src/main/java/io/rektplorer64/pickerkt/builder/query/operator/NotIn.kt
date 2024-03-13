package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun NotIn(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.NotIn, suffixOperand)

fun foldNotIn(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> NotIn(acc, o2) }

fun Expression.notIncludedIn(initializer: Expression.() -> Unit) {
    addOperand(foldNotIn(*Expression().apply(initializer).conditions.toTypedArray()))
}