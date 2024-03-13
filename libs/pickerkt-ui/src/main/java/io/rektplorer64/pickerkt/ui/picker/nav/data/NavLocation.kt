package io.rektplorer64.pickerkt.ui.picker.nav.data

import io.rektplorer64.pickerkt.ui.common.data.extension.navHostRoute
import io.rektplorer64.pickerkt.ui.picker.nav.data.preset.PresetNavLocation
import io.rektplorer64.pickerkt.collection.model.CollectionBase

sealed class NavLocation(
    val navHostRoute: String,
    val id: String
) {
    class Preset(presetNavLocation: PresetNavLocation) : NavLocation(
        id = if (presetNavLocation.correspondingCollection != null) {
            locationIdOfCollection(presetNavLocation.correspondingCollection!!.id)
        } else {
            locationIdOfLibrary(presetNavLocation.name)
        },
        navHostRoute = presetNavLocation.navHostRoute,
    )

    class Constant(route: String) : NavLocation(
        id = route,
        navHostRoute = route,
    )

    class Collection(collection: CollectionBase) : NavLocation(
        id = locationIdOfCollection(collection),
        navHostRoute = collection.navHostRoute,
    )

    companion object {
        inline fun locationIdOfCollection(id: String) = "collection-$id"
        inline fun locationIdOfCollection(collection: CollectionBase) = "collection-${collection.id}"
        inline fun locationIdOfLibrary(id: String) = "library-$id"
    }
}
