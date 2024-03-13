package io.rektplorer64.pickerkt.ui.component.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import io.rektplorer64.pickerkt.LazyListItem
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.MimeType
import io.rektplorer64.pickerkt.ui.common.compose.itemsIndexed
import org.threeten.bp.ZoneId
import org.threeten.bp.format.TextStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContentPickerGrid(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    scrollConnection: NestedScrollConnection? = null,
    mimeFilterSet: Set<MimeType>,
    content: LazyPagingItems<LazyListItem<out Content>>,
    selectionVisible: Boolean = false,
    selectionMap: Map<Long, Int>,
    onContentItemClick: (Content) -> Unit,
    onContentCheckClick: (Content) -> Unit
) {
    LazyVerticalGrid(
        state = state,
        modifier = modifier
            .let { if (scrollConnection != null) it.nestedScroll(scrollConnection) else it },
        cells = GridCells.Adaptive(120.dp),
        contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.navigationBars),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        contentItems(
            content = content,
            mimeFilterSet = mimeFilterSet,
            selectionVisible = selectionVisible,
            selectionMap = selectionMap,
            onContentItemClick = onContentItemClick,
            onContentCheckClick = onContentCheckClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyGridScope.contentItems(
    content: LazyPagingItems<LazyListItem<out Content>>,
    mimeFilterSet: Set<MimeType>,
    selectionVisible: Boolean = false,
    selectionMap: Map<Long, Int>,
    onContentItemClick: (Content) -> Unit,
    onContentCheckClick: (Content) -> Unit
) {
    itemsIndexed(
        items = content,
        span = {
            when (it) {
                is LazyListItem.Data -> GridItemSpan(1)
                is LazyListItem.TimeGroupHeader -> GridItemSpan(1)
            }
        }
    ) { _, item ->

        if (item == null) {
            Box(modifier = Modifier
                .aspectRatio(1f)
                .fillParentMaxWidth())
            return@itemsIndexed
        }

        when(item) {
            is LazyListItem.Data -> {
                val c = item.data
                CoilContentGridItem(
                    modifier = Modifier.animateItemPlacement(),
                    enabled = selectionVisible && (if (mimeFilterSet.isNotEmpty()) c.mimeType in mimeFilterSet else true),
                    content = c,
                    editModeActivated = true,
                    selectionIndex = selectionMap[c.id] ?: -1,
                    onClick = { onContentItemClick(c) },
                    onCheckClick = { onContentCheckClick(c) }
                )
            }
            is LazyListItem.TimeGroupHeader -> {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val date = item.time.atZone(ZoneId.systemDefault())
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = date.month.getDisplayName(TextStyle.FULL, java.util.Locale.forLanguageTag(Locale.current.toLanguageTag())),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = date.year.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}