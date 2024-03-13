package io.rektplorer64.pickerkt.ui.component.nav

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DrawerDividerItem(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outline
    val strokeWidth = with(LocalDensity.current) { 1.dp.toPx() }

    Canvas(
        modifier = modifier
            .padding(vertical = 16.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        drawLine(
            color = color,
            start = Offset.Zero,
            end = Offset(size.width, 0f),
            cap = StrokeCap.Round,
            strokeWidth = strokeWidth
        )
    }
}