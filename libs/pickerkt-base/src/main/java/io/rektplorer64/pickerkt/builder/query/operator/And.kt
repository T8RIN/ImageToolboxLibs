package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun And(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.And, suffixOperand)

fun foldAnd(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> And(acc, o2) }

fun Expression.and(initializer: Expression.() -> Unit) {
    addOperand(foldAnd(*Expression().apply(initializer).conditions.toTypedArray()))
}