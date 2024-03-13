package io.rektplorer64.pickerkt.ui.picker.nav

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.common.data.data
import io.rektplorer64.pickerkt.collection.model.Collection
import io.rektplorer64.pickerkt.collection.model.finalName
import io.rektplorer64.pickerkt.common.data.localizedName
import io.rektplorer64.pickerkt.ui.component.collection.CountBadge
import io.rektplorer64.pickerkt.ui.component.nav.DrawerGroupLabelItem
import io.rektplorer64.pickerkt.ui.component.nav.DrawerMenuItem
import io.rektplorer64.pickerkt.ui.component.nav.DrawerCollectionItem
import io.rektplorer64.pickerkt.ui.layout.PickerAppDrawerState
import io.rektplorer64.pickerkt.ui.picker.nav.data.NavLocation
import io.rektplorer64.pickerkt.ui.picker.nav.data.preset.PresetNavLocation
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
fun DrawerContent(
    modifier: Modifier = Modifier,
    drawerState: PickerAppDrawerState,
    collections: Result<List<Collection>>,
    selectionInCollectionMap: Map<String, Int>,
    currentRoute: String,
    presetNavLocations: List<PresetNavLocation>,
    onClick: (NavLocation) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
    ) {
        item {
            Spacer(modifier = Modifier.statusBarsHeight())
        }

        item {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Media Picker", style = MaterialTheme.typography.headlineSmall)

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                ) {
                    AnimatedContent(targetState = drawerState.isOpen, transitionSpec = { scaleIn() with scaleOut() }) {
                        Icon(
                            if (it) Icons.Default.MenuOpen else Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        presetNavLocations.groupBy { it.group }.entries.forEachIndexed { i, (t, u) ->
            item {
                DrawerGroupLabelItem(title = t.localizedName, showDivider = i != 0)
            }
            u.forEach {
                item {
                    DrawerMenuItem(
                        title = it.localizedName,
                        leadingIcon = {
                            Icon(
                                it.icon,
                                contentDescription = null
                            )
                        },
                        onClick = { onClick(NavLocation.Preset(it)) },
                        selected = it.navHostRoute == currentRoute,
                        trailingIcon = if (it == PresetNavLocation.AllFolders || it == PresetNavLocation.Download) {
                            {
                                val count = when (it) {
                                    PresetNavLocation.AllFolders -> selectionInCollectionMap.toList()
                                        .foldRight(0) { p, n -> p.second + n }
                                    PresetNavLocation.Download -> selectionInCollectionMap[it.correspondingCollection!!.id]
                                        ?: 0
                                    else -> 0
                                }
                                CountBadge(
                                    count = count,
                                    contentDescription = "Folder named \"${it.localizedName}\" has $count items selected"
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        item {
            DrawerGroupLabelItem(title = "Folders", showDivider = true)
        }

        repeat(if (collections is Result.Loading) 10 else 0) {
            item {
                DrawerCollectionItem(
                    enabled = false,
                    onClick = {},
                    name = null,
                    info = null,
                    imageUri = null,
                    selected = false,
                    trailingIcon = {},
                    loading = true
                )
            }
        }

        collections.data?.let { list ->
            itemsIndexed(list) { _, it ->
                DrawerCollectionItem(
                    onClick = { onClick(NavLocation.Collection(it)) },
                    name = it.finalName,
                    info = "${it.contentCount} • ${it.relativeTimeString}",
                    imageUri = it.lastContentItem?.uri,
                    selected = currentRoute.substringAfter("/").contains(it.id),
                    trailingIcon = {
                        val count = selectionInCollectionMap[it.id]
                        CountBadge(
                            count,
                            contentDescription = "Folder named \"${it.finalName}\" has $count items selected"
                        )
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.navigationBarsHeight())
        }
    }
}