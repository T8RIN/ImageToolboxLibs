/*
 *    Copyright 2018 huangyz0918
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */


/**
 * All the native methods in AndroidWM (https://github.com/huangyz0918/AndroidWM).
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */

#include <jni.h>
#include <android/bitmap.h>
#include <string>
#include <bitset>
#include <sstream>
#include <vector>
#include <cstdint>
#include <android/log.h>

using namespace std;

#define  LOG_TAG    "androidWM-native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGF(...)  __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

bool convolve1D(jdouble *in, jdouble *kernel, jdouble *out, jsize kernelSize, jsize dataSize);

namespace {

    constexpr char kTextPrefix[] = "2323";
    constexpr char kTextSuffix[] = "4545";
    constexpr char kImagePrefix[] = "1212";
    constexpr char kImageSuffix[] = "3434";

    bool lockBitmap(JNIEnv *env, jobject bitmap, AndroidBitmapInfo *info, void **pixels) {
        return bitmap != nullptr
                && AndroidBitmap_getInfo(env, bitmap, info) == ANDROID_BITMAP_RESULT_SUCCESS
                && AndroidBitmap_lockPixels(env, bitmap, pixels) == ANDROID_BITMAP_RESULT_SUCCESS;
    }

    uint32_t readArgb(const AndroidBitmapInfo &info, const void *pixels, uint32_t x, uint32_t y) {
        const auto *row = static_cast<const uint8_t *>(pixels) + y * info.stride;
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            return static_cast<const uint32_t *>(static_cast<const void *>(row))[x];
        }
        if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
            const uint16_t pixel = static_cast<const uint16_t *>(static_cast<const void *>(row))[x];
            const uint32_t red = (pixel >> 11) & 0x1f;
            const uint32_t green = (pixel >> 5) & 0x3f;
            const uint32_t blue = pixel & 0x1f;
            return 0xff000000 | ((red * 255 / 31) << 16) | ((green * 255 / 63)
                    << 8) | (blue * 255 / 31);
        }
        return 0;
    }

    void writeArgb(const AndroidBitmapInfo &info, void *pixels, uint32_t x, uint32_t y, uint32_t color) {
        auto *row = static_cast<uint8_t *>(pixels) + y * info.stride;
        static_cast<uint32_t *>(static_cast<void *>(row))[x] = color;
    }

    uint32_t replaceDigit(uint32_t color, uint32_t shift, uint32_t digit) {
        const uint32_t component = (color >> shift) & 0xff;
        const uint32_t replacement = (component / 10) * 10 + digit;
        return (color & ~(0xffu << shift)) | (replacement << shift);
    }

    char bitAt(const std::string &watermark, size_t index, const char *prefix, const char *suffix) {
        constexpr size_t flagLength = 4;
        if (index < flagLength) {
            return prefix[index];
        }
        index -= flagLength;
        const size_t watermarkBits = watermark.size() * 8;
        if (index < watermarkBits) {
            return ((static_cast<unsigned char>(watermark[index / 8])
                    >> (7 - index % 8)) & 1) ? '1' : '0';
        }
        return suffix[index - watermarkBits];
    }

    jstring extractWatermark(JNIEnv *env, jobject bitmap, bool isImage) {
        AndroidBitmapInfo info{};
        void *pixels = nullptr;
        if (!lockBitmap(env, bitmap, &info, &pixels)) {
            return nullptr;
        }
        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888
                && info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
            AndroidBitmap_unlockPixels(env, bitmap);
            return nullptr;
        }

        const char *prefix = isImage ? kImagePrefix : kTextPrefix;
        const char *suffix = isImage ? kImageSuffix : kTextSuffix;
        size_t prefixMatched = 0;
        size_t suffixMatched = 0;
        bool collecting = false;
        uint8_t byte = 0;
        int bitCount = 0;
        std::string result;
        bool found = false;
        const uint32_t shifts[] = {24, 16, 8, 0};

        for (uint32_t y = 0; y < info.height && !found; ++y) {
            for (uint32_t x = 0; x < info.width && !found; ++x) {
                const uint32_t color = readArgb(info, pixels, x, y);
                for (uint32_t shift: shifts) {
                    const char digit = static_cast<char>(((color >> shift) & 0xff) % 10 + '0');
                    if (!collecting) {
                        prefixMatched = digit == prefix[prefixMatched] ? prefixMatched + 1 : (digit == prefix[0] ? 1 : 0);
                        if (prefixMatched == 4) {
                            collecting = true;
                            suffixMatched = 0;
                        }
                        continue;
                    }
                    if (digit == suffix[suffixMatched]) {
                        if (++suffixMatched == 4) {
                            found = bitCount == 0;
                            break;
                        }
                        continue;
                    }
                    if (suffixMatched != 0 || (digit != '0' && digit != '1')) {
                        collecting = false;
                        prefixMatched = digit == prefix[0] ? 1 : 0;
                        suffixMatched = 0;
                        continue;
                    }
                    byte = static_cast<uint8_t>((byte << 1) | (digit - '0'));
                    if (++bitCount == 8) {
                        result.push_back(static_cast<char>(byte));
                        byte = 0;
                        bitCount = 0;
                    }
                }
            }
        }

        AndroidBitmap_unlockPixels(env, bitmap);
        return found ? env->NewStringUTF(result.c_str()) : nullptr;
    }

}

