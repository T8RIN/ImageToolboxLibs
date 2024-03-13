package io.rektplorer64.pickerkt.ui.layout

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.rektplorer64.pickerkt.ui.common.compose.LocalWindowSize
import io.rektplorer64.pickerkt.ui.common.compose.WindowSize
import kotlin.math.roundToInt


class SidePaneBottomSheetLayoutState(
    initialVisibility: Boolean = false
) {
    private val _state = mutableStateOf(initialVisibility)
    val state: State<Boolean> = _state

    fun setValue(value: Boolean) {
        _state.value = value
    }

    fun toggle() {
        _state.value = !_state.value
    }

    fun open() {
        _state.value = true
    }

    fun close() {
        _state.value = false
    }
}

@Composable
fun rememberSidePaneBottomSheetLayoutState(initialVisibility: Boolean = false) = remember {
    SidePaneBottomSheetLayoutState(initialVisibility)
}

data class ContentScope constructor(
    val bottomSheetVisible: Boolean,
    val drawerVisible: Boolean,
)

data class SwipeableScope @OptIn(ExperimentalMaterialApi::class) constructor(
    val sidePaneVisibility: Boolean = false,
    val swipeableState: SwipeableState<Int>,
    private val boxScope: BoxScope
) : BoxScope by boxScope

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SidePaneBottomSheetLayout(
    modifier: Modifier = Modifier,
    state: SidePaneBottomSheetLayoutState,
    swipeGestureDistance: Dp = 25.dp,
    drawerWidth: Dp = 360.dp,
    paneContent: @Composable ContentScope.(asSidePane: Boolean) -> Unit,
    body: @Composable SwipeableScope.() -> Unit
) {
    val windowSize = LocalWindowSize.current
    val scaffoldState = rememberBottomSheetScaffoldState()

    val sidePanelVisible by state.state

    LaunchedEffect(sidePanelVisible, windowSize) {
        with(scaffoldState.bottomSheetState) {
            if (windowSize < WindowSize.Expanded) {
                if (sidePanelVisible) expand() else collapse()
            } else {
                collapse()
            }
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        state.setValue(
            when (scaffoldState.bottomSheetState.currentValue) {
                BottomSheetValue.Collapsed -> false
                BottomSheetValue.Expanded -> true
            }
        )
    }

    val contentScope =
        remember(windowSize, sidePanelVisible, scaffoldState.bottomSheetState.currentValue) {
            ContentScope(
                bottomSheetVisible = when (scaffoldState.bottomSheetState.currentValue) {
                    BottomSheetValue.Collapsed -> false
                    BottomSheetValue.Expanded -> true
                },
                drawerVisible = windowSize >= WindowSize.Expanded && sidePanelVisible
            )
        }
    val swipeableState = rememberSwipeableState(initialValue = 0)
    when (windowSize) {
        WindowSize.Compact,
        WindowSize.Medium -> {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 0.dp,
                sheetGesturesEnabled = false,
                sheetContent = {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                        Box {
                            paneContent(contentScope, false)
                        }
                    }
                },
                sheetShape = if (windowSize <= WindowSize.Medium) {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                } else {
                    RectangleShape
                },
                sheetBackgroundColor = MaterialTheme.colorScheme.background,
                sheetContentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Box {
                    body(
                        remember(sidePanelVisible, this) {
                            SwipeableScope(sidePanelVisible, swipeableState, this)
                        }
                    )
                }
            }
        }
        WindowSize.Expanded -> {

            val swipeDistancePx = with(LocalDensity.current) { swipeGestureDistance.toPx() }
            LaunchedEffect(swipeableState.offset.value) {
                state.setValue(
                    if (windowSize >= WindowSize.Expanded) {
                        swipeableState.currentValue != swipeableState.targetValue && swipeableState.offset.value <= swipeDistancePx
                    } else {
                        false
                    }
                )
            }

            val drawerOffsetDp by animateDpAsState(targetValue = if (sidePanelVisible) 0.dp else drawerWidth)
            val drawerWidthDp by animateDpAsState(targetValue = if (!sidePanelVisible) 0.dp else drawerWidth)

            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        content = {
                            body(
                                remember(sidePanelVisible, this) {
                                    SwipeableScope(sidePanelVisible, swipeableState, this)
                                }
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .zIndex(10f)
                            .fillMaxHeight()
                            .width(drawerWidthDp)
                    )
                }

                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .align(Alignment.TopEnd)
                        .zIndex(10f)
                        .offset {
                            IntOffset(
                                drawerOffsetDp
                                    .toPx()
                                    .roundToInt(), 0
                            )
                        }
                ) {
                    Box {
                        paneContent(contentScope, true)
                    }
                }
            }
        }
    }
}