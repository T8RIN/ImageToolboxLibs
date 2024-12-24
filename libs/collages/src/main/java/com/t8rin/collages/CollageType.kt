package com.t8rin.collages

import com.t8rin.collages.model.TemplateItem

@ConsistentCopyVisibility
data class CollageType internal constructor(
    internal val templateItem: TemplateItem?,
    internal val index: Int?
) {
    companion object {
        val Empty by lazy {
            CollageType(
                templateItem = null,
                index = null
            )
        }
    }
}