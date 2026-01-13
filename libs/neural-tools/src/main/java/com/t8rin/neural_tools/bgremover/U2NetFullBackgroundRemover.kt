package com.t8rin.neural_tools.bgremover

internal object U2NetFullBackgroundRemover : GenericBackgroundRemover(
    downloadLink =
        "https://github.com/T8RIN/ImageToolboxRemoteResources/raw/refs/heads/main/onnx/bgremove/u2net.onnx",
    trainedSize = 320
)