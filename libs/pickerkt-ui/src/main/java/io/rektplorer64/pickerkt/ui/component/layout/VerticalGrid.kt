package io.rektplorer64.pickerkt.ui.component.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import kotlin.math.floor

@DslMarker
annotation class VerticalGridScopeMarker

@VerticalGridScopeMarker
interface VerticalGridScope {

    fun item(content: @Composable VerticalGridItemScope.() -> Unit)

    fun <T> items(items: List<T>, content: @Composable VerticalGridItemScope.(T) -> Unit)
}

class VerticalGridScopeImpl : VerticalGridScope {

    internal val itemList = mutableListOf<@Composable VerticalGridItemScope.() -> Unit>()

    override fun item(content: @Composable VerticalGridItemScope.() -> Unit) {
        itemList.add(content)
    }

    override fun <T> items(items: List<T>, content: @Composable VerticalGridItemScope.(T) -> Unit) {
        items.forEach {
            itemList.add { content(it) }
        }
    }
}


interface VerticalGridItemScope {

    fun Modifier.fillParentMaxWidth(fraction: Float = 1f): Modifier

}

class VerticalGridItemScopeImpl(val width: Dp) : VerticalGridItemScope {
    override fun Modifier.fillParentMaxWidth(fraction: Float): Modifier {
        return width(width * fraction)
    }
}


@Composable
fun VerticalGrid(
    modifier: Modifier = Modifier,
    mainAxisSize: SizeMode = SizeMode.Wrap,
    mainAxisAlignment: FlowMainAxisAlignment = FlowMainAxisAlignment.Start,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisAlignment: FlowCrossAxisAlignment = FlowCrossAxisAlignment.Start,
    crossAxisSpacing: Dp = 0.dp,
    lastLineMainAxisAlignment: FlowMainAxisAlignment = mainAxisAlignment,
    minWidth: Dp,
    content: VerticalGridScope.() -> Unit
) {

    BoxWithConstraints(modifier = modifier) {
        val width = remember(maxWidth, LocalDensity.current) {

            val availableWidthDp = maxWidth.value
            val minWidthDp = minWidth.value

            if (minWidthDp >= availableWidthDp) {
                return@remember availableWidthDp.dp
            }

            val nColumns = floor(availableWidthDp / minWidthDp).coerceAtLeast(1f)
            val spacingDp = mainAxisSpacing.value
            val totalSpacingDp = spacingDp * (nColumns - 1)

            val leftoverWidthDp = availableWidthDp - (minWidthDp * nColumns)

            val remainingWidthForItemsDp = leftoverWidthDp - totalSpacingDp
            val extraWidthForEachItemDp = remainingWidthForItemsDp / nColumns

            floor(minWidthDp + extraWidthForEachItemDp).dp
        }

        val scope = remember(content) { VerticalGridScopeImpl().apply(content) }
        val itemScope = remember(width) { VerticalGridItemScopeImpl(width) }


        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSize = mainAxisSize,
            mainAxisAlignment = mainAxisAlignment,
            mainAxisSpacing = mainAxisSpacing,
            crossAxisAlignment = crossAxisAlignment,
            crossAxisSpacing = crossAxisSpacing,
            lastLineMainAxisAlignment = lastLineMainAxisAlignment,
        ) {
            scope.itemList.forEach {
                Box(modifier = Modifier.width(width)) {
                    it(itemScope)
                }
            }
        }
    }
}