package com.t8rin.imagetoolbox.app

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun MainActivity.QrHypothesis() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        var data by remember {
            mutableStateOf("")
        }
        var isLoading by remember {
            mutableStateOf(false)
        }
        val painter = rememberQrCodePainter(
            data = data,
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
        )

        if (isLoading) {
            CircularProgressIndicator()
        }

        TextField(
            value = data,
            onValueChange = { data = it }
        )
    }
}