package io.rektplorer64.pickerkt.ui.common.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

enum class WindowSize {
    Compact,
    Medium,
    Expanded;
}

val LocalWindowSize = compositionLocalOf { WindowSize.Compact }

@Composable
fun Activity.rememberWindowSizeClass(): WindowSize {

    val configuration = LocalConfiguration.current
    val windowMetrics = remember(configuration) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    }

    val windowDpSize = with(LocalDensity.current) {
        windowMetrics.bounds.toComposeRect().size.toDpSize()
    }

    return when {
        windowDpSize.width < 600.dp -> WindowSize.Compact
        windowDpSize.width < 840.dp -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
}