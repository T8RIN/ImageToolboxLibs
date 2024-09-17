package com.t8rin.collages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.t8rin.collages.model.TemplateItem
import com.t8rin.collages.utils.FrameImageUtils.loadFrameImages

@Composable
fun CollageTypeSelection(
    imagesCount: Int,
    value: CollageType,
    onValueChange: (CollageType) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    itemModifierFactory: @Composable (isSelected: Boolean) -> Modifier = { Modifier },
    state: LazyListState = rememberLazyListState(),
    previewColor: Color = MaterialTheme.colorScheme.secondary,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    var allFrames: List<TemplateItem> by remember {
        mutableStateOf(emptyList())
    }
    val context = LocalContext.current

    LaunchedEffect(context) {
        allFrames = loadFrameImages(context)
    }

    val availableFrames by remember(allFrames, imagesCount) {
        derivedStateOf {
            allFrames.filter {
                it.photoItemList.size == imagesCount
            }
        }
    }

    LaunchedEffect(availableFrames) {
        if (
            availableFrames.isNotEmpty() && value == CollageType.Empty || (value.templateItem?.photoItemList?.size
                ?: 0) != imagesCount
        ) {
            onValueChange(
                CollageType(
                    templateItem = availableFrames.first(),
                    index = 0
                )
            )
        }
    }

    AnimatedVisibility(
        visible = availableFrames.size > 1
    ) {
        LazyRow(
            state = state,
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = contentPadding
        ) {
            itemsIndexed(availableFrames) { index, templateItem ->
                AsyncImage(
                    model = templateItem.preview,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(previewColor),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(shape)
                        .clickable {
                            onValueChange(
                                CollageType(
                                    templateItem = templateItem,
                                    index = index
                                )
                            )
                        }
                        .then(itemModifierFactory(value.index == index))
                )
            }
        }
    }

}