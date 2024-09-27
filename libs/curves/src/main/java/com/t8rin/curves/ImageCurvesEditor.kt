package com.t8rin.curves

import android.graphics.Bitmap
import android.graphics.PointF
import android.view.TextureView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.t8rin.curves.PhotoFilterCurvesControl.CurvesToolValue
import com.t8rin.curves.PhotoFilterCurvesControl.CurvesValue
import jp.co.cyberagent.android.gpuimage.GLTextureView
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter

internal object CurvesCache {
    var curvesToolValue: CurvesToolValue by mutableStateOf(CurvesToolValue())
}

@Composable
fun ImageCurvesEditor(
    bitmap: Bitmap
) {
    var textureView by remember {
        mutableStateOf<TextureView?>(null)
    }
    val context = LocalContext.current

    val gpuImage by remember(context) {
        mutableStateOf(
            GPUImage(context).apply {
                setImage(bitmap)
                setFilter(CurvesCache.buildFilter())
            }
        )
    }

    AndroidView(
        modifier = Modifier.aspectRatio(bitmap.width / bitmap.height.toFloat()),
        factory = {
            GLTextureView(it).apply {
                textureView = this
                gpuImage.setGLTextureView(this)
            }
        }
    )
    AndroidView(
        factory = {
            PhotoFilterCurvesControl(it, CurvesCache.curvesToolValue).apply {
                setActualArea(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                setDelegate {
                    gpuImage.setFilter(CurvesCache.buildFilter())
                }
            }
        }
    )
    Button(
        onClick = {
            CurvesCache.curvesToolValue.activeType =
                (CurvesCache.curvesToolValue.activeType + 1) % 4
        }
    ) { }
}

private fun CurvesCache.buildFilter(): GPUImageFilter = GPUImageToneCurveFilter().apply {
    setRgbCompositeControlPoints(
        curvesToolValue.luminanceCurve.toPoints()
    )
    setRedControlPoints(
        curvesToolValue.redCurve.toPoints()
    )
    setGreenControlPoints(
        curvesToolValue.greenCurve.toPoints()
    )
    setBlueControlPoints(
        curvesToolValue.blueCurve.toPoints()
    )
    updateToneCurveTexture()
}

private fun CurvesValue.toPoints(): Array<PointF> = listOf(
    PointF(0.0f, blacksLevel / 100f),
    PointF(0.25f, shadowsLevel / 100f),
    PointF(0.5f, midtonesLevel / 100f),
    PointF(0.75f, highlightsLevel / 100f),
    PointF(1.0f, whitesLevel / 100f),
).toTypedArray()