package io.rektplorer64.pickerkt.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun Placeholder(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    foregroundColor: Color = LocalContentColor.current,
    subtitle: @Composable () -> Unit = {},
    actions: @Composable ColumnScope.() -> Unit = {},
    title: @Composable () -> Unit,
    hero: @Composable () -> Unit
) {

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalArrangement
    ) {
        Box(modifier = Modifier.size(128.dp)) {
            hero()
        }

        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides foregroundColor) {
                ProvideTextStyle(value = MaterialTheme.typography.titleLarge) {
                    title()
                }
            }
        }


        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides foregroundColor) {
                ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                    subtitle()
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            actions()
        }
    }
}

@Composable
fun CircularHeroIconPlaceholder(
    heroIcon: ImageVector,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    subtitle: @Composable () -> Unit = {},
    actions: @Composable ColumnScope.() -> Unit = {},
    title: @Composable () -> Unit,
) {
    Placeholder(
        modifier = modifier,
        title = title,
        verticalArrangement = verticalArrangement,
        subtitle = subtitle,
        actions = actions,
    ) {
        Icon(
            heroIcon,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .padding(32.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun CircularHeroIconPlaceholder(
    heroIcon: Painter,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    subtitle: @Composable () -> Unit = {},
    actions: @Composable ColumnScope.() -> Unit = {},
    title: @Composable () -> Unit,
) {
    Placeholder(
        modifier = modifier,
        title = title,
        verticalArrangement = verticalArrangement,
        subtitle = subtitle,
        actions = actions,
    ) {
        Icon(
            painter = heroIcon,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .padding(32.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}