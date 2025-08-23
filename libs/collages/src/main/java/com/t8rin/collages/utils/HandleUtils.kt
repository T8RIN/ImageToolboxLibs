package com.t8rin.collages.utils

import com.t8rin.collages.view.PhotoItem

internal object HandleUtils {
    const val minSize: Float = 0.1f

    class InvalidSet : RuntimeException()

    fun subscribe(
        photoItem: PhotoItem,
        handles: List<Handle>,
        update: (photoItem: PhotoItem, values: FloatArray) -> Unit
    ) {
        // attach to photoItem
        photoItem.handles.addAll(handles)

        // register with all handles
        val managedRef = Handle.ManagedRef(photoItem, update)
        handles.forEach { it.managedItems.add(managedRef) }

        // perform initial update using current handle positions
        val values = FloatArray(handles.size)
        for (i in handles.indices) {
            val handle = handles[i]
            values[i] = handle.value
        }
        update(photoItem, values)
    }
}

