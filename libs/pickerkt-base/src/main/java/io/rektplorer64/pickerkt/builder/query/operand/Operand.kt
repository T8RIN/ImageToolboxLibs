package io.rektplorer64.pickerkt.builder.query.operand

import io.rektplorer64.pickerkt.contentresolver.ContentResolverColumn
import kotlinx.serialization.Serializable

interface Operand

@Serializable
open class Column(private val columnName: String, private val surroundedBy: String = "") : Operand {
    override fun toString(): String = "$surroundedBy${columnName}$surroundedBy"
}

@Serializable
class ContentColumn(
    val column: ContentResolverColumn,
) : Column(columnName = column.columnName, surroundedBy = "")