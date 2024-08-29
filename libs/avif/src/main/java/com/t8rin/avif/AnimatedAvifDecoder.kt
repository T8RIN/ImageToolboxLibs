package com.t8rin.avif

import android.graphics.Bitmap
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


class AnimatedAvifDecoder(
    private val sourceFile: File,
    private val scope: CoroutineScope
) {
    private val framesChannel: Channel<Bitmap> = Channel(Channel.BUFFERED)
    fun decodeFrames(): Flow<Bitmap> = framesChannel.receiveAsFlow()

    private val frameCountChannel: Channel<Int> = Channel(Channel.BUFFERED)
    fun frameCount(): Flow<Int> = frameCountChannel.receiveAsFlow()

    init {
        AVIFDrawable { FileReader(sourceFile) }.also { drawable ->
            scope.launch {
                val decoder = drawable.frameSeqDecoder
                decoder.bounds
                val frameCount = decoder.frameCount
                frameCountChannel.send(frameCount)
                val delay: MutableList<Int> = ArrayList()
                for (i in 0 until frameCount) {
                    delay.add(decoder.getFrame(i).frameDuration)
                }
                repeat(frameCount) {
                    if (!currentCoroutineContext().isActive) {
                        currentCoroutineContext().cancel(null)
                        return@launch
                    }
                    decoder.getFrameBitmap(it)?.let { bitmap ->
                        framesChannel.send(bitmap)
                    }
                }
            }
        }
    }
}