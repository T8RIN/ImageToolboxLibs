#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <png.h>
#include <zlib.h>

#include <algorithm>
#include <cmath>
#include <cstdio>
#include <cstring>
#include <string>
#include <vector>

using namespace std;

#define cimg_display 0
#define cimg_use_png
#define cimg_use_openmp

#include "CImg.h"

using namespace cimg_library;

#define LOG_TAG "advanced cropper JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define SAVE_FORMAT_JPEG 0
#define SAVE_FORMAT_PNG  1

namespace {

    struct PointD {
        double x;
        double y;
    };

    struct SizeI {
        int width;
        int height;
    };

    struct AffineTransform {
        PointD origin;
        PointD stepX;
        PointD stepY;
    };

    static int normalize_degrees(int degrees) {
        return (degrees % 360 + 360) % 360;
    }

    static SizeI rotated_size(SizeI source, int degrees) {
        const int normalized = normalize_degrees(degrees);
        if (normalized == 90 || normalized == 270) {
            return {source.height, source.width};
        }
        if (normalized == 0 || normalized == 180) {
            return source;
        }

        const double radians = normalized * cimg::PI / 180.0;
        const double cosine = std::cos(radians);
        const double sine = std::sin(radians);
        return {
                std::max(1, static_cast<int>(std::round(
                        1.0 + std::abs((source.width - 1) * cosine) +
                                std::abs((source.height - 1) * sine)))),
                std::max(1, static_cast<int>(std::round(
                        1.0 + std::abs((source.width - 1) * sine) +
                                std::abs((source.height - 1) * cosine))))
        };
    }

    static PointD inverse_rotate(PointD point, SizeI source, SizeI destination, int degrees) {
        const double radians = normalize_degrees(degrees) * cimg::PI / 180.0;
        const double cosine = std::cos(radians);
        const double sine = std::sin(radians);
        const double sourceCenterX = 0.5 * (source.width - 1);
        const double sourceCenterY = 0.5 * (source.height - 1);
        const double destinationCenterX = 0.5 * (destination.width - 1);
        const double destinationCenterY = 0.5 * (destination.height - 1);
        const double x = point.x - destinationCenterX;
        const double y = point.y - destinationCenterY;
        return {
                sourceCenterX + x * cosine + y * sine,
                sourceCenterY - x * sine + y * cosine
        };
    }

    class CropGeometry {
    public:
        CropGeometry(int sourceWidth, int sourceHeight,
                float fineAngle, float resizeScale,
                int sourceRotationDegrees, bool flipHorizontally,
                int exifDegrees, int exifTranslation)
                : sourceSize{sourceWidth, sourceHeight},
                  exifRotation(exifDegrees),
                  sourceRotation(sourceRotationDegrees),
                  flip(flipHorizontally),
                  exifFlip(exifTranslation != 1),
                  angle(std::abs(fineAngle) < 0.01f ? 0.0 : fineAngle),
                  scale(resizeScale > 0.0f ? resizeScale : 1.0f) {
            orientedSize = rotated_size(sourceSize, exifRotation);
            rotatedSize = rotated_size(orientedSize, sourceRotation);
            resizedSize = {
                    std::max(1, static_cast<int>(rotatedSize.width * scale)),
                    std::max(1, static_cast<int>(rotatedSize.height * scale))
            };

            const double radians = angle * cimg::PI / 180.0;
            fineCosine = std::cos(radians);
            fineSine = std::sin(radians);
            fineBoundsHalfWidth = 0.5 * (
                    std::abs(resizedSize.width * fineCosine) +
                            std::abs(resizedSize.height * fineSine));
            fineBoundsHalfHeight = 0.5 * (
                    std::abs(resizedSize.width * fineSine) +
                            std::abs(resizedSize.height * fineCosine));
        }

        PointD mapToSource(double x, double y) const {
            const double fineX = x - fineBoundsHalfWidth;
            const double fineY = y - fineBoundsHalfHeight;
            PointD point{
                    0.5 * resizedSize.width + fineX * fineCosine + fineY * fineSine,
                    0.5 * resizedSize.height - fineX * fineSine + fineY * fineCosine
            };

            if (resizedSize.width != rotatedSize.width) {
                point.x *= rotatedSize.width / static_cast<double>(resizedSize.width);
            }
            if (resizedSize.height != rotatedSize.height) {
                point.y *= rotatedSize.height / static_cast<double>(resizedSize.height);
            }

            point = inverse_rotate(point, orientedSize, rotatedSize, sourceRotation);
            if (flip) {
                point.x = orientedSize.width - 1.0 - point.x;
            }
            if (exifFlip) {
                point.x = orientedSize.width - 1.0 - point.x;
            }
            return inverse_rotate(point, sourceSize, orientedSize, exifRotation);
        }

