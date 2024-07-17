#include <android/bitmap.h>
#include "Util.h"
#include "OilFilter.h"
#include "TvFilter.h"
#include "SoftGlowFilter.h"
#include "SketchFilter.h"
#include "AverageSmoothFilter.h"
#include "GaussianBlurFilter.h"
#include "HDRFilter.h"
#include "SharpenFilter.h"
#include "MotionBlurFilter.h"
#include "GothamFilter.h"


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

jobject createBitmap(JNIEnv *env, int width, int height,
                     void (*onPixelsGet)(void *pixels, int width, int height, int stride)) {
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
    int lock;
    void *pixels;

    lock = AndroidBitmap_getInfo(env, resultBitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, resultBitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    onPixelsGet(pixels, info.width, info.height, info.stride);

    lock = AndroidBitmap_unlockPixels(env, resultBitmap);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

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
Java_com_t8rin_trickle_pipeline_EffectsPipelineImpl_softGlowImpl(JNIEnv *env, jobject object,
                                                                 jobject bitmap,
                                                                 jdouble blurSigma) {
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

    SoftGlowOptions options(blurSigma);
    int *result = PROC_IMAGE_WITH_OPTIONS(env, pixels, width, height, SoftGlowFilter, options);

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