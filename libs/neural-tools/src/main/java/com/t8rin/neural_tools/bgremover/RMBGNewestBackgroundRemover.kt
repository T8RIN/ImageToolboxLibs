package com.t8rin.neural_tools.bgremover

internal object RMBGNewestBackgroundRemover : GenericBackgroundRemover(
    downloadLink =
        "https://github.com/T8RIN/ImageToolboxRemoteResources/raw/refs/heads/main/onnx/bgremove/RMBG_2.0.onnx",
    trainedSize = 1024
)