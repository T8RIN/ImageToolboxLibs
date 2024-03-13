package io.rektplorer64.pickerkt.ui.common.data.extension

import io.rektplorer64.pickerkt.content.model.Content

fun Content.navHostRouteForPreviewByReferrer(referrer: String): String {
    return "content/preview/$id?referrer=${referrer}"
}