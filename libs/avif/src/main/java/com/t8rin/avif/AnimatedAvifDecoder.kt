package com.t8rin.avif

import android.content.Context
import android.net.Uri
import com.github.penfeizhou.animation.avif.AVIFDrawable
import com.github.penfeizhou.animation.io.FileReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


class AnimatedAvifDecoder(
    private val sourceFile: File,
    private val coroutineScope: CoroutineScope
) {
    constructor(
        context: Context,
        sourceUri: Uri,
        coroutineScope: CoroutineScope
    ) : this(
        sourceFile = sourceUri.toFile(context),
        coroutineScope = coroutineScope
    )

    private val framesChannel: Channel<AvifFrame> = Channel(Channel.BUFFERED)
    fun frames(): Flow<AvifFrame> = framesChannel.receiveAsFlow()

    private val frameCountChannel: Channel<Int> = Channel(Channel.BUFFERED)
    fun frameCount(): Flow<Int> = frameCountChannel.receiveAsFlow()

    init {
        AVIFDrawable { FileReader(sourceFile) }.also { drawable ->
            coroutineScope.launch {
                val decoder = drawable.frameSeqDecoder.apply { bounds }

                val frameCount = decoder.frameCount
                frameCountChannel.send(frameCount)

                val durations = List(frameCount) {
                    decoder.getFrame(it).frameDuration
                }
                durations.forEachIndexed { index, duration ->
                    if (!currentCoroutineContext().isActive) {
                        currentCoroutineContext().cancel(null)
                        return@launch
                    }
                    decoder.getFrameBitmap(index)?.let { bitmap ->
                        framesChannel.send(
                            AvifFrame(
                                bitmap = bitmap,
                                duration = duration
                            )
                        )
                    }
                }

                frameCountChannel.close()
                framesChannel.close()
                currentCoroutineContext().cancel()
                drawable.stop()
                decoder.stop()
            }
        }
    }
}

private fun Uri.inputStream(
    context: Context
): InputStream? = context
    .contentResolver
    .openInputStream(this)

private fun Uri.toFile(
    context: Context
): File {
    val file = File(context.cacheDir, "temp.webp")
    inputStream(context)?.use { stream ->
        stream.copyTo(file.outputStream())
    }
    return file
}