#include <jni.h>
#include <android/bitmap.h>
#include <vector>
#include <algorithm>

float SRGBToLinear(float v) {
    if (v <= 0.045) {
        return v / 12.92;
    } else {
        return pow((v + 0.055) / 1.055, 2.4);
    }
}

double luminance(float red, float green, float blue) {
    float r = SRGBToLinear(red / 255.0f);
    float g = SRGBToLinear(green / 255.0f);
    float b = SRGBToLinear(blue / 255.0f);

    return 0.2125 * r + 0.7154f * g + 0.0721 * b;
}

double perceptualLuma(double luma) {
    if (luma <= (216.0 / 24389)) {
        return luma * (24389.0 / 27);
    } else {
        return cbrt(luma) * 116 - 16;
    }
}

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
    std::vector<int> brightnessHistogram(100, 0);

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

            int luma = round(perceptualLuma(luminance(red, green, blue)));

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
        jfloatArray floatArray = env->NewFloatArray(histogram.size());
        env->SetFloatArrayRegion(floatArray, 0, histogram.size(),
                                 reinterpret_cast<const jfloat *>(histogram.data()));
        env->CallBooleanMethod(arrayList, arrayListAdd, floatArray);
        env->DeleteLocalRef(floatArray);
    }

    return arrayList;
}