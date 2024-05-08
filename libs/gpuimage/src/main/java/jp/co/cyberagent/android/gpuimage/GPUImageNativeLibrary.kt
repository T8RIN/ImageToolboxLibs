/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.cyberagent.android.gpuimage

import android.graphics.Bitmap
import android.graphics.Path

object GPUImageNativeLibrary {
    init {
        System.loadLibrary("yuv-decoder")
    }

    @JvmStatic
    external fun YUVtoRBGA(yuv: ByteArray?, width: Int, height: Int, out: IntArray?)

    @JvmStatic
    external fun adjustBitmap(srcBitmap: Bitmap?)

    external fun noise(srcBitmap: Bitmap, threshold: Int)

    external fun monochrome(
        srcBitmap: Bitmap,
        intensity: Float,
        red: Float,
        green: Float,
        blue: Float
    )

    external fun shuffle(srcBitmap: Bitmap, threshold: Float, strength: Float)

    external fun floodFill(
        srcBitmap: Bitmap,
        startX: Int,
        startY: Int,
        tolerance: Float,
        fillColor: Int
    ): Path?
}
