/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag


internal fun Project.configureCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            "implementation"(libs.findLibrary("androidx.material3").get())
            "implementation"(libs.findLibrary("androidx.material3.window.sizeclass").get())
            "implementation"(libs.findLibrary("androidx.material").get())
            "implementation"(libs.findLibrary("androidx.material.icons.extended").get())
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        featureFlags = setOf(
            ComposeFeatureFlag.OptimizeNonSkippingGroups
        )

        stabilityConfigurationFile =
            rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
    }
}
