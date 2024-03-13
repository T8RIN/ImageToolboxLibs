package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun In(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.In, suffixOperand)

fun foldIn(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> In(acc, o2) }

fun Expression.includedIn(initializer: Expression.() -> Unit) {
    addOperand(foldIn(*Expression().apply(initializer).conditions.toTypedArray()))
}