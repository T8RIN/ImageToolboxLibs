package com.t8rin.trickle

import com.t8rin.trickle.pipelines.EffectsPipeline
import com.t8rin.trickle.pipelines.LowPolyPipeline

object Trickle : LowPolyPipeline by LowPolyPipelineImpl,
    EffectsPipeline by EffectsPipelineImpl {

    init {
        System.loadLibrary("trickle")
    }

}