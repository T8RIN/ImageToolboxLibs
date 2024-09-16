package com.t8rin.collages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.photoeditor.photoeffect.model.TemplateItem
import com.photoeditor.photoeffect.utils.FrameImageUtils.loadFrameImages

@Composable
fun CollageTypeSelection(
    imagesCount: Int
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
}