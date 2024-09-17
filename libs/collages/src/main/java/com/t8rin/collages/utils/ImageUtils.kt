package com.t8rin.collages.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Matrix

internal object ImageUtils {

    class MemoryInfo {
        var availMem: Long = 0
        var totalMem: Long = 0
    }

    fun getMemoryInfo(context: Context): MemoryInfo {
        val info = MemoryInfo()
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        info.availMem = memInfo.availMem
        info.totalMem = memInfo.totalMem
        return info
    }

    fun createMatrixToDrawImageInCenterView(
        viewWidth: Float,
        viewHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Matrix {
        val ratioWidth = viewWidth / imageWidth
        val ratioHeight = viewHeight / imageHeight
        val ratio = Math.max(ratioWidth, ratioHeight)
        val dx = (viewWidth - imageWidth) / 2.0f
        val dy = (viewHeight - imageHeight) / 2.0f
        val result = Matrix()
        result.postTranslate(dx, dy)
        result.postScale(ratio, ratio, viewWidth / 2, viewHeight / 2)
        return result
    }

}
