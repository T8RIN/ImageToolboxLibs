package io.rektplorer64.pickerkt.ui.layout

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.rektplorer64.pickerkt.ui.common.compose.LocalWindowSize
import io.rektplorer64.pickerkt.ui.common.compose.WindowSize
import io.rektplorer64.pickerkt.ui.layout.PickerAppDrawerState.Companion.shouldUseModalDrawer

class AppLayout(boxScope: BoxScope, val windowSize: WindowSize) : BoxScope by boxScope

@OptIn(ExperimentalMaterial3Api::class)
class PickerAppDrawerState internal constructor(
    val materialDrawerState: DrawerState,
    val initialValue: DrawerValue = DrawerValue.Closed,
    internal var windowSize: WindowSize
) {

    var drawerValue by mutableStateOf(initialValue)
        private set

    suspend fun open() {
        drawerValue = DrawerValue.Open
        if (shouldUseModalDrawer(windowSize)) {
            materialDrawerState.open()
        }
    }

    suspend fun close() {
        drawerValue = DrawerValue.Closed
        materialDrawerState.close()
    }

    val isClosed: Boolean
        get() = if (!shouldUseModalDrawer(windowSize)) {
            drawerValue == DrawerValue.Closed
        } else {
            materialDrawerState.currentValue == DrawerValue.Closed
        }

    val isOpen: Boolean
        get() = if (!shouldUseModalDrawer(windowSize)) {
            drawerValue == DrawerValue.Open
        } else {
            materialDrawerState.currentValue == DrawerValue.Open
        }

    companion object {
        fun shouldUseModalDrawer(windowSize: WindowSize): Boolean = windowSize <= WindowSize.Medium

        val allowModalDrawer: Boolean
            @Composable get() = shouldUseModalDrawer(LocalWindowSize.current)

        val Saver = Saver<PickerAppDrawerState, Triple<DrawerValue, DrawerValue, WindowSize>>(
            save = {
                Triple(it.materialDrawerState.currentValue, it.drawerValue, it.windowSize)
            },
            restore = { (materialDrawerValue, drawerValue, windowSize) ->
                PickerAppDrawerState(
                    materialDrawerState = DrawerState(materialDrawerValue),
                    initialValue = drawerValue,
                    windowSize = windowSize
                )
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun rememberPickerAppDrawerState(initialValue: DrawerValue): PickerAppDrawerState {
    val windowSize = LocalWindowSize.current

    val materialDrawerState = rememberDrawerState(
        initialValue = if (shouldUseModalDrawer(windowSize)) initialValue else DrawerValue.Closed
    )

    val pickerAppDrawerState = rememberSaveable(windowSize, saver = PickerAppDrawerState.Saver) {
        PickerAppDrawerState(materialDrawerState = materialDrawerState, windowSize = windowSize)
    }

    LaunchedEffect(windowSize) {
        pickerAppDrawerState.windowSize = windowSize
    }

    LaunchedEffect(pickerAppDrawerState.drawerValue) {
        if (shouldUseModalDrawer(windowSize)) {
            when (pickerAppDrawerState.drawerValue) {
                DrawerValue.Closed -> pickerAppDrawerState.close()
                DrawerValue.Open -> pickerAppDrawerState.open()
            }
        }
    }

    LaunchedEffect(windowSize) {
        with(pickerAppDrawerState) {
            if (!shouldUseModalDrawer(windowSize)) {
                close()
            } else {
                if (isOpen) open() else close()
            }
        }

    }

    LaunchedEffect(pickerAppDrawerState.materialDrawerState.currentValue) {
        when (pickerAppDrawerState.materialDrawerState.currentValue) {
            DrawerValue.Closed -> pickerAppDrawerState.close()
            DrawerValue.Open -> pickerAppDrawerState.open()
        }
    }

    return pickerAppDrawerState
}


@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
fun PickerAppLayout(
    modifier: Modifier = Modifier,
    navColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
    windowSize: WindowSize = LocalWindowSize.current,
    drawerState: PickerAppDrawerState,
    drawerContent: (@Composable ColumnScope.() -> Unit)? = null,
    sidebar: @Composable BoxScope.() -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable AppLayout.() -> Unit
) {

    val windowSizeTransition = updateTransition(targetState = windowSize, label = "Window Size")
    val windowRoundedCorner by windowSizeTransition.animateDp(label = "Rounded Corner") {
        if (it > WindowSize.Compact) 32.dp else 0.dp
    }

    NavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState.materialDrawerState,
        drawerContent = drawerContent ?: {},
        drawerShape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp
        ),
        gesturesEnabled = windowSize <= WindowSize.Medium,
    ) {
        Scaffold(
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = { floatingActionButton() },
        ) {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .background(color = navColor)
            ) {
                AnimatedVisibility(
                    visible = windowSize >= WindowSize.Medium,
                    modifier = Modifier
                        .animateContentSize()
                        .wrapContentSize()
                ) {
                    AnimatedContent(
                        targetState = drawerState.isOpen && !PickerAppDrawerState.allowModalDrawer,
                        modifier = Modifier
                            .animateContentSize()
                            .wrapContentSize()
                    ) {
                        if (it) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(320.dp)
                            ) {
                                if (drawerContent != null) {
                                    drawerContent()
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxHeight()) {
                                sidebar()
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(top = if (windowSize >= WindowSize.Medium) 24.dp else 0.dp)
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = windowRoundedCorner))
                ) {
                    AppLayout(this, windowSize).content()
                }
            }
        }
    }
}