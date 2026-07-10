/*
 *    Copyright 2018 huangyz0918
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

#include <android/bitmap.h>
#include <jni.h>

#include <cstdint>
#include <algorithm>
#include <cmath>
#include <string>

namespace {

    constexpr uint32_t kLsbMagic = 0x4c534257;
    constexpr uint32_t kFdMagic = 0x4644574d;
    constexpr uint32_t kFdHeaderBits = 64;
    constexpr uint32_t kFdBitRedundancy = 3;
    constexpr double kFdStrength = 80.0;
    constexpr double kPi = 3.14159265358979323846;

    bool lockBitmap(JNIEnv *env, jobject bitmap, AndroidBitmapInfo *info, void **pixels) {
        return bitmap != nullptr
                && AndroidBitmap_getInfo(env, bitmap, info) == ANDROID_BITMAP_RESULT_SUCCESS
                && AndroidBitmap_lockPixels(env, bitmap, pixels) == ANDROID_BITMAP_RESULT_SUCCESS;
    }

    std::string stringFromJava(JNIEnv *env, jstring value) {
        const char *characters = env->GetStringUTFChars(value, nullptr);
        if (characters == nullptr) {
            return {};
        }
        std::string result(characters);
        env->ReleaseStringUTFChars(value, characters);
        return result;
    }

    uint32_t readArgb(const AndroidBitmapInfo &info, const void *pixels, uint32_t x, uint32_t y) {
        const auto *row = static_cast<const uint8_t *>(pixels) + y * info.stride;
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            return static_cast<const uint32_t *>(static_cast<const void *>(row))[x];
        }
        const uint16_t pixel = static_cast<const uint16_t *>(static_cast<const void *>(row))[x];
        const uint32_t red = (pixel >> 11) & 0x1f;
        const uint32_t green = (pixel >> 5) & 0x3f;
        const uint32_t blue = pixel & 0x1f;
        return 0xff000000 | ((red * 255 / 31) << 16) | ((green * 255 / 63)
                << 8) | (blue * 255 / 31);
    }

    void writeArgb(const AndroidBitmapInfo &info, void *pixels, uint32_t x, uint32_t y, uint32_t color) {
        auto *row = static_cast<uint8_t *>(pixels) + y * info.stride;
        static_cast<uint32_t *>(static_cast<void *>(row))[x] = color;
    }

    double dctScale(int index) {
        return index == 0 ? std::sqrt(0.125) : 0.5;
    }

    double dctCosine(int frequency, int coordinate) {
        return std::cos((2.0 * coordinate + 1.0) * frequency * kPi / 16.0);
    }

    void forwardDct(const double input[8][8], double output[8][8]) {
        for (int verticalFrequency = 0; verticalFrequency < 8; ++verticalFrequency) {
            for (int horizontalFrequency = 0; horizontalFrequency < 8; ++horizontalFrequency) {
                double sum = 0.0;
                for (int y = 0; y < 8; ++y) {
                    for (int x = 0; x < 8; ++x) {
                        sum += input[y][x] * dctCosine(verticalFrequency, y)
                                * dctCosine(horizontalFrequency, x);
                    }
                }
                output[verticalFrequency][horizontalFrequency] = dctScale(verticalFrequency)
                        * dctScale(horizontalFrequency) * sum;
            }
        }
    }

    void inverseDct(const double input[8][8], double output[8][8]) {
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 8; ++x) {
                double sum = 0.0;
                for (int verticalFrequency = 0; verticalFrequency < 8; ++verticalFrequency) {
                    for (int horizontalFrequency = 0; horizontalFrequency < 8; ++horizontalFrequency) {
                        sum += dctScale(verticalFrequency) * dctScale(horizontalFrequency)
                                * input[verticalFrequency][horizontalFrequency]
                                * dctCosine(verticalFrequency, y)
                                * dctCosine(horizontalFrequency, x);
                    }
                }
                output[y][x] = sum;
            }
        }
    }

    uint32_t clampColor(double value) {
        return static_cast<uint32_t>(std::round(std::max(0.0, std::min(255.0, value))));
    }

    bool readFdBit(const AndroidBitmapInfo &info, const void *pixels, uint32_t blockX, uint32_t blockY,
            uint32_t channelShift);

    bool writeFdBit(const AndroidBitmapInfo &info, void *pixels, uint32_t blockX, uint32_t blockY,
            uint32_t channelShift, bool bit) {
        double input[8][8];
        double spectrum[8][8];
        double output[8][8];
        for (int attempt = 1; attempt <= 3; ++attempt) {
            for (uint32_t y = 0; y < 8; ++y) {
                for (uint32_t x = 0; x < 8; ++x) {
                    input[y][x] = (readArgb(info, pixels, blockX * 8 + x, blockY * 8 + y)
                            >> channelShift) & 0xff;
                }
            }
            forwardDct(input, spectrum);
            const double difference = spectrum[2][3] - spectrum[3][2];
            const double targetDifference = bit ? kFdStrength * attempt : -kFdStrength * attempt;
            const double adjustment = (targetDifference - difference) / 2.0;
            spectrum[2][3] += adjustment;
            spectrum[3][2] -= adjustment;
            inverseDct(spectrum, output);
            for (uint32_t y = 0; y < 8; ++y) {
                for (uint32_t x = 0; x < 8; ++x) {
                    const uint32_t pixelX = blockX * 8 + x;
                    const uint32_t pixelY = blockY * 8 + y;
                    const uint32_t color = readArgb(info, pixels, pixelX, pixelY);
                    const uint32_t component = clampColor(output[y][x]);
                    writeArgb(info, pixels, pixelX, pixelY,
                            (color & ~(0xffu << channelShift)) | (component << channelShift));
                }
            }
            if (readFdBit(info, pixels, blockX, blockY, channelShift) == bit) {
                return true;
            }
        }
        return false;
    }

    bool readFdBit(const AndroidBitmapInfo &info, const void *pixels, uint32_t blockX, uint32_t blockY,
            uint32_t channelShift) {
        double input[8][8];
        double spectrum[8][8];
        for (uint32_t y = 0; y < 8; ++y) {
            for (uint32_t x = 0; x < 8; ++x) {
                input[y][x] = (readArgb(info, pixels, blockX * 8 + x, blockY * 8 + y)
                        >> channelShift) & 0xff;
            }
        }
        forwardDct(input, spectrum);
        return spectrum[2][3] >= spectrum[3][2];
    }

    bool frameBit(uint32_t magic, const std::string &watermark, bool isImage, uint32_t index) {
        if (index < 32) {
            return (magic >> (31 - index)) & 1;
        }
        if (index == 32) {
            return isImage;
        }
        if (index < 40) {
            return false;
        }
        if (index < kFdHeaderBits) {
            return (watermark.size() >> (63 - index)) & 1;
        }
        const uint32_t payloadIndex = index - kFdHeaderBits;
        return (static_cast<unsigned char>(watermark[payloadIndex / 8])
                >> (7 - payloadIndex % 8)) & 1;
    }

    uint64_t fdCapacity(const AndroidBitmapInfo &info) {
        return static_cast<uint64_t>(info.width / 8) * (info.height / 8) * 3 / kFdBitRedundancy;
    }

    bool readFdFrameBit(const AndroidBitmapInfo &info, const void *pixels, uint32_t index) {
        const uint32_t blockColumns = info.width / 8;
        const uint32_t shifts[] = {16, 8, 0};
        uint32_t ones = 0;
        for (uint32_t replica = 0; replica < kFdBitRedundancy; ++replica) {
            const uint32_t physicalIndex = index * kFdBitRedundancy + replica;
            const uint32_t blockIndex = physicalIndex / 3;
            ones += readFdBit(info, pixels, blockIndex % blockColumns, blockIndex / blockColumns,
                    shifts[physicalIndex % 3]);
        }
        return ones > kFdBitRedundancy / 2;
    }

    jstring extractLsbWatermark(JNIEnv *env, jobject bitmap, bool isImage) {
        AndroidBitmapInfo info{};
        void *pixels = nullptr;
        if (!lockBitmap(env, bitmap, &info, &pixels)) {
            return nullptr;
        }
        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888
                && info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
            AndroidBitmap_unlockPixels(env, bitmap);
            return nullptr;
        }
        const uint64_t capacity = static_cast<uint64_t>(info.width) * info.height * 3;
        if (capacity < kFdHeaderBits) {
        AndroidBitmap_unlockPixels(env, bitmap);
            return nullptr;
        }

        const uint32_t shifts[] = {16, 8, 0};
        const auto readBit = [&](uint32_t index) {
            const uint32_t pixelIndex = index / 3;
            const uint32_t color = readArgb(info, pixels, pixelIndex % info.width, pixelIndex / info.width);
            return (color >> shifts[index % 3]) & 1;
        };
        uint32_t magic = 0;
        for (uint32_t index = 0; index < 32; ++index) {
            magic = (magic << 1) | readBit(index);
    }
        if (magic != kLsbMagic || readBit(32) != (isImage == JNI_TRUE)) {
            AndroidBitmap_unlockPixels(env, bitmap);
            return nullptr;
    }
        uint32_t length = 0;
        for (uint32_t index = 40; index < kFdHeaderBits; ++index) {
            length = (length << 1) | readBit(index);
        }
        const uint64_t frameSize = kFdHeaderBits + static_cast<uint64_t>(length) * 8;
        if (frameSize > capacity) {
            AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }
        std::string result(length, '\0');
        for (uint32_t index = 0; index < length * 8; ++index) {
            result[index / 8] = static_cast<char>((static_cast<unsigned char>(result[index / 8])
                    << 1)
                    | readBit(kFdHeaderBits + index));
    }
        AndroidBitmap_unlockPixels(env, bitmap);
        return env->NewStringUTF(result.c_str());
    }

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_watermark_androidwm_utils_StringUtils_embedLsbWatermark(JNIEnv *env, jclass,
        jobject source, jobject destination,
        jstring watermark, jboolean isImage) {
    if (watermark == nullptr) {
        return JNI_FALSE;
    }
    const std::string watermarkBytes = stringFromJava(env, watermark);
    if (watermarkBytes.empty() || watermarkBytes.size() > (UINT32_MAX - kFdHeaderBits) / 8) {
        return JNI_FALSE;
    }

    AndroidBitmapInfo sourceInfo{};
    AndroidBitmapInfo destinationInfo{};
    void *sourcePixels = nullptr;
    void *destinationPixels = nullptr;
    if (!lockBitmap(env, source, &sourceInfo, &sourcePixels)) {
        return JNI_FALSE;
    }
    const bool destinationLocked = lockBitmap(env, destination, &destinationInfo, &destinationPixels);
    if (!destinationLocked) {
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }
    if ((sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888
            && sourceInfo.format != ANDROID_BITMAP_FORMAT_RGB_565)
            || destinationInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888
            || sourceInfo.width != destinationInfo.width || sourceInfo.height != destinationInfo.height) {
        AndroidBitmap_unlockPixels(env, destination);
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }

    const uint64_t frameSize = kFdHeaderBits + watermarkBytes.size() * 8;
    const uint64_t capacity = static_cast<uint64_t>(sourceInfo.width) * sourceInfo.height * 3;
    if (frameSize > capacity) {
        AndroidBitmap_unlockPixels(env, destination);
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }
    for (uint32_t y = 0; y < sourceInfo.height; ++y) {
        for (uint32_t x = 0; x < sourceInfo.width; ++x) {
            writeArgb(destinationInfo, destinationPixels, x, y, readArgb(sourceInfo, sourcePixels, x, y));
        }
    }
    const uint32_t shifts[] = {16, 8, 0};
    for (uint32_t index = 0; index < frameSize; ++index) {
        const uint32_t pixelIndex = index / 3;
        uint32_t color = readArgb(destinationInfo, destinationPixels,
                pixelIndex % destinationInfo.width, pixelIndex / destinationInfo.width);
        const uint32_t shift = shifts[index % 3];
        const uint32_t bit = frameBit(kLsbMagic, watermarkBytes, isImage == JNI_TRUE, index);
        color = (color & ~(1u << shift)) | (bit << shift);
        writeArgb(destinationInfo, destinationPixels,
                pixelIndex % destinationInfo.width, pixelIndex / destinationInfo.width, color);
    }
    AndroidBitmap_unlockPixels(env, destination);
    AndroidBitmap_unlockPixels(env, source);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_watermark_androidwm_utils_StringUtils_extractLsbWatermark(JNIEnv *env, jclass,
        jobject bitmap, jboolean isImage) {
    return extractLsbWatermark(env, bitmap, isImage == JNI_TRUE);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_watermark_androidwm_utils_StringUtils_embedFdWatermark(JNIEnv *env, jclass,
        jobject source, jobject destination,
        jstring watermark, jboolean isImage) {
    if (watermark == nullptr) {
        return JNI_FALSE;
    }
    const std::string watermarkBytes = stringFromJava(env, watermark);
    if (watermarkBytes.empty() || watermarkBytes.size() > (UINT32_MAX - kFdHeaderBits) / 8) {
        return JNI_FALSE;
    }

    AndroidBitmapInfo sourceInfo{};
    AndroidBitmapInfo destinationInfo{};
    void *sourcePixels = nullptr;
    void *destinationPixels = nullptr;
    if (!lockBitmap(env, source, &sourceInfo, &sourcePixels)) {
        return JNI_FALSE;
    }
    const bool destinationLocked = lockBitmap(env, destination, &destinationInfo, &destinationPixels);
    if (!destinationLocked) {
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }
    if ((sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888
            && sourceInfo.format != ANDROID_BITMAP_FORMAT_RGB_565)
            || destinationInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888
            || sourceInfo.width != destinationInfo.width || sourceInfo.height != destinationInfo.height) {
        AndroidBitmap_unlockPixels(env, destination);
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }

    const uint64_t frameSize = kFdHeaderBits + watermarkBytes.size() * 8;
    if (frameSize > fdCapacity(sourceInfo)) {
        AndroidBitmap_unlockPixels(env, destination);
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }
    for (uint32_t y = 0; y < sourceInfo.height; ++y) {
        for (uint32_t x = 0; x < sourceInfo.width; ++x) {
            writeArgb(destinationInfo, destinationPixels, x, y, readArgb(sourceInfo, sourcePixels, x, y));
        }
    }

    const uint32_t blockColumns = sourceInfo.width / 8;
    const uint32_t shifts[] = {16, 8, 0};
    for (uint32_t index = 0; index < frameSize; ++index) {
        const bool bit = frameBit(kFdMagic, watermarkBytes, isImage == JNI_TRUE, index);
        uint32_t encodedReplicas = 0;
        for (uint32_t replica = 0; replica < kFdBitRedundancy; ++replica) {
            const uint32_t physicalIndex = index * kFdBitRedundancy + replica;
            const uint32_t blockIndex = physicalIndex / 3;
            encodedReplicas += writeFdBit(destinationInfo, destinationPixels,
                    blockIndex % blockColumns, blockIndex / blockColumns,
                    shifts[physicalIndex % 3], bit);
        }
        if (encodedReplicas <= kFdBitRedundancy / 2) {
            AndroidBitmap_unlockPixels(env, destination);
            AndroidBitmap_unlockPixels(env, source);
            return JNI_FALSE;
        }
    }
    AndroidBitmap_unlockPixels(env, destination);
    AndroidBitmap_unlockPixels(env, source);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_watermark_androidwm_utils_StringUtils_extractFdWatermark(JNIEnv *env, jclass,
        jobject bitmap, jboolean isImage) {
    AndroidBitmapInfo info{};
    void *pixels = nullptr;
    if (!lockBitmap(env, bitmap, &info, &pixels)) {
        return nullptr;
    }
    if ((info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 && info.format != ANDROID_BITMAP_FORMAT_RGB_565)
            || fdCapacity(info) < kFdHeaderBits) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }

    uint32_t magic = 0;
    for (uint32_t index = 0; index < 32; ++index) {
        magic = (magic << 1) | readFdFrameBit(info, pixels, index);
    }
    if (magic != kFdMagic || readFdFrameBit(info, pixels, 32) != (isImage == JNI_TRUE)) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }
    uint32_t length = 0;
    for (uint32_t index = 40; index < kFdHeaderBits; ++index) {
        length = (length << 1) | readFdFrameBit(info, pixels, index);
    }
    const uint64_t frameSize = kFdHeaderBits + static_cast<uint64_t>(length) * 8;
    if (frameSize > fdCapacity(info)) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }
    std::string result(length, '\0');
    for (uint32_t index = 0; index < length * 8; ++index) {
        result[index / 8] = static_cast<char>((static_cast<unsigned char>(result[index / 8]) << 1)
                | readFdFrameBit(info, pixels, kFdHeaderBits + index));
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    return env->NewStringUTF(result.c_str());
}
