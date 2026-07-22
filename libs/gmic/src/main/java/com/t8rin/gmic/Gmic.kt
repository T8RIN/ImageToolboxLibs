package com.t8rin.gmic

import android.graphics.Bitmap
import android.os.SystemClock
import com.t8rin.gmic.model.GmicAlphaMode
import com.t8rin.gmic.model.GmicException
import com.t8rin.gmic.model.GmicExecutionOptions
import com.t8rin.gmic.model.GmicOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data object Gmic {
    const val VERSION: String = "4.0.2"

    private var customCommands: String? = null

    init {
        System.loadLibrary("gmic-android")
    }

    fun setCustomCommands(commands: String?) {
        customCommands = commands
    }

    /** Runs the complete command pipeline off the UI thread and propagates cancellation to G'MIC. */
    suspend fun runCancellable(
        input: Bitmap,
        filter: GmicFilter,
        executionOptions: GmicExecutionOptions = GmicExecutionOptions()
    ): Bitmap = runCancellable(
        input = input,
        command = filter.command,
        options = filter.options,
        executionOptions = executionOptions
    )

    suspend fun runCancellable(
        input: Bitmap,
        command: String,
        options: GmicOptions = GmicOptions(),
        executionOptions: GmicExecutionOptions = GmicExecutionOptions()
    ): Bitmap = withContext(Dispatchers.Default) {
        currentCoroutineContext().ensureActive()
        withPreparedInput(input, command, executionOptions) { prepared ->
            runNativeCancellable(
                prepared = prepared,
                command = command,
                options = options,
                executionOptions = executionOptions.withDefaultCustomCommands()
            )
        }
    }

    private inline fun <T> withPreparedInput(
        input: Bitmap,
        command: String,
        executionOptions: GmicExecutionOptions,
        block: (PreparedInput) -> T
    ): T {
        validate(input, command, executionOptions)
        val preparationStarted = SystemClock.elapsedRealtimeNanos()
        val nativeInput = if (input.config == Bitmap.Config.ARGB_8888) {
            input
        } else {
            input.copy(Bitmap.Config.ARGB_8888, false)
                ?: throw GmicException("Unable to copy input bitmap to ARGB_8888")
        }
        val prepared = PreparedInput(
            bitmap = nativeInput,
            preparationNanoseconds = SystemClock.elapsedRealtimeNanos() - preparationStarted,
            fullBitmapCopies = if (nativeInput === input) 0 else 1
        )

        return try {
            block(prepared)
        } finally {
            if (nativeInput !== input) nativeInput.recycle()
        }
    }

    private fun validate(
        input: Bitmap,
        command: String,
        executionOptions: GmicExecutionOptions
    ) {
        require(!input.isRecycled) { "Input bitmap is recycled" }
        require(command.isNotBlank()) { "G'MIC command must not be blank" }
        require(executionOptions.maxThreads == null || executionOptions.maxThreads > 0) {
            "maxThreads must be positive"
        }
    }

    private suspend fun runNativeCancellable(
        prepared: PreparedInput,
        command: String,
        options: GmicOptions,
        executionOptions: GmicExecutionOptions
    ): Bitmap = suspendCancellableCoroutine { continuation ->
        val operationId = nativeCreateOperation()
        continuation.invokeOnCancellation {
            nativeCancel(operationId)
        }

        try {
            val result = nativeRun(prepared, command, options, executionOptions, operationId)
            if (result == null) {
                continuation.cancel(CancellationException("G'MIC operation was cancelled"))
            } else {
                continuation.resume(result) { _, cancelledResult, _ ->
                    cancelledResult.recycle()
                }
            }
        } catch (throwable: Throwable) {
            continuation.resumeWith(Result.failure(throwable))
        } finally {
            nativeDestroyOperation(operationId)
        }
    }

    private fun nativeRun(
        prepared: PreparedInput,
        command: String,
        options: GmicOptions,
        executionOptions: GmicExecutionOptions,
        operationId: Long
    ): Bitmap? = nativeRun(
        operationId = operationId,
        input = prepared.bitmap,
        command = command,
        customCommands = executionOptions.customCommands,
        preserveAlpha = options.alphaMode == GmicAlphaMode.Preserve,
        outputIndex = options.outputIndex,
        maxThreads = executionOptions.maxThreads ?: 0,
        disableSmooth = executionOptions.disableSmooth,
        profilingEnabled = executionOptions.profilingEnabled,
        inputPreparationNanoseconds = prepared.preparationNanoseconds,
        inputPreparationCopies = prepared.fullBitmapCopies
    )

    private external fun nativeCreateOperation(): Long

    private external fun nativeCancel(operationId: Long)

    private external fun nativeDestroyOperation(operationId: Long)

    private external fun nativeRun(
        operationId: Long,
        input: Bitmap,
        command: String,
        customCommands: String?,
        preserveAlpha: Boolean,
        outputIndex: Int,
        maxThreads: Int,
        disableSmooth: Boolean,
        profilingEnabled: Boolean,
        inputPreparationNanoseconds: Long,
        inputPreparationCopies: Int
    ): Bitmap?

    private fun GmicExecutionOptions.withDefaultCustomCommands(): GmicExecutionOptions {
        val defaultCommands = this@Gmic.customCommands
        return if (customCommands != null || defaultCommands == null) this
        else copy(customCommands = defaultCommands)
    }

    private data class PreparedInput(
        val bitmap: Bitmap,
        val preparationNanoseconds: Long,
        val fullBitmapCopies: Int
    )
}
