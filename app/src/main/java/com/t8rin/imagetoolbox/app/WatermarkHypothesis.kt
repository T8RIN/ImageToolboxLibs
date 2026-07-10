package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.watermark.androidwm.WatermarkBuilder
import com.watermark.androidwm.WatermarkDetector
import com.watermark.androidwm.bean.WatermarkImage
import com.watermark.androidwm.listener.BuildFinishListener
import com.watermark.androidwm.listener.DetectFinishListener
import com.watermark.androidwm.task.DetectionReturnValue

@Composable
fun MainActivity.WatermarkHypothesis() {
    var encodedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf("Select a scenario") }

    fun runScenario(isLsb: Boolean, isImage: Boolean) {
        status = "Encoding ${if (isLsb) "LSB" else "FD"} ${if (isImage) "image" else "text"}"
        val background = createWatermarkBackground()
        val builder = WatermarkBuilder.create(this, background, false)
        if (isImage) {
            builder.loadWatermarkImage(WatermarkImage(createWatermarkImage()).setSize(0.05))
        } else {
            builder.loadWatermarkText("ImageToolbox watermark")
        }
        builder.setInvisibleWMListener(isLsb, object : BuildFinishListener<Bitmap> {
            override fun onSuccess(bitmap: Bitmap) {
                encodedBitmap = bitmap
                status = "Encoded. Detecting automatically..."
                WatermarkDetector.create(bitmap).detect(object : DetectFinishListener {
                    override fun onSuccess(result: DetectionReturnValue) {
                        status = result.watermarkString?.let { "Detected text: $it" }
                            ?: "Detected image: ${result.watermarkBitmap?.width}×${result.watermarkBitmap?.height}"
                    }

                    override fun onFailure(message: String) {
                        status = "Detection failed: $message"
                    }
                })
            }

            override fun onFailure(message: String) {
                status = "Encoding failed: $message"
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { runScenario(true, false) }) { Text("LSB text") }
            Button(onClick = { runScenario(true, true) }) { Text("LSB image") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { runScenario(false, false) }) { Text("FD text") }
            Button(onClick = { runScenario(false, true) }) { Text("FD image") }
        }
        Text(status)
        encodedBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(280.dp)
            )
        }
    }
}

private fun createWatermarkBackground(): Bitmap {
    return Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        val paint = Paint()
        for (y in 0 until bitmap.height) {
            paint.color = Color.rgb(y / 4, 80 + y / 8, 255 - y / 4)
            canvas.drawLine(0f, y.toFloat(), bitmap.width.toFloat(), y.toFloat(), paint)
        }
    }
}

private fun createWatermarkImage(): Bitmap {
    return Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(30, 100, 220) }
        canvas.drawCircle(32f, 32f, 24f, paint)
    }
}
