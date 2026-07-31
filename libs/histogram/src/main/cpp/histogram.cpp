#include <jni.h>
#include <android/bitmap.h>
#include <algorithm>
#include <array>
#include <cmath>
#include <cstdint>
#include <vector>
#include "ColorUtils.h"

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_histogram_generator_HistogramGenerator_generate(JNIEnv *env, jobject,
                                                               jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return nullptr;

    int width = info.width;
    int height = info.height;

    std::vector<int> redHistogram(256, 0);
    std::vector<int> greenHistogram(256, 0);
    std::vector<int> blueHistogram(256, 0);
    std::vector<int> brightnessHistogram(101, 0);

    uint32_t *line = (uint32_t *) pixels;

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t pixel = line[x];
            uint8_t blue = (pixel >> 16) & 0xFF;
            uint8_t green = (pixel >> 8) & 0xFF;
            uint8_t red = pixel & 0xFF;

            redHistogram[red]++;
            greenHistogram[green]++;
            blueHistogram[blue]++;

            int luma = round(colorToLAB(RGB(red, green, blue)).l);

            brightnessHistogram[luma]++;
        }
        line = (uint32_t *) ((char *) line + info.stride);
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    // Convert to List<FloatArray>
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListInit = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayList = env->NewObject(arrayListClass, arrayListInit);
    jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    for (const auto &histogram: {redHistogram, greenHistogram, blueHistogram,
                                 brightnessHistogram}) {
        jintArray intArray = env->NewIntArray(histogram.size());
        env->SetIntArrayRegion(intArray, 0, histogram.size(),
                               reinterpret_cast<const jint *>(histogram.data()));
        env->CallBooleanMethod(arrayList, arrayListAdd, intArray);
        env->DeleteLocalRef(intArray);
    }

    return arrayList;
}

namespace {

constexpr int WAVEFORM = 0;

inline int scopeIntensity(uint32_t count, uint32_t maximum) {
    if (count == 0 || maximum == 0) return 0;
    const float normalized = std::log1p(static_cast<float>(count)) /
                             std::log1p(static_cast<float>(maximum));
    return static_cast<int>(std::round(normalized * 255.0f));
}

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_t8rin_histogram_generator_HistogramGenerator_generateScope(
        JNIEnv *env,
        jobject,
        jobject bitmap,
        jint type,
        jint requestedSize,
        jobject outputBuffer
) {
    const int size = std::clamp(static_cast<int>(requestedSize), 64, 512);
    const int binCount = size * size;
    const int outputSize = binCount * 4;
    auto *result = static_cast<uint8_t *>(env->GetDirectBufferAddress(outputBuffer));
    const jlong outputCapacity = env->GetDirectBufferCapacity(outputBuffer);
    if (result == nullptr || outputCapacity < outputSize) {
        return JNI_FALSE;
    }
    std::fill_n(result, outputSize, 0);

    AndroidBitmapInfo info;
    void *pixels = nullptr;
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0 ||
        info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        return JNI_FALSE;
    }

