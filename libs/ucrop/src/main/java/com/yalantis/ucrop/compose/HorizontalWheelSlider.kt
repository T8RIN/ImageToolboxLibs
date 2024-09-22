package com.yalantis.ucrop.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView

@Composable
fun HorizontalWheelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    AndroidView(
        modifier = modifier,
        factory = {
            HorizontalProgressWheelView(it).apply {
                setScrollingListener(
                    object : HorizontalProgressWheelView.ScrollingListener {
                        override fun onScrollStart() {

                        }

                        override fun onScroll(delta: Float, totalDistance: Float) {
                            onValueChange(totalDistance)
                        }

                        override fun onScrollEnd() {

                        }
                    }
                )
            }
        },
        update = {
            it.setLinesColor(colorScheme.secondaryContainer.copy(0.5f).toArgb())
            it.setMiddleLineColor(colorScheme.primary.toArgb())
            it.mTotalScrollDistance = value
        }
    )
}