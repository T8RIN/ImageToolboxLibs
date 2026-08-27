/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

#include <jni.h>

#include <algorithm>
#include <cstdint>
#include <memory>
#include <vector>

#include "brotli/decode.h"
#include "brotli/encode.h"

namespace {

constexpr size_t kOutputSize = 64 * 1024;

struct EncoderContext {
    BrotliEncoderState* state = BrotliEncoderCreateInstance(nullptr, nullptr, nullptr);
    std::vector<uint8_t> pending;
    size_t offset = 0;
    bool finish_requested = false;

    ~EncoderContext() {
        BrotliEncoderDestroyInstance(state);
    }
};

struct DecoderContext {
    BrotliDecoderState* state = BrotliDecoderCreateInstance(nullptr, nullptr, nullptr);
    std::vector<uint8_t> pending;
    size_t offset = 0;
    bool input_finished = false;

    ~DecoderContext() {
        BrotliDecoderDestroyInstance(state);
    }
};

void throw_io_exception(JNIEnv* env, const char* message) {
    jclass type = env->FindClass("java/io/IOException");
    env->ThrowNew(type, message);
}

template <typename Context>
bool append_input(
        JNIEnv* env,
        Context* context,
        jbyteArray input,
        jint offset,
        jint length) {
    if (length == 0) return true;
    if (!context->pending.empty()) {
        throw_io_exception(env, "Previous Brotli input was not fully consumed");
        return false;
    }
    context->pending.resize(static_cast<size_t>(length));
    env->GetByteArrayRegion(
            input,
            offset,
            length,
            reinterpret_cast<jbyte*>(context->pending.data()));
    context->offset = 0;
    return !env->ExceptionCheck();
}

jbyteArray to_byte_array(JNIEnv* env, const std::vector<uint8_t>& output, size_t size) {
    jbyteArray result = env->NewByteArray(static_cast<jsize>(size));
    if (size > 0) {
        env->SetByteArrayRegion(
                result,
                0,
                static_cast<jsize>(size),
                reinterpret_cast<const jbyte*>(output.data()));
    }
    return result;
}

void trim_consumed(std::vector<uint8_t>* pending, size_t* offset, size_t available_in) {
    *offset = pending->size() - available_in;
    if (available_in == 0) {
        pending->clear();
        *offset = 0;
    }
}

}  // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_com_t8rin_archive_BrotliNative_createEncoder(JNIEnv* env, jobject) {
    auto context = std::make_unique<EncoderContext>();
    if (context->state == nullptr) {
        throw_io_exception(env, "Cannot create Brotli encoder");
        return 0;
    }
    BrotliEncoderSetParameter(context->state, BROTLI_PARAM_QUALITY, 6);
    return reinterpret_cast<jlong>(context.release());
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_t8rin_archive_BrotliNative_encode(
        JNIEnv* env,
        jobject,
        jlong handle,
        jbyteArray input,
        jint offset,
        jint length,
        jboolean finish) {
    auto* context = reinterpret_cast<EncoderContext*>(handle);
    if (context == nullptr || !append_input(env, context, input, offset, length)) return nullptr;
    context->finish_requested = context->finish_requested || finish;

    size_t available_in = context->pending.size() - context->offset;
    const uint8_t* next_in = available_in == 0
            ? nullptr
            : context->pending.data() + context->offset;
    std::vector<uint8_t> output(kOutputSize);
    size_t available_out = output.size();
    uint8_t* next_out = output.data();
    size_t total_out = 0;
    BrotliEncoderOperation operation = context->finish_requested
            ? BROTLI_OPERATION_FINISH
            : BROTLI_OPERATION_PROCESS;
    if (!BrotliEncoderCompressStream(
                context->state,
                operation,
                &available_in,
                &next_in,
                &available_out,
                &next_out,
                &total_out)) {
        throw_io_exception(env, "Brotli compression failed");
        return nullptr;
    }
    trim_consumed(&context->pending, &context->offset, available_in);
    return to_byte_array(env, output, output.size() - available_out);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_archive_BrotliNative_isEncoderFinished(JNIEnv*, jobject, jlong handle) {
    auto* context = reinterpret_cast<EncoderContext*>(handle);
    return context != nullptr && BrotliEncoderIsFinished(context->state);
}

extern "C" JNIEXPORT void JNICALL
Java_com_t8rin_archive_BrotliNative_destroyEncoder(JNIEnv*, jobject, jlong handle) {
    delete reinterpret_cast<EncoderContext*>(handle);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_t8rin_archive_BrotliNative_createDecoder(JNIEnv* env, jobject) {
    auto context = std::make_unique<DecoderContext>();
    if (context->state == nullptr) {
        throw_io_exception(env, "Cannot create Brotli decoder");
        return 0;
    }
    return reinterpret_cast<jlong>(context.release());
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_t8rin_archive_BrotliNative_decode(
        JNIEnv* env,
        jobject,
        jlong handle,
        jbyteArray input,
        jint offset,
        jint length,
        jboolean finish) {
    auto* context = reinterpret_cast<DecoderContext*>(handle);
    if (context == nullptr || !append_input(env, context, input, offset, length)) return nullptr;
    context->input_finished = context->input_finished || finish;

    size_t available_in = context->pending.size() - context->offset;
    const uint8_t* next_in = available_in == 0
            ? nullptr
            : context->pending.data() + context->offset;
    std::vector<uint8_t> output(kOutputSize);
    size_t available_out = output.size();
    uint8_t* next_out = output.data();
    size_t total_out = 0;
    BrotliDecoderResult result = BrotliDecoderDecompressStream(
            context->state,
            &available_in,
            &next_in,
            &available_out,
            &next_out,
            &total_out);
    trim_consumed(&context->pending, &context->offset, available_in);
    if (result == BROTLI_DECODER_RESULT_ERROR) {
        throw_io_exception(env, "Invalid Brotli stream");
        return nullptr;
    }
    if (context->input_finished && context->pending.empty() &&
            result == BROTLI_DECODER_RESULT_NEEDS_MORE_INPUT) {
        throw_io_exception(env, "Truncated Brotli stream");
        return nullptr;
    }
    return to_byte_array(env, output, output.size() - available_out);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_t8rin_archive_BrotliNative_isDecoderFinished(JNIEnv*, jobject, jlong handle) {
    auto* context = reinterpret_cast<DecoderContext*>(handle);
    return context != nullptr && BrotliDecoderIsFinished(context->state);
}

extern "C" JNIEXPORT void JNICALL
Java_com_t8rin_archive_BrotliNative_destroyDecoder(JNIEnv*, jobject, jlong handle) {
    delete reinterpret_cast<DecoderContext*>(handle);
}