        AffineTransform affineFor(int left, int top) const {
            const PointD origin = mapToSource(left, top);
            const PointD nextX = mapToSource(left + 1.0, top);
            const PointD nextY = mapToSource(left, top + 1.0);
            return {
                    origin,
                    {nextX.x - origin.x, nextX.y - origin.y},
                    {nextY.x - origin.x, nextY.y - origin.y}
            };
        }

    private:
        SizeI sourceSize{};
        SizeI orientedSize{};
        SizeI rotatedSize{};
        SizeI resizedSize{};
        int exifRotation;
        int sourceRotation;
        bool flip;
        bool exifFlip;
        double angle;
        double scale;
        double fineCosine{};
        double fineSine{};
        double fineBoundsHalfWidth{};
        double fineBoundsHalfHeight{};
    };

    static bool calculate_source_region(const CropGeometry &geometry,
            int sourceWidth, int sourceHeight,
            int left, int top, int width, int height,
            int result[4]) {
        if (sourceWidth <= 0 || sourceHeight <= 0 || width <= 0 || height <= 0) {
            return false;
        }

        const double right = left + width - 1.0;
        const double bottom = top + height - 1.0;
        const PointD corners[] = {
                geometry.mapToSource(left, top),
                geometry.mapToSource(right, top),
                geometry.mapToSource(right, bottom),
                geometry.mapToSource(left, bottom)
        };
        double minX = corners[0].x;
        double maxX = corners[0].x;
        double minY = corners[0].y;
        double maxY = corners[0].y;
        for (const PointD &corner: corners) {
            minX = std::min(minX, corner.x);
            maxX = std::max(maxX, corner.x);
            minY = std::min(minY, corner.y);
            maxY = std::max(maxY, corner.y);
        }

        result[0] = std::max(0, static_cast<int>(std::floor(minX)) - 2);
        result[1] = std::max(0, static_cast<int>(std::floor(minY)) - 2);
        result[2] = std::min(sourceWidth, static_cast<int>(std::ceil(maxX)) + 3);
        result[3] = std::min(sourceHeight, static_cast<int>(std::ceil(maxY)) + 3);
        return result[2] > result[0] && result[3] > result[1];
    }

    class LockedBitmap {
    public:
        LockedBitmap(JNIEnv *environment, jobject sourceBitmap)
                : env(environment), bitmap(sourceBitmap) {
            if (AndroidBitmap_getInfo(env, bitmap, &info) == ANDROID_BITMAP_RESULT_SUCCESS &&
                    info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 &&
                    AndroidBitmap_lockPixels(env, bitmap, &pixels) == ANDROID_BITMAP_RESULT_SUCCESS) {
                locked = true;
            }
        }

        ~LockedBitmap() {
            if (locked) {
                AndroidBitmap_unlockPixels(env, bitmap);
            }
        }

        bool isValid() const {
            return locked;
        }

        const AndroidBitmapInfo &getInfo() const {
            return info;
        }

        const unsigned char *getPixels() const {
            return static_cast<const unsigned char *>(pixels);
        }

    private:
        JNIEnv *env;
        jobject bitmap;
        AndroidBitmapInfo info{};
        void *pixels{};
        bool locked = false;
    };

    struct PixelD {
        double red;
        double green;
        double blue;
        double alpha;
    };

    static PixelD read_pixel(const unsigned char *pixels, const AndroidBitmapInfo &info,
            int sourceWidth, int sourceHeight,
            int regionLeft, int regionTop, int x, int y) {
        if (x < 0 || y < 0 || x >= sourceWidth || y >= sourceHeight) {
            return {};
        }
        const int localX = x - regionLeft;
        const int localY = y - regionTop;
        if (localX < 0 || localY < 0 ||
                localX >= static_cast<int>(info.width) || localY >= static_cast<int>(info.height)) {
            return {};
        }

        const unsigned char *pixel = pixels + localY * info.stride + localX * 4;
        return {
                static_cast<double>(pixel[0]),
                static_cast<double>(pixel[1]),
                static_cast<double>(pixel[2]),
                static_cast<double>(pixel[3])
        };
    }

