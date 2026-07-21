#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>

#include <algorithm>
#include <atomic>
#include <chrono>
#include <condition_variable>
#include <cstdint>
#include <cstring>
#include <exception>
#include <limits>
#include <malloc.h>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>
#include <thread>
#include <unordered_map>
#include <utility>
#include <vector>

#if defined(_OPENMP)
#include <omp.h>
#endif

#include "gmic.h"

namespace {

constexpr const char *kExceptionClass = "com/t8rin/gmic/model/GmicException";
constexpr const char *kProfileTag = "GmicProfile";
constexpr size_t kMaxIdleRuntimes = 4;
constexpr size_t kMaxCachedCustomPrototypes = 4;
constexpr size_t kLogChunkBytes = 3000;

using ProfileClock = std::chrono::steady_clock;

[[nodiscard]] int64_t elapsedNanoseconds(ProfileClock::time_point start) {
    return std::chrono::duration_cast<std::chrono::nanoseconds>(
        ProfileClock::now() - start
    ).count();
}

[[nodiscard]] size_t nativeHeapBytes() {
    const struct mallinfo info = mallinfo();
    return info.uordblks > 0 ? static_cast<size_t>(info.uordblks) : 0U;
}

struct DiagnosticsCounters final {
    std::atomic<uint64_t> prototypeCreations{0};
    std::atomic<uint64_t> stdlibLoads{0};
    std::atomic<uint64_t> customParses{0};
    std::atomic<uint64_t> runtimeCreations{0};
    std::atomic<uint64_t> runtimeReuses{0};
    std::atomic<uint64_t> runtimeResets{0};
};

DiagnosticsCounters gDiagnostics;

struct OperationState final {
    std::atomic_bool cancellationRequested{false};
    bool gmicAbort = false;
};

std::mutex gOperationsMutex;
std::unordered_map<jlong, std::shared_ptr<OperationState>> gOperations;
std::atomic<jlong> gNextOperationId{1};
std::mutex gAdmissionMutex;
std::mutex gPrototypeMutex;
std::condition_variable gAdmissionChanged;
std::condition_variable gPrototypeChanged;

class OperationAborted final : public std::exception {};

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

[[nodiscard]] std::shared_ptr<OperationState> findOperation(jlong operationId) {
    const std::lock_guard<std::mutex> lock(gOperationsMutex);
    const auto operation = gOperations.find(operationId);
    return operation == gOperations.end() ? nullptr : operation->second;
}

[[nodiscard]] bool isCancelled(const OperationState &operation) {
    return operation.cancellationRequested.load(std::memory_order_acquire);
}

void requestCancellation(OperationState &operation) {
    operation.cancellationRequested.store(true, std::memory_order_release);
    gmic_abort_store(&operation.gmicAbort, true);
    {
        const std::lock_guard<std::mutex> lock(gAdmissionMutex);
    }
    gAdmissionChanged.notify_all();
    {
        const std::lock_guard<std::mutex> lock(gPrototypeMutex);
    }
    gPrototypeChanged.notify_all();
}

void throwIfCancelled(const OperationState &operation) {
    if (isCancelled(operation)) throw OperationAborted();
}

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
        : env_(env),
          value_(value),
          chars_(value == nullptr ? nullptr : env->GetStringUTFChars(value, nullptr)) {}

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

void recycleBitmapAndDeleteLocalRef(JNIEnv *env, jobject bitmap) noexcept {
    if (bitmap == nullptr) return;
    jthrowable pending = env->ExceptionOccurred();
    if (pending != nullptr) env->ExceptionClear();

    jclass bitmapClass = env->GetObjectClass(bitmap);
    if (bitmapClass != nullptr) {
        jmethodID recycle = env->GetMethodID(bitmapClass, "recycle", "()V");
        if (recycle != nullptr) env->CallVoidMethod(bitmap, recycle);
    }
    if (env->ExceptionCheck()) env->ExceptionClear();
    if (bitmapClass != nullptr) env->DeleteLocalRef(bitmapClass);
    env->DeleteLocalRef(bitmap);

    if (pending != nullptr) {
        env->Throw(pending);
        env->DeleteLocalRef(pending);
    }
}

class OwnedOutputBitmap final {
public:
    OwnedOutputBitmap(JNIEnv *env, jobject bitmap) : env_(env), bitmap_(bitmap) {}

    ~OwnedOutputBitmap() {
        if (bitmap_ != nullptr) recycleBitmapAndDeleteLocalRef(env_, bitmap_);
    }

    OwnedOutputBitmap(const OwnedOutputBitmap &) = delete;
    OwnedOutputBitmap &operator=(const OwnedOutputBitmap &) = delete;

    [[nodiscard]] jobject get() const { return bitmap_; }

