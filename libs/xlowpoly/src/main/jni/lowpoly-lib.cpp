#include <jni.h>
#include <cstddef>
#include <android/bitmap.h>
#include <cstdlib>
#include <cstring>
#include "lowpoly.h"

#define CLAMP(value, min, max) ((value) < (min) ? (min) : ((value) > (max) ? (max) : (value)))

extern "C"
JNIEXPORT jintArray JNICALL
Java_io_github_xyzxqs_xlowpoly_LowPoly_getTriangles(JNIEnv *env, jclass type,
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
Java_io_github_xyzxqs_xlowpoly_LowPoly_generate(JNIEnv *env, jclass clazz, jobject input,
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

    int *triangles = (int *) (malloc(sizeof(int) * width * height));
    int tmpSize = 0;

    get_triangles(reinterpret_cast<const int *>(pixels), width * height,
                  width, height, threshold, alphaOrPointCount, triangles,
                  &tmpSize, lowPoly);

    jclass canvasClass = (*env).FindClass("android/graphics/Canvas");
    jmethodID canvasConstructor = (*env).GetMethodID(canvasClass, "<init>",
                                                      "(Landroid/graphics/Bitmap;)V");
    jobject canvas = (*env).NewObject(canvasClass, canvasConstructor, newImage);

    jclass paintClass = (*env).FindClass("android/graphics/Paint");
    jmethodID paintConstructor = (*env).GetMethodID(paintClass, "<init>", "()V");
    jobject paint = (*env).NewObject(paintClass, paintConstructor);

    jmethodID setAntiAliasMethod = (*env).GetMethodID(paintClass, "setAntiAlias", "(Z)V");
    (*env).CallVoidMethod(paint, setAntiAliasMethod, JNI_FALSE);

    jmethodID setStyleMethod = (*env).GetMethodID(paintClass, "setStyle",
                                                   "(Landroid/graphics/Paint$Style;)V");
    jclass paintStyleClass = (*env).FindClass("android/graphics/Paint$Style");
    jobject style = fill ? (*env).GetStaticObjectField(paintStyleClass,
                                                       (*env).GetStaticFieldID(
                                                                                 paintStyleClass,
                                                                                 "FILL",
                                                                                 "Landroid/graphics/Paint$Style;"))
                         : (*env).GetStaticObjectField(paintStyleClass,
                                                       (*env).GetStaticFieldID(
                                                                                 paintStyleClass,
                                                                                 "STROKE",
                                                                                 "Landroid/graphics/Paint$Style;"));
    (*env).CallVoidMethod(paint, setStyleMethod, style);

    jmethodID setColorMethod = (*env).GetMethodID(paintClass, "setColor", "(I)V");
    jmethodID drawPathMethod = (*env).GetMethodID(canvasClass, "drawPath",
                                                   "(Landroid/graphics/Path;Landroid/graphics/Paint;)V");
    jmethodID drawCircleMethod = (*env).GetMethodID(canvasClass, "drawCircle",
                                                     "(FFFLandroid/graphics/Paint;)V");

    jclass pathClass = (*env).FindClass("android/graphics/Path");
    jmethodID pathConstructor = (*env).GetMethodID(pathClass, "<init>", "()V");
    jobject path = (*env).NewObject(pathClass, pathConstructor);
    jmethodID moveToMethod = (*env).GetMethodID(pathClass, "moveTo", "(FF)V");
    jmethodID lineToMethod = (*env).GetMethodID(pathClass, "lineTo", "(FF)V");
    jmethodID closeMethod = (*env).GetMethodID(pathClass, "close", "()V");
    jmethodID rewindMethod = (*env).GetMethodID(pathClass, "rewind", "()V");

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
            uint32_t color = abgr_to_rgba(reinterpret_cast<uint32_t *>(pixels)[offset]);

            (*env).CallVoidMethod(paint, setColorMethod, (int) color);

            (*env).CallVoidMethod(path, rewindMethod);
            (*env).CallVoidMethod(path, moveToMethod, (float) x1, (float) y1);
            (*env).CallVoidMethod(path, lineToMethod, (float) x2, (float) y2);
            (*env).CallVoidMethod(path, lineToMethod, (float) x3, (float) y3);
            (*env).CallVoidMethod(path, closeMethod);

            (*env).CallVoidMethod(canvas, drawPathMethod, path, paint);
        }
    } else {
        for (int i = 0; i + 1 < width * height; i += 2) {
            int x1 = triangles[i];
            int y1 = triangles[i + 1];
            uint32_t rowsOffset = CLAMP(y1, 0, height - 1) * width;
            uint32_t offset = CLAMP(x1, 0, width - 1) + rowsOffset;;
            uint32_t color = abgr_to_rgba(reinterpret_cast<uint32_t *>(pixels)[offset]);

            (*env).CallVoidMethod(paint, setColorMethod, (int) color);
            (*env).CallVoidMethod(canvas, drawCircleMethod, (float) x1, (float) y1, 1.0f,
                                   paint);
        }
    }

    free(triangles);

    AndroidBitmap_unlockPixels(env, input);
    AndroidBitmap_unlockPixels(env, newImage);

    return newImage;
}