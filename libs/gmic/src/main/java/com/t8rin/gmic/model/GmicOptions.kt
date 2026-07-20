package com.t8rin.gmic.model

data class GmicOptions(
    val alphaMode: GmicAlphaMode = GmicAlphaMode.Preserve,
    /** Zero-based image index. Negative values select from the end of the G'MIC image list. */
    val outputIndex: Int = 0
)
