package com.t8rin.collages.utils

import android.graphics.PointF
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
        val values = FloatArray(handles.size) { i -> handles[i].value }
        update(photoItem, values)
    }

    /**
     * Try applying a drag to [handle] with [newValue].
     * Calls [onItemUpdated] for each affected item after updates.
     * Returns true if applied; rolls back and returns false if an InvalidSet occurs.
     */
    fun tryDrag(
        handle: Handle,
        newValue: Float,
        onItemUpdated: (photoItem: PhotoItem) -> Unit
    ): Boolean {
        val previous = handle.value
        handle.value = newValue
        val affected = handle.managedItems.toList()
        return try {
            for (managed in affected) {
                val values = FloatArray(managed.photoItem.handles.size) { i ->
                    managed.photoItem.handles[i].value
                }
                managed.update(managed.photoItem, values)
                onItemUpdated(managed.photoItem)
            }
            true
        } catch (ex: InvalidSet) {
            handle.value = previous
            for (managed in affected) {
                val values = FloatArray(managed.photoItem.handles.size) { i ->
                    managed.photoItem.handles[i].value
                }
                managed.update(managed.photoItem, values)
                onItemUpdated(managed.photoItem)
            }
            false
        }
    }

    // Helper factory methods to make collage code concise
    fun horizontalWall(
        initial: Float = 0.5f,
        computePoint: ((values: FloatArray) -> PointF)? = null
    ): Handle = Handle(initial, PointF(0f, 1f)).apply {
        draggablePointProvider = computePoint
    }

    fun verticalWall(
        initial: Float = 0.5f,
        computePoint: ((values: FloatArray) -> PointF)? = null
    ): Handle = Handle(initial, PointF(1f, 0f)).apply {
        draggablePointProvider = computePoint
    }
}

