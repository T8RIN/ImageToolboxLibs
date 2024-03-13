package io.rektplorer64.pickerkt.ui.picker.folder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rektplorer64.pickerkt.collection.model.Collection
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.common.data.data
import io.rektplorer64.pickerkt.ui.common.compose.animation.AnimatedAppBarIconVisibility
import io.rektplorer64.pickerkt.ui.component.collection.CollectionGridItem
import io.rektplorer64.pickerkt.ui.component.common.Placeholder

@Composable
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
fun CollectionListScreen(
    modifier: Modifier = Modifier,
    collections: Result<List<Collection>>,
    state: LazyListState = rememberLazyListState(),
    onCollectionClick: (Collection) -> Unit,
    onNavIconClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "Folders") },
                navigationIcon = {
                    AnimatedAppBarIconVisibility(visible = true) {
                        IconButton(onClick = onNavIconClick) {
                            Icon(Icons.Outlined.ArrowUpward, contentDescription = null)
                        }
                    }
                }
            )
        },
    ) {
        AnimatedContent(modifier = modifier, targetState = collections) {
            when(it) {
                Result.Loading -> {
                    CircularProgressIndicator()
                }
                is Result.Success -> {
                    LazyVerticalGrid(
                        state = state,
                        cells = GridCells.Adaptive(220.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        itemsIndexed(items = collections.data ?: return@LazyVerticalGrid) { i, it ->
                            CollectionGridItem(
                                collection = it,
                                onClick = { onCollectionClick(it) }
                            )
                        }
                    }
                }
                is Result.Error -> {
                    Placeholder(title = { Text(text = "Fail to load Collections") }) {
                        Icon(
                            Icons.Outlined.Folder,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                                .padding(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}