package com.t8rin.collages.utils

import android.graphics.PointF
import com.t8rin.collages.view.PhotoItem

internal typealias HandleUpdate = (photoItem: PhotoItem, values: FloatArray) -> Unit

internal data class Handle(
    var value: Float,
    var draggablePoint: PointF,
    var direction: PointF,
) {
    internal data class ManagedRef(
        val photoItem: PhotoItem,
        val update: HandleUpdate
    )

    val managedItems: MutableList<ManagedRef> = mutableListOf()
}


