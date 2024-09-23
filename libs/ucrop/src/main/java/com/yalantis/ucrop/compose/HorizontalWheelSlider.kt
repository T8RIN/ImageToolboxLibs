package com.yalantis.ucrop.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.yalantis.ucrop.view.CropRotationWheel

@Composable
fun HorizontalWheelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onStart: () -> Unit = {},
    onEnd: (Float) -> Unit = {},
    onRotate90: () -> Unit = {},
    onFlip: () -> Unit = {},
    hapticsStrength: Int,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    AndroidView(
        modifier = modifier,
        factory = {
            CropRotationWheel(it, hapticsStrength).apply {
                setListener(
                    object : CropRotationWheel.RotationWheelListener {
                        override fun onStart() = onStart()

                        override fun onChange(angle: Float) = onValueChange(angle)

                        override fun onEnd(angle: Float) = onEnd(angle)

                        override fun rotate90Pressed() = onRotate90()

                        override fun mirror() = onFlip()
                    }
                )
            }
        },
        update = {
            it.setRotation(value, true)
            it.setCenterLineColor(colorScheme.primary.toArgb())
            it.setSideLineColor(Color.White.toArgb())
            it.setHapticsStrength(hapticsStrength)
        }
    )
}