    static PixelD sample_bilinear(const unsigned char *pixels, const AndroidBitmapInfo &info,
            int sourceWidth, int sourceHeight,
            int regionLeft, int regionTop, double x, double y) {
        const int x0 = static_cast<int>(std::floor(x));
        const int y0 = static_cast<int>(std::floor(y));
        const double fractionX = x - x0;
        const double fractionY = y - y0;
        const PixelD p00 = read_pixel(
                pixels, info, sourceWidth, sourceHeight, regionLeft, regionTop, x0, y0);
        const PixelD p10 = read_pixel(
                pixels, info, sourceWidth, sourceHeight, regionLeft, regionTop, x0 + 1, y0);
        const PixelD p01 = read_pixel(
                pixels, info, sourceWidth, sourceHeight, regionLeft, regionTop, x0, y0 + 1);
        const PixelD p11 = read_pixel(
                pixels, info, sourceWidth, sourceHeight, regionLeft, regionTop, x0 + 1, y0 + 1);
        const double topWeight = 1.0 - fractionY;
        const double leftWeight = 1.0 - fractionX;
        return {
                (p00.red * leftWeight + p10.red * fractionX) * topWeight +
                        (p01.red * leftWeight + p11.red * fractionX) * fractionY,
                (p00.green * leftWeight + p10.green * fractionX) * topWeight +
                        (p01.green * leftWeight + p11.green * fractionX) * fractionY,
                (p00.blue * leftWeight + p10.blue * fractionX) * topWeight +
                        (p01.blue * leftWeight + p11.blue * fractionX) * fractionY,
                (p00.alpha * leftWeight + p10.alpha * fractionX) * topWeight +
                        (p01.alpha * leftWeight + p11.alpha * fractionX) * fractionY
        };
    }

    static unsigned char to_byte(double value) {
        return static_cast<unsigned char>(std::clamp(std::lround(value), 0L, 255L));
    }

    static void write_pixel(unsigned char *destination, PixelD pixel, bool premultiplied) {
        unsigned char alpha = to_byte(pixel.alpha);
        unsigned char red = to_byte(pixel.red);
        unsigned char green = to_byte(pixel.green);
        unsigned char blue = to_byte(pixel.blue);
        if (premultiplied && alpha > 0 && alpha < 255) {
            red = static_cast<unsigned char>(std::min(255, (red * 255 + alpha / 2) / alpha));
            green = static_cast<unsigned char>(std::min(255, (green * 255 + alpha / 2) / alpha));
            blue = static_cast<unsigned char>(std::min(255, (blue * 255 + alpha / 2) / alpha));
        }
        destination[0] = red;
        destination[1] = green;
        destination[2] = blue;
        destination[3] = alpha;
    }

    static bool is_integer_affine(const AffineTransform &transform,
            int outputWidth, int outputHeight) {
        constexpr double epsilon = 0.0001;
        const double maxX = std::max(0, outputWidth - 1);
        const double maxY = std::max(0, outputHeight - 1);
        const double accumulatedErrorX =
                std::abs(transform.origin.x - std::round(transform.origin.x)) +
                        maxX * std::abs(transform.stepX.x - std::round(transform.stepX.x)) +
                        maxY * std::abs(transform.stepY.x - std::round(transform.stepY.x));
        const double accumulatedErrorY =
                std::abs(transform.origin.y - std::round(transform.origin.y)) +
                        maxX * std::abs(transform.stepX.y - std::round(transform.stepX.y)) +
                        maxY * std::abs(transform.stepY.y - std::round(transform.stepY.y));
        return accumulatedErrorX < epsilon && accumulatedErrorY < epsilon;
    }

    static bool is_direct_copy(const AffineTransform &transform, bool premultiplied,
            int outputWidth, int outputHeight) {
        constexpr double epsilon = 0.0001;
        return !premultiplied &&
                is_integer_affine(transform, outputWidth, outputHeight) &&
                (outputWidth - 1.0) * std::abs(transform.stepX.x - 1.0) < epsilon &&
                (outputWidth - 1.0) * std::abs(transform.stepX.y) < epsilon &&
                (outputHeight - 1.0) * std::abs(transform.stepY.x) < epsilon &&
                (outputHeight - 1.0) * std::abs(transform.stepY.y - 1.0) < epsilon;
    }