    [[nodiscard]] jobject release() {
        jobject result = bitmap_;
        bitmap_ = nullptr;
        return result;
    }

private:
    JNIEnv *env_;
    jobject bitmap_;
};

[[nodiscard]] jobject createArgbBitmap(JNIEnv *env, uint32_t width, uint32_t height) {
    if (width > static_cast<uint32_t>(std::numeric_limits<jint>::max()) ||
        height > static_cast<uint32_t>(std::numeric_limits<jint>::max()) ||
        width > std::numeric_limits<size_t>::max() / height ||
        static_cast<size_t>(width) * height > std::numeric_limits<size_t>::max() / 4U) {
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

[[nodiscard]] unsigned int hardwareThreadCount() {
    return std::max(1U, std::thread::hardware_concurrency());
}

[[nodiscard]] unsigned int resolveThreadCount(jint maxThreads, unsigned int hardware) {
    if (maxThreads < 0) throw std::invalid_argument("maxThreads must not be negative");
    if (maxThreads == 0) return hardware;
    return std::min(hardware, static_cast<unsigned int>(maxThreads));
}

unsigned int gActiveThreadTokens = 0;
unsigned int gActiveRuns = 0;

class ActiveRunBudget final {
public:
    ActiveRunBudget(
        const OperationState &operation,
        unsigned int threadCount,
        unsigned int totalTokens
    )
        : threadCount_(threadCount) {
        std::unique_lock<std::mutex> lock(gAdmissionMutex);
        gAdmissionChanged.wait(lock, [&] {
            return isCancelled(operation) || gActiveThreadTokens + threadCount_ <= totalTokens;
        });
        throwIfCancelled(operation);
        gActiveThreadTokens += threadCount_;
        activeRunsAtAcquire_ = ++gActiveRuns;
        acquired_ = true;
    }

    ~ActiveRunBudget() {
        if (!acquired_) return;
        {
            const std::lock_guard<std::mutex> lock(gAdmissionMutex);
            gActiveThreadTokens -= threadCount_;
            --gActiveRuns;
        }
        gAdmissionChanged.notify_all();
    }

    ActiveRunBudget(const ActiveRunBudget &) = delete;
    ActiveRunBudget &operator=(const ActiveRunBudget &) = delete;

    [[nodiscard]] unsigned int activeRunsAtAcquire() const { return activeRunsAtAcquire_; }

private:
    unsigned int threadCount_;
    unsigned int activeRunsAtAcquire_ = 0;
    bool acquired_ = false;
};

struct InterpreterPrototype final {
    explicit InterpreterPrototype(std::unique_ptr<gmic> value)
        : interpreter(std::move(value)) {}

    std::unique_ptr<gmic> interpreter;
};

struct BaseBuildTimings final {
    int64_t emptyRuntimeNs = 0;
    int64_t stdlibDecompressNs = 0;
    int64_t stdlibParseNs = 0;
    int64_t androidCommandsParseNs = 0;
};

struct PrototypeAcquireMetrics final {
    bool builtBase = false;
    bool customCacheHit = false;
    bool parsedCustom = false;
    int64_t acquireNs = 0;
    int64_t customCloneNs = 0;
    int64_t customParseNs = 0;
};

std::once_flag gBasePrototypeOnce;
std::shared_ptr<const InterpreterPrototype> gBasePrototype;
BaseBuildTimings gBaseBuildTimings;

[[nodiscard]] std::unique_ptr<gmic> createEmptyInterpreter() {
    gmic_list<float> images;
    gmic_list<char> imageNames;
    return std::make_unique<gmic>(
        "",
        images,
        imageNames,
        nullptr,
        false,
        nullptr,
        nullptr
    );
}

[[nodiscard]] std::shared_ptr<const InterpreterPrototype> acquireBasePrototype(
    OperationState &operation,
    PrototypeAcquireMetrics &metrics
) {
    std::call_once(gBasePrototypeOnce, [&] {
        metrics.builtBase = true;
        auto stageStarted = ProfileClock::now();
        auto interpreter = createEmptyInterpreter();
        interpreter->is_abort = &operation.gmicAbort;
        gBaseBuildTimings.emptyRuntimeNs = elapsedNanoseconds(stageStarted);

        stageStarted = ProfileClock::now();
        const gmic_image<char> &stdlib = gmic::decompress_stdlib();
        gBaseBuildTimings.stdlibDecompressNs = elapsedNanoseconds(stageStarted);

        stageStarted = ProfileClock::now();
        try {
            interpreter->add_commands(stdlib._data);
        } catch (...) {
            if (isCancelled(operation)) throw OperationAborted();
            throw;
        }
        gBaseBuildTimings.stdlibParseNs = elapsedNanoseconds(stageStarted);

        stageStarted = ProfileClock::now();
        try {
            interpreter->add_commands(kAndroidGmicCommands);
        } catch (...) {
            if (isCancelled(operation)) throw OperationAborted();
            throw;
        }
        gBaseBuildTimings.androidCommandsParseNs = elapsedNanoseconds(stageStarted);
        throwIfCancelled(operation);
        gmic_abort_store(&interpreter->_is_abort, false);
        interpreter->is_abort = &interpreter->_is_abort;

        gBasePrototype = std::make_shared<const InterpreterPrototype>(std::move(interpreter));
        gDiagnostics.prototypeCreations.fetch_add(1, std::memory_order_relaxed);
        gDiagnostics.stdlibLoads.fetch_add(1, std::memory_order_relaxed);
    });
    return gBasePrototype;
}

struct PrototypeBuildSlot final {
    bool building = true;
    uint64_t lastUse = 0;
    std::shared_ptr<const InterpreterPrototype> prototype;
    std::exception_ptr failure;
};

std::unordered_map<std::string, std::shared_ptr<PrototypeBuildSlot>> gCustomPrototypes;
uint64_t gPrototypeUseSequence = 0;

void pruneCustomPrototypesLocked() {
    while (gCustomPrototypes.size() > kMaxCachedCustomPrototypes) {
        auto victim = gCustomPrototypes.end();
        for (auto it = gCustomPrototypes.begin(); it != gCustomPrototypes.end(); ++it) {
            const auto &slot = it->second;
            if (slot->building || !slot->prototype || slot->prototype.use_count() != 1) continue;
            if (victim == gCustomPrototypes.end() ||
                slot->lastUse < victim->second->lastUse) {
                victim = it;
            }
        }
        if (victim == gCustomPrototypes.end()) return;
        gCustomPrototypes.erase(victim);
    }
}

void pruneCustomPrototypeCache() noexcept {
    try {
        const std::lock_guard<std::mutex> lock(gPrototypeMutex);
        pruneCustomPrototypesLocked();
    } catch (...) {
        // Cache pruning is best-effort and must remain safe during stack unwinding.
    }
}

class PrototypeCachePruner final {
public:
    ~PrototypeCachePruner() noexcept { pruneCustomPrototypeCache(); }
};

[[nodiscard]] std::shared_ptr<const InterpreterPrototype> acquirePrototype(
    const char *customCommands,
    OperationState &operation,
    PrototypeAcquireMetrics &metrics
) {
    const auto acquireStarted = ProfileClock::now();
    const std::shared_ptr<const InterpreterPrototype> base = acquireBasePrototype(operation, metrics);
    throwIfCancelled(operation);
    if (customCommands == nullptr || customCommands[0] == '\0') {
        metrics.acquireNs = elapsedNanoseconds(acquireStarted);
        return base;
    }

    const std::string key(customCommands);
    for (;;) {
        std::shared_ptr<PrototypeBuildSlot> slot;
        bool isBuilder = false;
        {
            std::unique_lock<std::mutex> lock(gPrototypeMutex);
            const auto existing = gCustomPrototypes.find(key);
            if (existing == gCustomPrototypes.end()) {
                slot = std::make_shared<PrototypeBuildSlot>();
                slot->lastUse = ++gPrototypeUseSequence;
                gCustomPrototypes.emplace(key, slot);
                isBuilder = true;
            } else {
                slot = existing->second;
                slot->lastUse = ++gPrototypeUseSequence;
                while (slot->building && !isCancelled(operation)) gPrototypeChanged.wait(lock);
                throwIfCancelled(operation);
                if (slot->failure) std::rethrow_exception(slot->failure);
                if (slot->prototype) {
                    metrics.customCacheHit = true;
                    metrics.acquireNs = elapsedNanoseconds(acquireStarted);
                    return slot->prototype;
                }
                // A cancelled builder removed this slot. Retry with this operation.
                continue;
            }
        }

        if (isBuilder) {
            try {
                auto prototypeInterpreter = createEmptyInterpreter();
                auto stageStarted = ProfileClock::now();
                prototypeInterpreter->assign_isolated(*base->interpreter);
                metrics.customCloneNs = elapsedNanoseconds(stageStarted);

                stageStarted = ProfileClock::now();
                gDiagnostics.customParses.fetch_add(1, std::memory_order_relaxed);
                prototypeInterpreter->is_abort = &operation.gmicAbort;
                try {
                    prototypeInterpreter->add_commands(customCommands);
                } catch (...) {
                    if (isCancelled(operation)) throw OperationAborted();
                    throw;
                }
                metrics.customParseNs = elapsedNanoseconds(stageStarted);
                metrics.parsedCustom = true;
                throwIfCancelled(operation);
                gmic_abort_store(&prototypeInterpreter->_is_abort, false);
                prototypeInterpreter->is_abort = &prototypeInterpreter->_is_abort;

                auto prototype = std::make_shared<const InterpreterPrototype>(
                    std::move(prototypeInterpreter)
                );
                {
                    const std::lock_guard<std::mutex> lock(gPrototypeMutex);
                    slot->prototype = prototype;
                    slot->building = false;
                    slot->lastUse = ++gPrototypeUseSequence;
                    pruneCustomPrototypesLocked();
                }
                gDiagnostics.prototypeCreations.fetch_add(1, std::memory_order_relaxed);
                gPrototypeChanged.notify_all();
                metrics.acquireNs = elapsedNanoseconds(acquireStarted);
                return prototype;
            } catch (const OperationAborted &) {
                {
                    const std::lock_guard<std::mutex> lock(gPrototypeMutex);
                    slot->building = false;
                    const auto existing = gCustomPrototypes.find(key);
                    if (existing != gCustomPrototypes.end() && existing->second == slot) {
                        gCustomPrototypes.erase(existing);
                    }
                }
                gPrototypeChanged.notify_all();
                throw;
            } catch (...) {
                const std::exception_ptr failure = std::current_exception();
                {
                    const std::lock_guard<std::mutex> lock(gPrototypeMutex);
                    slot->failure = failure;
                    slot->building = false;
                    const auto existing = gCustomPrototypes.find(key);
                    if (existing != gCustomPrototypes.end() && existing->second == slot) {
                        gCustomPrototypes.erase(existing);
                    }
                }
                gPrototypeChanged.notify_all();
                std::rethrow_exception(failure);
            }
        }
    }
}

struct RuntimeAcquireMetrics final {
    bool reused = false;
    int64_t resetNs = 0;
};

std::mutex gRuntimePoolMutex;
std::vector<std::unique_ptr<gmic>> gIdleRuntimes;

void detachRuntimeHooks(gmic &runtime) {
    gmic_abort_store(&runtime._is_abort, false);
    runtime.is_abort = &runtime._is_abort;
    runtime._progress = 0;
    runtime.progress = &runtime._progress;
    runtime.starting_command_line = nullptr;
    runtime.is_running = false;
}

void returnRuntime(std::unique_ptr<gmic> runtime) noexcept {
    if (!runtime) return;
    try {
        detachRuntimeHooks(*runtime);
        const size_t maxIdle = std::min<size_t>(hardwareThreadCount(), kMaxIdleRuntimes);
        const std::lock_guard<std::mutex> lock(gRuntimePoolMutex);
        if (gIdleRuntimes.size() < maxIdle) gIdleRuntimes.push_back(std::move(runtime));
    } catch (...) {
        // Reuse is optional. Destroy the runtime if the pool cannot accept it.
    }
}

[[nodiscard]] std::unique_ptr<gmic> acquireRuntime(
    const InterpreterPrototype &prototype,
    OperationState &operation,
    RuntimeAcquireMetrics &metrics
) {
    std::unique_ptr<gmic> runtime;
    {
        const std::lock_guard<std::mutex> lock(gRuntimePoolMutex);
        if (!gIdleRuntimes.empty()) {
            runtime = std::move(gIdleRuntimes.back());
            gIdleRuntimes.pop_back();
            metrics.reused = true;
        }
    }
    if (runtime) {
        gDiagnostics.runtimeReuses.fetch_add(1, std::memory_order_relaxed);
    } else {
        runtime = createEmptyInterpreter();
        gDiagnostics.runtimeCreations.fetch_add(1, std::memory_order_relaxed);
    }

    try {
        throwIfCancelled(operation);
        gmic_abort_store(&operation.gmicAbort, false);
        const auto resetStarted = ProfileClock::now();
        runtime->assign_isolated(*prototype.interpreter, nullptr, &operation.gmicAbort);
        metrics.resetNs = elapsedNanoseconds(resetStarted);
        gDiagnostics.runtimeResets.fetch_add(1, std::memory_order_relaxed);
        if (isCancelled(operation)) {
            gmic_abort_store(&operation.gmicAbort, true);
            throw OperationAborted();
        }
        return runtime;
    } catch (const OperationAborted &) {
        returnRuntime(std::move(runtime));
        throw;
    } catch (...) {
        throw;
    }
}

class RuntimeLease final {
public:
    explicit RuntimeLease(std::unique_ptr<gmic> runtime) : runtime_(std::move(runtime)) {}

    ~RuntimeLease() noexcept { release(); }

    RuntimeLease(const RuntimeLease &) = delete;
    RuntimeLease &operator=(const RuntimeLease &) = delete;

    [[nodiscard]] gmic &get() const { return *runtime_; }

    void release() noexcept {
        if (runtime_) {
            returnRuntime(std::move(runtime_));
            pruneCustomPrototypeCache();
        }
    }

private:
    std::unique_ptr<gmic> runtime_;
};

void configureRuntimeThreads(gmic &runtime, unsigned int threadCount) {
#if defined(_OPENMP)
    omp_set_dynamic(0);
    omp_set_max_active_levels(1);
    omp_set_num_threads(static_cast<int>(threadCount));
#endif
    runtime._cpus_limit = threadCount;
    const std::string value = std::to_string(threadCount);
    runtime.set_variable("_cpus", '=', value.c_str());
}

[[nodiscard]] float clampByte(float value) {
    if (!(value > 0.0f)) return 0.0f;
    if (value >= 255.0f) return 255.0f;
    return value;
}

[[nodiscard]] uint8_t toByte(float value) {
    return static_cast<uint8_t>(clampByte(value) + 0.5f);
}

[[nodiscard]] float lerp(float start, float end, float fraction) {
    return start + (end - start) * fraction;
}

template<typename T>
[[nodiscard]] size_t imageElementCount(const gmic_image<T> &image) {
    const uint64_t count = static_cast<uint64_t>(image._width) * image._height *
        image._depth * image._spectrum;
    return count > std::numeric_limits<size_t>::max()
        ? std::numeric_limits<size_t>::max()
        : static_cast<size_t>(count);
}

template<typename T>
void swapImage(gmic_image<T> &first, gmic_image<T> &second) {
    std::swap(first._width, second._width);
    std::swap(first._height, second._height);
    std::swap(first._depth, second._depth);
    std::swap(first._spectrum, second._spectrum);
    std::swap(first._is_shared, second._is_shared);
    std::swap(first._data, second._data);
}

template<typename T>
void clearImage(gmic_image<T> &image) {
    if (!image._is_shared) delete[] image._data;
    image._width = image._height = image._depth = image._spectrum = 0;
    image._is_shared = false;
    image._data = nullptr;
}

[[nodiscard]] unsigned int rowWorkerCount(
    uint32_t width,
    uint32_t height,
    unsigned int threadLimit
) {
    constexpr size_t kParallelPixelThreshold = 1024U * 1024U;
    const size_t pixelCount = static_cast<size_t>(width) * height;
    return pixelCount < kParallelPixelThreshold
        ? 1U
        : std::min({threadLimit, hardwareThreadCount(), height});
}

template<typename Function>
unsigned int parallelForRows(
    uint32_t width,
    uint32_t height,
    unsigned int threadLimit,
    const OperationState &operation,
    Function function
) {
    const unsigned int workerCount = rowWorkerCount(width, height, threadLimit);
    if (workerCount == 1U) {
        for (uint32_t y = 0; y < height && !isCancelled(operation); ++y) function(y);
        return workerCount;
    }

    std::vector<std::thread> workers;
    workers.reserve(workerCount - 1U);
    const auto runChunk = [&](unsigned int worker) {
        const uint32_t begin = static_cast<uint32_t>(
            static_cast<uint64_t>(height) * worker / workerCount
        );
        const uint32_t end = static_cast<uint32_t>(
            static_cast<uint64_t>(height) * (worker + 1U) / workerCount
        );
        for (uint32_t y = begin; y < end && !isCancelled(operation); ++y) function(y);
    };

    try {
        for (unsigned int worker = 1; worker < workerCount; ++worker) {
            workers.emplace_back(runChunk, worker);
        }
        runChunk(0);
    } catch (...) {
        for (std::thread &worker : workers) {
            if (worker.joinable()) worker.join();
        }
        throw;
    }
    for (std::thread &worker : workers) worker.join();
    return workerCount;
}

[[nodiscard]] float sampleAlpha(
    const std::vector<uint8_t> &alpha,
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
    const int rawX0 = static_cast<int>(std::floor(sourceX));
    const int rawY0 = static_cast<int>(std::floor(sourceY));
    const int x0 = std::clamp(rawX0, 0, static_cast<int>(sourceWidth) - 1);
    const int y0 = std::clamp(rawY0, 0, static_cast<int>(sourceHeight) - 1);
    const int x1 = std::clamp(rawX0 + 1, 0, static_cast<int>(sourceWidth) - 1);
    const int y1 = std::clamp(rawY0 + 1, 0, static_cast<int>(sourceHeight) - 1);
    const float tx = std::clamp(sourceX - rawX0, 0.0f, 1.0f);
    const float ty = std::clamp(sourceY - rawY0, 0.0f, 1.0f);

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

unsigned int copyBitmapToGmic(
    const LockedBitmap &source,
    bool preserveAlpha,
    gmic_image<float> &image,
    std::vector<uint8_t> &sourceAlpha,
    unsigned int threadLimit,
    const OperationState &operation
) {
    const AndroidBitmapInfo &info = source.info();
    if (info.width > std::numeric_limits<size_t>::max() / info.height) {
        throw std::overflow_error("Input bitmap is too large");
    }
    const size_t pixelCount = static_cast<size_t>(info.width) * info.height;
    const uint32_t channels = preserveAlpha ? 3U : 4U;
    if (pixelCount > std::numeric_limits<size_t>::max() / channels / sizeof(float)) {
        throw std::overflow_error("Input bitmap is too large");
    }

    image.assign(info.width, info.height, 1, channels);
    const bool hasNonOpaqueAlpha =
        (info.flags & ANDROID_BITMAP_FLAGS_ALPHA_MASK) != ANDROID_BITMAP_FLAGS_ALPHA_OPAQUE;
    sourceAlpha.resize(preserveAlpha && hasNonOpaqueAlpha ? pixelCount : 0);

    float *red = image._data;
    float *green = red + pixelCount;
    float *blue = green + pixelCount;
    float *alpha = preserveAlpha ? nullptr : blue + pixelCount;
    const bool isPremultiplied =
        (info.flags & ANDROID_BITMAP_FLAGS_ALPHA_MASK) == ANDROID_BITMAP_FLAGS_ALPHA_PREMUL;

    const unsigned int workers = parallelForRows(
        info.width,
        info.height,
        threadLimit,
        operation,
        [&](uint32_t y) {
            const auto *row = static_cast<const uint8_t *>(source.pixels()) +
                static_cast<size_t>(y) * info.stride;
            for (uint32_t x = 0; x < info.width; ++x) {
                const auto *pixel = row + static_cast<size_t>(x) * 4;
                const size_t index = static_cast<size_t>(y) * info.width + x;
                const uint8_t a = pixel[3];
                if (!isPremultiplied || a == 255U) {
                    red[index] = pixel[0];
                    green[index] = pixel[1];
                    blue[index] = pixel[2];
                } else if (a == 0U) {
                    red[index] = green[index] = blue[index] = 0.0f;
                } else {
                    const float unpremultiply = 255.0f / a;
                    red[index] = clampByte(pixel[0] * unpremultiply);
                    green[index] = clampByte(pixel[1] * unpremultiply);
                    blue[index] = clampByte(pixel[2] * unpremultiply);
                }
                if (preserveAlpha) {
                    if (!sourceAlpha.empty()) sourceAlpha[index] = a;
                } else {
                    alpha[index] = a;
                }
            }
        }
    );
    throwIfCancelled(operation);
    return workers;
}

unsigned int copyGmicToBitmap(
    const gmic_image<float> &image,
    const std::vector<uint8_t> &sourceAlpha,
    uint32_t sourceWidth,
    uint32_t sourceHeight,
    bool preserveAlpha,
    const LockedBitmap &target,
    unsigned int threadLimit,
    const OperationState &operation
) {
    if (image._depth != 1) throw std::invalid_argument("G'MIC output must be a 2D image");
    if (image._spectrum == 0 || image._data == nullptr) {
        throw std::invalid_argument("G'MIC output image has no channels");
    }
    if (image._width == 0 || image._height == 0) {
        throw std::invalid_argument("G'MIC output dimensions must be positive");
    }

    const AndroidBitmapInfo &info = target.info();
    if (info.width != image._width || info.height != image._height) {
        throw std::invalid_argument("G'MIC output and target bitmap dimensions do not match");
    }
    if (image._width > std::numeric_limits<size_t>::max() / image._height) {
        throw std::overflow_error("G'MIC output bitmap is too large");
    }
    const size_t pixelCount = static_cast<size_t>(image._width) * image._height;
    const float *channel0 = image._data;
    const float *channel1 = image._spectrum >= 3 ? channel0 + pixelCount : channel0;
    const float *channel2 = image._spectrum >= 3 ? channel0 + 2U * pixelCount : channel0;
    const float *alphaChannel = image._spectrum >= 4
        ? channel0 + 3U * pixelCount
        : image._spectrum == 2
            ? channel0 + pixelCount
            : nullptr;
    const bool outputIsPremultiplied =
        (info.flags & ANDROID_BITMAP_FLAGS_ALPHA_MASK) == ANDROID_BITMAP_FLAGS_ALPHA_PREMUL;
    const bool alphaDimensionsMatch =
        sourceWidth == image._width && sourceHeight == image._height;

    const unsigned int workers = parallelForRows(
        image._width,
        image._height,
        threadLimit,
        operation,
        [&](uint32_t y) {
            auto *row = static_cast<uint8_t *>(target.pixels()) +
                static_cast<size_t>(y) * info.stride;
            for (uint32_t x = 0; x < image._width; ++x) {
                const size_t index = static_cast<size_t>(y) * image._width + x;
                auto *pixel = row + static_cast<size_t>(x) * 4;
                const float a = preserveAlpha
                    ? sourceAlpha.empty()
                        ? 255.0f
                        : alphaDimensionsMatch
                            ? sourceAlpha[index]
                            : sampleAlpha(
                                sourceAlpha,
                                sourceWidth,
                                sourceHeight,
                                x,
                                y,
                                image._width,
                                image._height
                            )
                    : alphaChannel != nullptr
                        ? alphaChannel[index]
                        : 255.0f;
                const float alphaByte = clampByte(a);
                const float premultiply = outputIsPremultiplied ? alphaByte / 255.0f : 1.0f;
                pixel[0] = toByte(channel0[index] * premultiply);
                pixel[1] = toByte(channel1[index] * premultiply);
                pixel[2] = toByte(channel2[index] * premultiply);
                pixel[3] = toByte(alphaByte);
            }
        }
    );
    throwIfCancelled(operation);
    return workers;
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

[[nodiscard]] bool ownsSharedView(
    const gmic_image<float> &owner,
    const gmic_image<float> &view
) {
    if (owner._is_shared || owner._data == nullptr || view._data == nullptr) return false;
    const size_t ownerElements = imageElementCount(owner);
    const size_t viewElements = imageElementCount(view);
    if (ownerElements > std::numeric_limits<size_t>::max() / sizeof(float) ||
        viewElements > std::numeric_limits<size_t>::max() / sizeof(float)) return false;
    const uintptr_t ownerBegin = reinterpret_cast<uintptr_t>(owner._data);
    const uintptr_t viewBegin = reinterpret_cast<uintptr_t>(view._data);
    const size_t ownerBytes = ownerElements * sizeof(float);
    const size_t viewBytes = viewElements * sizeof(float);
    if (ownerBegin > std::numeric_limits<uintptr_t>::max() - ownerBytes ||
        viewBegin > std::numeric_limits<uintptr_t>::max() - viewBytes) return false;
    return viewBegin >= ownerBegin && viewBegin + viewBytes <= ownerBegin + ownerBytes;
}

void logChunkedCommand(jlong operationId, const char *command) {
    const size_t length = std::strlen(command);
    const size_t parts = std::max<size_t>(1U, (length + kLogChunkBytes - 1U) / kLogChunkBytes);
    for (size_t part = 0; part < parts; ++part) {
        const size_t offset = part * kLogChunkBytes;
        const size_t count = std::min(kLogChunkBytes, length - offset);
        __android_log_print(
            ANDROID_LOG_INFO,
            kProfileTag,
            "op=%lld command_part=%zu/%zu command_bytes=%zu value=%.*s",
            static_cast<long long>(operationId),
            part + 1U,
            parts,
            length,
            static_cast<int>(count),
            command + offset
        );
    }
}

} // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_com_t8rin_gmic_Gmic_nativeCreateOperation(JNIEnv *env, jobject) {
    try {
        const jlong operationId = gNextOperationId.fetch_add(1, std::memory_order_relaxed);
        const std::lock_guard<std::mutex> lock(gOperationsMutex);
        gOperations.emplace(operationId, std::make_shared<OperationState>());
        return operationId;
    } catch (const std::bad_alloc &) {
        throwGmicException(env, "Not enough memory to start a G'MIC operation");
    } catch (const std::exception &exception) {
        throwGmicException(env, exception.what());
    } catch (...) {
        throwGmicException(env, "Unable to start a G'MIC operation");
    }
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_t8rin_gmic_Gmic_nativeCancel(JNIEnv *, jobject, jlong operationId) {
    const std::shared_ptr<OperationState> operation = findOperation(operationId);
    if (operation != nullptr) requestCancellation(*operation);
}

extern "C" JNIEXPORT void JNICALL
Java_com_t8rin_gmic_Gmic_nativeDestroyOperation(JNIEnv *, jobject, jlong operationId) {
    const std::lock_guard<std::mutex> lock(gOperationsMutex);
    gOperations.erase(operationId);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_t8rin_gmic_Gmic_nativeRun(
    JNIEnv *env,
    jobject,
    jlong operationId,
    jobject input,
    jstring command,
    jstring customCommands,
    jboolean preserveAlpha,
    jint outputIndex,
    jint maxThreads,
    jboolean profilingEnabled,
    jlong inputPreparationNanoseconds,
    jint inputPreparationCopies
) {
    if (input == nullptr || command == nullptr) {
        throwGmicException(env, "Input bitmap and G'MIC command must not be null");
        return nullptr;
    }

    constexpr int64_t kStageNotRunNs = -1;
    const bool profiling = profilingEnabled == JNI_TRUE;
    const auto totalStarted = ProfileClock::now();
    const size_t heapStarted = profiling ? nativeHeapBytes() : 0U;
    size_t heapPeak = heapStarted;
    const auto observeHeap = [&] {
        if (profiling) heapPeak = std::max(heapPeak, nativeHeapBytes());
    };
    const auto milliseconds = [](int64_t nanoseconds) {
        return nanoseconds < 0 ? -1.0 : nanoseconds / 1e6;
    };

    const char *currentStage = "operation_lookup";
    auto stageStarted = totalStarted;
    unsigned int threadCount = 0;
    unsigned int activeRunsAtAcquire = 0;
    uint32_t sourceWidth = 0;
    uint32_t sourceHeight = 0;
    uint32_t outputWidth = 0;
    uint32_t outputHeight = 0;
    uint32_t outputChannels = 0;
    unsigned int inputWorkers = 0;
    unsigned int outputWorkers = 0;
    unsigned int parsedItems = 0;
    uint64_t topLevelOperations = 0;
    uint64_t parallelBranches = 0;
    unsigned int parallelCommands = 0;
    unsigned int parallelPeakBranches = 0;
    unsigned int parallelInnerThreads = 0;
    bool inputAlphaAllocated = false;
    int64_t jniInputPreparationNs = kStageNotRunNs;
    int64_t admissionWaitNs = kStageNotRunNs;
    int64_t runtimeAcquireNs = kStageNotRunNs;
    int64_t inputLockNs = kStageNotRunNs;
    int64_t inputConversionNs = kStageNotRunNs;
    int64_t commandParseNs = kStageNotRunNs;
    int64_t commandExecutionNs = kStageNotRunNs;
    int64_t outputBitmapCreationNs = kStageNotRunNs;
    int64_t outputLockNs = kStageNotRunNs;
    int64_t outputConversionNs = kStageNotRunNs;
    int64_t cleanupNs = kStageNotRunNs;
    PrototypeAcquireMetrics prototypeMetrics;
    RuntimeAcquireMetrics runtimeMetrics;

    const auto logIncompleteProfile = [&](const char *status) noexcept {
        if (!profiling) return;
        const int64_t nativeTotalNs = elapsedNanoseconds(totalStarted);
        const int64_t currentStageNs = elapsedNanoseconds(stageStarted);
        const int64_t endToEndNs = nativeTotalNs +
            std::max<jlong>(0, inputPreparationNanoseconds);
        const size_t heapFinished = nativeHeapBytes();
        const size_t observedHeapPeak = std::max(heapPeak, heapFinished);
        const unsigned int preparationCopies = inputPreparationCopies > 0
            ? static_cast<unsigned int>(inputPreparationCopies)
            : 0U;
        const unsigned int bridgeCopiesCompleted =
            (inputConversionNs >= 0 ? 1U : 0U) + (outputConversionNs >= 0 ? 1U : 0U);
        const uint64_t inputPixelCount = static_cast<uint64_t>(sourceWidth) * sourceHeight;
        const uint64_t outputPixelCount = static_cast<uint64_t>(outputWidth) * outputHeight;
        const uint64_t knownCompletedAllocatedBytes = inputConversionNs >= 0
            ? inputPixelCount * ((preserveAlpha == JNI_TRUE ? 3U : 4U) * sizeof(float) +
                (inputAlphaAllocated ? 1U : 0U)) +
                (outputBitmapCreationNs >= 0 ? outputPixelCount * 4U : 0U)
            : 0U;

        __android_log_print(
            ANDROID_LOG_INFO,
            kProfileTag,
            "op=%lld status=%s current_stage=%s current_stage_ms=%.3f "
            "size=%ux%u output=%ux%u top_level_operations=%llu parsed_items=%u "
            "parallel_commands=%u parallel_branches=%llu parallel_peak_branches=%u "
            "parallel_inner_threads=%u threads_configured=%u",
            static_cast<long long>(operationId),
            status,
            currentStage,
            currentStageNs / 1e6,
            sourceWidth,
            sourceHeight,
            outputWidth,
            outputHeight,
            static_cast<unsigned long long>(topLevelOperations),
            parsedItems,
            parallelCommands,
            static_cast<unsigned long long>(parallelBranches),
            parallelPeakBranches,
            parallelInnerThreads,
            threadCount
        );
        __android_log_print(
            ANDROID_LOG_INFO,
            kProfileTag,
            "op=%lld partial_stages_ms input_prepare_java=%.3f input_prepare_jni=%.3f "
            "admission_wait=%.3f prototype_acquire=%.3f custom_clone=%.3f custom_parse=%.3f "
            "runtime_acquire=%.3f runtime_reset=%.3f lock_input=%.3f rgba_to_gmic=%.3f "
            "command_parse=%.3f "
            "command_execute=%.3f bitmap_create=%.3f lock_output=%.3f gmic_to_rgba=%.3f "
            "cleanup=%.3f native_total=%.3f end_to_end=%.3f",
            static_cast<long long>(operationId),
            std::max<jlong>(0, inputPreparationNanoseconds) / 1e6,
            milliseconds(jniInputPreparationNs),
            milliseconds(admissionWaitNs),
            milliseconds(prototypeMetrics.acquireNs),
            milliseconds(prototypeMetrics.customCloneNs),
            milliseconds(prototypeMetrics.customParseNs),
            milliseconds(runtimeAcquireNs),
            milliseconds(runtimeMetrics.resetNs),
            milliseconds(inputLockNs),
            milliseconds(inputConversionNs),
            milliseconds(commandParseNs),
            milliseconds(commandExecutionNs),
            milliseconds(outputBitmapCreationNs),
            milliseconds(outputLockNs),
            milliseconds(outputConversionNs),
            milliseconds(cleanupNs),
            nativeTotalNs / 1e6,
            endToEndNs / 1e6
        );
        __android_log_print(
            ANDROID_LOG_INFO,
            kProfileTag,
            "op=%lld partial_known_bridge_full_copies=%u preparation_full_copies=%u "
            "known_full_copies_completed=%u known_completed_bridge_allocated_bytes=%llu "
            "heap_start=%zu heap_peak_observed=%zu heap_end=%zu active_runs_at_acquire=%u "
            "counters_prototype_creations=%llu counters_stdlib_loads=%llu "
            "counters_custom_parses=%llu counters_runtime_creations=%llu "
            "counters_runtime_reuses=%llu counters_runtime_resets=%llu",
            static_cast<long long>(operationId),
            bridgeCopiesCompleted,
            preparationCopies,
            preparationCopies + bridgeCopiesCompleted,
            static_cast<unsigned long long>(knownCompletedAllocatedBytes),
            heapStarted,
            observedHeapPeak,
            heapFinished,
            activeRunsAtAcquire,
            static_cast<unsigned long long>(
                gDiagnostics.prototypeCreations.load(std::memory_order_relaxed)
            ),
            static_cast<unsigned long long>(
                gDiagnostics.stdlibLoads.load(std::memory_order_relaxed)
            ),
            static_cast<unsigned long long>(
                gDiagnostics.customParses.load(std::memory_order_relaxed)
            ),
            static_cast<unsigned long long>(
                gDiagnostics.runtimeCreations.load(std::memory_order_relaxed)
            ),
            static_cast<unsigned long long>(
                gDiagnostics.runtimeReuses.load(std::memory_order_relaxed)
            ),
            static_cast<unsigned long long>(
                gDiagnostics.runtimeResets.load(std::memory_order_relaxed)
            )
        );
    };

    try {
        const PrototypeCachePruner prunePrototypeCacheOnExit;

        const std::shared_ptr<OperationState> operation = findOperation(operationId);
        if (operation == nullptr) throw std::invalid_argument("Unknown G'MIC operation");
        throwIfCancelled(*operation);

        currentStage = "input_prepare_jni";
        stageStarted = ProfileClock::now();
        UtfChars commandChars(env, command);
        if (commandChars.get() == nullptr) {
            logIncompleteProfile("jni_exception");
            return nullptr;
        }
        UtfChars customCommandChars(env, customCommands);
        if (customCommands != nullptr && customCommandChars.get() == nullptr) {
            logIncompleteProfile("jni_exception");
            return nullptr;
        }
        jniInputPreparationNs = elapsedNanoseconds(stageStarted);
        if (profiling) logChunkedCommand(operationId, commandChars.get());

        const unsigned int hardwareThreads = hardwareThreadCount();
        threadCount = resolveThreadCount(maxThreads, hardwareThreads);
        currentStage = "admission_wait";
        stageStarted = ProfileClock::now();
        const ActiveRunBudget activeBudget(*operation, threadCount, hardwareThreads);
        admissionWaitNs = elapsedNanoseconds(stageStarted);
        activeRunsAtAcquire = activeBudget.activeRunsAtAcquire();

        currentStage = "prototype_acquire";
        stageStarted = ProfileClock::now();
        const std::shared_ptr<const InterpreterPrototype> prototype = acquirePrototype(
            customCommandChars.get(),
            *operation,
            prototypeMetrics
        );
        observeHeap();

        currentStage = "runtime_acquire";
        stageStarted = ProfileClock::now();
        std::unique_ptr<gmic> acquiredRuntime = acquireRuntime(
            *prototype,
            *operation,
            runtimeMetrics
        );
        runtimeAcquireNs = elapsedNanoseconds(stageStarted);
        RuntimeLease runtime(std::move(acquiredRuntime));
        currentStage = "runtime_configure";
        stageStarted = ProfileClock::now();
        configureRuntimeThreads(runtime.get(), threadCount);
        throwIfCancelled(*operation);
        observeHeap();

        std::vector<uint8_t> sourceAlpha;
        gmic_list<float> images;
        gmic_list<char> imageNames;
        images.assign(1);

        currentStage = "lock_input";
        stageStarted = ProfileClock::now();
        {
            LockedBitmap source(env, input);
            inputLockNs = elapsedNanoseconds(stageStarted);
            sourceWidth = source.info().width;
            sourceHeight = source.info().height;
            currentStage = "rgba_to_gmic";
            stageStarted = ProfileClock::now();
            inputWorkers = copyBitmapToGmic(
                source,
                preserveAlpha == JNI_TRUE,
                images[0],
                sourceAlpha,
                threadCount,
                *operation
            );
            inputConversionNs = elapsedNanoseconds(stageStarted);
            inputAlphaAllocated = !sourceAlpha.empty();
        }
        observeHeap();

        currentStage = "command_parse";
        stageStarted = ProfileClock::now();
        gmic_list<char> parsedCommand =
            runtime.get().command_line_to_CImgList(commandChars.get());
        commandParseNs = elapsedNanoseconds(stageStarted);
        parsedItems = parsedCommand._width;

        currentStage = "command_execute";
        stageStarted = ProfileClock::now();
        runtime.get().starting_command_line = commandChars.get();
        runtime.get().is_running = true;
        try {
            runtime.get()._run(parsedCommand, images, imageNames, true);
            runtime.get().is_running = false;
        } catch (...) {
            runtime.get().is_running = false;
            commandExecutionNs = elapsedNanoseconds(stageStarted);
            topLevelOperations = runtime.get().top_level_command_count;
            parallelBranches = runtime.get().parallel_branch_count;
            parallelCommands = runtime.get().parallel_command_count;
            parallelPeakBranches = runtime.get().parallel_peak_active_branches;
            parallelInnerThreads = runtime.get().parallel_max_inner_threads;
            observeHeap();
            throw;
        }
        commandExecutionNs = elapsedNanoseconds(stageStarted);
        topLevelOperations = runtime.get().top_level_command_count;
        parallelBranches = runtime.get().parallel_branch_count;
        parallelCommands = runtime.get().parallel_command_count;
        parallelPeakBranches = runtime.get().parallel_peak_active_branches;
        parallelInnerThreads = runtime.get().parallel_max_inner_threads;
        throwIfCancelled(*operation);
        observeHeap();
        if (images._width == 0) throw std::runtime_error("G'MIC command returned no images");

        currentStage = "output_select";
        stageStarted = ProfileClock::now();
        const size_t selectedIndex = resolveOutputIndex(outputIndex, images._width);
        gmic_image<float> detachedOutput;
        gmic_image<float> detachedOwner;
        const gmic_image<float> *output = &images[selectedIndex];
        bool releasedNonSelectedImages = false;
        if (!output->_is_shared) {
            swapImage(images[selectedIndex], detachedOutput);
            images.assign(0);
            output = &detachedOutput;
            releasedNonSelectedImages = true;
        } else {
            size_t ownerIndex = images._width;
            for (size_t index = 0; index < images._width; ++index) {
                if (index != selectedIndex && ownsSharedView(images[index], *output)) {
                    ownerIndex = index;
                    break;
                }
            }
            if (ownerIndex < images._width) {
                swapImage(images[selectedIndex], detachedOutput);
                swapImage(images[ownerIndex], detachedOwner);
                images.assign(0);
                output = &detachedOutput;
                releasedNonSelectedImages = true;
            }
        }
        imageNames.assign(0);

        if (output->_width == 0 || output->_height == 0) {
            throw std::invalid_argument("G'MIC output dimensions must be positive");
        }
        outputWidth = output->_width;
        outputHeight = output->_height;
        outputChannels = output->_spectrum;

        currentStage = "bitmap_create";
        stageStarted = ProfileClock::now();
        jobject result = createArgbBitmap(env, outputWidth, outputHeight);
        outputBitmapCreationNs = elapsedNanoseconds(stageStarted);
        if (result == nullptr) {
            if (!env->ExceptionCheck()) throw std::runtime_error("Unable to create output Bitmap");
            logIncompleteProfile("jni_exception");
            return nullptr;
        }
        OwnedOutputBitmap ownedResult(env, result);

        {
            currentStage = "lock_output";
            stageStarted = ProfileClock::now();
            LockedBitmap target(env, ownedResult.get());
            outputLockNs = elapsedNanoseconds(stageStarted);
            currentStage = "gmic_to_rgba";
            stageStarted = ProfileClock::now();
            outputWorkers = copyGmicToBitmap(
                *output,
                sourceAlpha,
                sourceWidth,
                sourceHeight,
                preserveAlpha == JNI_TRUE,
                target,
                threadCount,
                *operation
            );
            outputConversionNs = elapsedNanoseconds(stageStarted);
        }
        throwIfCancelled(*operation);
        observeHeap();

        const bool allocatedAlphaBuffer = inputAlphaAllocated;
        const size_t inputPixelCount = static_cast<size_t>(sourceWidth) * sourceHeight;
        const size_t outputPixelCount = static_cast<size_t>(outputWidth) * outputHeight;
        const size_t nativeIntegrationBytes =
            inputPixelCount * (preserveAlpha == JNI_TRUE ? 3U : 4U) * sizeof(float) +
            (allocatedAlphaBuffer ? inputPixelCount : 0U) +
            outputPixelCount * 4U;
        const unsigned int nativeLargeAllocations = allocatedAlphaBuffer ? 3U : 2U;
        const unsigned int preparationCopies = inputPreparationCopies > 0
            ? static_cast<unsigned int>(inputPreparationCopies)
            : 0U;

        currentStage = "cleanup";
        stageStarted = ProfileClock::now();
        parsedCommand.assign(0);
        images.assign(0);
        imageNames.assign(0);
        clearImage(detachedOutput);
        clearImage(detachedOwner);
        std::vector<uint8_t>().swap(sourceAlpha);
        runtime.release();
        cleanupNs = elapsedNanoseconds(stageStarted);
        const size_t heapFinished = profiling ? nativeHeapBytes() : 0U;
        const int64_t nativeTotalNs = elapsedNanoseconds(totalStarted);
        const int64_t endToEndNs = nativeTotalNs + std::max<jlong>(0, inputPreparationNanoseconds);

        if (profiling) {
            __android_log_print(
                ANDROID_LOG_INFO,
                kProfileTag,
                "op=%lld size=%ux%u input_pixels=%zu input_channels=%u output=%ux%u "
                "output_pixels=%zu output_channels=%u top_level_operations=%llu parsed_items=%u "
                "runtime_reused=%u custom_cache_hit=%u custom_parsed=%u released_nonselected=%u "
                "parallel_commands=%u parallel_branches=%llu parallel_peak_branches=%u "
                "parallel_inner_threads=%u",
                static_cast<long long>(operationId),
                sourceWidth,
                sourceHeight,
                inputPixelCount,
                preserveAlpha == JNI_TRUE ? 3U : 4U,
                outputWidth,
                outputHeight,
                outputPixelCount,
                outputChannels,
                static_cast<unsigned long long>(topLevelOperations),
                parsedItems,
                runtimeMetrics.reused ? 1U : 0U,
                prototypeMetrics.customCacheHit ? 1U : 0U,
                prototypeMetrics.parsedCustom ? 1U : 0U,
                releasedNonSelectedImages ? 1U : 0U,
                parallelCommands,
                static_cast<unsigned long long>(parallelBranches),
                parallelPeakBranches,
                parallelInnerThreads
            );
            __android_log_print(
                ANDROID_LOG_INFO,
                kProfileTag,
                "op=%lld stages_ms input_prepare_java=%.3f input_prepare_jni=%.3f admission_wait=%.3f "
                "prototype_acquire=%.3f custom_clone=%.3f custom_parse=%.3f runtime_acquire=%.3f "
                "runtime_reset=%.3f lock_input=%.3f rgba_to_gmic=%.3f command_parse=%.3f command_execute=%.3f "
                "bitmap_create=%.3f lock_output=%.3f gmic_to_rgba=%.3f cleanup=%.3f native_total=%.3f end_to_end=%.3f",
                static_cast<long long>(operationId),
                std::max<jlong>(0, inputPreparationNanoseconds) / 1e6,
                jniInputPreparationNs / 1e6,
                admissionWaitNs / 1e6,
                prototypeMetrics.acquireNs / 1e6,
                prototypeMetrics.customCloneNs / 1e6,
                prototypeMetrics.customParseNs / 1e6,
                runtimeAcquireNs / 1e6,
                runtimeMetrics.resetNs / 1e6,
                inputLockNs / 1e6,
                inputConversionNs / 1e6,
                commandParseNs / 1e6,
                commandExecutionNs / 1e6,
                outputBitmapCreationNs / 1e6,
                outputLockNs / 1e6,
                outputConversionNs / 1e6,
                cleanupNs / 1e6,
                nativeTotalNs / 1e6,
                endToEndNs / 1e6
            );
            if (prototypeMetrics.builtBase) {
                __android_log_print(
                    ANDROID_LOG_INFO,
                    kProfileTag,
                    "op=%lld base_once_ms empty_runtime=%.3f stdlib_decompress=%.3f "
                    "stdlib_parse=%.3f android_commands_parse=%.3f",
                    static_cast<long long>(operationId),
                    gBaseBuildTimings.emptyRuntimeNs / 1e6,
                    gBaseBuildTimings.stdlibDecompressNs / 1e6,
                    gBaseBuildTimings.stdlibParseNs / 1e6,
                    gBaseBuildTimings.androidCommandsParseNs / 1e6
                );
            }
            __android_log_print(
                ANDROID_LOG_INFO,
                kProfileTag,
                "op=%lld known_bridge_full_copies=2 preparation_full_copies=%u "
                "known_full_copies_total=%u known_bridge_large_allocations=%u "
                "known_bridge_allocated_bytes=%zu heap_start=%zu "
                "heap_peak_observed=%zu heap_end=%zu threads_configured=%u input_workers=%u "
                "output_workers=%u active_runs_at_acquire=%u",
                static_cast<long long>(operationId),
                preparationCopies,
                preparationCopies + 2U,
                nativeLargeAllocations,
                nativeIntegrationBytes,
                heapStarted,
                heapPeak,
                heapFinished,
                threadCount,
                inputWorkers,
                outputWorkers,
                activeRunsAtAcquire
            );
            __android_log_print(
                ANDROID_LOG_INFO,
                kProfileTag,
                "op=%lld counters prototype_creations=%llu stdlib_loads=%llu custom_parses=%llu "
                "runtime_creations=%llu runtime_reuses=%llu runtime_resets=%llu",
                static_cast<long long>(operationId),
                static_cast<unsigned long long>(
                    gDiagnostics.prototypeCreations.load(std::memory_order_relaxed)
                ),
                static_cast<unsigned long long>(
                    gDiagnostics.stdlibLoads.load(std::memory_order_relaxed)
                ),
                static_cast<unsigned long long>(
                    gDiagnostics.customParses.load(std::memory_order_relaxed)
                ),
                static_cast<unsigned long long>(
                    gDiagnostics.runtimeCreations.load(std::memory_order_relaxed)
                ),
                static_cast<unsigned long long>(
                    gDiagnostics.runtimeReuses.load(std::memory_order_relaxed)
                ),
                static_cast<unsigned long long>(
                    gDiagnostics.runtimeResets.load(std::memory_order_relaxed)
                )
            );
        }
        currentStage = "complete";
        return ownedResult.release();
    } catch (const OperationAborted &) {
        logIncompleteProfile("cancelled");
        return nullptr;
    } catch (const gmic_exception &exception) {
        logIncompleteProfile("gmic_error");
        throwGmicException(env, exception.what());
    } catch (const std::bad_alloc &) {
        logIncompleteProfile("out_of_memory");
        throwGmicException(env, "Not enough memory to process this bitmap with G'MIC");
    } catch (const std::exception &exception) {
        logIncompleteProfile("native_error");
        throwGmicException(env, exception.what());
    } catch (...) {
        logIncompleteProfile("unknown_error");
        throwGmicException(env, "Unknown native G'MIC error");
    }
    return nullptr;
}
