package io.rektplorer64.pickerkt.ui.picker.nav

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.rektplorer64.pickerkt.common.data.localizedName
import io.rektplorer64.pickerkt.ui.component.collection.CountBadge
import io.rektplorer64.pickerkt.ui.component.nav.DrawerDividerItem
import io.rektplorer64.pickerkt.ui.picker.nav.data.NavLocation
import io.rektplorer64.pickerkt.ui.picker.nav.data.preset.PresetNavLocation

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationRailContent(
    modifier: Modifier = Modifier,
    selectionInCollectionMap: Map<String, Int>,
    presetNavLocations: List<PresetNavLocation>,
    navRoute: String?,
    onNavIconClick: () -> Unit,
    onClick: (NavLocation) -> Unit
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        header = {
            IconButton(onClick = { onNavIconClick() }) {
                Icon(Icons.Default.Menu, contentDescription = null)
            }
        },
        containerColor = Color.Transparent,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            presetNavLocations
                .groupBy { it.group }
                .entries
                .forEachIndexed { i, (_, u) ->
                    if (i != 0) {
                        DrawerDividerItem()
                    }
                    u.forEach {
                        NavigationRailItem(
                            selected = it.navHostRoute == navRoute,
                            onClick = { onClick(NavLocation.Preset(it)) },
                            label = { Text(it.localizedName, maxLines = 1) },
                            icon = {
                                if (it == PresetNavLocation.AllFolders) {
                                    BadgedBox(
                                        badge = {
                                            val selectionCount =
                                                selectionInCollectionMap.values.fold(0) { x, y -> x + y }
                                            CountBadge(
                                                count = selectionCount,
                                                contentDescription = null,
                                                icon = null
                                            )
                                        }
                                    ) {
                                        Icon(
                                            it.icon,
                                            contentDescription = null
                                        )
                                    }
                                } else {
                                    Icon(
                                        it.icon,
                                        contentDescription = null
                                    )
                                }
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
        }
    }
}