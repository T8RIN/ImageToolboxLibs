package com.t8rin.fast_noise.texture.maze

import android.graphics.Bitmap
import com.t8rin.fast_noise.texture.ProceduralTextureGenerator
import com.t8rin.fast_noise.texture.ProceduralTextureParameters
import com.t8rin.fast_noise.texture.internal.ProceduralTextureNative

enum class MazeAlgorithm(internal val nativeValue: Int) {
    Backtracking(0),
    Prim(1),
    Kruskal(2)
}

data class MazeTextureParameters(
    override val seed: Int = 1337,
    val columns: Int = 32,
    val rows: Int = 32,
    val algorithm: MazeAlgorithm = MazeAlgorithm.Backtracking,
    val wallWidth: Float = 0.12f,
    val padding: Float = 0.04f,
    val openings: Boolean = true,
    val wallColor: Int = 0xFF172638.toInt(),
    val backgroundColor: Int = 0xFFFFF3D8.toInt()
) : ProceduralTextureParameters

class MazeTextureGenerator : ProceduralTextureGenerator<MazeTextureParameters> {
    override fun generate(width: Int, height: Int, parameters: MazeTextureParameters): Bitmap? {
        require(width > 0 && height > 0) { "Texture dimensions must be positive" }
        require(width.toLong() * height <= Int.MAX_VALUE) { "Texture is too large" }
        require(parameters.columns in 1..256 && parameters.rows in 1..256) {
            "Maze dimensions must be between 1 and 256"
        }
        return ProceduralTextureNative.generateMaze(
            width = width,
            height = height,
            columns = parameters.columns,
            rows = parameters.rows,
            algorithm = parameters.algorithm.nativeValue,
            seed = parameters.seed,
            wallWidth = parameters.wallWidth,
            padding = parameters.padding,
            openings = parameters.openings,
            wallColor = parameters.wallColor,
            backgroundColor = parameters.backgroundColor
        )
    }
}
