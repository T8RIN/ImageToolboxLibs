#include <jni.h>
#include <cstddef>
#include <android/bitmap.h>
#include <cstdlib>
#include <cstring>
#include <cmath>
#include "lowpoly.h"
#include "cairo-surface-inline.h"
#include "_log.h"
#include <string>

#define CLAMP(value, min, max) ((value) < (min) ? (min) : ((value) > (max) ? (max) : (value)))

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_t8rin_trickle_pipeline_LowPolyPipelineImpl_getTriangles(JNIEnv *env, jclass type,
                                                        jobject input,
                                                        jint threshold,
                                                        jfloat alpha_count, jboolean lowPoly) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, input, &info) < 0) {
        return nullptr;
    }

    void *allocatedPixels;
    if (AndroidBitmap_lockPixels(env, input, &allocatedPixels) < 0) {
        return nullptr;
    }

    int width = (int) info.width;
    int height = (int) info.height;

    jint *pixelArray = (jint *) malloc(sizeof(jint) * width * height);
    memcpy(pixelArray, allocatedPixels, sizeof(jint) * width * height);

    jintArray pixels_ = (*env).NewIntArray(width * height);
    (*env).SetIntArrayRegion(pixels_, 0, width * height, pixelArray);

    jint *pixels = (*env).GetIntArrayElements(pixels_, nullptr);
    jint size = (*env).GetArrayLength(pixels_);

    jintArray tmpArray_ = (*env).NewIntArray(size);
    jint *tmpArray = (*env).GetIntArrayElements(tmpArray_, nullptr);

    int tmpSize = 0;

    get_triangles(pixels, size, width, height, threshold, alpha_count, tmpArray, &tmpSize, lowPoly);

    (*env).ReleaseIntArrayElements(pixels_, pixels, 0);

    jintArray result = (*env).NewIntArray(tmpSize);

    (*env).SetIntArrayRegion(result, 0, tmpSize, tmpArray);

    (*env).ReleaseIntArrayElements(tmpArray_, tmpArray, 0);
    return result;
}

inline uint32_t abgr_to_rgba(uint32_t abgr) {
    uint32_t alpha = (abgr >> 24) & 0xFF;
    uint32_t blue = (abgr >> 16) & 0xFF;
    uint32_t green = (abgr >> 8) & 0xFF;
    uint32_t red = abgr & 0xFF;
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_pipeline_LowPolyPipelineImpl_generate(JNIEnv *env, jclass clazz,
                                                             jobject input,
                                                    jint threshold,
                                                    jfloat alphaOrPointCount, jboolean lowPoly,
                                                    jboolean fill) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, input, &info) < 0) {
        return nullptr;
    }

    void *pixels;
    if (AndroidBitmap_lockPixels(env, input, &pixels) < 0) {
        return nullptr;
    }

    int width = (int) info.width;
    int height = (int) info.height;
    int stride = (int) info.stride;

    jclass bitmapClass = (*env).FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = (*env).GetStaticMethodID(bitmapClass, "createBitmap",
                                                            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jclass bitmapConfigClass = (*env).FindClass("android/graphics/Bitmap$Config");
    jfieldID argb8888Field = (*env).GetStaticFieldID(bitmapConfigClass, "ARGB_8888",
                                                     "Landroid/graphics/Bitmap$Config;");
    jobject argb8888 = (*env).GetStaticObjectField(bitmapConfigClass, argb8888Field);

    jobject newImage = (*env).CallStaticObjectMethod(bitmapClass, createBitmapMethod, width,
                                                     height, argb8888);
    if (newImage == nullptr) {
        AndroidBitmap_unlockPixels(env, input);
        return nullptr;
    }

    void *newPixels;
    if (AndroidBitmap_lockPixels(env, newImage, &newPixels) < 0) {
        AndroidBitmap_unlockPixels(env, input);
        return nullptr;
    }

    if (fill) {
        memcpy(newPixels, pixels, stride * height);
    } else {
        memset(newPixels, 0, stride * height);
    }

    int *triangles = (int *) (malloc(sizeof(int) * width * height));
    int tmpSize = 0;

    get_triangles(reinterpret_cast<const int *>(pixels), width * height,
                  width, height, threshold, alphaOrPointCount, triangles,
                  &tmpSize, lowPoly);

    cairo_surface_t *surface = cairo_image_surface_create_for_data(
            reinterpret_cast<unsigned char *>(newPixels),
            CAIRO_FORMAT_ARGB32,
            width,
            height,
            stride
    );

    cairo_t *cr = cairo_create(surface);


    cairo_scale(cr, width, height);

    cairo_set_antialias(cr, CAIRO_ANTIALIAS_BEST);

    if (lowPoly) {
        for (int i = 0; i + 5 < width * height; i += 6) {
            int x1 = triangles[i];
            int y1 = triangles[i + 1];
            int x2 = triangles[i + 2];
            int y2 = triangles[i + 3];
            int x3 = triangles[i + 4];
            int y3 = triangles[i + 5];

            uint32_t rowsOffset = CLAMP((y1 + y2 + y3) / 3, 0, height - 1) * width;
            uint32_t offset = CLAMP((x1 + x2 + x3) / 3, 0, width - 1) + rowsOffset;
            auto *color = reinterpret_cast<uint8_t *>(pixels);

            auto px = offset * 4;

            auto red = color[px];
            auto green = color[px + 1];
            auto blue = color[px + 2];
            auto alpha = color[px + 3];

            cairo_set_source_rgba(cr, blue / 255.0, green / 255.0, red / 255.0, alpha / 255.0);

            cairo_move_to(cr, (double) x1 / (double) width, (double) y1 / (double) height);
            cairo_line_to(cr, (double) x2 / (double) width, (double) y2 / (double) height);
            cairo_line_to(cr, (double) x3 / (double) width, (double) y3 / (double) height);
            cairo_close_path(cr);


            if (fill) {
                cairo_fill(cr);
            } else {
                cairo_set_line_width(cr, 0.0005);
                cairo_stroke_preserve(cr);
                cairo_set_source_rgba(cr, blue / 255.0, green / 255.0, red / 255.0, alpha / 255.0);
                cairo_set_line_width(cr, 0.0005);
                cairo_stroke(cr);
            }
        }
    } else {
        for (int i = 0; i + 1 < width * height; i += 2) {
            int x1 = triangles[i];
            int y1 = triangles[i + 1];
            uint32_t rowsOffset = CLAMP(y1, 0, height - 1) * width;
            uint32_t offset = CLAMP(x1, 0, width - 1) + rowsOffset;
            auto *color = reinterpret_cast<uint8_t *>(pixels);

            auto px = offset * 4;

            auto red = color[px];
            auto green = color[px + 1];
            auto blue = color[px + 2];
            auto alpha = color[px + 3];

            cairo_set_source_rgba(cr, blue / 255.0, green / 255.0, red / 255.0, alpha / 255.0);

            cairo_arc(cr, (double) x1 / (double) width, (double) y1 / (double) height, 1.0f, 0,
                      2 * M_PI);

            if (fill) {
                cairo_fill(cr);
            } else {
                cairo_set_line_width(cr, 0.0005);
                cairo_stroke_preserve(cr);
                cairo_set_source_rgba(cr, blue / 255.0, green / 255.0, red / 255.0, alpha / 255.0);
                cairo_set_line_width(cr, 0.0005);
                cairo_stroke(cr);
            }
        }
    }

    free(triangles);
    cairo_destroy(cr);
    cairo_surface_destroy(surface);

    AndroidBitmap_unlockPixels(env, input);
    AndroidBitmap_unlockPixels(env, newImage);

    return newImage;
}