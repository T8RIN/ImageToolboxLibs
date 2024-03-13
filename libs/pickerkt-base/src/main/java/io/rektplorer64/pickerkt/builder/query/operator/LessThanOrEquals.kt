package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun LessThanOrEquals(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.LessThanOrEquals, suffixOperand)

fun foldLessThanOrEquals(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> LessThanOrEquals(acc, o2) }

fun Expression.lessThanOrEquals(initializer: Expression.() -> Unit) {
    addOperand(foldLessThanOrEquals(*Expression().apply(initializer).conditions.toTypedArray()))
}