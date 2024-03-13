package io.rektplorer64.pickerkt.builder

import android.os.Parcelable
import io.rektplorer64.pickerkt.contentresolver.ContentResolverColumn
import io.rektplorer64.pickerkt.contentresolver.Order
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class Ordering(
    val column: ContentResolverColumn,
    val order: Order = Order.Descending
) {
    override fun toString(): String {
        return "${column.columnName} ${order.sqlKeyword}"
    }
}
