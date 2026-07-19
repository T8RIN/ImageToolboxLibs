#include <jni.h>
#include <sys/stat.h>

#include <algorithm>
#include <cstdint>
#include <cstring>
#include <limits>
#include <memory>
#include <new>

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
        }

        bool validOutput() const {
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

    jintArray makeIntArray(JNIEnv *env, const jint *values, jsize size) {
        jintArray result = env->NewIntArray(size);
        if (result != nullptr) {
            env->SetIntArrayRegion(result, 0, size, values);
            if (env->ExceptionCheck()) {
                env->ExceptionClear();
                return nullptr;
            }
        }
        return result;
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

extern "C" JNIEXPORT jintArray JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeInfo(JNIEnv *env, jobject, jlong handle) {
    const Session *session = fromHandle(handle);
    if (session == nullptr || session->raw == nullptr) return nullptr;
    const jint values[] = {
            static_cast<jint>(session->raw->sizes.width),
            static_cast<jint>(session->raw->sizes.height),
            static_cast<jint>(session->raw->sizes.flip),
            static_cast<jint>(session->raw->thumbs_list.thumbcount),
            static_cast<jint>(session->raw->idata.dng_version)
    };
    return makeIntArray(env, values, 5);
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
    if (libraw_dcraw_process(session->raw) != LIBRAW_SUCCESS) return JNI_FALSE;
    int error = LIBRAW_SUCCESS;
    session->clearOutput();
    session->image = libraw_dcraw_make_mem_image(session->raw, &error);
    session->outputFlip = session->inputFlip;
    if (error != LIBRAW_SUCCESS || !session->validOutput()) {
        session->clearOutput();
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeOutputInfo(JNIEnv *env, jobject, jlong handle) {
    const Session *session = fromHandle(handle);
    if (session == nullptr || !session->validOutput()) return nullptr;
    const jint values[] = {
            static_cast<jint>(session->image->type),
            static_cast<jint>(session->image->width),
            static_cast<jint>(session->image->height),
            static_cast<jint>(session->image->colors),
            static_cast<jint>(session->image->bits),
            static_cast<jint>(session->image->data_size),
            static_cast<jint>(session->outputFlip)
    };
    return makeIntArray(env, values, 7);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_t8rin_raw_1coder_LibRawBridge_nativeOutputData(JNIEnv *env, jobject, jlong handle) {
    const Session *session = fromHandle(handle);
    if (session == nullptr || !session->validOutput() ||
            session->image->data_size > static_cast<unsigned>(std::numeric_limits<jsize>::max())) {
        return nullptr;
    }
    const jsize size = static_cast<jsize>(session->image->data_size);
    jbyteArray result = env->NewByteArray(size);
    if (result == nullptr) return nullptr;
    env->SetByteArrayRegion(result, 0, size,
            reinterpret_cast<const jbyte *>(session->image->data));
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        return nullptr;
    }
    return result;
}
