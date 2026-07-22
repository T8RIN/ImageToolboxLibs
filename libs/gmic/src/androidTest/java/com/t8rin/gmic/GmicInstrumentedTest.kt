@file:Suppress("ConvertLongToDuration")

package com.t8rin.gmic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.t8rin.gmic.filters.FrostedGlass
import com.t8rin.gmic.filters.HopePoster
import com.t8rin.gmic.model.GmicAlphaMode
import com.t8rin.gmic.model.GmicExecutionOptions
import com.t8rin.gmic.model.GmicException
import com.t8rin.gmic.model.GmicOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GmicInstrumentedTest {

    private fun createTexturedBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setHasAlpha(false)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                intArrayOf(
                    Color.rgb(16, 42, 96),
                    Color.rgb(224, 112, 48),
                    Color.rgb(32, 176, 144),
                    Color.rgb(232, 214, 150)
                ),
                null,
                Shader.TileMode.MIRROR
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        val tile = (minOf(width, height) / 48).coerceAtLeast(8)
        for (y in 0 until height step tile) {
            for (x in 0 until width step tile) {
                if ((x / tile + y / tile) % 3 == 0) {
                    paint.color = Color.argb(112, (x * 37) and 255, (y * 53) and 255, 192)
                    canvas.drawRect(
                        x.toFloat(),
                        y.toFloat(),
                        minOf(x + tile, width).toFloat(),
                        minOf(y + tile, height).toFloat(),
                        paint
                    )
                }
            }
        }
        return bitmap
    }

    private fun sampledBitmapHash(bitmap: Bitmap): Long {
        var hash = -3750763034362895579L
        val xStep = (bitmap.width / 31).coerceAtLeast(1)
        val yStep = (bitmap.height / 29).coerceAtLeast(1)
        for (y in 0 until bitmap.height step yStep) {
            for (x in 0 until bitmap.width step xStep) {
                hash = (hash xor bitmap.getPixel(x, y).toLong()) * 1099511628211L
            }
        }
        return hash
    }

    @Test
    fun roundTripPreservesAlpha() = runBlocking {
        val colors = intArrayOf(
            Color.argb(0, 255, 0, 0),
            Color.argb(64, 20, 80, 140),
            Color.argb(128, 200, 100, 50),
            Color.argb(255, 10, 20, 30)
        )
        val input = Bitmap.createBitmap(colors, 2, 2, Bitmap.Config.ARGB_8888)
        val inputColors = IntArray(colors.size)
        input.getPixels(inputColors, 0, 2, 0, 0, 2, 2)

        val output = Gmic.runCancellable(input, "+ 0")
        val outputColors = IntArray(colors.size)
        output.getPixels(outputColors, 0, 2, 0, 0, 2, 2)

        assertEquals(inputColors.toList(), outputColors.toList())
        input.recycle()
        output.recycle()
    }

    @Test
    fun disableSmoothSkipsNestedParallelSmoothCommands() = runBlocking {
        val input = createTexturedBitmap(192, 128)
        val inputHash = sampledBitmapHash(input)

        val disabledOutput = Gmic.runCancellable(
            input = input,
            command = "apc \"smooth 20,0,1,3,1\"",
            executionOptions = GmicExecutionOptions(maxThreads = 3, disableSmooth = true)
        )
        val enabledOutput = Gmic.runCancellable(
            input = input,
            command = "apc \"smooth 20,0,1,3,1\"",
            executionOptions = GmicExecutionOptions(maxThreads = 3)
        )

        assertEquals(inputHash, sampledBitmapHash(disabledOutput))
        assertTrue(inputHash != sampledBitmapHash(enabledOutput))
        input.recycle()
        disabledOutput.recycle()
        enabledOutput.recycle()
    }

    @Test
    fun fftwRoundTripSupportsNonPowerOfTwoDimensions() = runBlocking {
        val input = Bitmap.createBitmap(37, 29, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    setPixel(x, y, Color.rgb((x * 7) and 255, (y * 9) and 255, ((x + y) * 5) and 255))
                }
            }
        }

        val output = Gmic.runCancellable(input, "fft ifft rm[1]")

        for ((x, y) in listOf(0 to 0, 5 to 7, 19 to 13, 36 to 28)) {
            val expected = input.getPixel(x, y)
            val actual = output.getPixel(x, y)
            assertTrue(kotlin.math.abs(Color.red(expected) - Color.red(actual)) <= 1)
            assertTrue(kotlin.math.abs(Color.green(expected) - Color.green(actual)) <= 1)
            assertTrue(kotlin.math.abs(Color.blue(expected) - Color.blue(actual)) <= 1)
        }
        input.recycle()
        output.recycle()
    }

    @Test
    fun customCommandsAreAvailableToInterpreter() = runBlocking {
        val input = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            setPixel(0, 0, Color.rgb(10, 20, 30))
        }
        Gmic.setCustomCommands("double_pixels : mul 2")

        try {
            val globalOutput = Gmic.runCancellable(input, "double_pixels")
            val perRunOutput = Gmic.runCancellable(
                input = input,
                command = "triple_pixels",
                options = GmicOptions(),
                executionOptions = GmicExecutionOptions(
                    customCommands = "triple_pixels : mul 3"
                )
            )

            assertEquals(Color.rgb(20, 40, 60), globalOutput.getPixel(0, 0))
            assertEquals(Color.rgb(30, 60, 90), perRunOutput.getPixel(0, 0))
            globalOutput.recycle()
            perRunOutput.recycle()
        } finally {
            Gmic.setCustomCommands(null)
            input.recycle()
        }
    }

    @Test
    fun concurrentRunsProduceIndependentResults() = runBlocking {
        val input = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(10, 20, 30))
        }
        val commands = List(12) { index -> "+ ${index + 1}" }

        val executionOptions = GmicExecutionOptions(
            maxThreads = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        )
        val outputs = commands.map { command ->
            async {
                Gmic.runCancellable(input, command, GmicOptions(), executionOptions)
            }
        }.awaitAll()

        outputs.forEachIndexed { index, output ->
            assertEquals(11 + index, Color.red(output.getPixel(0, 0)))
            assertEquals(21 + index, Color.green(output.getPixel(0, 0)))
            assertEquals(31 + index, Color.blue(output.getPixel(0, 0)))
            output.recycle()
        }
        input.recycle()
    }

    @Test
    fun twoInterpretersRunAtTheSameTime() = runBlocking {
        assumeTrue(Runtime.getRuntime().availableProcessors() >= 2)
        val input = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        Gmic.runCancellable(input, "+ 0").recycle()
        val executionOptions = GmicExecutionOptions(
            maxThreads = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        )

        val serialStartedAt = SystemClock.elapsedRealtime()
        Gmic.runCancellable(input, "wait 600 + 1", GmicOptions(), executionOptions).recycle()
        Gmic.runCancellable(input, "wait 600 + 2", GmicOptions(), executionOptions).recycle()
        val serialDuration = SystemClock.elapsedRealtime() - serialStartedAt

        val concurrentStartedAt = SystemClock.elapsedRealtime()

        val outputs = withTimeout(3_000) {
            listOf(
                async {
                    Gmic.runCancellable(input, "wait 600 + 1", GmicOptions(), executionOptions)
                },
                async {
                    Gmic.runCancellable(input, "wait 600 + 2", GmicOptions(), executionOptions)
                }
            ).awaitAll()
        }
        val concurrentDuration = SystemClock.elapsedRealtime() - concurrentStartedAt

        assertTrue(
            "Independent interpreters were serialized: serial=$serialDuration concurrent=$concurrentDuration",
            concurrentDuration < serialDuration * 0.8
        )
        input.recycle()
        outputs.forEach(Bitmap::recycle)
    }

    @Test
    fun queuedRunCancelsWithoutWaitingForAdmission() = runBlocking {
        val input = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val exclusive = GmicExecutionOptions(
            maxThreads = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        )
        val first = launch {
            Gmic.runCancellable(input, "wait 2000", GmicOptions(), exclusive).recycle()
        }
        delay(100)
        val queued = launch {
            Gmic.runCancellable(input, "+ 1", GmicOptions(), exclusive).recycle()
        }
        delay(100)

        val cancellationStarted = SystemClock.elapsedRealtime()
        withTimeout(1_000) { queued.cancelAndJoin() }
        assertTrue(SystemClock.elapsedRealtime() - cancellationStarted < 1_000)

        first.cancelAndJoin()
        input.recycle()
    }

    @Test
    fun commandCannotRaiseConfiguredThreadCap() = runBlocking {
        val input = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLACK)
        }
        val output = Gmic.runCancellable(
            input,
            "_cpus=999 split c fill[0-2] \$_cpus append c",
            GmicOptions(),
            GmicExecutionOptions(maxThreads = 1, profilingEnabled = true)
        )

        assertEquals(Color.rgb(1, 1, 1), output.getPixel(0, 0))
        input.recycle()
        output.recycle()
    }

    @Test
    fun commandsCanChangeFullResolutionCanvas() = runBlocking {
        val input = Bitmap.createBitmap(3000, 2000, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(10, 20, 30))
        }

        val output = Gmic.runCancellable(
            input = input,
            command = "resize 150%,50%"
        )

        assertEquals(4500, output.width)
        assertEquals(1000, output.height)
        input.recycle()
        output.recycle()
    }

    @Test
    fun processedAlphaSurvivesCanvasResize() = runBlocking {
        val input = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    setPixel(x, y, Color.argb(if ((x + y) % 2 == 0) 32 else 224, 80, 120, 160))
                }
            }
        }
        val output = Gmic.runCancellable(
            input,
            "resize 200%,150%,100%,100%,3",
            GmicOptions(alphaMode = GmicAlphaMode.Process)
        )

        assertEquals(16, output.width)
        assertEquals(12, output.height)
        val sampledAlphas = buildSet {
            for (y in 0 until output.height step 2) {
                for (x in 0 until output.width step 2) add(Color.alpha(output.getPixel(x, y)))
            }
        }
        assertTrue("Processed alpha became opaque or constant", sampledAlphas.size > 1)
        input.recycle()
        output.recycle()
    }

    @Test
    fun nonArgbInputIsConvertedWithoutMutation() = runBlocking {
        val input = Bitmap.createBitmap(32, 24, Bitmap.Config.RGB_565).apply {
            eraseColor(Color.rgb(40, 80, 120))
        }
        val output = Gmic.runCancellable(input, "+ 1")

        assertEquals(Bitmap.Config.RGB_565, input.config)
        assertEquals(Bitmap.Config.ARGB_8888, output.config)
        assertEquals(32, output.width)
        assertEquals(24, output.height)
        input.recycle()
        output.recycle()
    }

    @Test
    fun twoChannelOutputUsesLuminanceAndAlpha() = runBlocking {
        val input = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            setPixel(0, 0, Color.argb(128, 100, 50, 25))
        }

        val output = Gmic.runCancellable(
            input = input,
            command = "split c keep[0,3] append c",
            options = GmicOptions(alphaMode = GmicAlphaMode.Process)
        )
        val color = output.getPixel(0, 0)

        assertEquals(128, Color.alpha(color))
        assertEquals(Color.red(color), Color.green(color))
        assertEquals(Color.red(color), Color.blue(color))
        input.recycle()
        output.recycle()
    }

    @Test
    fun representativePipelineRunsInOneInvocation() = runBlocking {
        val input = Bitmap.createBitmap(128, 96, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(80, 120, 160))
        }
        val pipeline = "polygonize 24,6 polaroid 5,30 rotate 20 drop_shadow ,"

        val output = withTimeout(60_000) {
            Gmic.runCancellable(
                input,
                pipeline,
                GmicOptions(),
                GmicExecutionOptions(profilingEnabled = true)
            )
        }

        assertTrue(output.width > 0)
        assertTrue(output.height > 0)
        input.recycle()
        output.recycle()
    }

    @Test
    fun texturedGlassRunsWithProcessedAlpha() = runBlocking {
        val input = Bitmap.createBitmap(128, 96, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    setPixel(
                        x,
                        y,
                        Color.argb(if ((x / 8 + y / 8) % 2 == 0) 32 else 224, 80, 120, 160)
                    )
                }
            }
        }

        val output = withTimeout(60_000) {
            Gmic.runCancellable(input, FrostedGlass())
        }

        assertEquals(input.width, output.width)
        assertEquals(input.height, output.height)
        val inputAlpha = IntArray(input.width * input.height).also {
            input.getPixels(it, 0, input.width, 0, 0, input.width, input.height)
        }.map(Color::alpha)
        val outputAlpha = IntArray(output.width * output.height).also {
            output.getPixels(it, 0, output.width, 0, 0, output.width, output.height)
        }.map(Color::alpha)
        assertTrue("Processed alpha was unchanged", inputAlpha != outputAlpha)
        input.recycle()
        output.recycle()
    }

    @Test
    fun runtimeResetDoesNotLeakPerRunCommands() = runBlocking {
        val input = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            setPixel(0, 0, Color.rgb(10, 20, 30))
        }
        val customOutput = Gmic.runCancellable(
            input,
            "temporary_filter",
            GmicOptions(),
            GmicExecutionOptions(customCommands = "temporary_filter : + 7")
        )
        assertEquals(Color.rgb(17, 27, 37), customOutput.getPixel(0, 0))
        customOutput.recycle()

        var rejected = false
        try {
            Gmic.runCancellable(input, "temporary_filter").recycle()
        } catch (_: GmicException) {
            rejected = true
        }
        assertTrue("A per-run custom command leaked into the base runtime", rejected)
        input.recycle()
    }

    @Test
    fun repeatedRunsDoNotAccumulateNativeWorkingBuffers() = runBlocking {
        val input = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(10, 20, 30))
        }
        Gmic.runCancellable(input, "+ 0").recycle()
        repeat(3) {
            Runtime.getRuntime().gc()
            delay(100)
        }
        val heapBefore = Debug.getNativeHeapAllocatedSize()
        val samples = ArrayList<Double>(50)

        repeat(25) {
            val startedAt = SystemClock.elapsedRealtimeNanos()
            Gmic.runCancellable(input, "+ 1").recycle()
            samples += (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0
        }
        repeat(3) {
            Runtime.getRuntime().gc()
            delay(100)
        }
        val heapAfterFirstBatch = Debug.getNativeHeapAllocatedSize()
        repeat(25) {
            val startedAt = SystemClock.elapsedRealtimeNanos()
            Gmic.runCancellable(input, "+ 1").recycle()
            samples += (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0
        }
        repeat(3) {
            Runtime.getRuntime().gc()
            delay(100)
        }
        val heapAfterSecondBatch = Debug.getNativeHeapAllocatedSize()
        val totalGrowth = heapAfterSecondBatch - heapBefore
        val secondBatchGrowth = heapAfterSecondBatch - heapAfterFirstBatch

        Log.i(
            "GmicBenchmark",
            "repeat=50 first10_avg_ms=${samples.take(10).average()} " +
                "last10_avg_ms=${samples.takeLast(10).average()} " +
                "native_heap_total_growth=$totalGrowth second_batch_growth=$secondBatchGrowth"
        )
        assertTrue(
            "Second batch retained $secondBatchGrowth bytes",
            secondBatchGrowth < 8L * 1024 * 1024
        )
        assertTrue("Native heap grew by $totalGrowth bytes", totalGrowth < 16L * 1024 * 1024)
        input.recycle()
    }

    @Test
    fun cancellingLongRunDoesNotBlockNextRun() = runBlocking {
        val slowInput = Bitmap.createBitmap(2048, 2048, Bitmap.Config.ARGB_8888)
        val slowRun = launch {
            Gmic.runCancellable(
                input = slowInput,
                command = "fx_poster_hope 0,3",
                options = GmicOptions(),
                executionOptions = GmicExecutionOptions(profilingEnabled = true)
            ).recycle()
        }

        delay(250)
        withTimeout(5_000) {
            slowRun.cancelAndJoin()
        }

        val fastInput = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            setPixel(0, 0, Color.rgb(1, 2, 3))
        }
        val fastOutput = withTimeout(5_000) {
            Gmic.runCancellable(fastInput, "+ 1")
        }

        assertEquals(Color.rgb(2, 3, 4), fastOutput.getPixel(0, 0))
        slowInput.recycle()
        fastInput.recycle()
        fastOutput.recycle()
    }

    @Test
    @LargeTest
    fun roundTrips8kBitmap() = runBlocking {
        val input = Bitmap.createBitmap(7680, 4320, Bitmap.Config.ARGB_8888).apply {
            setHasAlpha(false)
            eraseColor(Color.rgb(10, 20, 30))
        }
        val startedAt = SystemClock.elapsedRealtime()

        val output = withTimeout(30_000) {
            Gmic.runCancellable(input, "+ 0")
        }

        Log.i(
            "GmicBenchmark",
            "8K Bitmap -> G'MIC -> Bitmap: ${SystemClock.elapsedRealtime() - startedAt} ms"
        )
        assertEquals(7680, output.width)
        assertEquals(4320, output.height)
        assertEquals(Color.rgb(10, 20, 30), output.getPixel(0, 0))
        input.recycle()
        output.recycle()
    }

    @Test
    @LargeTest
    fun benchmarksCommandChainsAt1080p() = runBlocking {
        val input = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888).apply {
            setHasAlpha(false)
            eraseColor(Color.rgb(10, 20, 30))
        }
        Gmic.runCancellable(input, "+ 0").recycle()

        for (operationCount in listOf(1, 2, 5, 10, 20)) {
            val command = List(operationCount) { "+ 1" }.joinToString(" ")
            Gmic.runCancellable(input, command).recycle()
            val samples = List(5) {
                val startedAt = SystemClock.elapsedRealtimeNanos()
                val output = Gmic.runCancellable(
                    input,
                    command,
                    GmicOptions(),
                    GmicExecutionOptions(profilingEnabled = true)
                )
                val elapsedMs =
                    (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0
                assertEquals(10 + operationCount, Color.red(output.getPixel(0, 0)))
                output.recycle()
                elapsedMs
            }

            Log.i(
                "GmicBenchmark",
                "1920x1080 operations=$operationCount " +
                    "min_ms=${samples.min()} avg_ms=${samples.average()} samples_ms=$samples"
            )
        }
        input.recycle()
    }

    @Test
    @LargeTest
    fun benchmarksFullResolutionRoundTrips() = runBlocking {
        for ((width, height) in listOf(1920 to 1080, 4000 to 3000, 6000 to 4000)) {
            val input = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setHasAlpha(false)
                eraseColor(Color.rgb(10, 20, 30))
            }
            val startedAt = SystemClock.elapsedRealtimeNanos()
            val output = Gmic.runCancellable(
                input,
                "+ 1",
                GmicOptions(),
                GmicExecutionOptions(profilingEnabled = true)
            )
            val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0

            Log.i(
                "GmicBenchmark",
                "${width}x$height pixels=${width.toLong() * height} total_ms=$elapsedMs"
            )
            assertEquals(Color.rgb(11, 21, 31), output.getPixel(width / 2, height / 2))
            input.recycle()
            output.recycle()
        }
    }

    @Test
    @LargeTest
    fun profiles48MegapixelRoundTripWithoutDownscale() = runBlocking {
        val input = Bitmap.createBitmap(8000, 6000, Bitmap.Config.ARGB_8888).apply {
            setHasAlpha(false)
            eraseColor(Color.rgb(10, 20, 30))
        }
        val startedAt = SystemClock.elapsedRealtimeNanos()
        val output = withTimeout(120_000) {
            Gmic.runCancellable(
                input,
                "+ 1",
                GmicOptions(),
                GmicExecutionOptions(profilingEnabled = true)
            )
        }
        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0

        Log.i("GmicBenchmark", "8000x6000 pixels=48000000 total_ms=$elapsedMs")
        assertEquals(8000, output.width)
        assertEquals(6000, output.height)
        assertEquals(Color.rgb(11, 21, 31), output.getPixel(4000, 3000))
        input.recycle()
        output.recycle()
    }

    @Test
    @LargeTest
    fun benchmarksOpenMpThreadScalingAt1080p() = runBlocking {
        val input = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888).apply {
            setHasAlpha(false)
            eraseColor(Color.rgb(80, 120, 160))
        }
        val results = linkedMapOf<Int, List<Double>>()
        for (threads in listOf(1, Runtime.getRuntime().availableProcessors().coerceAtMost(4)).distinct()) {
            val executionOptions = GmicExecutionOptions(maxThreads = threads)
            repeat(2) {
                Gmic.runCancellable(
                    input,
                    "blur 12",
                    GmicOptions(),
                    executionOptions
                ).recycle()
            }
            results[threads] = List(5) {
                val startedAt = SystemClock.elapsedRealtimeNanos()
                Gmic.runCancellable(
                    input,
                    "blur 12",
                    GmicOptions(),
                    executionOptions
                ).recycle()
                (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0
            }
            Gmic.runCancellable(
                input,
                "blur 12",
                GmicOptions(),
                executionOptions.copy(profilingEnabled = true)
            ).recycle()
        }
        results.forEach { (threads, samples) ->
            val sorted = samples.sorted()
            Log.i(
                "GmicBenchmark",
                "OpenMP 1920x1080 threads=$threads median_ms=${sorted[sorted.size / 2]} " +
                    "min_ms=${sorted.first()} samples_ms=$samples"
            )
        }
        input.recycle()
    }

    @Test
    @LargeTest
    fun profilesHopePosterStagesAt1080p() = runBlocking {
        val input = createTexturedBitmap(1920, 1080)
        val stages = linkedMapOf(
            "to_rgb" to "to_rgb",
            "parallel_smooth" to "apc \"smooth 200,0,1,3,1\"",
            "quantize" to "quantize 7,0",
            "index_formula" to "to_gray f i!=5?i:i+1-2*(y%2)",
            "palette_map" to
                "to_gray (0,32,47;0,32,47;209,1,23;209,1,23;90,141,145;" +
                "-1,-1,-1;253,221,138) permute. yzcx map[0] [1] rm[1]",
            "complete" to HopePoster().command
        )
        val executionOptions = GmicExecutionOptions(
            maxThreads = Runtime.getRuntime().availableProcessors().coerceAtMost(4),
            profilingEnabled = true
        )

        for ((stage, command) in stages) {
            val startedAt = SystemClock.elapsedRealtimeNanos()
            val output = withTimeout(120_000) {
                Gmic.runCancellable(input, command, GmicOptions(), executionOptions)
            }
            val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0
            Log.i("GmicBenchmark", "HopePoster stage=$stage total_ms=$elapsedMs")
            output.recycle()
        }
        input.recycle()
    }

    @Test
    @LargeTest
    fun benchmarksHopePosterChannelParallelism() = runBlocking {
        val input = createTexturedBitmap(1280, 720)
        val maxThreads = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val threadCounts = listOf(1, maxThreads.coerceAtMost(4), maxThreads).distinct()
        var referenceHash: Long? = null

        for (threads in threadCounts) {
            val options = GmicExecutionOptions(maxThreads = threads)
            Gmic.runCancellable(
                input,
                "apc \"smooth 200,0,1,3,1\"",
                GmicOptions(),
                options
            ).recycle()
            val samples = List(3) {
                val startedAt = SystemClock.elapsedRealtimeNanos()
                Gmic.runCancellable(
                    input,
                    "apc \"smooth 200,0,1,3,1\"",
                    GmicOptions(),
                    options
                ).recycle()
                (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0
            }
            val profiledOutput = Gmic.runCancellable(
                input,
                "apc \"smooth 200,0,1,3,1\"",
                GmicOptions(),
                options.copy(profilingEnabled = true)
            )
            val outputHash = sampledBitmapHash(profiledOutput)
            referenceHash?.let { assertEquals(it, outputHash) } ?: run {
                referenceHash = outputHash
            }
            profiledOutput.recycle()
            Log.i(
                "GmicBenchmark",
                "HopePoster smooth threads=$threads min_ms=${samples.min()} " +
                    "avg_ms=${samples.average()} samples_ms=$samples"
            )
        }
        input.recycle()
    }

    @Test
    @LargeTest
    fun hopePosterCompletesAt12MegapixelsWithoutDownscale() = runBlocking {
        val input = createTexturedBitmap(4000, 3000)
        val inputHash = sampledBitmapHash(input)
        val startedAt = SystemClock.elapsedRealtime()

        val output = withTimeout(180_000) {
            Gmic.runCancellable(
                input,
                HopePoster().command,
                GmicOptions(),
                GmicExecutionOptions(profilingEnabled = true)
            )
        }

        Log.i(
            "GmicBenchmark",
            "4000x3000 HopePoster: ${SystemClock.elapsedRealtime() - startedAt} ms " +
                "sample_hash=${sampledBitmapHash(output)}"
        )
        assertEquals(4000, output.width)
        assertEquals(3000, output.height)
        assertTrue(sampledBitmapHash(output) != inputHash)
        input.recycle()
        output.recycle()
    }

    @Test
    @LargeTest
    fun hopePosterCompletesAt48MegapixelsWithoutDownscale() = runBlocking {
        val input = createTexturedBitmap(8000, 6000)
        val inputHash = sampledBitmapHash(input)
        val startedAt = SystemClock.elapsedRealtime()

        val output = withTimeout(300_000) {
            Gmic.runCancellable(
                input,
                HopePoster().command,
                GmicOptions(),
                GmicExecutionOptions(profilingEnabled = true)
            )
        }

        Log.i(
            "GmicBenchmark",
            "8000x6000 HopePoster: ${SystemClock.elapsedRealtime() - startedAt} ms " +
                "sample_hash=${sampledBitmapHash(output)}"
        )
        assertEquals(8000, output.width)
        assertEquals(6000, output.height)
        assertTrue(sampledBitmapHash(output) != inputHash)
        input.recycle()
        output.recycle()
    }

    @Test
    @LargeTest
    fun cancelling8kRunReleasesInterpreter() = runBlocking {
        val slowInput = Bitmap.createBitmap(7680, 4320, Bitmap.Config.ARGB_8888).apply {
            setHasAlpha(false)
        }
        val slowRun = launch {
            Gmic.runCancellable(
                input = slowInput,
                command = "fx_poster_hope 0,3"
            ).recycle()
        }

        delay(1_500)
        withTimeout(5_000) {
            slowRun.cancelAndJoin()
        }
        slowInput.recycle()

        val fastInput = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            setPixel(0, 0, Color.BLACK)
        }
        val fastOutput = withTimeout(5_000) {
            Gmic.runCancellable(fastInput, "+ 1")
        }

        assertEquals(1, Color.red(fastOutput.getPixel(0, 0)))
        fastInput.recycle()
        fastOutput.recycle()
    }
}
