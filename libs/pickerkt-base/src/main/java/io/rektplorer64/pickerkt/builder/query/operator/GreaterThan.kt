package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun GreaterThan(prefixOperand: Operand, suffixOperand: Operand) =
    BinaryOperatorExpression(prefixOperand, Operator.GreaterThan, suffixOperand)

fun foldGreaterThan(vararg operands: Operand): Operand =
    operands.foldFromFirst { acc, o2 -> GreaterThan(acc, o2) }

fun Expression.greaterThan(initializer: Expression.() -> Unit) {
    addOperand(foldGreaterThan(*Expression().apply(initializer).conditions.toTypedArray()))
}