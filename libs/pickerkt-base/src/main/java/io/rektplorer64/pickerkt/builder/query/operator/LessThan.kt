package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun LessThan(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.LessThan, suffixOperand)

fun foldLessThan(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> LessThan(acc, o2) }

fun Expression.lessThan(initializer: Expression.() -> Unit) {
    addOperand(foldLessThan(*Expression().apply(initializer).conditions.toTypedArray()))
}