//
// Created by malik on 17.07.2024.
//
#include <jni.h>

#include <android/bitmap.h>
#include <cstring>
#include <cstdlib>
#include <cmath>
#include <algorithm>
#include <cmath>
#include <vector>
#include <string>
#include "ColorUtils.h"


void
calculateMeanAndStdLAB(JNIEnv *env, void *pixels, int width, int height, int stride, double mean[3],
                       double std[3]) {
    int pixelCount = width * height;

    for (int y = 0; y < height; ++y) {
        auto pixelsComp = reinterpret_cast<uint8_t *>(reinterpret_cast<uint8_t *>(pixels) +
                                                      y * stride);
        int x = 0;

        for (; x < width; ++x) {
            int r = pixelsComp[0];
            int g = pixelsComp[1];
            int b = pixelsComp[2];

            float lab[3];
            colorToLAB(r, g, b, lab);

            mean[0] += lab[0];
            mean[1] += lab[1];
            mean[2] += lab[2];

            pixelsComp += 4;
        }
    }

    mean[0] /= pixelCount;
    mean[1] /= pixelCount;
    mean[2] /= pixelCount;

    for (int y = 0; y < height; ++y) {
        auto pixelsComp = reinterpret_cast<uint8_t *>(reinterpret_cast<uint8_t *>(pixels) +
                                                      y * stride);
        int x = 0;

        for (; x < width; ++x) {
            int r = pixelsComp[0];
            int g = pixelsComp[1];
            int b = pixelsComp[2];

            float lab[3];
            colorToLAB(r, g, b, lab);

            std[0] += (lab[0] - mean[0]) * (lab[0] - mean[0]);
            std[1] += (lab[1] - mean[1]) * (lab[1] - mean[1]);
            std[2] += (lab[2] - mean[2]) * (lab[2] - mean[2]);

            pixelsComp += 4;
        }
    }

    std[0] = sqrt(std[0] / pixelCount);
    std[1] = sqrt(std[1] / pixelCount);
    std[2] = sqrt(std[2] / pixelCount);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_trickle_EffectsPipelineImpl_transferPaletteImpl(
        JNIEnv *env, jobject obj, jobject sourceBitmap, jobject targetBitmap, jfloat intensity) {

    AndroidBitmapInfo sourceInfo;
    void *sourcePixels;
    if (AndroidBitmap_getInfo(env, sourceBitmap, &sourceInfo) < 0) {
        return nullptr;
    }
    if (AndroidBitmap_lockPixels(env, sourceBitmap, &sourcePixels) < 0) {
        return nullptr;
    }

    AndroidBitmapInfo targetInfo;
    void *targetPixels;
    if (AndroidBitmap_getInfo(env, targetBitmap, &targetInfo) < 0) {
        return nullptr;
    }
    if (AndroidBitmap_lockPixels(env, targetBitmap, &targetPixels) < 0) {
        return nullptr;
    }

    auto sourceWidth = sourceInfo.width;
    auto sourceHeight = sourceInfo.height;

    auto targetWidth = targetInfo.width;
    auto targetHeight = targetInfo.height;

    jclass bitmapConfig = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID rgba8888FieldID = env->GetStaticFieldID(bitmapConfig, "ARGB_8888",
                                                     "Landroid/graphics/Bitmap$Config;");
    jobject rgba8888Obj = env->GetStaticObjectField(bitmapConfig, rgba8888FieldID);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");

    jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject result = env->CallStaticObjectMethod(bitmapClass, createBitmapMethodID,
                                                 static_cast<jint>(targetWidth),
                                                 static_cast<jint>(targetHeight), rgba8888Obj);

    double sourceMean[3] = {0.0, 0.0, 0.0};
    double sourceStd[3] = {0.0, 0.0, 0.0};
    calculateMeanAndStdLAB(env, sourcePixels, sourceWidth, sourceHeight, sourceInfo.stride,
                           sourceMean, sourceStd);

    double targetMean[3] = {0.0, 0.0, 0.0};
    double targetStd[3] = {0.0, 0.0, 0.0};
    calculateMeanAndStdLAB(env, targetPixels, targetWidth, targetHeight, targetInfo.stride,
                           targetMean, targetStd);

    void *resultPixels;
    if (AndroidBitmap_lockPixels(env, result, &resultPixels) < 0) {
        AndroidBitmap_unlockPixels(env, targetBitmap);
        AndroidBitmap_unlockPixels(env, sourceBitmap);
        return nullptr;
    }

    for (int y = 0; y < targetHeight; ++y) {
        auto pixelsComp = reinterpret_cast<uint8_t *>(reinterpret_cast<uint8_t *>(targetPixels) +
                                                      y * targetInfo.stride);

        auto dst = reinterpret_cast<uint8_t *>(reinterpret_cast<uint8_t *>(resultPixels) +
                                               y * targetInfo.stride);
        int x = 0;

        for (; x < targetWidth; ++x) {
            int r = pixelsComp[0];
            int g = pixelsComp[1];
            int b = pixelsComp[2];

            float lab[3];
            colorToLAB(r, g, b, lab);

            double newL = targetStd[0] != 0.0 ? (
                    (lab[0] - targetMean[0]) * (sourceStd[0] / targetStd[0]) +
                    sourceMean[0]) : 0.0;
            double newA = targetStd[1] != 0.0 ? (
                    (lab[1] - targetMean[1]) * (sourceStd[1] / targetStd[1]) +
                    sourceMean[1]) : 0.0;
            double newB = targetStd[2] != 0.0 ? (
                    (lab[2] - targetMean[2]) * (sourceStd[2] / targetStd[2]) +
                    sourceMean[2]) : 0.0;

            double finalL = lab[0] + intensity * (newL - lab[0]);
            double finalA = lab[1] + intensity * (newA - lab[1]);
            double finalB = lab[2] + intensity * (newB - lab[2]);

            int rgb[3];
            labToColor(finalL, finalA, finalB, rgb);

            dst[0] = rgb[0];
            dst[1] = rgb[1];
            dst[2] = rgb[2];
            dst[3] = pixelsComp[3];

            dst += 4;
            pixelsComp += 4;
        }
    }

    AndroidBitmap_unlockPixels(env, sourceBitmap);
    AndroidBitmap_unlockPixels(env, targetBitmap);
    AndroidBitmap_unlockPixels(env, result);

    return result;
}