package com.t8rin.imagetoolbox.app

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t8rin.editbox.EditBox

@Composable
fun MainActivity.EditBoxHypothesis() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        var selectedEditBox by remember {
            mutableIntStateOf(0)
        }
        var value by remember {
            mutableStateOf("edit cok cokckcc dva cock")
        }
        var value2 by remember {
            mutableStateOf("LOREM IPSUM")
        }

        val focus = LocalFocusManager.current
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            selectedEditBox = -1
                            focus.clearFocus()
                        }
                    }
            )
            var firstAspectRatio by remember {
                mutableFloatStateOf(1f)
            }
            EditBox(
                modifier = Modifier.wrapContentSize(),
                enabled = selectedEditBox == 0,
                onTap = { selectedEditBox = 0 },
                state = viewModel.editBoxTextStates[0],
                content = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.test,
                            contentDescription = null,
                            modifier = Modifier
                                .sizeIn(maxWidth = 150.dp)
                                .aspectRatio(firstAspectRatio),
                            onSuccess = {
                                firstAspectRatio =
                                    it.result.image.run { width / height.toFloat() }
                            }
                        )
                    }
                }
            )

            if (false) {
                var firstAspectRatio2 by remember {
                    mutableFloatStateOf(1f)
                }
                EditBox(
                    modifier = Modifier.wrapContentSize(),
                    enabled = selectedEditBox == 3,
                    onTap = { selectedEditBox = 3 },
                    state = viewModel.editBoxTextStates[1],
                    content = {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = com.smarttoolfactory.cropper.R.drawable.landscape2,
                                contentDescription = null,
                                modifier = Modifier
                                    .sizeIn(maxWidth = 150.dp)
                                    .aspectRatio(firstAspectRatio2),
                                onSuccess = {
                                    firstAspectRatio2 =
                                        it.result.image.run { width / height.toFloat() }
                                }
                            )
                        }
                    }
                )

                EditBox(
                    modifier = Modifier.wrapContentSize(),
                    enabled = selectedEditBox == 1,
                    onTap = { selectedEditBox = 1 },
                    state = viewModel.editBoxTextStates[2],
                    content = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.sizeIn(minHeight = 80.dp, minWidth = 80.dp)
                        ) {
                            Text(
                                text = value,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )
                EditBox(
                    modifier = Modifier.wrapContentSize(),
                    enabled = selectedEditBox == 2,
                    onTap = { selectedEditBox = 2 },
                    state = viewModel.editBoxTextStates[3],
                    content = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.sizeIn(minHeight = 80.dp, minWidth = 80.dp)
                        ) {
                            Text(
                                text = value2,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )
            }
        }

        Button(
            modifier = Modifier.safeDrawingPadding(),
            onClick = {
                val temp = value2
                value2 = value
                value = temp
            }
        ) { }
    }

}