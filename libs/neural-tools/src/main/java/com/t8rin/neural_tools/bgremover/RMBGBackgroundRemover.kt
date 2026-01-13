package com.t8rin.neural_tools.bgremover

internal object RMBGBackgroundRemover : GenericBackgroundRemover(
    downloadLink =
        "https://github.com/T8RIN/ImageToolboxRemoteResources/raw/refs/heads/main/onnx/bgremove/RMBG_1.4.ort",
    trainedSize = 1024
)