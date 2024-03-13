package io.rektplorer64.pickerkt.ui.viewer.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.common.data.data
import io.rektplorer64.pickerkt.common.unit.formatAsHumanReadableString
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.ui.common.compose.animation.slideContentTransitionSpec
import io.rektplorer64.pickerkt.ui.common.compose.data.randomizeStringForPlaceholder
import io.rektplorer64.pickerkt.ui.common.compose.shimmerPlaceholder
import io.rektplorer64.pickerkt.ui.common.data.extension.icon
import io.rektplorer64.pickerkt.ui.component.common.DragHandler
import io.rektplorer64.pickerkt.util.getActualFilePath
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DetailPane(
    modifier: Modifier = Modifier,
    content: Result<Content>,
    sidePaneTopBarVisible: Boolean = false,
    onCloseClick: () -> Unit
) {

    val context = LocalContext.current
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 24.dp)) {

        item {
            if (sidePaneTopBarVisible) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCloseClick) {
                            Icon(Icons.Default.Close, contentDescription = "Close Detail Pane")
                        }

                        Text(text = "Info", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                DragHandler()
            }
        }

        item {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedContent(
                    modifier = Modifier.weight(1f),
                    targetState = content,
                    transitionSpec = slideContentTransitionSpec { it.data?.id ?: 0 }
                ) {
                    Column {
                        Text(
                            text = it.data?.name ?: randomizeStringForPlaceholder(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.shimmerPlaceholder(visible = it.data == null)
                        )

                        Text(
                            text = it
                                .data
                                ?.mimeType
                                ?.let { "${it.displayName.uppercase()} • ${it.id}" }
                                ?: randomizeStringForPlaceholder(),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .paddingFromBaseline(top = 16.dp)
                                .shimmerPlaceholder(visible = it.data == null)
                        )
                    }
                }

                when (content) {
                    Result.Loading -> {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                                .size(24.dp)
                                .padding(8.dp)
                        )
                    }
                    is Result.Success -> {
                        AnimatedContent(targetState = content.data.mimeType) {
                            Icon(
                                painterResource(it.icon),
                                contentDescription = it.displayName,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                                    .padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        when (content) {
            Result.Loading -> {
                repeat(5) {
                    item {
                        DetailPaneItemPlaceholder()
                    }
                }
            }
            is Result.Success -> {
                items(
                    listOf(
                        Triple(
                            "Path",
                            content.data.uri.getActualFilePath(context) ?: "Unknown",
                            Icons.Outlined.Folder
                        ),
                        Triple(
                            "Size",
                            content.data.size.formatAsHumanReadableString(),
                            Icons.Outlined.DonutSmall
                        ),
                        Triple(
                            "Time Added",
                            content.data.dateAdded.atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                            Icons.Outlined.DateRange
                        ),
                        Triple(
                            "Android Content ID",
                            content.data.id.toString(),
                            Icons.Outlined.Attachment
                        ),
                        Triple(
                            "Android Content URI",
                            content.data.uri.toString(),
                            Icons.Outlined.Link
                        ),
                    )
                ) { (title, value, icon) ->
                    DetailPaneItem(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = { /*TODO*/ },
                        title = { Text(title) },
                        subtitle = {
                            AnimatedContent(
                                targetState = content,
                                transitionSpec = slideContentTransitionSpec { it.data.id }
                            ) {
                                Text(value, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        icon = {
                            Icon(icon, contentDescription = null)
                        },
                        enabled = false
                    )
                }
            }
        }
    }
}

