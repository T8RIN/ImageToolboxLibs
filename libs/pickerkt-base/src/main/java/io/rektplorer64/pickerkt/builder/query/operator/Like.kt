package io.rektplorer64.pickerkt.builder.query.operator

import io.rektplorer64.pickerkt.builder.query.BinaryOperatorExpression
import io.rektplorer64.pickerkt.builder.query.Expression
import io.rektplorer64.pickerkt.builder.query.operand.Operand
import io.rektplorer64.pickerkt.util.foldFromFirst

@Suppress("FunctionName")
fun Like(prefixOperand: Operand, suffixOperand: Operand) =
        BinaryOperatorExpression(prefixOperand, Operator.Like, suffixOperand)

fun foldLike(vararg operands: Operand): Operand = operands.foldFromFirst { acc, o2 -> Like(acc, o2) }

fun Expression.like(initializer: Expression.() -> Unit) {
    addOperand(foldLike(*Expression().apply(initializer).conditions.toTypedArray()))
}