package com.t8rin.collages.utils

import android.graphics.PointF
import com.t8rin.collages.view.PhotoItem

internal typealias HandleUpdate = (photoItem: PhotoItem, values: FloatArray) -> Unit

internal data class Handle(
    var value: Float,
    var direction: PointF,
) {
    // Optional: dependencies and a point provider to compute draggablePoint from current handle values
    var dependencies: List<Handle> = emptyList()
    var draggablePointProvider: ((values: FloatArray) -> PointF)? = null

    internal data class ManagedRef(
        val photoItem: PhotoItem,
        val update: HandleUpdate
    )

    val managedItems: MutableList<ManagedRef> = mutableListOf()

    fun getDraggablePoint(): PointF {
        val provider = draggablePointProvider
        return if (provider != null && dependencies.isNotEmpty()) {
            val values = FloatArray(dependencies.size) { i -> dependencies[i].value }
            provider(values)
        } else {
            // Default mapping: project scalar value onto dominant axis of direction, other axis at 0.5
            val dx = direction.x
            val dy = direction.y
            return if (kotlin.math.abs(dx) >= kotlin.math.abs(dy)) {
                PointF(value, 0.5f)
            } else {
                PointF(0.5f, value)
            }
        }
    }
}


