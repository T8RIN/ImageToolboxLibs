package io.rektplorer64.pickerkt.ui.common.compose.animation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable

@Composable
fun AnimatedAppBarIconVisibility(visible: Boolean, icon: @Composable AnimatedVisibilityScope.() -> Unit) = AnimatedVisibility(
    visible = visible,
    enter = slideInVertically { -it } + fadeIn(),
    exit = slideOutVertically { -it } + fadeOut(),
    content = icon
)