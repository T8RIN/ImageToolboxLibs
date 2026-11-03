#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <android/log.h>
#include <android/bitmap.h>
#include <memory>
#include <algorithm>
#include <chrono>
#include <thread>
#include "../../../../EncodingInterruption.h"
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

    if (!resultBitmap) return nullptr;
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


namespace {
    bool waitForDecode(ddjvu_context_t *context, ddjvu_job_t *job,
                       const EncodingInterruption &interruption) {
        while (!ddjvu_job_done(job)) {
            if (interruption.requested()) {
                ddjvu_job_stop(job);
                interruption.throwIfRequested();
                return false;
            }
            while (const ddjvu_message_t *message = ddjvu_message_peek(context)) {
                const bool error = message->m_any.tag == DDJVU_ERROR;
                ddjvu_message_pop(context);
                if (error) return false;
            }
            if (!ddjvu_job_done(job)) std::this_thread::sleep_for(std::chrono::milliseconds(4));
        }
        return !interruption.throwIfRequested() && !ddjvu_job_error(job);
    }
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_t8rin_djvu_1coder_DJVUDecoder_decodeDJVUFile(
        JNIEnv *env,
        jobject thiz,
        jstring path,
        jint page,
        jint dpi) {
    const EncodingInterruption interruption(env);
    if (interruption.throwIfRequested() || !path || dpi <= 0) return nullptr;
    const std::unique_ptr<ddjvu_context_t, decltype(&ddjvu_context_release)> context(
            ddjvu_context_create("DjvuViewer"), ddjvu_context_release);
    if (!context) return nullptr;

    const char *nativePath = env->GetStringUTFChars(path, nullptr);
    if (!nativePath) return nullptr;
    const auto releaseDocument = [](ddjvu_document_t *value) { ddjvu_document_release(value); };
    const std::unique_ptr<ddjvu_document_t, decltype(releaseDocument)> document(
            ddjvu_document_create_by_filename(context.get(), nativePath, FALSE), releaseDocument);
    env->ReleaseStringUTFChars(path, nativePath);
    if (!document || !waitForDecode(context.get(), ddjvu_document_job(document.get()), interruption)) {
        return nullptr;
    }

    const auto releasePage = [](ddjvu_page_t *value) { ddjvu_page_release(value); };
    const std::unique_ptr<ddjvu_page_t, decltype(releasePage)> decodedPage(
            ddjvu_page_create_by_pageno(document.get(), page), releasePage);
    if (!decodedPage || !waitForDecode(context.get(), ddjvu_page_job(decodedPage.get()), interruption)) {
        return nullptr;
    }

    const auto width = static_cast<uint64_t>(ddjvu_page_get_width(decodedPage.get())) * 100 / dpi;
    const auto height = static_cast<uint64_t>(ddjvu_page_get_height(decodedPage.get())) * 100 / dpi;
    if (width == 0 || height == 0 || width > INT32_MAX / 4 || height > INT32_MAX / width || height > SIZE_MAX / 4 / width) {
        return nullptr;
    }
    const ddjvu_rect_t rect{0, 0, static_cast<unsigned int>(width), static_cast<unsigned int>(height)};
    const std::unique_ptr<ddjvu_format_t, decltype(&ddjvu_format_release)> format(
            ddjvu_format_create(DDJVU_FORMAT_RGB24, 0, nullptr), ddjvu_format_release);
    if (!format) return nullptr;
    ddjvu_format_set_row_order(format.get(), 1);
    ddjvu_format_set_y_direction(format.get(), 1);

    constexpr unsigned int rowsPerBand = 128;
    const size_t rowSize = width * 3;
    std::unique_ptr<unsigned char, decltype(&free)> rgb(
            static_cast<unsigned char *>(malloc(rowSize * std::min(rowsPerBand, rect.h))), free);
    std::unique_ptr<unsigned char, decltype(&free)> rgba(
            static_cast<unsigned char *>(malloc(width * height * 4)), free);
    if (!rgb || !rgba) return nullptr;

    for (unsigned int y = 0; y < rect.h; y += rowsPerBand) {
        if (interruption.throwIfRequested()) return nullptr;
        const ddjvu_rect_t band{0, static_cast<int>(y), rect.w, std::min(rowsPerBand, rect.h - y)};
        if (!ddjvu_page_render(decodedPage.get(), DDJVU_RENDER_COLOR, &rect, &band, format.get(),
                               rowSize, reinterpret_cast<char *>(rgb.get()))) {
            return nullptr;
        }
        const auto *source = rgb.get();
        auto *target = rgba.get() + y * width * 4;
        for (size_t i = 0; i < width * band.h; ++i) {
            target[0] = source[0];
            target[1] = source[1];
            target[2] = source[2];
            target[3] = 255;
            source += 3;
            target += 4;
        }
    }

    if (interruption.throwIfRequested()) return nullptr;
    return createBitmap(env, reinterpret_cast<int *>(rgba.get()), width, height, width * 4);
}