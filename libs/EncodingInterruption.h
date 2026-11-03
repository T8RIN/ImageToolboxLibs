#pragma once

#include <jni.h>

class EncodingInterruption {
public:
    explicit EncodingInterruption(JNIEnv *env) : env(env) {
        jclass cls = env->FindClass("java/lang/Thread");
        if (!cls) return;
        jmethodID current = env->GetStaticMethodID(cls, "currentThread", "()Ljava/lang/Thread;");
        if (!current) return;
        interrupted = env->GetMethodID(cls, "isInterrupted", "()Z");
        if (interrupted) thread = env->CallStaticObjectMethod(cls, current);
        env->DeleteLocalRef(cls);
    }

    bool requested() const {
        return thread && env->CallBooleanMethod(thread, interrupted) == JNI_TRUE;
    }

    bool throwIfRequested() const {
        if (!requested()) return false;
        jclass exception = env->FindClass("java/lang/InterruptedException");
        if (exception) env->ThrowNew(exception, "Encoding interrupted");
        return true;
    }

private:
    JNIEnv *env;
    jobject thread = nullptr;
    jmethodID interrupted = nullptr;
};