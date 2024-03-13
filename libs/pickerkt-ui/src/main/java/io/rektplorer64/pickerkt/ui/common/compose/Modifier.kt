package io.rektplorer64.pickerkt.ui.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.shimmer

fun Modifier.shimmerPlaceholder(
    visible: Boolean,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color? = null
): Modifier = composed {
    placeholder(
        visible = visible,
        highlight = PlaceholderHighlight.shimmer(
            animationSpec = PlaceholderDefaults.shimmerAnimationSpec,
            progressForMaxAlpha = 0.8f,
            highlightColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if(isSystemInDarkTheme()) 0.6F else 0.3F),
        ),
        shape = shape,
        color = color ?: LocalContentColor.current.copy(alpha = if(isSystemInDarkTheme()) 0.1F else 0.2F)
    )
}