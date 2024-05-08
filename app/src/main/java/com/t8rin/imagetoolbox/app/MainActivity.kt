package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.t8rin.imagetoolbox.app.ui.theme.ImageToolboxLibsTheme
import com.t8rin.logger.makeLog
import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageToolboxLibsTheme {
                Surface {
                    var bitmap by remember {
                        mutableStateOf<Bitmap?>(null)
                    }
                    var path by remember {
                        mutableStateOf<Path?>(null)
                    }
                    val context = LocalContext.current
                    LaunchedEffect(bitmap) {
                        if (bitmap == null) {
//                            bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
//                            Canvas(bitmap!!).apply {
//                                drawColor(Color.Green.toArgb())
//                                drawRect(
//                                    200f, 200f, 400f, 400f,
//                                    android.graphics.Paint().apply {
//                                        style = android.graphics.Paint.Style.FILL
//                                        color = Color.Red.toArgb()
//                                    }
//                                )
//                            }
                            bitmap =
                                BitmapFactory.decodeResource(context.resources, R.drawable.test)
                        }
                    }
                    Column {
                        val scope = rememberCoroutineScope()
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        it.makeLog()
                                        scope.launch(Dispatchers.IO) {
                                            path = GPUImageNativeLibrary.floodFill(
                                                bitmap!!,
                                                it.x.toInt(),
                                                it.y.toInt(),
                                                1f,
                                                Color.Cyan.toArgb()
                                            )
                                        }
                                    }
                                }
                        ) {
                            bitmap?.asImageBitmap()?.let { drawImage(it) }
                            path?.asComposePath()?.let { drawPath(it, Color.Green) }
//                            path.forEach {
//                                drawContext.canvas.nativeCanvas.drawPoint(
//                                    it.x.toFloat(),
//                                    it.y.toFloat(),
//                                    Paint().apply {
//                                        color = Color.Green.toArgb()
//                                    }
//                                )
//                            }
                        }
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}