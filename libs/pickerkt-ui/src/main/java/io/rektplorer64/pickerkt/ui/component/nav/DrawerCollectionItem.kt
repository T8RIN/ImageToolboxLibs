package io.rektplorer64.pickerkt.ui.component.nav

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.*
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.rektplorer64.pickerkt.ui.R
import io.rektplorer64.pickerkt.ui.common.compose.data.randomizeStringForPlaceholder
import io.rektplorer64.pickerkt.ui.common.compose.shimmerPlaceholder

@Composable
fun DrawerCollectionItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
    trailingIcon: @Composable BoxScope.() -> Unit,
    name: String?,
    info: String?,
    imageUri: Uri?
) {
    val backgroundAlpha by animateFloatAsState(targetValue = if (selected) 1f else 0f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = backgroundAlpha),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCacheKey(imageUri.toString())
                    .build()
                ,
                contentDescription = null,
                imageLoader = LocalImageLoader.current,
                modifier = Modifier
                    .padding(end = 24.dp, top = 12.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .size(56.dp)
                    .shimmerPlaceholder(loading, shape = RoundedCornerShape(16.dp)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            ) {
                when (it) {
                    AsyncImagePainter.State.Empty,
                    is AsyncImagePainter.State.Loading,
                    is AsyncImagePainter.State.Success -> {
                        AsyncImageContent()
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

            CompositionLocalProvider(LocalContentColor provides if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize()
                ) {
                    Text(
                        text = name ?: randomizeStringForPlaceholder(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.shimmerPlaceholder(loading)
                    )

                    Text(
                        text = info ?: randomizeStringForPlaceholder(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LocalContentColor.current.copy(
                                alpha = 0.6F
                            )
                        ),
                        modifier = Modifier.shimmerPlaceholder(loading)
                    )
                }
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(start = 8.dp)
                .animateContentSize()) {
                trailingIcon()
            }
        }
    }
}