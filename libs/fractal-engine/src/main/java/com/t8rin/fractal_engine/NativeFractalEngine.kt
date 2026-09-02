/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.t8rin.fractal_engine

import android.graphics.Bitmap

internal object NativeFractalEngine {

    @Volatile
    private var loaded = false

    @Volatile
    private var loadFailure: Throwable? = null

    @Volatile
    private var loadedApiVersion = 0

    fun tryLoad(): Boolean {
        if (loaded) return true
        if (loadFailure != null) return false

        return synchronized(this) {
            if (!loaded && loadFailure == null) {
                try {
                    System.loadLibrary("fractal_engine")
                    val apiVersion = nativeApiVersion()
                    check(apiVersion == FractalEngineBridge.API_VERSION) {
                        "Native API version $apiVersion does not match " +
                            "${FractalEngineBridge.API_VERSION}"
                    }
                    check(nativeSourceChecksum().matches(SOURCE_CHECKSUM_PATTERN)) {
                        "Native source checksum is missing or malformed"
                    }
                    loadedApiVersion = apiVersion
                    loaded = true
                } catch (throwable: Throwable) {
                    loadedApiVersion = 0
                    loadFailure = throwable
                }
            }
            loaded
        }
    }

    fun requireLoaded() {
        if (!tryLoad()) {
            throw FractalEngineException(
                message = "Native fractal engine is unavailable",
                cause = loadFailure
            )
        }
    }

    fun apiVersion(): Int = if (tryLoad()) loadedApiVersion else 0

    external fun nativeApiVersion(): Int

    external fun nativeSourceChecksum(): String

    external fun nativeCreateSession(): Long

    external fun nativeCancel(sessionHandle: Long)

    external fun nativeDestroySession(sessionHandle: Long)

    external fun nativeRenderInto(
        sessionHandle: Long,
        bitmap: Bitmap,
        typeId: Int,
        maxIterations: Int,
        parameters: DoubleArray,
        exactCenterX: String,
        exactCenterY: String,
        exactSpan: String,
        palette: IntArray,
        lyapunovSequence: String
    ): Int

    private val SOURCE_CHECKSUM_PATTERN = Regex("[0-9a-f]{64}")
}
