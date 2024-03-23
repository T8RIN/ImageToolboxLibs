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

plugins {
    alias(libs.plugins.image.toolbox.library)
    id("maven-publish")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = "com.github.t8rin"
                artifactId = "imageToolboxLibs"
                version = "1.3.0"
                from(components["release"])
            }
        }
    }
}

android.namespace = "com.t8rin.image_toolbox"

dependencies {
    api(projects.libs.androidwm)
    api(projects.libs.beforeafter)
    api(projects.libs.colordetector)
    api(projects.libs.colorpicker) {
        exclude("com.github.SmartToolFactory", "Compose-Color-Detector")
    }
    api(projects.libs.cropper)
    api(projects.libs.dynamicTheme)
    api(projects.libs.extendedcolors)
    api(projects.libs.gesture)
    api(projects.libs.gpuimage)
    api(projects.libs.image)
    api(projects.libs.logger)
    api(projects.libs.modalsheet)
    api(projects.libs.placeholder)
    api(projects.libs.screenshot)
    api(projects.libs.systemuicontroller)
    api(projects.libs.zoomable)
    api(projects.libs.gifConverter)
    api(projects.libs.apng)
}
