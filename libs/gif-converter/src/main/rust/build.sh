#!/bin/sh
set -e

cd "$(dirname "$0")"

# Build against the pinned Quantette source with cooperative cancellation.
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
source_dir="$(
  cargo metadata --locked --format-version 1 --filter-platform aarch64-linux-android --manifest-path gif_encoder/Cargo.toml |
    python3 -c 'import json, pathlib, sys
packages = json.load(sys.stdin)["packages"]
print(pathlib.Path(next(p["manifest_path"] for p in packages if p["name"] == "quantette")).parent)'
)"
cp -R "$source_dir" "$work/quantette"
chmod -R u+w "$work/quantette"
patch -s -d "$work/quantette" -p1 < patches/quantette-0.6.0.patch

mkdir "$work/gif_encoder"
cp -R gif_encoder/Cargo.toml gif_encoder/Cargo.lock gif_encoder/src "$work/gif_encoder/"
cp interruption.rs "$work/"
export CARGO_TARGET_DIR="${CARGO_TARGET_DIR:-$PWD/gif_encoder/target}"
output="$PWD/../jniLibs"
patch_config="patch.crates-io.quantette.path='$work/quantette'"

(
  cd "$work/gif_encoder"
  cargo update --offline -p quantette --config "$patch_config"
  cargo ndk \
    -t arm64-v8a \
    -t armeabi-v7a \
    -t x86_64 \
    -o "$output" \
    build --release --locked --config "$patch_config"
)
