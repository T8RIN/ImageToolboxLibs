#!/bin/sh
set -e

cd "$(dirname "$0")"

(
  cd jhlabs
  cargo ndk \
    -t arm64-v8a \
    -t armeabi-v7a \
    -t x86_64 \
    -o ../../jniLibs \
    build --release --locked
)