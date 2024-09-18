#include <jni.h>
#include <android/bitmap.h>
#include <vector>
#include <algorithm>

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_histogram_HistogramGenerator_generate(JNIEnv *env, jobject, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return nullptr;

    int width = info.width;
    int height = info.height;

    std::vector<int> redHistogram(256, 0);
    std::vector<int> greenHistogram(256, 0);
    std::vector<int> blueHistogram(256, 0);
    std::vector<int> brightnessHistogram(256, 0); // Для яркости

    uint32_t *line = (uint32_t *) pixels;

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t pixel = line[x];
            uint8_t red = (pixel >> 16) & 0xFF;
            uint8_t green = (pixel >> 8) & 0xFF;
            uint8_t blue = pixel & 0xFF;

            redHistogram[red]++;
            greenHistogram[green]++;
            blueHistogram[blue]++;

            // Яркость вычисляется как среднее значение по каналам RGB
            uint8_t brightness = static_cast<uint8_t>((red + green + blue) / 3);
            brightnessHistogram[brightness]++;
        }
        line = (uint32_t *) ((char *) line + info.stride);
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    // Convert to List<FloatArray>
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListInit = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayList = env->NewObject(arrayListClass, arrayListInit);
    jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    for (const auto &histogram: {blueHistogram, greenHistogram, redHistogram,
                                 brightnessHistogram}) {
        jfloatArray floatArray = env->NewFloatArray(256);
        env->SetFloatArrayRegion(floatArray, 0, 256,
                                 reinterpret_cast<const jfloat *>(histogram.data()));
        env->CallBooleanMethod(arrayList, arrayListAdd, floatArray);
        env->DeleteLocalRef(floatArray);
    }

    return arrayList;
}