#include <jni.h>
#include <android/bitmap.h>
#include <signal.h>
#include <sys/stat.h>
#include <unistd.h>

#include <algorithm>
#include <cerrno>
#include <cstdint>
#include <cstring>
#include <memory>
#include <new>
#include <thread>

#include "libraw/libraw.h"

namespace {

    constexpr uint64_t kMaxInputBytes = 2ULL * 1024 * 1024 * 1024;
    constexpr uint64_t kMaxOutputPixels = 100'000'000ULL;
    constexpr uint64_t kMaxOutputBytes = 512ULL * 1024 * 1024;

    struct Session {
        libraw_data_t *raw = nullptr;
        libraw_processed_image_t *image = nullptr;
        int inputFlip = 0;
        int outputFlip = 0;
        int outputWidth = 0;
        int outputHeight = 0;
        int outputColors = 0;
        int outputBits = 0;
        bool processed = false;

        ~Session() {
            if (image != nullptr) {
                libraw_dcraw_clear_mem(image);
            }
            if (raw != nullptr) {
                libraw_close(raw);
            }
        }

        void clearOutput() {
            if (image != nullptr) {
                libraw_dcraw_clear_mem(image);
                image = nullptr;
            }
            processed = false;
            outputWidth = outputHeight = outputColors = outputBits = 0;
        }

        bool validOutput() const {
            if (processed) {
                const uint64_t pixels = static_cast<uint64_t>(outputWidth) * outputHeight;
                return outputWidth > 0 && outputHeight > 0 &&
                        (outputColors == 1 || outputColors == 3 || outputColors == 4) &&
                        (outputBits == 8 || outputBits == 16) && pixels <= kMaxOutputPixels;
            }
            if (image == nullptr || image->data_size == 0 || image->data_size > kMaxOutputBytes) {
                return false;
            }
            if (image->type == LIBRAW_IMAGE_JPEG) {
                return image->data_size >= 2 && image->data[0] == 0xff && image->data[1] == 0xd8;
            }
            if (image->type != LIBRAW_IMAGE_BITMAP || image->width == 0 || image->height == 0) {
                return false;
            }
            const uint64_t pixels = static_cast<uint64_t>(image->width) * image->height;
            const uint64_t bytesPerSample = image->bits > 8 ? 2 : 1;
            const uint64_t expected = pixels * image->colors * bytesPerSample;
            return pixels <= kMaxOutputPixels && expected <= image->data_size;
        }
    };

    Session *fromHandle(jlong handle) {
        return reinterpret_cast<Session *>(static_cast<intptr_t>(handle));
    }

    bool validFile(const char *path) {
        struct stat info{};
        return path != nullptr && stat(path, &info) == 0 && info.st_size > 0 &&
                static_cast<uint64_t>(info.st_size) <= kMaxInputBytes;
    }

    int bestThumbnail(const libraw_data_t *raw) {
        int best = -1;
        uint64_t bestArea = 0;
        for (int i = 0; i < raw->thumbs_list.thumbcount; ++i) {
            const auto &thumb = raw->thumbs_list.thumblist[i];
            const uint64_t area = static_cast<uint64_t>(thumb.twidth) * thumb.theight;
            if (area > bestArea && thumb.tlength > 0 && thumb.tlength <= kMaxOutputBytes) {
                best = i;
                bestArea = area;
            }
        }
        return best;
    }

}

extern "C" JNIEXPORT jlong JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeOpen(JNIEnv *env, jobject, jstring pathValue) {
    if (pathValue == nullptr) return 0;
    const char *path = env->GetStringUTFChars(pathValue, nullptr);
    if (path == nullptr) return 0;
    std::unique_ptr<Session> session(new(std::nothrow) Session());
    if (!session || !validFile(path)) {
        env->ReleaseStringUTFChars(pathValue, path);
        return 0;
    }
    session->raw = libraw_init(0);
    if (session->raw == nullptr) {
        env->ReleaseStringUTFChars(pathValue, path);
        return 0;
    }
    session->raw->rawparams.max_raw_memory_mb = 512;
    const int result = libraw_open_file(session->raw, path);
    env->ReleaseStringUTFChars(pathValue, path);
    if (result != LIBRAW_SUCCESS || session->raw->idata.raw_count == 0 ||
            session->raw->sizes.width == 0 || session->raw->sizes.height == 0) {
        return 0;
    }
    const uint64_t pixels = static_cast<uint64_t>(session->raw->sizes.width) *
            session->raw->sizes.height;
    if (pixels > kMaxOutputPixels) return 0;
    session->inputFlip = session->raw->sizes.flip;
    return static_cast<jlong>(reinterpret_cast<intptr_t>(session.release()));
}

