package io.rektplorer64.pickerkt.ui.component.collection

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.rektplorer64.pickerkt.collection.model.Collection
import io.rektplorer64.pickerkt.common.unit.formatAsHumanReadableString
import io.rektplorer64.pickerkt.ui.component.list.MediumSizeListItem

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CollectionGridItem(
    modifier: Modifier = Modifier,
    collection: Collection,
    onLongClick: () -> Unit = {},
    compact: Boolean = false,
    onClick: () -> Unit
) {
    MediumSizeListItem(
        modifier = modifier,
        onClick = onClick,
        icon = {
            AsyncImage(
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .data(collection.lastContentItem?.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                error = {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        },
        compact = compact,
        title = collection.name,
        subtitle = collection.size.formatAsHumanReadableString()
    )
}