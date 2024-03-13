package io.rektplorer64.pickerkt.ui.picker.library

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.map
import io.rektplorer64.pickerkt.LazyListItem
import io.rektplorer64.pickerkt.collection.model.Collection
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.common.data.collectAsResultState
import io.rektplorer64.pickerkt.common.data.data
import io.rektplorer64.pickerkt.common.data.localizedName
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.ui.common.compose.animation.AnimatedAppBarIconVisibility
import io.rektplorer64.pickerkt.ui.common.compose.shouldDisplayMenuIconOnAppBar
import io.rektplorer64.pickerkt.ui.component.SectionHeader
import io.rektplorer64.pickerkt.ui.component.collection.CollectionGridItem
import io.rektplorer64.pickerkt.ui.component.common.Placeholder
import io.rektplorer64.pickerkt.ui.component.common.SectionLoadingIndicator
import io.rektplorer64.pickerkt.ui.component.content.contentItems
import io.rektplorer64.pickerkt.ui.component.layout.VerticalGrid
import io.rektplorer64.pickerkt.ui.component.list.MediumSizeListItem
import io.rektplorer64.pickerkt.ui.component.list.MediumSizeListItemDefaults
import io.rektplorer64.pickerkt.ui.picker.folder.CollectionListScreen
import io.rektplorer64.pickerkt.ui.picker.nav.data.preset.PresetNavLocation
import kotlinx.coroutines.flow.map


