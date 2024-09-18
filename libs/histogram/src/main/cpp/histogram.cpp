#include <jni.h>
#include <android/bitmap.h>
#include <vector>
#include <algorithm>

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_histogram_Histogram_generateHistogram(JNIEnv *env, jobject, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return nullptr;

    int width = info.width;
    int height = info.height;

    std::vector<int> redHistogram(256, 0);
    std::vector<int> greenHistogram(256, 0);
    std::vector<int> blueHistogram(256, 0);

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
        }
        line = (uint32_t *) ((char *) line + info.stride);
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    // Normalize histograms to range [0, 1]
    int totalPixels = 256;
    std::vector<float> redNorm(256), greenNorm(256), blueNorm(256);

    std::transform(redHistogram.begin(), redHistogram.end(), redNorm.begin(),
                   [totalPixels](int val) { return static_cast<float>(val) / totalPixels; });
    std::transform(greenHistogram.begin(), greenHistogram.end(), greenNorm.begin(),
                   [totalPixels](int val) { return static_cast<float>(val) / totalPixels; });
    std::transform(blueHistogram.begin(), blueHistogram.end(), blueNorm.begin(),
                   [totalPixels](int val) { return static_cast<float>(val) / totalPixels; });

    // Convert to List<FloatArray>
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListInit = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayList = env->NewObject(arrayListClass, arrayListInit);
    jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jclass floatArrayClass = env->FindClass("[F");

    for (const auto &histogram: {redNorm, greenNorm, blueNorm}) {
        jfloatArray floatArray = env->NewFloatArray(256);
        env->SetFloatArrayRegion(floatArray, 0, 256, histogram.data());
        env->CallBooleanMethod(arrayList, arrayListAdd, floatArray);
        env->DeleteLocalRef(floatArray);
    }

    return arrayList;
}