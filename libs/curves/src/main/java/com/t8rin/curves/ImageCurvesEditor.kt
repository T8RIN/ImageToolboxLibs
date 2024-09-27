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
import jp.co.cyberagent.android.gpuimage.GLTextureView
import jp.co.cyberagent.android.gpuimage.GPUImage
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
                setFilter(
                    GPUImageToneCurveFilter()
                )
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
                    CurvesCache.curvesToolValue.fillBuffer()
                    gpuImage.setFilter(
                        GPUImageToneCurveFilter().apply {
                            setRgbCompositeControlPoints(
                                CurvesCache.curvesToolValue.luminanceCurve.let { curve ->
                                    listOf(
                                        PointF(0.0f, curve.blacksLevel / 100f),
                                        PointF(0.25f, curve.shadowsLevel / 100f),
                                        PointF(0.5f, curve.midtonesLevel / 100f),
                                        PointF(0.75f, curve.highlightsLevel / 100f),
                                        PointF(1.0f, curve.whitesLevel / 100f),
                                    ).toTypedArray()
                                }
                            )
                            setRedControlPoints(
                                CurvesCache.curvesToolValue.redCurve.let { curve ->
                                    listOf(
                                        PointF(0.0f, curve.blacksLevel / 100f),
                                        PointF(0.25f, curve.shadowsLevel / 100f),
                                        PointF(0.5f, curve.midtonesLevel / 100f),
                                        PointF(0.75f, curve.highlightsLevel / 100f),
                                        PointF(1.0f, curve.whitesLevel / 100f),
                                    ).toTypedArray()
                                }
                            )
                            setGreenControlPoints(
                                CurvesCache.curvesToolValue.greenCurve.let { curve ->
                                    listOf(
                                        PointF(0.0f, curve.blacksLevel / 100f),
                                        PointF(0.25f, curve.shadowsLevel / 100f),
                                        PointF(0.5f, curve.midtonesLevel / 100f),
                                        PointF(0.75f, curve.highlightsLevel / 100f),
                                        PointF(1.0f, curve.whitesLevel / 100f),
                                    ).toTypedArray()
                                }
                            )
                            setBlueControlPoints(
                                CurvesCache.curvesToolValue.blueCurve.let { curve ->
                                    listOf(
                                        PointF(0.0f, curve.blacksLevel / 100f),
                                        PointF(0.25f, curve.shadowsLevel / 100f),
                                        PointF(0.5f, curve.midtonesLevel / 100f),
                                        PointF(0.75f, curve.highlightsLevel / 100f),
                                        PointF(1.0f, curve.whitesLevel / 100f),
                                    ).toTypedArray()
                                }
                            )
                            updateToneCurveTexture()
                        }
                    )
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