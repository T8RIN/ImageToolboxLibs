package com.t8rin.palette.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette

/**
 * Composable to display a color swatch
 */
@Composable
fun ColorSwatch(
    color: PALColor,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    showName: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(size),
            colors = CardDefaults.cardColors(
                containerColor = color.toColor()
            )
        ) {}
        if (showName && color.name.isNotEmpty()) {
            Text(
                text = color.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Composable to display a palette as a row of color swatches
 */
@Composable
fun PaletteView(
    palette: PALPalette,
    modifier: Modifier = Modifier,
    swatchSize: androidx.compose.ui.unit.Dp = 48.dp,
    showNames: Boolean = true
) {
    Column(modifier = modifier) {
        if (palette.name.isNotEmpty()) {
            Text(
                text = palette.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(palette.colors) { color ->
                ColorSwatch(
                    color = color,
                    size = swatchSize,
                    showName = showNames
                )
            }
        }
    }
}

/**
 * Composable to display a gradient as a row of color swatches
 */
@Composable
fun GradientView(
    gradient: com.t8rin.palette.PALGradient,
    modifier: Modifier = Modifier,
    swatchCount: Int = 20,
    swatchSize: androidx.compose.ui.unit.Dp = 48.dp
) {
    val colors = gradient.colors(swatchCount)
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(colors) { color ->
            Card(
                modifier = Modifier.size(swatchSize),
                colors = CardDefaults.cardColors(
                    containerColor = color.toColor()
                )
            ) {}
        }
    }
}

/**
 * Composable to display a gradient as a continuous bar
 */
@Composable
fun GradientBar(
    gradient: com.t8rin.palette.PALGradient,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 48.dp
) {
    val colors = gradient.colors(100) // Sample 100 colors for smooth gradient
    Box(
        modifier = modifier.height(height)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(color.toColor()),
                    contentAlignment = Alignment.Center
                ) {}
            }
        }
    }
}

