#include <android/bitmap.h>
#include <__functional/function.h>
#include "Util.h"
#include "OilFilter.h"
#include "TvFilter.h"
#include "SketchFilter.h"
#include "AverageSmoothFilter.h"
#include "GaussianBlurFilter.h"
#include "HDRFilter.h"
#include "SharpenFilter.h"
#include "MotionBlurFilter.h"
#include "GothamFilter.h"
#include "ColorUtils.h"


jobject createBitmap(JNIEnv *env, int *targetPixels, int width, int height, int stride,
                     bool autoDelete = false) {
    jclass bitmapConfig = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID rgba8888FieldID = env->GetStaticFieldID(bitmapConfig, "ARGB_8888",
                                                     "Landroid/graphics/Bitmap$Config;");
    jobject rgba8888Obj = env->GetStaticObjectField(bitmapConfig, rgba8888FieldID);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");

    jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject resultBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethodID,
                                                       static_cast<jint>(width),
                                                       static_cast<jint>(height), rgba8888Obj);

    AndroidBitmapInfo info;
    void *pixels;
    int lock;

    lock = AndroidBitmap_getInfo(env, resultBitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, resultBitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    memcpy(pixels, targetPixels, stride * height);

    lock = AndroidBitmap_unlockPixels(env, resultBitmap);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    if (autoDelete) {
        delete[] targetPixels;
    }

    return resultBitmap;
}

jobject createBitmap(JNIEnv *env, int width, int height) {
    jclass bitmapConfig = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID rgba8888FieldID = env->GetStaticFieldID(bitmapConfig, "ARGB_8888",
                                                     "Landroid/graphics/Bitmap$Config;");
    jobject rgba8888Obj = env->GetStaticObjectField(bitmapConfig, rgba8888FieldID);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");

    jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject resultBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethodID,
                                                       static_cast<jint>(width),
                                                       static_cast<jint>(height), rgba8888Obj);

    return resultBitmap;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_pipeline_EffectsPipelineImpl_oilImpl(JNIEnv *env,
                                                            jobject object, jobject bitmap,
                                                            jint oilRange) {
    AndroidBitmapInfo info;
    void *pixels;
    int lock;

    lock = AndroidBitmap_getInfo(env, bitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    uint32_t width = info.width;
    uint32_t height = info.height;
    uint32_t stride = info.stride;

    OilFilterOptions options(oilRange);

    int *result = PROC_IMAGE_WITH_OPTIONS(env, pixels, width, height, OilFilter, options);
    return createBitmap(env, result, width, height, stride, true);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_pipeline_EffectsPipelineImpl_tvImpl(JNIEnv *env, jobject object,
                                                           jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;
    int lock;

    lock = AndroidBitmap_getInfo(env, bitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    uint32_t width = info.width;
    uint32_t height = info.height;
    uint32_t stride = info.stride;

    int *result = PROC_IMAGE_WITHOUT_OPTIONS(env, pixels, width, height, TvFilter);
    return createBitmap(env, result, width, height, stride, true);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_pipeline_EffectsPipelineImpl_hdrImpl(JNIEnv *env, jobject object,
                                                            jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;
    int lock;

    lock = AndroidBitmap_getInfo(env, bitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    uint32_t width = info.width;
    uint32_t height = info.height;
    uint32_t stride = info.stride;

    int *result = PROC_IMAGE_WITHOUT_OPTIONS(env, pixels, width, height, HDRFilter);
    return createBitmap(env, result, width, height, stride, true);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_pipeline_EffectsPipelineImpl_sketchImpl(JNIEnv *env, jobject object,
                                                               jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;
    int lock;

    lock = AndroidBitmap_getInfo(env, bitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    uint32_t width = info.width;
    uint32_t height = info.height;
    uint32_t stride = info.stride;

    int *result = PROC_IMAGE_WITHOUT_OPTIONS(env, pixels, width, height, SketchFilter);

    return createBitmap(env, result, width, height, stride, true);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_pipeline_EffectsPipelineImpl_gothamImpl(JNIEnv *env, jobject object,
                                                               jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;
    int lock;

    lock = AndroidBitmap_getInfo(env, bitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    uint32_t width = info.width;
    uint32_t height = info.height;
    uint32_t stride = info.stride;

    int *result = PROC_IMAGE_WITHOUT_OPTIONS(env, pixels, width, height, GothamFilter);
    return createBitmap(env, result, width, height, stride, true);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_pipeline_EffectsPipelineImpl_colorPosterizeImpl(JNIEnv *env, jobject thiz,
                                                                       jobject input,
                                                                       jintArray colors) {
    AndroidBitmapInfo sourceInfo;
    void *sourcePixels;
    if (AndroidBitmap_getInfo(env, input, &sourceInfo) < 0) {
        return nullptr;
    }
    if (AndroidBitmap_lockPixels(env, input, &sourcePixels) < 0) {
        return nullptr;
    }

    uint32_t width = sourceInfo.width;
    uint32_t height = sourceInfo.height;
    uint32_t stride = sourceInfo.stride;

    jsize len = (*env).GetArrayLength(colors);
    jint *body = (*env).GetIntArrayElements(colors, nullptr);
    RGB rgbColors[len];
    for (int i = 0; i < len; i++) {
        rgbColors[i] = ColorToRGB(body[i]);
    }

    jobject resultBitmap = createBitmap(env, width, height);

    AndroidBitmapInfo resultInfo;
    void *resultPixels;
    if (AndroidBitmap_getInfo(env, resultBitmap, &resultInfo) < 0) {
        AndroidBitmap_unlockPixels(env, resultBitmap);
        return nullptr;
    }
    if (AndroidBitmap_lockPixels(env, resultBitmap, &resultPixels) < 0) {
        AndroidBitmap_unlockPixels(env, resultBitmap);
        return nullptr;
    }

    for (int y = 0; y < height; ++y) {
        auto src = reinterpret_cast<uint8_t *>(reinterpret_cast<uint8_t *>(sourcePixels) +
                                               y * stride);

        auto dst = reinterpret_cast<uint8_t *>(reinterpret_cast<uint8_t *>(resultPixels) +
                                               y * stride);
        int x = 0;

        for (; x < width; ++x) {
            int r = src[0];
            int g = src[1];
            int b = src[2];
            int srcAlpha = src[3];

            if (srcAlpha > 0) {
                double minDiff = 1000.;
                RGB rgb;

                for (auto paletteColor: rgbColors) {
                    double newDiff = colorDiff(paletteColor, RGB(r, g, b)) / 255.0;
                    if (newDiff < minDiff) {
                        minDiff = newDiff;
                        rgb = paletteColor;
                    }
                }

                dst[0] = rgb.r;
                dst[1] = rgb.g;
                dst[2] = rgb.b;
            }

            dst[3] = srcAlpha;

            dst += 4;
            src += 4;
        }
    }

    if (AndroidBitmap_unlockPixels(env, input) < 0) {
        AndroidBitmap_unlockPixels(env, resultBitmap);
        return nullptr;
    }

    if (AndroidBitmap_unlockPixels(env, resultBitmap) < 0) {
        AndroidBitmap_unlockPixels(env, input);
        return nullptr;
    }

    return resultBitmap;
}