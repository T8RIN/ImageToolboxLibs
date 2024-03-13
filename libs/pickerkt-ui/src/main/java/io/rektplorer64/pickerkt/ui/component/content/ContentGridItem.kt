package io.rektplorer64.pickerkt.ui.component.content

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.*
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.ui.R
import io.rektplorer64.pickerkt.ui.common.compose.shimmerPlaceholder
import io.rektplorer64.pickerkt.ui.common.data.extension.icon
import io.rektplorer64.pickerkt.ui.component.common.CheckMarkButton
import io.rektplorer64.pickerkt.ui.component.content.ContentGridItemDefaults.CheckIconEnterAnimation
import io.rektplorer64.pickerkt.ui.component.content.ContentGridItemDefaults.CheckIconExitAnimation
import io.rektplorer64.pickerkt.ui.component.content.ContentGridItemDefaults.BottomEndGradientBackdrop
import io.rektplorer64.pickerkt.ui.component.content.ContentGridItemDefaults.TopStartGradientBackdrop

object ContentGridItemDefaults {

    private val MimeTypeIconGradient = listOf(
        Color.Black.copy(alpha = 0.25f),
        Color.Transparent
    )

    val CheckIconEnterAnimation = slideIn { IntOffset(-it.width / 2, -it.height / 2) } + fadeIn()
    val CheckIconExitAnimation = slideOut { IntOffset(-it.width / 2, -it.height / 2) } + fadeOut()

    val TopStartGradientBackdrop = Brush.linearGradient(
        colors = MimeTypeIconGradient,
        start = Offset(0f, 0f),
        end = Offset(0f, 90f)
    )

    val BottomEndGradientBackdrop = Brush.radialGradient(
        colors = MimeTypeIconGradient,
        center = Offset(x = Float.POSITIVE_INFINITY, y = Float.POSITIVE_INFINITY),
        radius = Float.POSITIVE_INFINITY
    )
}

@Composable
fun ContentGridItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectionIndex: Int = -1,
    selected: Boolean = selectionIndex != -1,
    editModeActivated: Boolean,
    onClick: () -> Unit,
    onCheckClick: () -> Unit,
    badge: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {

    val selectionTransition = updateTransition(targetState = selected, label = "Selection")

    val backgroundAlpha by selectionTransition.animateFloat(label = "Background Alpha") {
        if (it) 0.05f else 0f
    }

    val padding by selectionTransition.animateDp(label = "Padding") {
        if (it) 16.dp else 0.dp
    }

    val cornerRadius by selectionTransition.animateDp(label = "Corner Radius") {
        if (it) 16.dp else 0.dp
    }

    val boxOpacity by animateFloatAsState(targetValue = if (enabled) 1f else 0.4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(boxOpacity)
            .aspectRatio(1f)
            .clipToBounds()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(padding)
                .clip(RoundedCornerShape(cornerRadius))
                .clickable(enabled) {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {

            content()

            AnimatedVisibility(
                visible = editModeActivated,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TopStartGradientBackdrop)
                )
            }
        }

        AnimatedVisibility(
            visible = editModeActivated,
            enter = CheckIconEnterAnimation,
            exit = CheckIconExitAnimation,
            modifier = Modifier.align(Alignment.TopStart)
        ) {

            val checkOpacity by animateFloatAsState(
                targetValue = if (enabled || selected) 1f else boxOpacity
            )

            CheckMarkButton(
                modifier = Modifier.alpha(checkOpacity),
                selected = selected,
                label = ((selectionIndex.takeIf { it >= 0 } ?: selectionIndex) + 1).toString(),
                borderColor = Color.White
            ) {
                onCheckClick()
            }
        }

        Box(
            modifier = Modifier.align(Alignment.BottomEnd),
            content = badge
        )
    }
}

@Composable
fun CoilContentGridItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    editModeActivated: Boolean = false,
    selectionIndex: Int,
    selected: Boolean = selectionIndex >= 0,
    content: Content,
    onClick: () -> Unit,
    onCheckClick: () -> Unit,
) {
    CoilContentGridItem(
        modifier = modifier,
        enabled = enabled,
        editModeActivated = editModeActivated,
        selectionIndex = selectionIndex,
        selected = selected,
        onClick = onClick,
        imageUri = content.uri,
        contentDescription = null,
        onCheckClick = onCheckClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .background(BottomEndGradientBackdrop)
        )
        Icon(
            painterResource(content.mimeType.icon),
            contentDescription = content.mimeType.displayName,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            tint = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
@OptIn(ExperimentalCoilApi::class)
fun CoilContentGridItem(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    contentDescription: String?,
    enabled: Boolean = true,
    editModeActivated: Boolean = false,
    selectionIndex: Int,
    selected: Boolean = selectionIndex >= 0,
    onClick: () -> Unit,
    onCheckClick: () -> Unit,
    badge: @Composable BoxScope.() -> Unit = {}
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUri)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCacheKey(imageUri.toString())
            .diskCacheKey(imageUri.toString())
            .allowHardware(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) { state ->

        // val hasImageLoaded = state is AsyncImagePainter.State.Success
        // val hasImageError = state is AsyncImagePainter.State.Error

        ContentGridItem(
            modifier = modifier,
            enabled = enabled,
            selected = selected,
            selectionIndex = selectionIndex,
            editModeActivated = if (!selected) {
                editModeActivated
            } else {
                true
            },
            onClick = onClick,
            onCheckClick = onCheckClick,
            badge = badge
        ) {

            when (state) {
                AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading,
                is AsyncImagePainter.State.Success -> {
                    this@AsyncImage.AsyncImageContent(
                        modifier = Modifier
                            .shimmerPlaceholder(
                                visible = state is AsyncImagePainter.State.Loading,
                                shape = RectangleShape,
                                color = Color.Transparent
                            )
                    )
                }
                is AsyncImagePainter.State.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_alert_circle_outline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}