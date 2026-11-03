#pragma once

#include <jni.h>

class EncodingInterruption {
public:
    explicit EncodingInterruption(JNIEnv *env, bool withWorkers = false) : env(env), previous(currentInterruption) {
        currentInterruption = this;
        jclass cls = env->FindClass("java/lang/Thread");
        if (!cls) return;
        jmethodID current = env->GetStaticMethodID(cls, "currentThread", "()Ljava/lang/Thread;");
        if (!current) return;
        interrupted = env->GetMethodID(cls, "isInterrupted", "()Z");
        if (interrupted) thread = env->CallStaticObjectMethod(cls, current);
        env->DeleteLocalRef(cls);
        if (withWorkers && thread && env->GetJavaVM(&vm) == JNI_OK) {
            sharedThread = env->NewGlobalRef(thread);
        }
    }

    ~EncodingInterruption() {
        currentInterruption = previous;
        if (sharedThread) env->DeleteGlobalRef(sharedThread);
        if (thread) env->DeleteLocalRef(thread);
    }

    EncodingInterruption(const EncodingInterruption &) = delete;
    EncodingInterruption &operator=(const EncodingInterruption &) = delete;

    static bool currentRequested() {
        return currentInterruption && currentInterruption->requested();
    }

    bool requested() const {
        return thread && !env->ExceptionCheck() && env->CallBooleanMethod(thread, interrupted) == JNI_TRUE;
    }

    class Worker {
    public:
        explicit Worker(const EncodingInterruption *interruption) : owner(interruption) {
            if (!owner || !owner->sharedThread) return;
            if (owner->vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) == JNI_EDETACHED) {
                attached = owner->vm->AttachCurrentThread(&env, nullptr) == JNI_OK;
            }
        }

        ~Worker() {
            if (attached) owner->vm->DetachCurrentThread();
        }

        Worker(const Worker &) = delete;
        Worker &operator=(const Worker &) = delete;

        bool requested() const {
            return env && env->CallBooleanMethod(owner->sharedThread, owner->interrupted) == JNI_TRUE;
        }

    private:
        const EncodingInterruption *owner;
        JNIEnv *env = nullptr;
        bool attached = false;
    };

    static const EncodingInterruption *current() { return currentInterruption; }

    bool throwIfRequested() const {
        if (!requested()) return false;
        jclass exception = env->FindClass("java/lang/InterruptedException");
        if (exception) env->ThrowNew(exception, "Encoding interrupted");
        return true;
    }

private:
    inline static thread_local const EncodingInterruption *currentInterruption = nullptr;
    JNIEnv *env;
    const EncodingInterruption *previous;
    JavaVM *vm = nullptr;
    jobject sharedThread = nullptr;
    jobject thread = nullptr;
    jmethodID interrupted = nullptr;
};