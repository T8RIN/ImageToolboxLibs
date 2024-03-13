package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun Or(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.Or, suffixOperand)

fun foldOr(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> Or(acc, o2) }

fun Expression.or(initializer: Expression.() -> Unit) {
    addOperand(foldOr(*Expression().apply(initializer).conditions.toTypedArray()))
}