    class CancellationSignal {
    public:
        CancellationSignal(JNIEnv *environment, jobject value)
                : env(environment), signal(value) {
            if (signal == nullptr) return;
            jclass signalClass = env->GetObjectClass(signal);
            if (signalClass != nullptr) {
                getMethod = env->GetMethodID(signalClass, "get", "()Z");
                env->DeleteLocalRef(signalClass);
            }
        }

        bool isCancelled() const {
            if (signal == nullptr) return false;
            if (getMethod == nullptr || env->ExceptionCheck()) return true;
            return env->CallBooleanMethod(signal, getMethod) == JNI_TRUE ||
                    env->ExceptionCheck();
        }

    private:
        JNIEnv *env;
        jobject signal;
        jmethodID getMethod{};
    };

    static bool write_transformed_png(const char *outputPath,
            const unsigned char *pixels, const AndroidBitmapInfo &info,
            int sourceWidth, int sourceHeight,
            int regionLeft, int regionTop,
            int outputWidth, int outputHeight,
            const AffineTransform &transform,
            const CancellationSignal &cancellationSignal) {
        if (cancellationSignal.isCancelled()) return false;
        std::vector<unsigned char> row(static_cast<size_t>(outputWidth) * 4);
        FILE *file = std::fopen(outputPath, "wb");
        if (file == nullptr) return false;

        png_structp png = png_create_write_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
        if (png == nullptr) {
            std::fclose(file);
            return false;
        }
        png_infop infoPtr = png_create_info_struct(png);
        if (infoPtr == nullptr) {
            png_destroy_write_struct(&png, nullptr);
            std::fclose(file);
            return false;
        }
        if (setjmp(png_jmpbuf(png))) {
            png_destroy_write_struct(&png, &infoPtr);
            std::fclose(file);
            std::remove(outputPath);
            return false;
        }

        png_init_io(png, file);
        png_set_compression_level(png, Z_BEST_SPEED);
        png_set_filter(png, PNG_FILTER_TYPE_BASE, PNG_FILTER_SUB);
        png_set_IHDR(
                png, infoPtr,
                outputWidth, outputHeight, 8,
                PNG_COLOR_TYPE_RGBA, PNG_INTERLACE_NONE,
                PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);
        png_write_info(png, infoPtr);

        const bool integerTransform = is_integer_affine(transform, outputWidth, outputHeight);
        const bool premultiplied =
                (info.flags & ANDROID_BITMAP_FLAGS_ALPHA_MASK) == ANDROID_BITMAP_FLAGS_ALPHA_PREMUL;
        const bool directCopy = is_direct_copy(
                transform, premultiplied, outputWidth, outputHeight);
        for (int y = 0; y < outputHeight; ++y) {
            if ((y & 7) == 0 && cancellationSignal.isCancelled()) {
                png_destroy_write_struct(&png, &infoPtr);
                std::fclose(file);
                std::remove(outputPath);
                return false;
            }
            double sourceX = transform.origin.x + y * transform.stepY.x;
            double sourceY = transform.origin.y + y * transform.stepY.y;
            if (directCopy) {
                const int localX = static_cast<int>(std::lround(sourceX)) - regionLeft;
                const int localY = static_cast<int>(std::lround(sourceY)) - regionTop;
                if (localX >= 0 && localY >= 0 &&
                        localX + outputWidth <= static_cast<int>(info.width) &&
                        localY < static_cast<int>(info.height)) {
                    std::memcpy(
                            row.data(), pixels + localY * info.stride + localX * 4,
                            static_cast<size_t>(outputWidth) * 4);
                    png_write_row(png, row.data());
                    continue;
                }
            }
            unsigned char *destination = row.data();
            for (int x = 0; x < outputWidth; ++x) {
                PixelD pixel;
                if (integerTransform) {
                    pixel = read_pixel(
                            pixels, info, sourceWidth, sourceHeight, regionLeft, regionTop,
                            static_cast<int>(std::lround(sourceX)),
                            static_cast<int>(std::lround(sourceY)));
                } else {
                    pixel = sample_bilinear(
                            pixels, info, sourceWidth, sourceHeight, regionLeft, regionTop,
                            sourceX, sourceY);
                }
                write_pixel(destination, pixel, premultiplied);
                destination += 4;
                sourceX += transform.stepX.x;
                sourceY += transform.stepX.y;
            }
            png_write_row(png, row.data());
        }
        if (cancellationSignal.isCancelled()) {
            png_destroy_write_struct(&png, &infoPtr);
            std::fclose(file);
            std::remove(outputPath);
            return false;
        }
        png_write_end(png, infoPtr);
        png_destroy_write_struct(&png, &infoPtr);
        bool success = std::fflush(file) == 0 && std::ferror(file) == 0;
        if (std::fclose(file) != 0) {
            success = false;
        }
        if (!success) {
            std::remove(outputPath);
        }
        return success;
    }

