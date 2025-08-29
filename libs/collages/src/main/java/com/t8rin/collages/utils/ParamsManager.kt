package com.t8rin.collages.utils

import com.t8rin.collages.view.PhotoItem

typealias ParamT = Int

internal typealias ItemUpdate = (photoItem: PhotoItem, values: FloatArray) -> Unit

internal class ParamsManager(
    private val values: FloatArray,
    private val itemUpdateFunctions: List<ItemUpdate?>,
    private val itemHandles: List<List<Handle>>,
    private val itemsByIndex: List<PhotoItem?>,
    private val paramToDependentItems: Array<IntArray>
) {
    class InvalidValues : RuntimeException()

    companion object {
        const val minSize: Float = 0.05f
    }

    var onItemUpdated: (Int) -> Unit = {}

    fun getHandles(itemIndex: Int): List<Handle> =
        if (itemIndex in itemHandles.indices) itemHandles[itemIndex] else emptyList()

    fun snapshotValues(): FloatArray = values.copyOf()
    // Only for handles registered with this manager
    internal fun valuesRef(): FloatArray = values

    fun updateParams(params: List<ParamT>, newValues: FloatArray, notify: Boolean = true) {
        val previous = FloatArray(params.size) { i -> values[params[i]] }
        try {
            for (i in params.indices) {
                values[params[i]] = newValues[i]
            }

            val affected: MutableSet<Int> = mutableSetOf()
            for (param in params) {
                val arr = if (param in paramToDependentItems.indices) paramToDependentItems[param] else intArrayOf()
                for (item in arr) affected.add(item)
            }
            for (itemIndex in affected) {
                val photoItem = if (itemIndex in itemsByIndex.indices) itemsByIndex[itemIndex] else null
                val update = if (itemIndex in itemUpdateFunctions.indices) itemUpdateFunctions[itemIndex] else null
                if (photoItem != null && update != null) update(photoItem, values)
            }
        } catch (e: InvalidValues) {
            //rollback
            updateParams(params, previous, false)
            throw e
        }

        val notified: MutableSet<Int> = mutableSetOf()
        for (param in params) {
            val arr = if (param in paramToDependentItems.indices) paramToDependentItems[param] else intArrayOf()
            for (item in arr) if (notified.add(item)) onItemUpdated(item)
        }
    }
}