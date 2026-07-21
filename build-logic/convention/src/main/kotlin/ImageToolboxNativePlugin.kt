/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.LibraryExtension
import com.t8rin.imagetoolbox.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private val supportedAbis = arrayOf("armeabi-v7a", "arm64-v8a", "x86_64")

@Suppress("unused")
class ImageToolboxNativePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<LibraryExtension> {
                ndkVersion = libs.versions.androidNdk.get()
                defaultConfig {
                    ndk.abiFilters.clear()
                    ndk.abiFilters.addAll(supportedAbis)
                }

                val cmakeLists = layout.projectDirectory.file("src/main/cpp/CMakeLists.txt")
                if (cmakeLists.asFile.exists()) {
                    defaultConfig {
                        externalNativeBuild {
                            cmake {
                                arguments += "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"
                            }
                        }
                    }
                    externalNativeBuild {
                        cmake {
                            path = cmakeLists.asFile
                            version = libs.versions.androidCmake.get()
                        }
                    }
                }
            }
        }
    }
}