    if (type == WAVEFORM) {
        std::vector<uint32_t> redBins(binCount, 0);
        std::vector<uint32_t> greenBins(binCount, 0);
        std::vector<uint32_t> blueBins(binCount, 0);
        std::vector<int> scopeColumns(info.width);
        for (int x = 0; x < static_cast<int>(info.width); ++x) {
            scopeColumns[x] = std::min(
                    size - 1,
                    static_cast<int>(
                            static_cast<int64_t>(x) * size / info.width
                    )
            );
        }
        std::array<int, 256> scopeRows{};
        for (int value = 0; value < 256; ++value) {
            scopeRows[value] =
                    (size - 1 - value * (size - 1) / 255) * size;
        }
        uint32_t redMaximum = 0;
        uint32_t greenMaximum = 0;
        uint32_t blueMaximum = 0;

        for (int y = 0; y < static_cast<int>(info.height); ++y) {
            const auto *line = reinterpret_cast<const uint32_t *>(
                    static_cast<const char *>(pixels) + y * info.stride
            );
            for (int x = 0; x < static_cast<int>(info.width); ++x) {
                const uint32_t pixel = line[x];
                const int red = pixel & 0xFF;
                const int green = (pixel >> 8) & 0xFF;
                const int blue = (pixel >> 16) & 0xFF;
                const int scopeX = scopeColumns[x];
                const int redIndex = scopeRows[red] + scopeX;
                const int greenIndex = scopeRows[green] + scopeX;
                const int blueIndex = scopeRows[blue] + scopeX;
                redMaximum = std::max(redMaximum, ++redBins[redIndex]);
                greenMaximum = std::max(greenMaximum, ++greenBins[greenIndex]);
                blueMaximum = std::max(blueMaximum, ++blueBins[blueIndex]);
            }
        }

        for (int index = 0; index < binCount; ++index) {
            const int red = scopeIntensity(redBins[index], redMaximum);
            const int green = scopeIntensity(greenBins[index], greenMaximum);
            const int blue = scopeIntensity(blueBins[index], blueMaximum);
            const int resultIndex = index * 4;
            result[resultIndex] = static_cast<uint8_t>(red);
            result[resultIndex + 1] = static_cast<uint8_t>(green);
            result[resultIndex + 2] = static_cast<uint8_t>(blue);
            result[resultIndex + 3] = static_cast<uint8_t>(std::max({red, green, blue}));
        }
    } else {
        std::vector<uint32_t> counts(binCount, 0);
        std::vector<uint64_t> redSums(binCount, 0);
        std::vector<uint64_t> greenSums(binCount, 0);
        std::vector<uint64_t> blueSums(binCount, 0);
        uint32_t maximum = 0;

        for (int y = 0; y < static_cast<int>(info.height); ++y) {
            const auto *line = reinterpret_cast<const uint32_t *>(
                    static_cast<const char *>(pixels) + y * info.stride
            );
            for (int x = 0; x < static_cast<int>(info.width); ++x) {
                const uint32_t pixel = line[x];
                const int red = pixel & 0xFF;
                const int green = (pixel >> 8) & 0xFF;
                const int blue = (pixel >> 16) & 0xFF;
                const float redValue = red / 255.0f;
                const float greenValue = green / 255.0f;
                const float blueValue = blue / 255.0f;
                const float luma = 0.2126f * redValue +
                                   0.7152f * greenValue +
                                   0.0722f * blueValue;
                const float cb = std::clamp(
                        (blueValue - luma) / 1.8556f + 0.5f,
                        0.0f,
                        1.0f
                );
                const float cr = std::clamp(
                        (redValue - luma) / 1.5748f + 0.5f,
                        0.0f,
                        1.0f
                );
                const int scopeX = static_cast<int>(std::round(cb * (size - 1)));
                const int scopeY = static_cast<int>(std::round((1.0f - cr) * (size - 1)));
                const int index = scopeY * size + scopeX;
                maximum = std::max(maximum, ++counts[index]);
                redSums[index] += red;
                greenSums[index] += green;
                blueSums[index] += blue;
            }
        }

        for (int index = 0; index < binCount; ++index) {
            const uint32_t count = counts[index];
            if (count == 0) continue;
            const int intensity = scopeIntensity(count, maximum);
            const int red = static_cast<int>(redSums[index] / count);
            const int green = static_cast<int>(greenSums[index] / count);
            const int blue = static_cast<int>(blueSums[index] / count);
            const int resultIndex = index * 4;
            result[resultIndex] = static_cast<uint8_t>(red);
            result[resultIndex + 1] = static_cast<uint8_t>(green);
            result[resultIndex + 2] = static_cast<uint8_t>(blue);
            result[resultIndex + 3] = static_cast<uint8_t>(intensity);
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return JNI_TRUE;
}
