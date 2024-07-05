#include <jni.h>
#include <stddef.h>
#include <android/bitmap.h>
#include <stdlib.h>
#include <string.h>
#include "lowpoly.h"

JNIEXPORT jintArray JNICALL
Java_io_github_xyzxqs_xlowpoly_LowPoly_getTriangles(JNIEnv *env, jclass type,
                                                    jobject input,
                                                    jint threshold,
                                                    jfloat alpha_count, jboolean lowPoly) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, input, &info) < 0) {
        return NULL;
    }

    void *allocatedPixels;
    if (AndroidBitmap_lockPixels(env, input, &allocatedPixels) < 0) {
        return NULL;
    }

    int width = (int) info.width;
    int height = (int) info.height;

    jint *pixelArray = (jint *) malloc(sizeof(jint) * width * height);
    memcpy(pixelArray, allocatedPixels, sizeof(jint) * width * height);

    jintArray pixels_ = (*env)->NewIntArray(env, width * height);
    (*env)->SetIntArrayRegion(env, pixels_, 0, width * height, pixelArray);

    jint *pixels = (*env)->GetIntArrayElements(env, pixels_, NULL);
    jint size = (*env)->GetArrayLength(env, pixels_);

    jintArray tmpArray_ = (*env)->NewIntArray(env, size);
    jint *tmpArray = (*env)->GetIntArrayElements(env, tmpArray_, NULL);

    int tmpSize = 0;

    get_triangles(pixels, size, width, height, threshold, alpha_count, tmpArray, &tmpSize, lowPoly);

    (*env)->ReleaseIntArrayElements(env, pixels_, pixels, 0);

    jintArray result = (*env)->NewIntArray(env, tmpSize);

    (*env)->SetIntArrayRegion(env, result, 0, tmpSize, tmpArray);

    (*env)->ReleaseIntArrayElements(env, tmpArray_, tmpArray, 0);
    return result;
}

uint32_t abgr_to_rgba(uint32_t abgr) {
    uint32_t alpha = (abgr >> 24) & 0xFF;
    uint32_t blue = (abgr >> 16) & 0xFF;
    uint32_t green = (abgr >> 8) & 0xFF;
    uint32_t red = abgr & 0xFF;
    return (red << 24) | (green << 16) | (blue << 8) | alpha;
}

uint32_t clamp(uint32_t d, uint32_t min, uint32_t max) {
    const uint32_t t = d < min ? min : d;
    return t > max ? max : t;
}

