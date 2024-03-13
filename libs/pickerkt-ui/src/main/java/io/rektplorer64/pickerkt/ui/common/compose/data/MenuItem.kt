package io.rektplorer64.pickerkt.ui.common.compose.data

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class MenuItem internal constructor(
    val name: String,
    @StringRes val nameRes: Int?,
    val subtitle: String?,
    val composable: @Composable MenuItem.() -> Unit
) {
    val finalName: String
        @Composable get() = nameRes?.let { stringResource(it) } ?: name

    @Composable
    fun Composable() {
        this.composable()
    }
}

fun menuItemOf(
    name: String,
    @StringRes nameRes: Int?,
    subtitle: String?,
    composable: @Composable MenuItem.() -> Unit
) = MenuItem(name = name, nameRes = nameRes, subtitle = subtitle, composable = composable)

private object AppBarMenuItemDefaults {
    @OptIn(ExperimentalAnimationApi::class)
    val EnterAnimation = fadeIn() + scaleIn()

    @OptIn(ExperimentalAnimationApi::class)
    val ExitAnimation = fadeOut() + scaleOut()
}

fun appBarMenuItemOf(
    name: String,
    @StringRes nameRes: Int?,
    subtitle: String?,
    visible: Boolean = true,
    onClick: () -> Unit,
    composable: @Composable MenuItem.() -> Unit
) = MenuItem(name = name, nameRes = nameRes, subtitle = subtitle) {
    AnimatedVisibility(
        visible = visible,
        enter = AppBarMenuItemDefaults.EnterAnimation,
        exit = AppBarMenuItemDefaults.ExitAnimation
    ) {
        IconButton(onClick = onClick) {
            this@MenuItem.composable()
        }
    }
}
