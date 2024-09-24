package com.t8rin.opencv_tools.autocrop.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropEdges(val top: Int, val bottom: Int) : Parcelable {

    constructor(edges: Pair<Int, Int>) : this(edges.first, edges.second)

    constructor(edges: List<Int>) : this(edges.first(), edges.last())

    val height: Int
        get() = bottom - top
}