package com.t8rin.neural_tools.bgremover

internal object ISNetBackgroundRemover : GenericBackgroundRemover(
    downloadLink = "https://huggingface.co/T8RIN/ddcolor-onnx/resolve/main/isnet-general-use.onnx?download=true",
    trainedSize = 1024
)