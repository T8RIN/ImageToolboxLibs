package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun NotLike(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.NotLike, suffixOperand)

fun foldNotLike(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> NotLike(acc, o2) }

fun Expression.notLike(initializer: Expression.() -> Unit) {
    addOperand(foldNotLike(*Expression().apply(initializer).conditions.toTypedArray()))
}