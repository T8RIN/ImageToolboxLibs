#include <android/bitmap.h>
#include <jni.h>

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <limits>
#include <mutex>
#include <stdexcept>
#include <string>
#include <vector>

#include "gmic.h"

namespace {

constexpr const char *kExceptionClass = "com/t8rin/gmic/model/GmicException";

// G'MIC keeps interpreter runs and command definitions in process-global storage.
// Running multiple interpreter instances concurrently can corrupt that storage.
std::mutex gGmicRunMutex;

// CImg's built-in FFT on Android accepts only power-of-two dimensions. The stock
// command resizes to the largest operand, which can still be an arbitrary size.
constexpr const char *kAndroidGmicCommands = R"gmic(
convolve_fft : check ${is_image_arg\ $1}" && isin(${2=2},0,1,2,3)"
  pass$1 store. kernel
  foreach {
    if w
      w0,h0,d0={w},{h},{d}
      $kernel
      if $2!=2 r[0] {[w#0,h#0,d#0]+2*round([w#1>1?w#1:0,h#1>1?h#1:0,d#1>1?d#1:0]/2)},100%,0,$2,0.5,0.5 fi
      r[0,1] {2^ceil(log2(max(w#0,w#1)))},{2^ceil(log2(max(h#0,h#1)))},{2^ceil(log2(max(d#0,d#1)))},100%,0,0,0.5,0.5
      r 100%,100%,100%,${-max_s}
      fft[1] fft[0]
      +*[1,2] +*[0,3] +[-2,-1] *[1,3] *[0,2] -[0,1]
      ifft rm.
      shift {-int(([w,h,d]-1)/2)},0,2
      r $w0,$h0,$d0,100%,0,0,0.5,0.5
    fi
  }
)gmic";

void throwGmicException(JNIEnv *env, const std::string &message) {
    if (env->ExceptionCheck()) return;

    jclass exceptionClass = env->FindClass(kExceptionClass);
    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, message.c_str());
        env->DeleteLocalRef(exceptionClass);
    }
}

class UtfChars final {
public:
    UtfChars(JNIEnv *env, jstring value)
        : env_(env), value_(value), chars_(env->GetStringUTFChars(value, nullptr)) {}

    ~UtfChars() {
        if (chars_ != nullptr) env_->ReleaseStringUTFChars(value_, chars_);
    }

    UtfChars(const UtfChars &) = delete;
    UtfChars &operator=(const UtfChars &) = delete;

    [[nodiscard]] const char *get() const { return chars_; }

private:
    JNIEnv *env_;
    jstring value_;
    const char *chars_;
};

class LockedBitmap final {
public:
    LockedBitmap(JNIEnv *env, jobject bitmap) : env_(env), bitmap_(bitmap) {
        const int infoResult = AndroidBitmap_getInfo(env_, bitmap_, &info_);
        if (infoResult != ANDROID_BITMAP_RESULT_SUCCESS) {
            throw std::runtime_error("Unable to read bitmap information");
        }
        if (info_.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
            throw std::invalid_argument("Native input bitmap must use ARGB_8888 configuration");
        }
        if (info_.width == 0 || info_.height == 0) {
            throw std::invalid_argument("Bitmap dimensions must be positive");
        }

        const int lockResult = AndroidBitmap_lockPixels(env_, bitmap_, &pixels_);
        if (lockResult != ANDROID_BITMAP_RESULT_SUCCESS || pixels_ == nullptr) {
            throw std::runtime_error("Unable to lock bitmap pixels");
        }
    }

    ~LockedBitmap() {
        if (pixels_ != nullptr) AndroidBitmap_unlockPixels(env_, bitmap_);
    }

    LockedBitmap(const LockedBitmap &) = delete;
    LockedBitmap &operator=(const LockedBitmap &) = delete;

    [[nodiscard]] const AndroidBitmapInfo &info() const { return info_; }
    [[nodiscard]] void *pixels() const { return pixels_; }

private:
    JNIEnv *env_;
    jobject bitmap_;
    AndroidBitmapInfo info_{};
    void *pixels_ = nullptr;
};

[[nodiscard]] jobject createArgbBitmap(JNIEnv *env, uint32_t width, uint32_t height) {
    if (width > static_cast<uint32_t>(std::numeric_limits<jint>::max()) ||
        height > static_cast<uint32_t>(std::numeric_limits<jint>::max())) {
        throw std::overflow_error("G'MIC output dimensions are too large for an Android Bitmap");
    }

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    if (bitmapClass == nullptr) return nullptr;

    jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
    if (configClass == nullptr) {
        env->DeleteLocalRef(bitmapClass);
        return nullptr;
    }

    jfieldID argbField = env->GetStaticFieldID(
        configClass,
        "ARGB_8888",
        "Landroid/graphics/Bitmap$Config;"
    );
    jmethodID createMethod = env->GetStaticMethodID(
        bitmapClass,
        "createBitmap",
        "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;"
    );

    jobject config = argbField == nullptr ? nullptr : env->GetStaticObjectField(configClass, argbField);
    jobject bitmap = createMethod == nullptr || config == nullptr
        ? nullptr
        : env->CallStaticObjectMethod(
            bitmapClass,
            createMethod,
            static_cast<jint>(width),
            static_cast<jint>(height),
            config
        );

    if (config != nullptr) env->DeleteLocalRef(config);
    env->DeleteLocalRef(configClass);
    env->DeleteLocalRef(bitmapClass);
    return bitmap;
}

[[nodiscard]] float clampByte(float value) {
    if (!std::isfinite(value)) return 0.0f;
    return std::clamp(value, 0.0f, 255.0f);
}

[[nodiscard]] uint8_t toByte(float value) {
    return static_cast<uint8_t>(clampByte(value) + 0.5f);
}

[[nodiscard]] float lerp(float start, float end, float fraction) {
    return start + (end - start) * fraction;
}

[[nodiscard]] float sampleAlpha(
    const std::vector<float> &alpha,
    uint32_t sourceWidth,
    uint32_t sourceHeight,
    uint32_t outputX,
    uint32_t outputY,
    uint32_t outputWidth,
    uint32_t outputHeight
) {
    if (sourceWidth == outputWidth && sourceHeight == outputHeight) {
        return alpha[static_cast<size_t>(outputY) * sourceWidth + outputX];
    }

    const float sourceX = ((static_cast<float>(outputX) + 0.5f) * sourceWidth / outputWidth) - 0.5f;
    const float sourceY = ((static_cast<float>(outputY) + 0.5f) * sourceHeight / outputHeight) - 0.5f;
    const int x0 = std::clamp(static_cast<int>(std::floor(sourceX)), 0, static_cast<int>(sourceWidth) - 1);
    const int y0 = std::clamp(static_cast<int>(std::floor(sourceY)), 0, static_cast<int>(sourceHeight) - 1);
    const int x1 = std::min(x0 + 1, static_cast<int>(sourceWidth) - 1);
    const int y1 = std::min(y0 + 1, static_cast<int>(sourceHeight) - 1);
    const float tx = std::clamp(sourceX - std::floor(sourceX), 0.0f, 1.0f);
    const float ty = std::clamp(sourceY - std::floor(sourceY), 0.0f, 1.0f);

    const float top = lerp(
        alpha[static_cast<size_t>(y0) * sourceWidth + x0],
        alpha[static_cast<size_t>(y0) * sourceWidth + x1],
        tx
    );
    const float bottom = lerp(
        alpha[static_cast<size_t>(y1) * sourceWidth + x0],
        alpha[static_cast<size_t>(y1) * sourceWidth + x1],
        tx
    );
    return lerp(top, bottom, ty);
}

void copyBitmapToGmic(
    const LockedBitmap &source,
    bool preserveAlpha,
    gmic_image<float> &image,
    std::vector<float> &sourceAlpha
) {
    const AndroidBitmapInfo &info = source.info();
    const size_t pixelCount = static_cast<size_t>(info.width) * info.height;
    const uint32_t channels = preserveAlpha ? 3U : 4U;
    if (pixelCount > std::numeric_limits<size_t>::max() / channels / sizeof(float)) {
        throw std::overflow_error("Input bitmap is too large");
    }

    image.assign(info.width, info.height, 1, channels);
    sourceAlpha.resize(preserveAlpha ? pixelCount : 0);

    float *red = image._data;
    float *green = red + pixelCount;
    float *blue = green + pixelCount;
    float *alpha = preserveAlpha ? nullptr : blue + pixelCount;
    const bool isPremultiplied =
        (info.flags & ANDROID_BITMAP_FLAGS_ALPHA_MASK) == ANDROID_BITMAP_FLAGS_ALPHA_PREMUL;

    for (uint32_t y = 0; y < info.height; ++y) {
        const auto *row = static_cast<const uint8_t *>(source.pixels()) + static_cast<size_t>(y) * info.stride;
        for (uint32_t x = 0; x < info.width; ++x) {
            const auto *pixel = row + static_cast<size_t>(x) * 4;
            const size_t index = static_cast<size_t>(y) * info.width + x;
            const float a = pixel[3];
            const float unpremultiply = isPremultiplied && a > 0.0f ? 255.0f / a : 1.0f;

            red[index] = a == 0.0f && isPremultiplied ? 0.0f : clampByte(pixel[0] * unpremultiply);
            green[index] = a == 0.0f && isPremultiplied ? 0.0f : clampByte(pixel[1] * unpremultiply);
            blue[index] = a == 0.0f && isPremultiplied ? 0.0f : clampByte(pixel[2] * unpremultiply);
            if (preserveAlpha) sourceAlpha[index] = a;
            else alpha[index] = a;
        }
    }
}

void copyGmicToBitmap(
    const gmic_image<float> &image,
    const std::vector<float> &sourceAlpha,
    uint32_t sourceWidth,
    uint32_t sourceHeight,
    bool preserveAlpha,
    const LockedBitmap &target
) {
    if (image._depth != 1) {
        throw std::invalid_argument("G'MIC output must be a 2D image");
    }
    if (image._spectrum == 0 || image._data == nullptr) {
        throw std::invalid_argument("G'MIC output image has no channels");
    }

    const AndroidBitmapInfo &info = target.info();
    const size_t pixelCount = static_cast<size_t>(image._width) * image._height;
    const float *channel0 = image._data;
    const float *channel1 = image._spectrum > 1 ? channel0 + pixelCount : channel0;
    const float *channel2 = image._spectrum > 2 ? channel1 + pixelCount : channel0;
    const float *channel3 = image._spectrum > 3 ? channel2 + pixelCount : nullptr;
    const bool outputIsPremultiplied =
        (info.flags & ANDROID_BITMAP_FLAGS_ALPHA_MASK) == ANDROID_BITMAP_FLAGS_ALPHA_PREMUL;

    for (uint32_t y = 0; y < image._height; ++y) {
        auto *row = static_cast<uint8_t *>(target.pixels()) + static_cast<size_t>(y) * info.stride;
        for (uint32_t x = 0; x < image._width; ++x) {
            const size_t index = static_cast<size_t>(y) * image._width + x;
            auto *pixel = row + static_cast<size_t>(x) * 4;
            const float a = preserveAlpha
                ? sampleAlpha(
                    sourceAlpha,
                    sourceWidth,
                    sourceHeight,
                    x,
                    y,
                    image._width,
                    image._height
                )
                : channel3 != nullptr
                    ? channel3[index]
                    : image._spectrum == 2
                        ? channel1[index]
                        : 255.0f;
            const float alphaByte = clampByte(a);
            const float premultiply = outputIsPremultiplied ? alphaByte / 255.0f : 1.0f;

            pixel[0] = toByte(channel0[index] * premultiply);
            pixel[1] = toByte(channel1[index] * premultiply);
            pixel[2] = toByte(channel2[index] * premultiply);
            pixel[3] = toByte(alphaByte);
        }
    }
}

[[nodiscard]] size_t resolveOutputIndex(jint requestedIndex, size_t imageCount) {
    const int64_t resolved = requestedIndex >= 0
        ? requestedIndex
        : static_cast<int64_t>(imageCount) + requestedIndex;
    if (resolved < 0 || resolved >= static_cast<int64_t>(imageCount)) {
        throw std::out_of_range(
            "G'MIC output index " + std::to_string(requestedIndex) +
            " is outside the returned image list of size " + std::to_string(imageCount)
        );
    }
    return static_cast<size_t>(resolved);
}

} // namespace

extern "C" JNIEXPORT jobject JNICALL
Java_com_t8rin_gmic_Gmic_nativeRun(
    JNIEnv *env,
    jobject,
    jobject input,
    jstring command,
    jboolean preserveAlpha,
    jint outputIndex
) {
    if (input == nullptr || command == nullptr) {
        throwGmicException(env, "Input bitmap and G'MIC command must not be null");
        return nullptr;
    }

    try {
        UtfChars commandChars(env, command);
        if (commandChars.get() == nullptr) return nullptr;

        uint32_t sourceWidth;
        uint32_t sourceHeight;
        std::vector<float> sourceAlpha;
        gmic_list<float> images;
        gmic_list<char> imageNames;
        images.assign(1);
        {
            LockedBitmap source(env, input);
            sourceWidth = source.info().width;
            sourceHeight = source.info().height;
            copyBitmapToGmic(source, preserveAlpha == JNI_TRUE, images[0], sourceAlpha);
        }

        const std::lock_guard<std::mutex> lock(gGmicRunMutex);
        const std::string commandLine = std::string("v - ") + commandChars.get();
        gmic interpreter(
            commandLine.c_str(),
            images,
            imageNames,
            kAndroidGmicCommands,
            true,
            nullptr,
            nullptr
        );
        if (images._width == 0) {
            throw std::runtime_error("G'MIC command returned no images");
        }

        const gmic_image<float> &output = images[resolveOutputIndex(outputIndex, images._width)];
        jobject result = createArgbBitmap(env, output._width, output._height);
        if (result == nullptr) {
            if (!env->ExceptionCheck()) throw std::runtime_error("Unable to create output Bitmap");
            return nullptr;
        }

        {
            LockedBitmap target(env, result);
            copyGmicToBitmap(
                output,
                sourceAlpha,
                sourceWidth,
                sourceHeight,
                preserveAlpha == JNI_TRUE,
                target
            );
        }
        return result;
    } catch (const gmic_exception &exception) {
        throwGmicException(env, exception.what());
    } catch (const std::bad_alloc &) {
        throwGmicException(env, "Not enough memory to process this bitmap with G'MIC");
    } catch (const std::exception &exception) {
        throwGmicException(env, exception.what());
    } catch (...) {
        throwGmicException(env, "Unknown native G'MIC error");
    }
    return nullptr;
}
