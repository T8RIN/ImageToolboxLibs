package com.t8rin.neural_tools.bgremover

internal object BiRefNetTinyBackgroundRemover : GenericBackgroundRemover(
    downloadLink =
        "https://huggingface.co/T8RIN/ddcolor-onnx/resolve/main/birefnet_swin_tiny.ort?download=true",
    trainedSize = 1024
)