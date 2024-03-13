package io.rektplorer64.pickerkt.ui.common.data.extension

import io.rektplorer64.pickerkt.collection.model.CollectionBase

val CollectionBase.navHostRoute: String
    get() = "collections/$id"