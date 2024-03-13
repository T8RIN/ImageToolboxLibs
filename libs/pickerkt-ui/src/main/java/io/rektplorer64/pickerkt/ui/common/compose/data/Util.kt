package io.rektplorer64.pickerkt.ui.common.compose.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.rektplorer64.pickerkt.ui.common.randomizeWhiteSpaces

@Composable
fun randomizeStringForPlaceholder(lengthRange: IntRange = 10..30): String = remember {
    randomizeWhiteSpaces(lengthRange = lengthRange)
}