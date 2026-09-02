#!/bin/sh
set -e

cd "$(dirname "$0")"

SOURCE_FILES=$(
  {
    printf '%s\n' \
      "build.sh" \
      "fractal_engine/Cargo.toml" \
      "fractal_engine/Cargo.lock"
    find "fractal_engine/src" -type f -name '*.rs'
  } | LC_ALL=C sort
)
FRACTAL_ENGINE_SOURCE_CHECKSUM=$(
  for source_file in $SOURCE_FILES; do
    printf '%s\0' "$source_file"
    command cat "$source_file"
    printf '\0'
  done | shasum -a 256 | awk '{print $1}'
)
export FRACTAL_ENGINE_SOURCE_CHECKSUM

(
  cd fractal_engine
  cargo ndk \
    -t arm64-v8a \
    -t armeabi-v7a \
    -t x86_64 \
    -o ../../jniLibs \
    build --release --locked
)

checksum_file="fractal_engine.android.sha256"
temporary_checksum_file="${checksum_file}.tmp"
printf '%s\n' "$FRACTAL_ENGINE_SOURCE_CHECKSUM" > "$temporary_checksum_file"
mv "$temporary_checksum_file" "$checksum_file"
