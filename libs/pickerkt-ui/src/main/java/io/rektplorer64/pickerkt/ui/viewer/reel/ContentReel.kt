package io.rektplorer64.pickerkt.ui.viewer.reel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemsIndexed
import coil.compose.AsyncImage
import coil.compose.AsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.MimeType
import io.rektplorer64.pickerkt.ui.common.compose.shimmerPlaceholder
import io.rektplorer64.pickerkt.ui.common.data.extension.icon
import io.rektplorer64.pickerkt.ui.selection.ContentSelectionController

@Composable
@OptIn(
    ExperimentalSnapperApi::class,
    ExperimentalPagerApi::class
)
fun ContentReel(
    modifier: Modifier = Modifier,
    state: LazyListState,
    currentContent: Content?,
    contents: LazyPagingItems<Content>,
    selectionController: ContentSelectionController,
    onClick: (index: Int) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        LazyRow(
            state = state,
            modifier = Modifier.height(FocusedReelItemHeight),
            flingBehavior = rememberSnapperFlingBehavior(
                lazyListState = state,
                snapOffsetForItem = SnapOffsets.Center
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(horizontal = maxWidth / 2)
        ) {
            itemsIndexed(
                contents,
                key = { _, it -> it.id }
            ) { i, it ->

                it ?: return@itemsIndexed
                val isCurrentContent by rememberUpdatedState(newValue = currentContent?.id == it.id)

                ContentReelItem(
                    selectionIndex = selectionController.canonicalSelectionList.indexOfFirst { x -> it.id == x.id },
                    highlighted = isCurrentContent,
                    onClick = { onClick(i) }
                ) {
                    when (it.mimeType.group) {
                        MimeType.Group.Image -> AsyncImage(
                            ImageRequest.Builder(LocalContext.current)
                                .data(it.uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        ) {
                            when (it) {
                                AsyncImagePainter.State.Empty,
                                is AsyncImagePainter.State.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .shimmerPlaceholder(visible = true)
                                    )
                                }
                                is AsyncImagePainter.State.Success -> {
                                    AsyncImageContent()
                                }
                                is AsyncImagePainter.State.Error -> {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .background(Color.Red)
                                    )
                                }
                            }
                        }
                        else -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(it.mimeType.group.icon),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}