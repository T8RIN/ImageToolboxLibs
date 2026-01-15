package com.t8rin.neural_tools.bgremover

import android.graphics.Bitmap
import com.t8rin.neural_tools.DownloadProgress
import com.t8rin.neural_tools.NeuralTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object BgRemover : NeuralTool() {

    enum class Type {
        RMBG1_4,
        InSPyReNet,
        U2NetP,
        U2Net,
        BiRefNet,
        BiRefNetTiny,
        ISNet
    }

    val downloadedModels: StateFlow<List<Type>> = channelFlow {
        val map = ConcurrentHashMap<Type, Boolean>()

        Type.entries.forEach { type ->
            if (type == Type.U2NetP) {
                map[type] = true
                send(map.filterValues { it }.keys.toList())
            }

            launch {
                getRemover(type).isDownloaded.collectLatest { isDownloaded ->
                    map[type] = isDownloaded
                    send(map.filterValues { it }.keys.toList())
                }
            }
        }
    }.debounce(500).stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.Eagerly,
        initialValue = listOf(Type.U2NetP)
    )

    fun downloadModel(
        type: Type
    ): Flow<DownloadProgress> = getRemover(type).startDownload()

    fun removeBackground(
        image: Bitmap,
        type: Type
    ): Bitmap? = runCatching {
        getRemover(type).removeBackground(image)
    }.getOrNull()

    fun closeAll() {
        Type.entries.forEach {
            getRemover(it).close()
        }
    }

    fun getRemover(type: Type) = when (type) {
        Type.RMBG1_4 -> RMBGBackgroundRemover
        Type.InSPyReNet -> InSPyReNetBackgroundRemover
        Type.U2NetP -> U2NetPortableBackgroundRemover
        Type.U2Net -> U2NetFullBackgroundRemover
        Type.BiRefNet -> BiRefNetBackgroundRemover
        Type.BiRefNetTiny -> BiRefNetTinyBackgroundRemover
        Type.ISNet -> ISNetBackgroundRemover
    }

}