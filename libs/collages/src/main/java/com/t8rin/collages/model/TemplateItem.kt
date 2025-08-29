package com.t8rin.collages.model

import android.net.Uri
import com.t8rin.collages.view.PhotoItem
import com.t8rin.collages.utils.ParamsManager


internal data class TemplateItem(
    val preview: Uri,
    val title: String,
    val paramsManager: ParamsManager? = null,
    val photoItemList: List<PhotoItem> = emptyList()
)