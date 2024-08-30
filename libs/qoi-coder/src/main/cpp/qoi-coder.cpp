#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <android/log.h>
#include <android/bitmap.h>

#include "qoi.h"

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

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_t8rin_qoi_1coder_QOIEncoder_encodeQOIBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        return nullptr;
    }

    void *pixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        return nullptr;
    }

    uint32_t width = (int) info.width;
    uint32_t height = (int) info.height;
    int stride = (int) info.stride;
    int encoded_qoi_size;
    qoi_desc description = (qoi_desc) {
            .width = width,
            .height = height,
            .channels = 4,
            .colorspace = QOI_SRGB
    };

    void *bytes = qoi_encode(pixels, &description, &encoded_qoi_size);

    jbyteArray ret = env->NewByteArray(encoded_qoi_size);
    env->SetByteArrayRegion(ret, 0, encoded_qoi_size, reinterpret_cast<jbyte *>(bytes));

    return ret;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_qoi_1coder_QOIDecoder_decodeJP2ByteArray(JNIEnv *env, jobject thiz,
                                                        jbyteArray data) {
    jbyte *dataArray = (jbyte *) env->GetByteArrayElements(data, NULL);
    jint size = env->GetArrayLength(data);
    qoi_desc info = qoi_desc();
    auto pixels = qoi_decode(dataArray, size, &info, 0);
    int stride = 4 * ((info.width * info.channels + 3) / 4);

    env->ReleaseByteArrayElements(data, dataArray, 0);

    return createBitmap(env, reinterpret_cast<int *>(pixels), info.width, info.height, stride,
                        true);
}