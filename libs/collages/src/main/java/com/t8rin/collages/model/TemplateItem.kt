package com.t8rin.collages.model

import android.net.Uri
import com.t8rin.collages.view.PhotoItem


internal data class TemplateItem(
    val preview: Uri,
    val title: String,
    val photoItemList: List<PhotoItem> = emptyList()
)