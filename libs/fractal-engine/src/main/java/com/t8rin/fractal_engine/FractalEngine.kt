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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

object FractalEngine {

    val apiVersion: Int
        get() = FractalEngineBridge.apiVersion()

    val isAvailable: Boolean
        get() = FractalEngineBridge.isAvailable()

    val supportedTypes: Set<FractalType>
        get() = FractalEngineBridge.supportedTypeIds()
            .map(FractalType::fromStableId)
            .filterNotNull()
            .toSet()

    suspend fun render(request: FractalRenderRequest): Bitmap {
        request.validate()
        val bitmap = Bitmap.createBitmap(request.width, request.height, Bitmap.Config.ARGB_8888)
        return try {
            renderInto(bitmap, request)
        } catch (throwable: Throwable) {
            bitmap.recycle()
            throw throwable
        }
    }

    /** Renders into and returns the same [bitmap] instance. */
    suspend fun renderInto(
        bitmap: Bitmap,
        request: FractalRenderRequest
    ): Bitmap = withContext(Dispatchers.Default) {
        request.validate()
        require(bitmap.width == request.width && bitmap.height == request.height) {
            "Bitmap dimensions must match the render request"
        }
        currentCoroutineContext().ensureActive()
        val parameters = request.toNativeParameters()
        val palette = request.palette.copyOf()

        suspendCancellableCoroutine { continuation ->
            val sessionHandle = FractalEngineBridge.createSession()
            continuation.invokeOnCancellation {
                FractalEngineBridge.cancel(sessionHandle)
            }

            try {
                when (
                    FractalEngineBridge.renderIntoBlocking(
                        sessionHandle = sessionHandle,
                        bitmap = bitmap,
                        typeId = request.type.stableId,
                        maxIterations = request.maxIterations,
                        parameters = parameters,
                        exactViewport = request.viewport.exact,
                        palette = palette,
                        lyapunovSequence = request.lyapunovSequence
                    )
                ) {
                    FractalEngineBridge.RESULT_COMPLETED -> continuation.resume(bitmap) { _, _, _ -> }
                    FractalEngineBridge.RESULT_CANCELLED -> continuation.cancel(
                        CancellationException("Fractal render was cancelled")
                    )
                }
            } catch (throwable: Throwable) {
                continuation.resumeWith(Result.failure(throwable))
            } finally {
                FractalEngineBridge.destroySession(sessionHandle)
            }
        }
    }
}