JNIEXPORT jobject JNICALL
Java_io_github_xyzxqs_xlowpoly_LowPoly_generate(JNIEnv *env, jclass clazz, jobject input,
                                                jint threshold,
                                                jfloat alphaOrPointCount, jboolean lowPoly,
                                                jboolean fill) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, input, &info) < 0) {
        return NULL;
    }

    void *pixels;
    if (AndroidBitmap_lockPixels(env, input, &pixels) < 0) {
        return NULL;
    }

    int width = (int) info.width;
    int height = (int) info.height;

    jclass bitmapClass = (*env)->FindClass(env, "android/graphics/Bitmap");
    jmethodID createBitmapMethod = (*env)->GetStaticMethodID(env, bitmapClass, "createBitmap",
                                                             "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jclass bitmapConfigClass = (*env)->FindClass(env, "android/graphics/Bitmap$Config");
    jfieldID argb8888Field = (*env)->GetStaticFieldID(env, bitmapConfigClass, "ARGB_8888",
                                                      "Landroid/graphics/Bitmap$Config;");
    jobject argb8888 = (*env)->GetStaticObjectField(env, bitmapConfigClass, argb8888Field);

    jobject newImage = (*env)->CallStaticObjectMethod(env, bitmapClass, createBitmapMethod, width,
                                                      height, argb8888);
    if (newImage == NULL) {
        AndroidBitmap_unlockPixels(env, input);
        return NULL;
    }

    void *newPixels;
    if (AndroidBitmap_lockPixels(env, newImage, &newPixels) < 0) {
        AndroidBitmap_unlockPixels(env, input);
        return NULL;
    }

    jint *pixelArray = (jint *) malloc(sizeof(jint) * width * height);
    memcpy(pixelArray, pixels, sizeof(jint) * width * height);

    jintArray pixelsArray = (*env)->NewIntArray(env, width * height);
    (*env)->SetIntArrayRegion(env, pixelsArray, 0, width * height, pixelArray);

    jintArray trianglesArray = Java_io_github_xyzxqs_xlowpoly_LowPoly_getTriangles(
            env,
            clazz,
            input,
            threshold,
            alphaOrPointCount,
            lowPoly
    );
    jsize trianglesLength = (*env)->GetArrayLength(env, trianglesArray);
    jint *triangles = (jint *) malloc(sizeof(jint) * trianglesLength);
    (*env)->GetIntArrayRegion(env, trianglesArray, 0, trianglesLength, triangles);

    jclass canvasClass = (*env)->FindClass(env, "android/graphics/Canvas");
    jmethodID canvasConstructor = (*env)->GetMethodID(env, canvasClass, "<init>",
                                                      "(Landroid/graphics/Bitmap;)V");
    jobject canvas = (*env)->NewObject(env, canvasClass, canvasConstructor, newImage);

    jclass paintClass = (*env)->FindClass(env, "android/graphics/Paint");
    jmethodID paintConstructor = (*env)->GetMethodID(env, paintClass, "<init>", "()V");
    jobject paint = (*env)->NewObject(env, paintClass, paintConstructor);

    jmethodID setAntiAliasMethod = (*env)->GetMethodID(env, paintClass, "setAntiAlias", "(Z)V");
    (*env)->CallVoidMethod(env, paint, setAntiAliasMethod, JNI_FALSE);

    jmethodID setStyleMethod = (*env)->GetMethodID(env, paintClass, "setStyle",
                                                   "(Landroid/graphics/Paint$Style;)V");
    jclass paintStyleClass = (*env)->FindClass(env, "android/graphics/Paint$Style");
    jobject style = fill ? (*env)->GetStaticObjectField(env, paintStyleClass,
                                                        (*env)->GetStaticFieldID(env,
                                                                                 paintStyleClass,
                                                                                 "FILL",
                                                                                 "Landroid/graphics/Paint$Style;"))
                         : (*env)->GetStaticObjectField(env, paintStyleClass,
                                                        (*env)->GetStaticFieldID(env,
                                                                                 paintStyleClass,
                                                                                 "STROKE",
                                                                                 "Landroid/graphics/Paint$Style;"));
    (*env)->CallVoidMethod(env, paint, setStyleMethod, style);

    jmethodID setColorMethod = (*env)->GetMethodID(env, paintClass, "setColor", "(I)V");
    jmethodID drawPathMethod = (*env)->GetMethodID(env, canvasClass, "drawPath",
                                                   "(Landroid/graphics/Path;Landroid/graphics/Paint;)V");
    jmethodID drawCircleMethod = (*env)->GetMethodID(env, canvasClass, "drawCircle",
                                                     "(FFFLandroid/graphics/Paint;)V");

    jclass pathClass = (*env)->FindClass(env, "android/graphics/Path");
    jmethodID pathConstructor = (*env)->GetMethodID(env, pathClass, "<init>", "()V");
    jobject path = (*env)->NewObject(env, pathClass, pathConstructor);
    jmethodID moveToMethod = (*env)->GetMethodID(env, pathClass, "moveTo", "(FF)V");
    jmethodID lineToMethod = (*env)->GetMethodID(env, pathClass, "lineTo", "(FF)V");
    jmethodID closeMethod = (*env)->GetMethodID(env, pathClass, "close", "()V");
    jmethodID rewindMethod = (*env)->GetMethodID(env, pathClass, "rewind", "()V");

    if (lowPoly) {
        for (int i = 0; i + 5 < trianglesLength; i += 6) {
            int x1 = triangles[i];
            int y1 = triangles[i + 1];
            int x2 = triangles[i + 2];
            int y2 = triangles[i + 3];
            int x3 = triangles[i + 4];
            int y3 = triangles[i + 5];

            uint32_t rowsOffset = clamp((y1 + y2 + y3) / 3, 0, height - 1) * width;
            uint32_t offset = clamp((x1 + x2 + x3) / 3, 0, width - 1) * rowsOffset;
            uint32_t color = abgr_to_rgba(((uint32_t *) pixelArray)[offset]);

            (*env)->CallVoidMethod(env, paint, setColorMethod, (int) color);

            (*env)->CallVoidMethod(env, path, rewindMethod);
            (*env)->CallVoidMethod(env, path, moveToMethod, (float) x1, (float) y1);
            (*env)->CallVoidMethod(env, path, lineToMethod, (float) x2, (float) y2);
            (*env)->CallVoidMethod(env, path, lineToMethod, (float) x3, (float) y3);
            (*env)->CallVoidMethod(env, path, closeMethod);

            (*env)->CallVoidMethod(env, canvas, drawPathMethod, path, paint);
        }
    } else {
        for (int i = 0; i + 1 < trianglesLength; i += 2) {
            int x1 = triangles[i];
            int y1 = triangles[i + 1];
            uint32_t rowsOffset = clamp(y1, 0, height - 1) * width;
            uint32_t offset = clamp(x1, 0, width - 1) * rowsOffset;
            uint32_t color = abgr_to_rgba(((uint32_t *) pixelArray)[offset]);
            (*env)->CallVoidMethod(env, paint, setColorMethod, (int) color);
            (*env)->CallVoidMethod(env, canvas, drawCircleMethod, (float) x1, (float) y1, 1.0f,
                                   paint);
        }
    }

    free(pixelArray);
    free(triangles);

    AndroidBitmap_unlockPixels(env, input);
    AndroidBitmap_unlockPixels(env, newImage);

    return newImage;
}