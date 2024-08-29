package com.t8rin.awebp.decoder

import android.graphics.Bitmap
import com.github.penfeizhou.animation.io.FileReader
import com.github.penfeizhou.animation.webp.WebPDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class AnimatedWebpDecoder(
    private val sourceFile: File,
    private val scope: CoroutineScope
) {
    private val framesChannel: Channel<Bitmap> = Channel(Channel.BUFFERED)
    fun decodeFrames(): Flow<Bitmap> = framesChannel.receiveAsFlow()

    private val frameCountChannel: Channel<Int> = Channel(Channel.BUFFERED)
    fun frameCount(): Flow<Int> = frameCountChannel.receiveAsFlow()

    init {
        WebPDrawable { FileReader(sourceFile) }.also { drawable ->
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
                    decoder.getFrameBitmap(it)?.let { bitmap ->
                        framesChannel.send(bitmap)
                    }
                }
            }
        }
    }
}