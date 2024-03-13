package io.rektplorer64.pickerkt.ui.viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil.request.ImageRequest
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.common.data.data
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.MimeType
import io.rektplorer64.pickerkt.ui.common.compose.animation.slideContentTransitionSpec
import io.rektplorer64.pickerkt.ui.component.common.CheckMark
import io.rektplorer64.pickerkt.ui.component.common.CheckMarkSize
import io.rektplorer64.pickerkt.ui.component.common.CircularHeroIconPlaceholder
import io.rektplorer64.pickerkt.ui.layout.ContentScope
import io.rektplorer64.pickerkt.ui.layout.SidePaneBottomSheetLayout
import io.rektplorer64.pickerkt.ui.viewer.content.AudioContentViewer
import io.rektplorer64.pickerkt.ui.viewer.content.ImageContentViewer
import io.rektplorer64.pickerkt.ui.viewer.content.VideoContentViewer
import io.rektplorer64.pickerkt.ui.viewer.detail.DetailPane
import io.rektplorer64.pickerkt.ui.selection.ContentSelectionController
import io.rektplorer64.pickerkt.ui.viewer.reel.ContentReel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

object ContentPreviewScreenDefaults {
    val ControlVisibilityEnterTransition = fadeIn()
    val ControlVisibilityExitTransition = fadeOut()
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
)
fun ContentPreviewScreen(
    modifier: Modifier = Modifier,
    state: ContentPreviewState = rememberContentPreviewState(),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    foregroundColor: Color = LocalContentColor.current,
    title: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    info: @Composable (ContentScope.(asSidePane: Boolean) -> Unit),
    actions: @Composable (RowScope.() -> Unit),
    bottomBar: @Composable (RowScope.() -> Unit),
    navigationIcon: @Composable () -> Unit,
    body: @Composable () -> Unit
) {
    val pageContent = @Composable {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .background(backgroundColor)
            ) {
                CompositionLocalProvider(LocalContentColor provides foregroundColor) {
                    body()
                }
            }

            AnimatedVisibility(
                visible = state.controlVisible.value,
                enter = ContentPreviewScreenDefaults.ControlVisibilityEnterTransition,
                exit = ContentPreviewScreenDefaults.ControlVisibilityExitTransition
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            AnimatedVisibility(
                visible = state.controlVisible.value,
                enter = ContentPreviewScreenDefaults.ControlVisibilityEnterTransition,
                exit = ContentPreviewScreenDefaults.ControlVisibilityExitTransition
            ) {
                SmallTopAppBar(
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.TopCenter),
                    title = title,
                    scrollBehavior = null,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        }
    }

    SidePaneBottomSheetLayout(
        state = state.infoState,
        paneContent = info
    ) {
        pageContent()

        AnimatedVisibility(
            visible = state.controlVisible.value,
            enter = ContentPreviewScreenDefaults.ControlVisibilityEnterTransition,
            exit = ContentPreviewScreenDefaults.ControlVisibilityExitTransition,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(256.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .semantics {
                        testTag = "Bottom Gradient Overlay"
                    }
            )
        }

        val sizePx = with(LocalDensity.current) { 50.dp.toPx() }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(65.dp)
                .swipeable(
                    state = swipeableState,
                    anchors = mapOf(0f to 0, sizePx to 1),
                    orientation = Orientation.Vertical,
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                )
                .semantics {
                    testTag = "Bottom Handle for opening info drawer"
                }
        )
        AnimatedVisibility(
            visible = state.controlVisible.value,
            enter = ContentPreviewScreenDefaults.ControlVisibilityEnterTransition,
            exit = ContentPreviewScreenDefaults.ControlVisibilityExitTransition,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        floatingActionButton()
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .semantics {
                            testTag = "Bottom action bar"
                        },
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    content = bottomBar
                )
            }
        }
    }
}


