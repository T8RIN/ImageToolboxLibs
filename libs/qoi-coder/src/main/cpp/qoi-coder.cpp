#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <memory>
#include <android/log.h>
#include <android/bitmap.h>

#include "qoi.h"
#include "../../../../EncodingInterruption.h"

jobject createBitmap(JNIEnv *env, const uint8_t *source, int width, int height) {
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

    if (!resultBitmap) return nullptr;

    AndroidBitmapInfo info;
    void *pixels;
    int lock;

    lock = AndroidBitmap_getInfo(env, resultBitmap, &info);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    lock = AndroidBitmap_lockPixels(env, resultBitmap, &pixels);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    const size_t rowSize = static_cast<size_t>(width) * 4;
    auto *destination = static_cast<uint8_t *>(pixels);
    for (int row = 0; row < height; ++row) {
        if (EncodingInterruption::currentRequested()) break;
        memcpy(destination + row * static_cast<size_t>(info.stride), source + row * rowSize, rowSize);
    }

    lock = AndroidBitmap_unlockPixels(env, resultBitmap);
    if (lock != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;

    return resultBitmap;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_t8rin_qoi_1coder_QOIEncoder_encodeQOIBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    EncodingInterruption interruption(env);
    if (env->ExceptionCheck() || interruption.throwIfRequested()) return nullptr;

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

    void *bytes = qoi_encode_interruptible(pixels, &description, &encoded_qoi_size,
            [](void *context) {
                return static_cast<EncodingInterruption *>(context)->requested();
            }, &interruption);
    AndroidBitmap_unlockPixels(env, bitmap);

    if (interruption.throwIfRequested() || !bytes) {
        free(bytes);
        return nullptr;
    }
    jbyteArray ret = env->NewByteArray(encoded_qoi_size);
    if (ret) {
        env->SetByteArrayRegion(ret, 0, encoded_qoi_size, reinterpret_cast<jbyte *>(bytes));
    }
    free(bytes);

    return ret;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_qoi_1coder_QOIDecoder_decodeQOIByteArray(JNIEnv *env, jobject thiz,
                                                        jbyteArray data) {
    EncodingInterruption interruption(env);
    if (interruption.throwIfRequested()) return nullptr;
    if (!data) return nullptr;
    jint size = env->GetArrayLength(data);
    jbyte *dataArray = env->GetByteArrayElements(data, nullptr);
    if (!dataArray) return nullptr;

    qoi_desc info{};
    std::unique_ptr<void, decltype(&free)> pixels(qoi_decode_interruptible(
            dataArray, size, &info, 4,
            [](void *context) {
                return static_cast<EncodingInterruption *>(context)->requested();
            }, &interruption), free);
    env->ReleaseByteArrayElements(data, dataArray, JNI_ABORT);
    if (interruption.throwIfRequested() || !pixels) return nullptr;

    jobject bitmap = createBitmap(env, static_cast<const uint8_t *>(pixels.get()), info.width, info.height);
    return interruption.throwIfRequested() ? nullptr : bitmap;
}