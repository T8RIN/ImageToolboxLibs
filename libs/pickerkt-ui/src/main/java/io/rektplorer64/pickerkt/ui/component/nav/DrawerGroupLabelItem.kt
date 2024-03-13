package io.rektplorer64.pickerkt.ui.component.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerGroupLabelItem(modifier: Modifier = Modifier, title: String, showDivider: Boolean = false) {
    Column(modifier = modifier) {
        if (showDivider) {
            DrawerDividerItem()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
                .copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp,
                top = if (showDivider) 0.dp else 24.dp
            )
        )
    }
}