/**
 * native method for calculating the Convolution 1D.
 */
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_watermark_androidwm_utils_StringUtils_calConv1D(JNIEnv *env, jobject instance,
                                                         jdoubleArray inputArray1_,
                                                         jdoubleArray inputArray2_) {
    jdouble *inputArray1 = env->GetDoubleArrayElements(inputArray1_, NULL);
    jdouble *inputArray2 = env->GetDoubleArrayElements(inputArray2_, NULL);

    jsize size1 = env->GetArrayLength(inputArray1_);
    jsize size2 = env->GetArrayLength(inputArray2_);
    jsize outSize = size1 + size2 - 1;
    jsize kernelSize;

    if (size1 > size2) {
        kernelSize = size1;
    } else {
        kernelSize = size2;
    }

    jdoubleArray outputArray = env->NewDoubleArray(outSize);
    jdouble *outputValues = env->GetDoubleArrayElements(outputArray, NULL);
    convolve1D(inputArray1, inputArray2, outputValues, kernelSize, outSize);

    env->ReleaseDoubleArrayElements(inputArray1_, inputArray1, 0);
    env->ReleaseDoubleArrayElements(inputArray2_, inputArray2, 0);
    env->ReleaseDoubleArrayElements(outputArray, outputValues, 0);
    return outputArray;
}

bool convolve1D(jdouble *in, jdouble *kernel, jdouble *out, jsize kernelSize, jsize dataSize) {
    int i, j, k;
    if (!in || !out || !kernel) return false;
    if (dataSize <= 0 || kernelSize <= 0) return false;

    for (i = kernelSize - 1; i < dataSize; ++i) {
        out[i] = 0;

        for (j = i, k = 0; k < kernelSize; --j, ++k)
            out[i] += in[j] * kernel[k];
    }

    for (i = 0; i < kernelSize - 1; ++i) {
        out[i] = 0;

        for (j = i, k = 0; j >= 0; --j, ++k)
            out[i] += in[j] * kernel[k];
    }

    return true;
}


/*
 * Convent the Jstring to the C++ style string (std::string).
 * */
string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes,
                                                                       env->NewStringUTF("UTF-8"));
    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte *pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    string ret = string((char *) pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}


/**
 * Converting a {@link String} text into a binary text.
 * <p>
 * This is the native version.
 */
extern "C"
JNIEXPORT jstring JNICALL
Java_com_watermark_androidwm_utils_StringUtils_stringToBinary(JNIEnv *env, jobject instance,
                                                              jstring inputText_) {
    const char *inputText = env->GetStringUTFChars(inputText_, 0);
    if (inputText == NULL) {
        return NULL;
    }

    string input = jstring2string(env, inputText_);
    string result;

    for (int i = 0; i < input.length(); i++) {
        string temp = bitset<8>(input[i]).to_string();
        result.append(temp);
    }

    env->ReleaseStringUTFChars(inputText_, inputText);

    return env->NewStringUTF(result.c_str());
}


/**
 * String to integer array.
 * <p>
 * This is the native version.
 */