extern "C" JNIEXPORT void JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeClose(JNIEnv *, jobject, jlong handle) {
    delete fromHandle(handle);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeInfo(JNIEnv *, jobject, jlong handle) {
    const Session *session = fromHandle(handle);
    if (session == nullptr || session->raw == nullptr) return 0;
    return static_cast<jlong>(session->raw->sizes.width & 0xffffU) |
            static_cast<jlong>(session->raw->sizes.height & 0xffffU) << 16 |
            static_cast<jlong>(session->raw->sizes.flip & 0xfU) << 32 |
            static_cast<jlong>(session->raw->thumbs_list.thumbcount & 0xffU) << 36 |
            static_cast<jlong>(session->raw->idata.dng_version != 0) << 44;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeUnpackThumbnail(JNIEnv *, jobject, jlong handle) {
    Session *session = fromHandle(handle);
    if (session == nullptr || session->raw == nullptr) return JNI_FALSE;
    const int thumbnail = bestThumbnail(session->raw);
    if (thumbnail < 0 || libraw_unpack_thumb_ex(session->raw, thumbnail) != LIBRAW_SUCCESS) {
        return JNI_FALSE;
    }
    session->outputFlip = session->raw->thumbs_list.thumblist[thumbnail].tflip;
    int error = LIBRAW_SUCCESS;
    session->clearOutput();
    session->image = libraw_dcraw_make_mem_thumb(session->raw, &error);
    if (error != LIBRAW_SUCCESS || !session->validOutput()) {
        session->clearOutput();
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeUnpack(JNIEnv *, jobject, jlong handle) {
    Session *session = fromHandle(handle);
    return session != nullptr && session->raw != nullptr &&
            libraw_unpack(session->raw) == LIBRAW_SUCCESS
            ? JNI_TRUE
            : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeProcess(
        JNIEnv *, jobject, jlong handle, jboolean cameraWb, jboolean autoWb,
        jint outputColor, jint highlight, jint quality, jboolean halfSize, jboolean output16Bit) {
    Session *session = fromHandle(handle);
    if (session == nullptr || session->raw == nullptr) return JNI_FALSE;
    auto &params = session->raw->params;
    params.use_camera_wb = cameraWb == JNI_TRUE;
    params.use_auto_wb = autoWb == JNI_TRUE;
    params.output_color = std::clamp(static_cast<int>(outputColor), 1, 6);
    params.highlight = std::clamp(static_cast<int>(highlight), 0, 9);
    params.user_qual = static_cast<int>(quality);
    params.half_size = halfSize == JNI_TRUE;
    params.output_bps = output16Bit == JNI_TRUE ? 16 : 8;
    params.user_flip = 0;
    session->clearOutput();
    if (libraw_dcraw_process(session->raw) != LIBRAW_SUCCESS) return JNI_FALSE;
    auto *processor = static_cast<LibRaw *>(session->raw->parent_class);
    processor->get_mem_image_format(
            &session->outputWidth,
            &session->outputHeight,
            &session->outputColors,
            &session->outputBits);
    session->processed = true;
    session->outputFlip = session->inputFlip;
    if (!session->validOutput()) {
        session->clearOutput();
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

namespace {

    bool clearException(JNIEnv *env) {
        if (!env->ExceptionCheck()) return false;
        env->ExceptionClear();
        return true;
    }

    jobject createBitmap(JNIEnv *env, uint32_t width, uint32_t height, bool output16Bit) {
        jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
        if (configClass == nullptr || bitmapClass == nullptr || clearException(env)) return nullptr;
        const char *configName = output16Bit ? "RGBA_F16" : "ARGB_8888";
        jfieldID configField = env->GetStaticFieldID(
                configClass, configName, "Landroid/graphics/Bitmap$Config;");
        jmethodID createMethod = env->GetStaticMethodID(
                bitmapClass,
                "createBitmap",
                "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
        if (configField == nullptr || createMethod == nullptr || clearException(env)) return nullptr;
        jobject config = env->GetStaticObjectField(configClass, configField);
        jobject bitmap = env->CallStaticObjectMethod(
                bitmapClass,
                createMethod,
                static_cast<jint>(width),
                static_cast<jint>(height),
                config);
        if (clearException(env)) return nullptr;
        return bitmap;
    }

    void recycleBitmap(JNIEnv *env, jobject bitmap) {
        if (bitmap == nullptr) return;
        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
        if (bitmapClass == nullptr || clearException(env)) return;
        jmethodID recycleMethod = env->GetMethodID(bitmapClass, "recycle", "()V");
        if (recycleMethod == nullptr || clearException(env)) return;
        env->CallVoidMethod(bitmap, recycleMethod);
        clearException(env);
    }

    bool copyOutputToBitmap(JNIEnv *env, const Session *session, jobject bitmap) {
        if (session == nullptr || !session->validOutput() || bitmap == nullptr ||
                session->image->type != LIBRAW_IMAGE_BITMAP) {
            return false;
        }

        AndroidBitmapInfo bitmapInfo{};
        if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) != ANDROID_BITMAP_RESULT_SUCCESS ||
                bitmapInfo.width != session->image->width ||
                bitmapInfo.height != session->image->height ||
                (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
                        bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_F16)) {
            return false;
        }

        void *pixels = nullptr;
        if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS ||
                pixels == nullptr) {
            return false;
        }

        const auto *source = session->image->data;
        const unsigned colors = session->image->colors;
        const unsigned sampleBytes = session->image->bits > 8 ? 2 : 1;
        const uint64_t pixelCount = static_cast<uint64_t>(session->image->width) *
                session->image->height;
        const uint64_t requiredBytes = pixelCount * colors * sampleBytes;
        if ((colors != 1 && (colors < 3 || colors > 4)) ||
                requiredBytes > session->image->data_size) {
            AndroidBitmap_unlockPixels(env, bitmap);
            return false;
        }

        const auto readSample = [source, colors, sampleBytes](
                uint64_t pixel, unsigned component) -> uint16_t {
            if (colors == 1) component = 0;
            const uint64_t offset = (pixel * colors + component) * sampleBytes;
            if (sampleBytes == 1) return static_cast<uint16_t>(source[offset]) * 257U;
            uint16_t value;
            std::memcpy(&value, source + offset, sizeof(value));
            return value;
        };
        const auto toHalf = [](uint16_t value) {
            const _Float16 half = static_cast<_Float16>(static_cast<float>(value) / 65535.0f);
            uint16_t bits;
            std::memcpy(&bits, &half, sizeof(bits));
            return bits;
        };

        for (uint32_t y = 0; y < bitmapInfo.height; ++y) {
            auto *row = static_cast<uint8_t *>(pixels) +
                    static_cast<size_t>(y) * bitmapInfo.stride;
            for (uint32_t x = 0; x < bitmapInfo.width; ++x) {
                const uint64_t pixel = static_cast<uint64_t>(y) * bitmapInfo.width + x;
                const uint16_t red = readSample(pixel, 0);
                const uint16_t green = readSample(pixel, colors == 1 ? 0 : 1);
                const uint16_t blue = readSample(pixel, colors == 1 ? 0 : 2);
                if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_F16) {
                    auto *target = reinterpret_cast<uint16_t *>(row) + x * 4;
                    target[0] = toHalf(red);
                    target[1] = toHalf(green);
                    target[2] = toHalf(blue);
                    target[3] = toHalf(65535);
                } else {
                    auto *target = row + x * 4;
                    target[0] = static_cast<uint8_t>(red >> 8);
                    target[1] = static_cast<uint8_t>(green >> 8);
                    target[2] = static_cast<uint8_t>(blue >> 8);
                    target[3] = 255;
                }
            }
        }

        return AndroidBitmap_unlockPixels(env, bitmap) == ANDROID_BITMAP_RESULT_SUCCESS;
    }

    bool copyProcessedToBitmap(JNIEnv *env, const Session *session, jobject bitmap) {
        if (session == nullptr || !session->processed || bitmap == nullptr) return false;
        AndroidBitmapInfo bitmapInfo{};
        if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) != ANDROID_BITMAP_RESULT_SUCCESS ||
                bitmapInfo.width != static_cast<uint32_t>(session->outputWidth) ||
                bitmapInfo.height != static_cast<uint32_t>(session->outputHeight) ||
                (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
                        bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_F16)) {
            return false;
        }
        void *pixels = nullptr;
        if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS ||
                pixels == nullptr) {
            return false;
        }
        auto *processor = static_cast<LibRaw *>(session->raw->parent_class);
        const int result = processor->copy_mem_image_rgba(
                pixels,
                static_cast<int>(bitmapInfo.stride),
                bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_F16);
        const int unlockResult = AndroidBitmap_unlockPixels(env, bitmap);
        return result == LIBRAW_SUCCESS && unlockResult == ANDROID_BITMAP_RESULT_SUCCESS;
    }

    jobject decodeJpeg(JNIEnv *env, const unsigned char *data, size_t size) {
        jclass parcelFdClass = env->FindClass("android/os/ParcelFileDescriptor");
        jclass inputClass = env->FindClass("android/os/ParcelFileDescriptor$AutoCloseInputStream");
        jclass bitmapFactoryClass = env->FindClass("android/graphics/BitmapFactory");
        if (parcelFdClass == nullptr || inputClass == nullptr || bitmapFactoryClass == nullptr ||
                clearException(env)) {
            return nullptr;
        }

        jmethodID createPipeMethod = env->GetStaticMethodID(
                parcelFdClass, "createPipe", "()[Landroid/os/ParcelFileDescriptor;");
        jmethodID getFdMethod = env->GetMethodID(parcelFdClass, "getFd", "()I");
        jmethodID closeFdMethod = env->GetMethodID(parcelFdClass, "close", "()V");
        jmethodID inputConstructor = env->GetMethodID(
                inputClass, "<init>", "(Landroid/os/ParcelFileDescriptor;)V");
        jmethodID closeInputMethod = env->GetMethodID(inputClass, "close", "()V");
        jmethodID decodeMethod = env->GetStaticMethodID(
                bitmapFactoryClass,
                "decodeStream",
                "(Ljava/io/InputStream;)Landroid/graphics/Bitmap;");
        if (createPipeMethod == nullptr || getFdMethod == nullptr || closeFdMethod == nullptr ||
                inputConstructor == nullptr || closeInputMethod == nullptr ||
                decodeMethod == nullptr || clearException(env)) {
            return nullptr;
        }

        auto pipe = static_cast<jobjectArray>(
                env->CallStaticObjectMethod(parcelFdClass, createPipeMethod));
        if (pipe == nullptr || clearException(env)) return nullptr;
        jobject readFd = env->GetObjectArrayElement(pipe, 0);
        jobject writeFd = env->GetObjectArrayElement(pipe, 1);
        const int writerFd = dup(env->CallIntMethod(writeFd, getFdMethod));
        if (writerFd < 0 || clearException(env)) return nullptr;
        env->CallVoidMethod(writeFd, closeFdMethod);
        if (clearException(env)) {
            close(writerFd);
            return nullptr;
        }

        jobject input = env->NewObject(inputClass, inputConstructor, readFd);
        if (input == nullptr || clearException(env)) {
            close(writerFd);
            return nullptr;
        }

        std::thread writer([writerFd, data, size] {
            sigset_t blockedSignals;
            sigemptyset(&blockedSignals);
            sigaddset(&blockedSignals, SIGPIPE);
            pthread_sigmask(SIG_BLOCK, &blockedSignals, nullptr);
            size_t offset = 0;
            while (offset < size) {
                const ssize_t written = write(writerFd, data + offset, size - offset);
                if (written < 0 && errno == EINTR) continue;
                if (written <= 0) break;
                offset += static_cast<size_t>(written);
            }
            close(writerFd);
        });
        jobject bitmap = env->CallStaticObjectMethod(bitmapFactoryClass, decodeMethod, input);
        const bool decodeFailed = clearException(env);
        env->CallVoidMethod(input, closeInputMethod);
        clearException(env);
        writer.join();
        return decodeFailed ? nullptr : bitmap;
    }

}

extern "C" JNIEXPORT jobject JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeOutputBitmap(
        JNIEnv *env, jobject, jlong handle, jboolean output16Bit) {
    const Session *session = fromHandle(handle);
    if (session == nullptr || !session->validOutput()) return nullptr;
    if (session->processed) {
        jobject bitmap = createBitmap(
                env,
                static_cast<uint32_t>(session->outputWidth),
                static_cast<uint32_t>(session->outputHeight),
                output16Bit == JNI_TRUE && session->outputBits > 8);
        if (bitmap == nullptr) return nullptr;
        if (!copyProcessedToBitmap(env, session, bitmap)) {
            recycleBitmap(env, bitmap);
            return nullptr;
        }
        return bitmap;
    }
    if (session->image->type == LIBRAW_IMAGE_JPEG) {
        return decodeJpeg(env, session->image->data, session->image->data_size);
    }
    if (session->image->type != LIBRAW_IMAGE_BITMAP) return nullptr;

    jobject bitmap = createBitmap(
            env,
            session->image->width,
            session->image->height,
            output16Bit == JNI_TRUE && session->image->bits > 8);
    if (bitmap == nullptr) return nullptr;
    if (!copyOutputToBitmap(env, session, bitmap)) {
        recycleBitmap(env, bitmap);
        return nullptr;
    }
    return bitmap;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeOutputOrientation(
        JNIEnv *, jobject, jlong handle) {
    const Session *session = fromHandle(handle);
    return session != nullptr && session->validOutput()
            ? static_cast<jint>(session->outputFlip)
            : 0;
}
