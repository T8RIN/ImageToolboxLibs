package com.t8rin.imagetoolbox.app

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrShapes
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.cutCorners
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun MainActivity.QrHypothesis() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .safeContentPadding()
    ) {
        var value by remember {
            mutableFloatStateOf(0.35f)
        }
        var data by remember {
            mutableStateOf("")
        }
        var isLoading by remember {
            mutableStateOf(false)
        }
        val painter = rememberQrCodePainter(
            data = data,
            shapes = QrShapes(
                frame = QrFrameShape.cutCorners(
                    value,
                    topLeft = "1" in data,
                    topRight = "2" in data,
                    bottomLeft = "3" in data,
                    bottomRight = "4" in data
                ),
                ball = QrBallShape.circle()
            ),
            onFailure = {
                isLoading = true
                Toast.makeText(
                    this@QrHypothesis, it.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            },
            onSuccess = {
                isLoading = false
            }
        )

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(32.dp)
                .height(300.dp)
        )

        if (isLoading) {
            CircularProgressIndicator()
        }

        TextField(
            label = { Text(value.toString()) },
            value = data,
            onValueChange = { data = it }
        )

        Slider(
            value = value,
            onValueChange = { value = it },
            valueRange = 0f..0.35f
        )
    }
}