extern "C"
JNIEXPORT jintArray JNICALL
Java_com_watermark_androidwm_utils_StringUtils_stringToIntArray(JNIEnv *env, jobject instance,
                                                                jstring inputString_) {
    const string input = jstring2string(env, inputString_);
    jintArray resultArray = env->NewIntArray(static_cast<jsize>(input.size()));
    if (resultArray == nullptr) {
        return nullptr;
    }
    vector<jint> result(input.size());
    for (size_t i = 0; i < input.size(); ++i) {
        result[i] = input[i] - '0';
    }
    env->SetIntArrayRegion(resultArray, 0, static_cast<jsize>(result.size()), result.data());

    return resultArray;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_watermark_androidwm_utils_StringUtils_embedLsbWatermark(JNIEnv *env, jobject instance,
        jobject source, jobject destination,
        jstring watermark_, jboolean isImage) {
    if (watermark_ == nullptr) {
        return JNI_FALSE;
    }
    const string watermark = jstring2string(env, watermark_);
    if (watermark.empty() || watermark.size() > (SIZE_MAX - 8) / 8) {
        return JNI_FALSE;
    }
    const size_t patternSize = watermark.size() * 8 + 8;

    AndroidBitmapInfo sourceInfo{};
    AndroidBitmapInfo destinationInfo{};
    void *sourcePixels = nullptr;
    void *destinationPixels = nullptr;
    if (!lockBitmap(env, source, &sourceInfo, &sourcePixels)) {
        return JNI_FALSE;
    }
    const bool destinationLocked = lockBitmap(env, destination, &destinationInfo, &destinationPixels);
    if (!destinationLocked) {
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }
    if ((sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888
            && sourceInfo.format != ANDROID_BITMAP_FORMAT_RGB_565)
            || destinationInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888
            || sourceInfo.width != destinationInfo.width || sourceInfo.height != destinationInfo.height) {
        AndroidBitmap_unlockPixels(env, destination);
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }

    const uint64_t channelCount = static_cast<uint64_t>(sourceInfo.width) * sourceInfo.height * 4;
    if (patternSize > channelCount) {
        AndroidBitmap_unlockPixels(env, destination);
        AndroidBitmap_unlockPixels(env, source);
        return JNI_FALSE;
    }
    const char *prefix = isImage ? kImagePrefix : kTextPrefix;
    const char *suffix = isImage ? kImageSuffix : kTextSuffix;
    const uint32_t shifts[] = {24, 16, 8, 0};
    size_t patternIndex = 0;
    for (uint32_t y = 0; y < sourceInfo.height; ++y) {
        for (uint32_t x = 0; x < sourceInfo.width; ++x) {
            uint32_t color = readArgb(sourceInfo, sourcePixels, x, y);
            for (uint32_t shift: shifts) {
                color = replaceDigit(color, shift, static_cast<uint32_t>(bitAt(watermark, patternIndex, prefix, suffix) - '0'));
                if (++patternIndex == patternSize) {
                    patternIndex = 0;
                }
            }
            writeArgb(destinationInfo, destinationPixels, x, y, color);
        }
    }
    AndroidBitmap_unlockPixels(env, destination);
    AndroidBitmap_unlockPixels(env, source);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_watermark_androidwm_utils_StringUtils_extractLsbWatermark(JNIEnv *env, jobject instance,
        jobject bitmap, jboolean isImage) {
    return extractWatermark(env, bitmap, isImage == JNI_TRUE);
}


/**
 * Converting a binary string to a ASCII string.
 */
extern "C"
JNIEXPORT jstring JNICALL
Java_com_watermark_androidwm_utils_StringUtils_binaryToString(JNIEnv *env, jobject instance,
                                                              jstring inputText_) {
    const char *inputText = env->GetStringUTFChars(inputText_, 0);
    string inputString = jstring2string(env, inputText_);

    stringstream stream(inputString);
    string output;

    while (stream.good()) {
        bitset<8> bits;
        stream >> bits;
        char c = char(bits.to_ulong());
        output += c;
    }

    jstring outputString = env->NewStringUTF(output.c_str());
    env->ReleaseStringUTFChars(inputText_, inputText);
    return outputString;
}

/**
 * Replace the wrong rgb number in a form of binary,
 * the only case is 0 - 1 = 9, so, we need to replace
 * all nines to zero.
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_watermark_androidwm_utils_StringUtils_replaceNines(JNIEnv *env, jobject instance,
                                                            jintArray inputArray_) {
    jint *inputArray = env->GetIntArrayElements(inputArray_, NULL);
    jsize size = env->GetArrayLength(inputArray_);

    for (int i = 0; i < size; i++) {
        if (inputArray[i] == 9) {
            inputArray[i] = 0;
        }
    }

    env->ReleaseIntArrayElements(inputArray_, inputArray, 0);
}

/**
 * Int array to string.
 */
extern "C"
JNIEXPORT jstring JNICALL
Java_com_watermark_androidwm_utils_StringUtils_intArrayToString(JNIEnv *env, jobject instance,
                                                                jintArray inputArray_) {
    jint *inputArray = env->GetIntArrayElements(inputArray_, NULL);
    jsize size = env->GetArrayLength(inputArray_);
    ostringstream oss("");

    for (int i = 0; i < size; ++i) {
        oss << inputArray[i];
    }

    string output = oss.str();

    env->ReleaseIntArrayElements(inputArray_, inputArray, 0);
    return env->NewStringUTF(output.c_str());
}