    static void throw_java(JNIEnv *env, const char *className, const char *message) {
        if (env->ExceptionCheck()) return;
        jclass exceptionClass = env->FindClass(className);
        if (exceptionClass != nullptr) {
            env->ThrowNew(exceptionClass, message);
        }
    }

    class UtfChars {
    public:
        UtfChars(JNIEnv *environment, jstring value)
                : env(environment), stringValue(value), characters(
                value != nullptr ? env->GetStringUTFChars(value, nullptr) : nullptr) {
        }

        ~UtfChars() {
            if (characters != nullptr) {
                env->ReleaseStringUTFChars(stringValue, characters);
            }
        }

        const char *get() const {
            return characters;
        }

    private:
        JNIEnv *env;
        jstring stringValue;
        const char *characters;
    };

    static string to_string(JNIEnv *env, jstring value) {
        UtfChars characters(env, value);
        return characters.get() != nullptr ? string(characters.get()) : string();
    }

    static void save_image(CImg<unsigned char> &image, const char *outputPath,
            jint format, jint quality) {
    if (format == SAVE_FORMAT_JPEG) {
        image.save_jpeg(outputPath, quality);
    } else if (format == SAVE_FORMAT_PNG) {
        image.save_png(outputPath, 0);
    } else {
        image.save(outputPath);
    }
}

    static void apply_exif(CImg<unsigned char> &image, jint exifDegrees, jint exifTranslation) {
    if (exifDegrees != 0) {
        image.rotate(exifDegrees);
    }
    if (exifTranslation != 1) {
        image.mirror("x");
    }
    }

} // namespace