@Composable
@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalSnapperApi::class,
    ExperimentalAnimationApi::class
)
fun ContentPreviewScreenBody(
    modifier: Modifier = Modifier,
    collectionLazyPagingItems: LazyPagingItems<Content>,
    contentId: Long,
    selectionController: ContentSelectionController,
    onCurrentContentSelectionClick: (Content) -> Unit,
    onMainPreviewChange: (Content) -> Unit,
    onBackPress: () -> Unit
) {
    val state = rememberContentPreviewState()
    val coroutineScope = rememberCoroutineScope()

    val contentPagingSnapshot = remember { mutableStateListOf<Content>() }
    LaunchedEffect(
        collectionLazyPagingItems.itemCount,
        collectionLazyPagingItems.itemSnapshotList
    ) {
        contentPagingSnapshot.clear()
        contentPagingSnapshot.addAll(
            collectionLazyPagingItems
                .itemSnapshotList
                .mapNotNull { it }
        )
    }

    val pagerState = rememberPagerState(
        initialPage = collectionLazyPagingItems
            .indexOfFirst { it.id == contentId }
            .takeIf { it >= 0 } ?: 0
    )

    LaunchedEffect(pagerState.targetPage) {
        onMainPreviewChange(collectionLazyPagingItems[pagerState.targetPage]!!)
    }

    val lazyRowState = rememberLazyListState()

    var content by remember { mutableStateOf<Result<Content>>(Result.Loading) }

    LaunchedEffect(contentId) {
        contentId.let { contentId ->
            val contentIndexOnPagingItems = collectionLazyPagingItems
                .itemSnapshotList
                .items
                .indexOfFirst { it.id == contentId }
                .takeIf { it >= 0 } ?: 0

            content = Result.Success(collectionLazyPagingItems[contentIndexOnPagingItems]!!)
            delay(200)
            lazyRowState.animateScrollToItem(contentIndexOnPagingItems)
        }
    }

    val controlVisible by state.controlVisible
    val backgroundColor by animateColorAsState(
        targetValue = if (controlVisible) MaterialTheme.colorScheme.background else Color.Black
    )

    val foregroundColor by animateColorAsState(
        targetValue = if (controlVisible) MaterialTheme.colorScheme.onBackground else Color.White
    )

    val systemUiColor by animateFloatAsState(
        targetValue = if (controlVisible) 0.5f else 0f
    )

    val systemUiController = rememberSystemUiController()
    LaunchedEffect(systemUiColor, controlVisible) {
        systemUiController.setSystemBarsColor(
            color = Color.Black.copy(alpha = systemUiColor),
            darkIcons = false
        )
        systemUiController.isStatusBarVisible = controlVisible
    }

    ContentPreviewScreen(
        modifier = modifier,
        state = state,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        title = {
            AnimatedContent(
                targetState = content,
                transitionSpec = slideContentTransitionSpec { it.data?.id ?: 0 }
            ) {
                Text(text = it.data?.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPress) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val selectionIndex =
                    selectionController.canonicalSelectionList.indexOfFirst { it.id == contentId }
                val isSelected = selectionIndex >= 0

                SmallFloatingActionButton(
                    onClick = { state.toggleInfoSection() },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null)
                }
                ExtendedFloatingActionButton(
                    onClick = { onCurrentContentSelectionClick(content.data!!) },
                    text = {
                        Text(
                            if (isSelected) "Unselect" else "Select",
                            modifier = Modifier.animateContentSize()
                        )
                    },
                    icon = {
                        CheckMark(
                            label = (selectionIndex + 1).toString(),
                            selected = isSelected,
                            size = CheckMarkSize.Mini
                        )
                    }
                )
            }
        },
        info = {
            DetailPane(
                modifier = Modifier.systemBarsPadding(),
                content = content,
                sidePaneTopBarVisible = it,
                onCloseClick = { state.toggleInfoSection() }
            )
        },
        actions = {
            IconButton(onClick = { onCurrentContentSelectionClick(content.data!!) }) {
                val selectionIndex =
                    selectionController.canonicalSelectionList.indexOfFirst { it.id == contentId }
                CheckMark(label = (selectionIndex + 1).toString(), selected = selectionIndex >= 0)
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ContentReel(
                    modifier = Modifier.weight(1f),
                    state = lazyRowState,
                    contents = collectionLazyPagingItems,
                    currentContent = content.data,
                    selectionController = selectionController,
                    onClick = {
                        coroutineScope.launch {
                            if ((pagerState.currentPage - it).absoluteValue <= 10) {
                                pagerState.animateScrollToPage(it)
                            } else {
                                pagerState.scrollToPage(it)
                            }
                        }
                    }
                )
            }
        },
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        HorizontalPager(
            count = contentPagingSnapshot.size,
            state = pagerState,
            key = { contentPagingSnapshot[it].id }
        ) {
            val currentContent = contentPagingSnapshot[it]
            when (currentContent.mimeType.group) {
                MimeType.Group.Image -> {
                    ImageContentViewer(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds(),
                        coilImageModel = ImageRequest.Builder(LocalContext.current)
                            .data(currentContent.uri)
                            .crossfade(true)
                            .build(),
                        onClick = {
                            state.toggleControlVisibility()
                        }
                    )
                }
                MimeType.Group.Video -> {
                    VideoContentViewer(
                        onClick = {
                            state.toggleControlVisibility()
                        },
                        title = {
                            Text(text = currentContent.name)
                        }
                    )
                }
                MimeType.Group.Audio -> {
                    AudioContentViewer(
                        onClick = {
                            state.toggleControlVisibility()
                        },
                        title = {
                            Text(text = currentContent.name)
                        }
                    )
                }
                else -> {
                    CircularHeroIconPlaceholder(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(indication = null, interactionSource = interactionSource) {
                                state.toggleControlVisibility()
                            },
                        heroIcon = Icons.Outlined.Warning
                    ) {
                        Text(text = "Unknown file type, we cannot open it.")
                    }
                }
            }
        }
    }
}


