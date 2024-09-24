package com.t8rin.opencv_tools.autocrop.model

data class CropParameters(
    val edges: CropEdges,
    val candidates: List<Int>
)