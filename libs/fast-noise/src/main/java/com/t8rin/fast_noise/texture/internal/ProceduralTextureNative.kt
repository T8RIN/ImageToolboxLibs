package com.t8rin.fast_noise.texture.internal

import android.graphics.Bitmap

internal object ProceduralTextureNative {

    init {
        System.loadLibrary("fast-noise")
    }

    external fun generate(
        width: Int,
        height: Int,
        textureType: Int,
        seed: Int,
        colors: IntArray,
        parameters: FloatArray
    ): Bitmap?
}

internal object TextureType {
    const val LAVA = 0
    const val CLOUDS = 1
    const val SMOKE = 2
    const val STONE = 3
    const val WOOD = 4
    const val CAMOUFLAGE = 5
    const val PAPER = 6
    const val RUST = 7
    const val FABRIC = 8
    const val TOPOGRAPHY = 9
    const val CELLS = 10
    const val CRACKS = 11
    const val WATER_RIPPLES = 12
    const val FOLIAGE = 13
    const val BRICKS = 14
    const val TERRAIN = 15
    const val ICE = 16
    const val SAND = 17
    const val NEBULA = 18
    const val HONEYCOMB = 19
    const val GRASS = 20
    const val DIRT = 21
    const val LEATHER = 22
    const val CONCRETE = 23
    const val ASPHALT = 24
    const val MOSS = 25
    const val FIRE = 26
    const val AURORA = 27
    const val OIL_SLICK = 28
    const val WATERCOLOR = 29
    const val FLOW = 30
    const val OPAL = 31
    const val DAMASCUS = 32
    const val LIGHTNING = 33
    const val VELVET = 34
    const val INK_MARBLING = 35
    const val HOLOGRAPHIC = 36
    const val BIOLUMINESCENCE = 37
    const val COSMIC_VORTEX = 38
    const val LAVA_LAMP = 39
    const val EVENT_HORIZON = 40
    const val FRACTAL_BLOOM = 41
    const val CHROMATIC_TUNNEL = 42
    const val ECLIPSE_CORONA = 43
    const val STRANGE_ATTRACTOR = 44
    const val FERROFLUID_CROWN = 45
    const val SUPERNOVA = 46
    const val IRIS = 47
    const val PEACOCK_FEATHER = 48
    const val NAUTILUS_SHELL = 49
    const val RINGED_PLANET = 50
    const val GEODE = 51
    const val PRISMATIC_LIGHT = 52
    const val STAINED_GLASS = 53
    const val KELP_FOREST = 54
    const val FROST_FERN = 55
    const val LIQUID_CRYSTAL = 56
    const val DRAGON_SCALES = 57
    const val FIREFLY_SWARM = 58
    const val MYCELIUM = 59
    const val KINTSUGI = 60
    const val CARBON_FIBER = 61
    const val CIRCUIT_BOARD = 62
    const val SOAP_FILM = 63
    const val MOIRE_GUILLOCHE = 64
    const val SNAKE_SKIN = 65
    const val TERRAZZO = 66
    const val GALAXY_FILAMENTS = 67
    const val VOLCANIC_OBSIDIAN = 68
    const val MOTHERBOARD_HEATMAP = 69
    const val MICROSCOPIC_DIATOMS = 70
    const val REACTION_DIFFUSION = 71
    const val CORAL_GROWTH = 72
    const val SLIME_MOLD = 73
    const val DENDRITIC_CRYSTAL = 74
    const val ELECTRIC_ARC_FIELD = 75
    const val CLOUD_CHAMBER = 76
    const val TURBULENT_INK = 77
    const val CELLULAR_EMBRYO = 78
    const val NEURAL_GARDEN = 79
    const val MAGNETIC_FIELD = 80
    const val RIVER_DELTA = 81
    const val LICHEN_COLONY = 82
    const val BACTERIAL_CULTURE = 83
    const val FLUID_VORTICITY = 84
    const val CRYSTAL_GROWTH = 85
    const val GALACTIC_WEB = 86
    const val VEINED_LEAF = 87
    const val POROUS_SPONGE = 88
    const val RAIN_ON_GLASS = 89
    const val EMBER_FIELD = 90
    const val QUANTUM_FOAM = 91
    const val CHLADNI_PLATE = 92
    const val CYMATIC_ROSETTE = 93
    const val LICHTENBERG_FIGURE = 94
    const val QUASICRYSTAL = 95
    const val MANDELBROT = 96
    const val BURNING_SHIP = 97
    const val JULIA_SET = 98
    const val KALEIDOSCOPE_CRYSTAL = 99
    const val SPECTRAL_PRISM = 100
    const val TOPOLOGICAL_KNOT = 101
    const val X_RAY_BOTANICAL = 102
    const val CHROMATOPHORE = 103
    const val BIOMECHANICAL_TISSUE = 104
    const val GILDED_FILIGREE = 105
    const val ANCIENT_RUNES = 106
    const val SOLAR_GRANULATION = 107
    const val LUNAR_EJECTA = 108
    const val OCEAN_CURRENTS = 109
    const val INK_WASH_MOUNTAINS = 110
    const val NEON_CITY = 111
    const val PHYLLOTAXIS_BLOOM = 112
    const val SIERPINSKI_TRIANGLE = 113
    const val APOLLONIAN_GASKET = 114
    const val HYPERBOLIC_TILING = 115
    const val MOEBIUS_WEAVE = 116
    const val RORSCHACH_INKBLOT = 117
    const val SEISMIC_INTERFERENCE = 118
    const val RAYLEIGH_BENARD = 119
    const val ORIGAMI_FACETS = 120
    const val FIBER_OPTIC_BUNDLE = 121
}
