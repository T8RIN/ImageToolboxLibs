package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun GreaterThanOrEquals(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.GreaterThanOrEquals, suffixOperand)

fun foldGreaterThanOrEquals(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> Or(acc, o2) }

fun Expression.greaterThanOrEquals(initializer: Expression.() -> Unit) {
    addOperand(foldGreaterThanOrEquals(*Expression().apply(initializer).conditions.toTypedArray()))
}