package io.rektplorer64.pickerkt.ui.component.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


object MediumSizeListItemDefaults {
    val Border: BorderStroke
        @Composable get() = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )

    val BorderLight: BorderStroke
        @Composable get() = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MediumSizeListItem(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit,
    icon: @Composable BoxWithConstraintsScope.() -> Unit,
    border: BorderStroke? = MediumSizeListItemDefaults.Border,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {},
) {
    val height by animateDpAsState(
        targetValue = if (!compact) 128.dp else 72.dp
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            ),
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(16.dp),
        border = border,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            BoxWithConstraints(
                modifier = Modifier
                    .padding(4.dp)
                    .height(height)
                    .aspectRatio(3 / 4f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
                content = icon
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                ProvideTextStyle(value = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)) {
                    Box(
                        modifier = Modifier.padding(end = 16.dp),
                        content = { title() }
                    )
                }

                ProvideTextStyle(value = MaterialTheme.typography.bodySmall) {
                    Box(
                        modifier = Modifier.padding(end = 16.dp),
                        content = { subtitle() }
                    )
                }

//                                Row(
//                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                ) {
//                                    collection.contentGroupCounts?.forEach { (t, u) ->
//                                        Chip(
//                                            size = ChipSize.Compact,
//                                            leadingIcon = {
//                                                Icon(
//                                                    painter = painterResource(t.icon),
//                                                    contentDescription = t.displayName
//                                                )
//                                            },
//                                        ) {
//                                            Text(text = u.toString())
//                                        }
//                                    }
//                                }
            }
        }
    }
}

@Composable
fun MediumSizeListItem(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    border: BorderStroke? = MediumSizeListItemDefaults.Border,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit,
    icon: @Composable BoxWithConstraintsScope.() -> Unit,
    title: String,
    subtitle: String? = null,
) {
    MediumSizeListItem(
        modifier = modifier,
        compact = compact,
        border = border,
        onLongClick = onLongClick,
        onClick = onClick,
        icon = icon,
        title = {
            Text(
                text = title,
                maxLines = if (!compact) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        subtitle = {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        },
    )
}