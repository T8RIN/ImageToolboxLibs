#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <android/log.h>
#include <android/bitmap.h>
#include <memory>
#include "libdjvu/ddjvuapi.h"


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


extern "C" JNIEXPORT jobject JNICALL
Java_com_t8rin_djvu_1coder_DJVUDecoder_decodeDJVUFile(
        JNIEnv *env,
        jobject thiz,
        jstring path,
        jint page,
        jint dpi) {
    const char *nativePath = env->GetStringUTFChars(path, 0);
    ddjvu_context_t *ctx = ddjvu_context_create("DjvuViewer");
    ddjvu_document_t *document = ddjvu_document_create_by_filename(ctx, nativePath, FALSE);

    if (!document) {
        ddjvu_context_release(ctx);
        return nullptr;
    }

    while (!ddjvu_document_decoding_done(document)) {
        const ddjvu_message_t *msg = ddjvu_message_wait(ctx);
        while ((msg = ddjvu_message_peek(ctx))) {
            if (msg->m_any.tag == DDJVU_ERROR) {
                ddjvu_message_pop(ctx);
                ddjvu_document_release(document);
                ddjvu_context_release(ctx);
                return nullptr;
            }
            ddjvu_message_pop(ctx);
        }
    }

    ddjvu_page_t *djvu_page = ddjvu_page_create_by_pageno(document, page);
    if (!djvu_page) {
        ddjvu_document_release(document);
        ddjvu_context_release(ctx);
        return nullptr;
    }

    while (!ddjvu_page_decoding_done(djvu_page)) {
        const ddjvu_message_t *msg = ddjvu_message_wait(ctx);
        while ((msg = ddjvu_message_peek(ctx))) {
            if (msg->m_any.tag == DDJVU_ERROR) {
                ddjvu_message_pop(ctx);
                ddjvu_page_release(djvu_page);
                ddjvu_document_release(document);
                ddjvu_context_release(ctx);
                return nullptr;
            }
            ddjvu_message_pop(ctx);
        }
    }

    ddjvu_rect_t rect;
    rect.x = 0;
    rect.y = 0;
    rect.w = ddjvu_page_get_width(djvu_page) * 100 / dpi;
    rect.h = ddjvu_page_get_height(djvu_page) * 100 / dpi;

    ddjvu_format_t *format = ddjvu_format_create(DDJVU_FORMAT_RGB24, 0, 0);
    ddjvu_format_set_row_order(format, 1);

    unsigned long rowsize = rect.w * 3;
    std::shared_ptr<unsigned char> rgbBuffer(
            static_cast<unsigned char *>(malloc(rect.w * rect.h * 3)), free);
    ddjvu_page_render(djvu_page, DDJVU_RENDER_COLOR, &rect, &rect, format, rowsize,
                      (char *) rgbBuffer.get());

    std::shared_ptr<unsigned char> imgData(
            static_cast<unsigned char *>(malloc(rect.w * rect.h * 4)), free);

    // Convert RGB to RGBA
    for (int i = 0, j = 0; i < rect.w * rect.h * 3; i += 3, j += 4) {
        imgData.get()[j] = rgbBuffer.get()[i];
        imgData.get()[j + 1] = rgbBuffer.get()[i + 1];
        imgData.get()[j + 2] = rgbBuffer.get()[i + 2];
        imgData.get()[j + 3] = 255; // Alpha channel
    }

    // Create Bitmap
    jobject bitmap = createBitmap(env, reinterpret_cast<int *>(imgData.get()), rect.w, rect.h,
                                  rect.w * 4 * sizeof(uint8_t));

    // Clean up
    ddjvu_format_release(format);
    ddjvu_page_release(djvu_page);
    ddjvu_document_release(document);
    ddjvu_context_release(ctx);
    env->ReleaseStringUTFChars(path, nativePath);

    return bitmap;
}