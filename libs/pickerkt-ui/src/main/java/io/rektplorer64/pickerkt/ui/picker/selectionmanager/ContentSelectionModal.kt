package io.rektplorer64.pickerkt.ui.picker.selectionmanager

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.content.model.groupByMimeTypeAndCount
import io.rektplorer64.pickerkt.contentresolver.MimeType
import io.rektplorer64.pickerkt.ui.common.compose.LocalWindowSize
import io.rektplorer64.pickerkt.ui.R
import io.rektplorer64.pickerkt.ui.common.compose.LocalPickerConfig
import io.rektplorer64.pickerkt.ui.common.compose.WindowSize
import io.rektplorer64.pickerkt.ui.component.collection.CountBadge
import io.rektplorer64.pickerkt.ui.component.common.CircularHeroIconPlaceholder
import io.rektplorer64.pickerkt.ui.component.content.CoilContentGridItem
import io.rektplorer64.pickerkt.ui.component.filter.MimeCollectionFilterBar
import io.rektplorer64.pickerkt.ui.selection.ContentSelectionController
import io.rektplorer64.pickerkt.ui.selection.isNotEmpty
import io.rektplorer64.pickerkt.ui.selection.rememberContentSelectionController
import todo.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ContentSelectionModal(
    modifier: Modifier = Modifier,
    selectedContents: List<Content>,
    onCloseClick: (List<Content>) -> Unit,
    onConfirmed: (List<Content>) -> Unit
) {

    val tempSelectionController = rememberContentSelectionController(
        initialSelections = selectedContents,
        maxSelection = LocalPickerConfig.current.selection.maxSelection ?: Int.MAX_VALUE
    )

    ContentSelectionManagerGrid(
        modifier = modifier
            .padding(
                top = 36.dp,
                bottom = if (LocalWindowSize.current == WindowSize.Compact) 0.dp else 36.dp,
            )
            .fillMaxWidth(fraction = if (LocalWindowSize.current >= WindowSize.Expanded) 0.6f else 1f)
            .clip(RoundedCornerShape(16.dp)),
        contents = selectedContents,
        temporarySelection = tempSelectionController,
        onCloseClick = onCloseClick,
        onConfirmed = onConfirmed
    )
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
private fun ContentSelectionManagerGrid(
    modifier: Modifier = Modifier,
    contents: List<Content>,
    temporarySelection: ContentSelectionController,
    onCloseClick: (List<Content>) -> Unit,
    onConfirmed: (List<Content>) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler {
        // FIXME: This doesn't get called when back press.
        onConfirmed(temporarySelection.canonicalSelectionList)
    }

    val selectedMimes = remember { mutableStateMapOf<MimeType, Unit>() }
    val mimeTypeMap = contents.groupByMimeTypeAndCount()

    fun getSelectionForManipulation() = if (selectedMimes.isEmpty()) {
        contents
    } else {
        contents.filter { it.mimeType in selectedMimes }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                SmallTopAppBar(
                    title = { Text(stringResource(R.string.selection_page_title)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { onCloseClick(temporarySelection.canonicalSelectionList) }
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                getSelectionForManipulation().forEach {
                                    temporarySelection.select(it)
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.SelectAll,
                                contentDescription = stringResource(R.string.selection_action_select_all_visible)
                            )
                        }

                        IconButton(
                            onClick = {
                                getSelectionForManipulation().forEach {
                                    temporarySelection.unselect(it)
                                }
                            }
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_select_remove),
                                contentDescription = stringResource(R.string.selection_button_confirm)
                            )
                        }

                        IconButton(
                            onClick = {
                                getSelectionForManipulation().forEach {
                                    temporarySelection.toggleSelection(it)
                                }
                            }
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_select_inverse),
                                contentDescription = stringResource(R.string.selection_action_invert_selection)
                            )
                        }
                    }
                )

                MimeCollectionFilterBar(
                    mimeTypeMap = mimeTypeMap,
                    selectedMimeSet = selectedMimes.keys,
                    onClick = {
                        if (it in selectedMimes) {
                            selectedMimes.remove(it)
                        } else {
                            selectedMimes.putIfAbsent(it, Unit)
                        }
                    }
                )
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (LocalPickerConfig.current.selection.maxSelection != null) {
                            stringResource(
                                R.string.selection_count_select_with_max,
                                temporarySelection.size,
                                LocalPickerConfig.current.selection.maxSelection!!
                            )
                        } else {
                            stringResource(R.string.selection_count_select, temporarySelection.size)
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                    Button(
                        enabled = temporarySelection.isNotEmpty(),
                        modifier = Modifier.animateContentSize(),
                        onClick = { onConfirmed(temporarySelection.canonicalSelectionList) }
                    ) {

                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Text(
                            modifier = Modifier.padding(end = if (temporarySelection.isNotEmpty()) 8.dp else 0.dp),
                            text = stringResource(R.string.selection_button_confirm)
                        )

                        if (temporarySelection.size > 0) {
                            CountBadge(
                                count = temporarySelection.size,
                                contentDescription = null,
                                icon = null
                            )
                        }
                    }
                }
            }
        }
    ) {

        if (contents.isEmpty()) {
            CircularHeroIconPlaceholder(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                heroIcon = Icons.Outlined.HighlightAlt
            ) {
                Text(text = "You haven't select anything!")
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                state = lazyListState,
                cells = GridCells.Adaptive(120.dp),
                contentPadding = it
            ) {
                itemsIndexed(contents) { _, c ->
                    CoilContentGridItem(
                        content = c,
                        editModeActivated = true,
                        selectionIndex = temporarySelection.canonicalSelectionList.indexOfFirst { x -> x.id == c.id },
                        onClick = { temporarySelection.toggleSelection(c) },
                        onCheckClick = { temporarySelection.toggleSelection(c) },
                        enabled = if (selectedMimes.isEmpty()) true else c.mimeType in selectedMimes
                    )
                }
            }
        }

    }
}