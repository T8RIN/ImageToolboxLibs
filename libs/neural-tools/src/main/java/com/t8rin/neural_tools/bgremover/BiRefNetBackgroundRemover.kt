package com.t8rin.neural_tools.bgremover

internal object BiRefNetBackgroundRemover : GenericBackgroundRemover(
    downloadLink =
        "https://huggingface.co/T8RIN/ddcolor-onnx/resolve/main/birefnet_fp16.ort?download=true",
    trainedSize = 1024
)