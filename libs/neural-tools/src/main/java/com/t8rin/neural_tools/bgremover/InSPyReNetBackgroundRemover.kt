package com.t8rin.neural_tools.bgremover

internal object InSPyReNetBackgroundRemover : GenericBackgroundRemover(
    downloadLink =
        "https://github.com/T8RIN/ImageToolboxRemoteResources/raw/refs/heads/main/onnx/bgremove/inspyrenet.onnx",
    trainedSize = 1024
)