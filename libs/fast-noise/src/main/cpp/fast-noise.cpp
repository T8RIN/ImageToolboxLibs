#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <android/log.h>
#include <android/bitmap.h>
#include <vector>

#include "FastNoiseLite.h"
#include "ColorUtils.h"
#include "ColorTranslator.h"

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

float lerp(float start, float stop, float fraction) {
    return (1 - fraction) * start + fraction * stop;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_fast_1noise_FastNoise_generateNoiseImage(JNIEnv *env, jobject thiz, jint width,
                                                        jint height, jint seed, jfloat frequency,
                                                        jint noise_type, jint rotation_type3_d,
                                                        jint fractal_type, jint fractal_octaves,
                                                        jfloat fractal_lacunarity,
                                                        jfloat fractal_gain,
                                                        jfloat fractal_weighted_strength,
                                                        jfloat fractal_ping_pong_strength,
                                                        jint cellular_distance_function,
                                                        jint cellular_return_type,
                                                        jfloat cellular_jitter,
                                                        jint domain_warp_type,
                                                        jfloat domain_warp_amp) {
    FastNoiseLite generator = FastNoiseLite(seed);

    generator.SetFrequency(frequency);
    generator.SetNoiseType(static_cast<FastNoiseLite::NoiseType>(noise_type));
    generator.SetRotationType3D(static_cast<FastNoiseLite::RotationType3D>(rotation_type3_d));
    generator.SetFractalType(static_cast<FastNoiseLite::FractalType>(fractal_type));
    generator.SetFractalOctaves(fractal_octaves);
    generator.SetFractalLacunarity(fractal_lacunarity);
    generator.SetFractalGain(fractal_gain);
    generator.SetFractalWeightedStrength(fractal_weighted_strength);
    generator.SetFractalPingPongStrength(fractal_ping_pong_strength);
    generator.SetCellularDistanceFunction(
            static_cast<FastNoiseLite::CellularDistanceFunction>(cellular_distance_function));
    generator.SetCellularReturnType(
            static_cast<FastNoiseLite::CellularReturnType>(cellular_return_type));
    generator.SetCellularJitter(cellular_jitter);
    generator.SetDomainWarpType(static_cast<FastNoiseLite::DomainWarpType>(domain_warp_type));
    generator.SetDomainWarpAmp(domain_warp_amp);


    LAB startColor = colorToLAB(ColorToRGB(0xFF000000));
    LAB stopColor = colorToLAB(ColorToRGB(0xFFFFFFFF));

    std::vector<int> noiseData(width * height);
    int index = 0;
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            float noise = generator.GetNoise((float) x, (float) y) + 1.0f;
            float fraction = noise / 2.0f;
            LAB lab = LAB(
                    lerp(startColor.l, stopColor.l, fraction),
                    lerp(startColor.a, stopColor.a, fraction),
                    lerp(startColor.b, stopColor.b, fraction)
            );
            RGB rgb = labToColor(lab);
            int color = RGB2Color(rgb.r, rgb.g, rgb.b);

            noiseData[index++] = color;
        }
    }

    return createBitmap(env, noiseData.data(), width, height, width * 3 * sizeof(uint8_t));
}