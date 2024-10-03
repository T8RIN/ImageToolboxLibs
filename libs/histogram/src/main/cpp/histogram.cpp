#include <jni.h>
#include <android/bitmap.h>
#include <vector>
#include <algorithm>
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