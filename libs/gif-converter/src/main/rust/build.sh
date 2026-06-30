#!/bin/sh
set -e

cd "$(dirname "$0")"

(
  cd gif_encoder
  cargo ndk \
    -t arm64-v8a \
    -t armeabi-v7a \
    -t x86 \
    -t x86_64 \
    -o ../../libs \
    build --release --locked
)
