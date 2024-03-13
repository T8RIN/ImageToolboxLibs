package io.rektplorer64.pickerkt.ui.viewer.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.rektplorer64.pickerkt.ui.component.common.CircularHeroIconPlaceholder

@Composable
fun AudioContentViewer(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit,
    title: @Composable () -> Unit
) {
    CircularHeroIconPlaceholder(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick
            ),
        heroIcon = Icons.Outlined.Videocam,
        subtitle = {
            Text(text = "We do not support audio right now :(")
        },
        title = title
    )
}