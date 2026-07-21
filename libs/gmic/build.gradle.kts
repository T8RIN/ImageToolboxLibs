plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.gmic"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}