@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel,
    navLocation: PresetNavLocation,
    selectionMap: Map<Long, Int>,
    onNavIconClick: () -> Unit,
    onContentItemClick: (Content) -> Unit,
    onContentCheckClick: (Content) -> Unit,
    onCollectionClick: (Collection) -> Unit
) {

    val collections by viewModel.collectionList.collectAsResultState()

    val latestContents = remember {
        viewModel.recentContentList.map { it.map { LazyListItem.Data(it) } }
    }
        .collectAsLazyPagingItems()

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "library",
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        composable(
            route = "library",
        ) {
            DefaultLibraryScreen(
                modifier = modifier,
                navLocation = navLocation,
                collections = collections,
                latestContents = latestContents as LazyPagingItems<LazyListItem<out Content>>,
                maxVisibleFolders = 30,
                selectionMap = selectionMap,
                onNavIconClick = onNavIconClick,
                onContentCheckClick = onContentCheckClick,
                onContentItemClick = onContentItemClick,
                onAllFolderClick = { navController.navigate("FolderList") },
                onCollectionClick = onCollectionClick,
            )
        }

        composable(
            route = "FolderList"
        ) {
            navController.backQueue.map { it.destination.route }

            CollectionListScreen(
                collections = collections,
                onCollectionClick = onCollectionClick,
                onNavIconClick = { navController.navigateUp() }
            )
        }
    }
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
fun DefaultLibraryScreen(
    modifier: Modifier = Modifier,
    maxVisibleFolders: Int = Int.MAX_VALUE,
    navLocation: PresetNavLocation,
    collections: Result<List<Collection>>,
    latestContents: LazyPagingItems<LazyListItem<out Content>>,
    onNavIconClick: () -> Unit,
    selectionMap: Map<Long, Int>,
    onContentItemClick: (Content) -> Unit,
    onContentCheckClick: (Content) -> Unit,
    onAllFolderClick: () -> Unit,
    onCollectionClick: (Collection) -> Unit
) {

    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LargeTopAppBar(
            title = { Text(text = navLocation.localizedName) },
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                AnimatedAppBarIconVisibility(shouldDisplayMenuIconOnAppBar()) {
                    IconButton(
                        onClick = { onNavIconClick() },
                        content = { Icon(Icons.Default.Menu, contentDescription = null) }
                    )
                }
            },
            actions = {

            }
        )

        LazyVerticalGrid(
            cells = GridCells.Adaptive(140.dp),
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {

            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItemPlacement()
                        .animateContentSize()
                ) {
                    val folderDifferenceCount = (collections.data?.size ?: 0) - maxVisibleFolders

                    SectionHeader(
                        modifier = Modifier,
                        title = { Text("Folders".uppercase()) },
                        action = {
                            AnimatedVisibility(visible = folderDifferenceCount >= 0) {
                                TextButton(onClick = { onAllFolderClick() }) {
                                    Text("See All folders")
                                }
                            }
                        }
                    )
                    when (collections) {
                        Result.Loading -> SectionLoadingIndicator()
                        is Result.Error -> {
                        }
                        is Result.Success -> {
                            val collectionList = collections.data
                            AnimatedContent(collectionList.isEmpty()) { isEmpty ->
                                if (isEmpty) {
                                    Placeholder(
                                        title = { Text("No Collection on your device") },
                                        subtitle = { Text("Add some images so that it will show here.") }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Image,
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
                                } else {
                                    VerticalGrid(
                                        minWidth = 200.dp,
                                        modifier = Modifier.fillMaxWidth(),
                                        crossAxisSpacing = 8.dp,
                                        mainAxisSpacing = 8.dp
                                    ) {
                                        items(collectionList.take(maxVisibleFolders)) { c ->
                                            CollectionGridItem(
                                                modifier = Modifier.fillParentMaxWidth(),
                                                compact = true,
                                                collection = c,
                                                onClick = { onCollectionClick(c) }
                                            )
                                        }

                                        if (folderDifferenceCount > 0) {
                                            item {
                                                MediumSizeListItem(
                                                    modifier = Modifier.fillParentMaxWidth(1.2f),
                                                    compact = true,
                                                    onClick = { /*TODO*/ },
                                                    border = MediumSizeListItemDefaults.BorderLight,
                                                    icon = {
                                                        Icon(
                                                            Icons.Outlined.FolderOpen,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    title = "$folderDifferenceCount more folders"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Spacer(modifier = Modifier.fillMaxWidth())
            }

            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Text(
                    "Most Recent Items".uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            contentItems(
                content = latestContents,
                mimeFilterSet = emptySet(),
                selectionVisible = true,
                selectionMap = selectionMap,
                onContentItemClick = onContentItemClick,
                onContentCheckClick = onContentCheckClick
            )
        }

//        collections.data?.let { list ->
//            var state by remember { mutableStateOf(CollapsibleSectionState.HalfExpanded) }
//            CollapsibleSection(
//                state = state,
//                halfExpandedHeight = 250.dp,
//                footer = {
//                    val rotation by animateFloatAsState(targetValue = if (state == CollapsibleSectionState.HalfExpanded) 180f else 0f)
//
//                    Surface(
//                        modifier = Modifier.fillMaxSize(),
//                        onClick = {
//                            state = when (state) {
//                                CollapsibleSectionState.HalfExpanded -> CollapsibleSectionState.Expanded
//                                CollapsibleSectionState.Expanded -> CollapsibleSectionState.HalfExpanded
//                                else -> CollapsibleSectionState.Expanded
//                            }
//                        }
//                    ) {
//                        Icon(
//                            Icons.Default.KeyboardArrowUp, contentDescription = null,
//                            modifier = Modifier
//                                .padding(16.dp)
//                                .rotate(rotation)
//                        )
//                    }
//
//                    Divider(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .align(Alignment.TopCenter)
//                    )
//                },
//                header = {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(16.dp),
//                            modifier = Modifier.padding(vertical = 4.dp)
//                        ) {
//                            Icon(Icons.Default.Folder, contentDescription = null)
//
//                            Text(
//                                text = "Folders".uppercase(),
//                                modifier = Modifier
//                                    .padding(vertical = 8.dp),
//                                style = MaterialTheme.typography.labelLarge
//                            )
//                        }
//
//                        IconButton(
//                            onClick = {},
//                        ) {
//                            Icon(
//                                Icons.Default.ChevronRight, contentDescription = null
//                            )
//                        }
//                    }
//                }
//            ) {
//
//                // List of Collections with that kind of type
//                LazyVerticalGrid(
//                    cells = GridCells.Adaptive(190.dp),
//                    modifier = Modifier
//                        .nestedScroll(scrollBehavior.nestedScrollConnection)
//                        .height(500.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    contentPadding = PaddingValues(16.dp)
//                ) {
//                    items(items = list) { collection ->
//                        CollectionGridItem(
//                            modifier = Modifier.fillParentMaxWidth(),
//                            compact = true,
//                            collection = collection,
//                            onClick = {
//                            }
//                        )
//                    }
//                }
//            }
//
//        }

        // Recent File Section

//        MainContentPickerGrid(
//            modifier = Modifier.height(500.dp),
//            mimeFilterSet = emptySet(),
//            content = latestContents,
//            selection = listOf(),
//            onContentItemClick = {
//
//            },
//            onContentCheckClick = {
//
//            }
//        )
    }
}


enum class CollapsibleSectionState {
    Collapsed,
    HalfExpanded,
    Expanded
}

val CollapsibleSectionMaxThreshold = 600.dp

@Composable
fun CollapsibleSection(
    modifier: Modifier = Modifier,
    state: CollapsibleSectionState = CollapsibleSectionState.HalfExpanded,
    halfExpandedHeight: Dp = 360.dp,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    val transition = updateTransition(targetState = state, label = "State")

    val density = LocalDensity.current
    var trueHeight by remember { mutableStateOf(Dp.Unspecified) }

    val preferredHeight =
        if (trueHeight == Dp.Unspecified) CollapsibleSectionMaxThreshold else trueHeight

    val height by transition.animateDp(
        label = "Section height",
        transitionSpec = { spring() }
    ) {
        when (it) {
            CollapsibleSectionState.Collapsed -> 0.dp
            CollapsibleSectionState.HalfExpanded -> halfExpandedHeight
            CollapsibleSectionState.Expanded -> preferredHeight
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .animateContentSize(animationSpec = spring()) { _, target ->
                if (state == CollapsibleSectionState.Expanded && trueHeight == Dp.Unspecified) {
                    trueHeight = with(density) {
                        target.height.toDp()
                    }
                }
            }
            .wrapContentHeight()
            .fillMaxWidth()
            .height(if (height < preferredHeight) height else trueHeight),
        content = content
    )

}

@Composable
fun CollapsibleSection(
    modifier: Modifier = Modifier,
    state: CollapsibleSectionState = CollapsibleSectionState.HalfExpanded,
    halfExpandedHeight: Dp = 360.dp,
    header: @Composable BoxScope.() -> Unit = {},
    footer: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            content = header
        )
        CollapsibleSection(
            state = state,
            halfExpandedHeight = halfExpandedHeight,
            content = content
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            content = footer
        )
    }
}