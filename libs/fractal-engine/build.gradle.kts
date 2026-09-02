import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Validation-only task has no outputs")
abstract class ValidateFractalEngineNativeBinaries : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val checksumFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val binaryFiles: ConfigurableFileCollection

    @get:Internal
    abstract val sourceRoot: DirectoryProperty

    @get:Internal
    abstract val moduleRoot: DirectoryProperty

    @TaskAction
    fun validateBinaries() {
        val root = sourceRoot.get().asFile
        val expectedChecksum = sourceChecksum(
            root = root,
            files = sourceFiles.files
        )
        val checksum = checksumFile.get().asFile
        if (!checksum.isFile) {
            throw GradleException(
                "Missing ${checksum.relativeToModule()}; run src/main/rust/build.sh"
            )
        }
        val recordedChecksum = checksum.readText().trim()
        if (recordedChecksum != expectedChecksum) {
            throw GradleException(
                "Rust sources changed after libfractal_engine.so was built; " +
                    "run src/main/rust/build.sh"
            )
        }
        val checksumBytes = expectedChecksum.toByteArray(StandardCharsets.US_ASCII)
        binaryFiles.files.forEach { binary ->
            if (!binary.isFile || !binary.readBytes().containsSequence(checksumBytes)) {
                throw GradleException(
                    "${binary.relativeToModule()} is stale or missing; " +
                        "run src/main/rust/build.sh"
                )
            }
        }
    }

    private fun sourceChecksum(root: File, files: Collection<File>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        files
            .sortedBy { it.relativeTo(root).invariantSeparatorsPath }
            .forEach { sourceFile ->
                digest.update(
                    sourceFile.relativeTo(root).invariantSeparatorsPath
                        .toByteArray(StandardCharsets.UTF_8)
                )
                digest.update(0)
                digest.update(sourceFile.readBytes())
                digest.update(0)
            }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun ByteArray.containsSequence(sequence: ByteArray): Boolean {
        if (sequence.isEmpty()) return true
        if (sequence.size > size) return false
        for (startIndex in 0..size - sequence.size) {
            var matches = true
            for (sequenceIndex in sequence.indices) {
                if (this[startIndex + sequenceIndex] != sequence[sequenceIndex]) {
                    matches = false
                    break
                }
            }
            if (matches) return true
        }
        return false
    }

    private fun File.relativeToModule(): String = relativeTo(
        moduleRoot.get().asFile
    ).invariantSeparatorsPath
}

plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.fractal_engine"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
}

val rustRoot = layout.projectDirectory.dir("src/main/rust")
val nativeSourceChecksum = rustRoot.file("fractal_engine.android.sha256")
val rustSources = files(
    rustRoot.file("build.sh"),
    rustRoot.file("fractal_engine/Cargo.toml"),
    rustRoot.file("fractal_engine/Cargo.lock"),
    rustRoot.dir("fractal_engine/src").asFileTree.matching {
        include("**/*.rs")
    }
)
val nativeBinaries = listOf("arm64-v8a", "armeabi-v7a", "x86_64").map { abi ->
    layout.projectDirectory.file("src/main/jniLibs/$abi/libfractal_engine.so")
}

val validateFractalEngineNativeBinaries =
    tasks.register<ValidateFractalEngineNativeBinaries>("validateFractalEngineNativeBinaries") {
        sourceFiles.from(rustSources)
        checksumFile.set(nativeSourceChecksum)
        binaryFiles.from(nativeBinaries)
        sourceRoot.set(rustRoot)
        moduleRoot.set(layout.projectDirectory)
}

tasks.named("preBuild").configure {
    dependsOn(validateFractalEngineNativeBinaries)
}
