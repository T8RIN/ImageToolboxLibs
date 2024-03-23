package com.idapgroup.snowfall

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import java.util.concurrent.ThreadLocalRandom


internal fun ClosedRange<Float>.random() =
    ThreadLocalRandom.current().nextFloat() * (endInclusive - start) + start

internal fun Float.random() =
    ThreadLocalRandom.current().nextFloat() * this

internal fun Int.random() =
    ThreadLocalRandom.current().nextInt(this)

internal fun IntSize.randomPosition() =
    Offset(width.random().toFloat(), height.random().toFloat())