extern "C" JNIEXPORT jintArray JNICALL
Java_com_t8rin_crop_advanced_task_BitmapCropTask_calculateSourceRegion(
        JNIEnv *env, jclass,
        jint sourceWidth, jint sourceHeight,
        jint left, jint top, jint width, jint height,
        jfloat angle, jfloat resizeScale,
        jint sourceRotationDegrees, jboolean flipHorizontally,
        jint exifDegrees, jint exifTranslation) {
    const CropGeometry geometry(
            sourceWidth, sourceHeight, angle, resizeScale,
            sourceRotationDegrees, flipHorizontally,
            exifDegrees, exifTranslation);
    int region[4];
    if (!calculate_source_region(
            geometry, sourceWidth, sourceHeight, left, top, width, height, region)) {
        return nullptr;
    }

    jintArray result = env->NewIntArray(4);
    if (result != nullptr) {
        env->SetIntArrayRegion(result, 0, 4, region);
    }
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_crop_advanced_task_BitmapCropTask_cropBitmapRegionNative(
        JNIEnv *env, jclass,
        jobject bitmap, jstring pathResult,
        jint sourceWidth, jint sourceHeight,
        jint regionLeft, jint regionTop,
        jint left, jint top, jint width, jint height,
        jfloat angle, jfloat resizeScale,
        jint sourceRotationDegrees, jboolean flipHorizontally,
        jint exifDegrees, jint exifTranslation,
        jobject cancellationSignal) {
    if (bitmap == nullptr || pathResult == nullptr || width <= 0 || height <= 0) {
        return false;
    }

    try {
        const string outputPath = to_string(env, pathResult);
        if (outputPath.empty()) return false;

        LockedBitmap source(env, bitmap);
        if (!source.isValid()) {
            throw_java(env, "java/io/IOException", "Unable to lock crop source bitmap");
            return false;
        }

        const CropGeometry geometry(
                sourceWidth, sourceHeight, angle, resizeScale,
                sourceRotationDegrees, flipHorizontally,
                exifDegrees, exifTranslation);
        const CancellationSignal cancellation(env, cancellationSignal);
        return write_transformed_png(
                outputPath.c_str(), source.getPixels(), source.getInfo(),
                sourceWidth, sourceHeight, regionLeft, regionTop,
                width, height, geometry.affineFor(left, top), cancellation);
    } catch (const std::bad_alloc &exception) {
        throw_java(env, "java/lang/OutOfMemoryError", exception.what());
    } catch (const std::exception &exception) {
        throw_java(env, "java/io/IOException", exception.what());
    }
    return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_crop_advanced_task_BitmapCropTask_cropCImgTransformed(
        JNIEnv *env, jclass,
        jstring pathSource, jstring pathResult,
        jint left, jint top, jint width, jint height, jfloat angle, jfloat resizeScale,
        jint sourceRotationDegrees, jboolean flipHorizontally,
        jint format, jint quality,
        jint exifDegrees, jint exifTranslation) {
    LOGD("Crop image with CImg");

    try {
        const string sourcePath = to_string(env, pathSource);
        const string outputPath = to_string(env, pathResult);
        if (sourcePath.empty() || outputPath.empty()) return false;

        CImg<unsigned char> image(sourcePath.c_str());
        apply_exif(image, exifDegrees, exifTranslation);
        if (flipHorizontally) {
            image.mirror("x");
        }
        if (sourceRotationDegrees != 0) {
            image.rotate(sourceRotationDegrees);
        }

        if (resizeScale != 1) {
            image.resize(
                    image.width() * resizeScale, image.height() * resizeScale,
                    -100, -100, 1, 0, 0, 0, 0, 0);
        }

        const int x1 = left + width - 1;
        const int y1 = top + height - 1;
        if (std::abs(angle) < 0.01f) {
            image.crop(left, top, x1, y1, 0);
        } else {
            CImg<float> warp(width, height, 1, 2);
            const float radians = angle * cimg::PI / 180;
            const float cosine = std::cos(radians);
            const float sine = std::sin(radians);
            const float ux = cimg::abs(image.width() * cosine);
            const float uy = cimg::abs(image.width() * sine);
            const float vx = cimg::abs(image.height() * sine);
            const float vy = cimg::abs(image.height() * cosine);
            const float sourceHalfWidth = 0.5f * image.width();
            const float sourceHalfHeight = 0.5f * image.height();
            const float boundsHalfWidth = 0.5f * (ux + vx);
            const float boundsHalfHeight = 0.5f * (uy + vy);

            cimg_forXY(warp, x, y) {
                    const float outputX = x + left - boundsHalfWidth;
                    const float outputY = y + top - boundsHalfHeight;
                    warp(x, y, 0) = sourceHalfWidth + outputX * cosine + outputY * sine;
                    warp(x, y, 1) = sourceHalfHeight - outputX * sine + outputY * cosine;
                }
            image = image.get_warp(warp, 0, 1, 0);
        }

        save_image(image, outputPath.c_str(), format, quality);
        return true;
    } catch (const CImgInstanceException &exception) {
        throw_java(env, "java/lang/OutOfMemoryError", exception.what());
    } catch (const CImgIOException &exception) {
        throw_java(env, "java/io/IOException", exception.what());
    } catch (const std::bad_alloc &exception) {
        throw_java(env, "java/lang/OutOfMemoryError", exception.what());
    } catch (const std::exception &exception) {
        throw_java(env, "java/io/IOException", exception.what());
    }
    return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_crop_advanced_task_BitmapCropTask_transformCImg(
        JNIEnv *env, jclass,
        jstring pathSource, jstring pathResult,
        jint rotateDegrees, jboolean flipHorizontally,
        jint format, jint quality,
        jint exifDegrees, jint exifTranslation) {
    LOGD("Transform image with CImg");

    try {
        const string sourcePath = to_string(env, pathSource);
        const string outputPath = to_string(env, pathResult);
        if (sourcePath.empty() || outputPath.empty()) return false;

        CImg<unsigned char> image(sourcePath.c_str());
        apply_exif(image, exifDegrees, exifTranslation);
        if (flipHorizontally) {
            image.mirror("x");
        }
        if (rotateDegrees != 0) {
            image.rotate(rotateDegrees);
        }
        save_image(image, outputPath.c_str(), format, quality);
        return true;
    } catch (const CImgInstanceException &exception) {
        throw_java(env, "java/lang/OutOfMemoryError", exception.what());
    } catch (const CImgIOException &exception) {
        throw_java(env, "java/io/IOException", exception.what());
    } catch (const std::bad_alloc &exception) {
        throw_java(env, "java/lang/OutOfMemoryError", exception.what());
    } catch (const std::exception &exception) {
        throw_java(env, "java/io/IOException", exception.what());
    }
    return false;
}
