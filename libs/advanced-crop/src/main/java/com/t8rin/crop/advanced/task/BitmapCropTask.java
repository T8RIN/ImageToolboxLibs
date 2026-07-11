package com.t8rin.crop.advanced.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.t8rin.crop.advanced.callback.BitmapCropCallback;
import com.t8rin.crop.advanced.model.CropParameters;
import com.t8rin.crop.advanced.model.ExifInfo;
import com.t8rin.crop.advanced.model.ImageState;
import com.t8rin.crop.advanced.util.FileUtils;
import com.t8rin.crop.advanced.util.ImageHeaderParser;
import com.t8rin.crop.advanced.util.TaskExecutor;
import com.t8rin.exif.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Crops part of image that fills the crop bounds.
 * <p/>
 * First image is downscaled if max size was set and if resulting image is larger that max size.
 * Then image is rotated accordingly.
 * Finally new Bitmap object is created and saved to file.
 */
public class BitmapCropTask {

    private static final String TAG = "BitmapCropTask";

    static {
        System.loadLibrary("Advancedcrop");
    }

    private final RectF mCropRect;
    private final RectF mCurrentImageRect;
    private final int mMaxResultImageSizeX, mMaxResultImageSizeY;
    private final Bitmap.CompressFormat mCompressFormat;
    private final int mCompressQuality;
    private final String mImageInputPath, mImageOutputPath;
    private final ExifInfo mExifInfo;
    private final BitmapCropCallback mCropCallback;
    private final AtomicBoolean mCancelled = new AtomicBoolean();
    private Bitmap mViewBitmap;
    private float mCurrentScale;
    private final float mFineAngle;
    private final int mSourceRotationDegrees;
    private final boolean mFlipHorizontally;
    private int mCroppedImageWidth, mCroppedImageHeight;
    private int cropOffsetX, cropOffsetY;

    public BitmapCropTask(@Nullable Bitmap viewBitmap, @NonNull ImageState imageState, @NonNull CropParameters cropParameters,
                          @Nullable BitmapCropCallback cropCallback) {

        mViewBitmap = viewBitmap;
        mCropRect = imageState.getCropRect();
        mCurrentImageRect = imageState.getCurrentImageRect();

        mCurrentScale = imageState.getCurrentScale();
        mSourceRotationDegrees = cropParameters.getSourceRotationDegrees();
        mFlipHorizontally = cropParameters.isFlipHorizontally();
        float fineAngle = normalizeAngle(imageState.getCurrentAngle() - mSourceRotationDegrees);
        mFineAngle = Math.abs(fineAngle) < 0.01f ? 0f : fineAngle;
        mMaxResultImageSizeX = cropParameters.getMaxResultImageSizeX();
        mMaxResultImageSizeY = cropParameters.getMaxResultImageSizeY();

        mCompressFormat = cropParameters.getCompressFormat();
        mCompressQuality = cropParameters.getCompressQuality();

        mImageInputPath = cropParameters.getImageInputPath();
        mImageOutputPath = cropParameters.getImageOutputPath();
        if (cropParameters.getExifInfo() != null) {
            mExifInfo = cropParameters.getExifInfo();
        } else {
            mExifInfo = new ExifInfo(0, 0, 1);
        }

        mCropCallback = cropCallback;
    }

    public static boolean
    cropCImg(String inputPath, String outputPath,
             int left, int top, int width, int height,
             float angle, float resizeScale,
             int format, int quality,
             int exifDegrees, int exifTranslation) throws IOException, OutOfMemoryError {
        return cropCImgTransformed(
                inputPath, outputPath, left, top, width, height, angle, resizeScale,
                0, false, format, quality, exifDegrees, exifTranslation);
    }

    @SuppressWarnings("JniMissingFunction")
    native private static boolean
    cropCImgTransformed(String inputPath, String outputPath,
                        int left, int top, int width, int height,
                        float angle, float resizeScale,
                        int sourceRotationDegrees, boolean flipHorizontally,
             int format, int quality,
             int exifDegrees, int exifTranslation) throws IOException, OutOfMemoryError;

    @SuppressWarnings("JniMissingFunction")
    native private static int[]
    calculateSourceRegion(int sourceWidth, int sourceHeight,
                          int left, int top, int width, int height,
                          float angle, float resizeScale,
                          int sourceRotationDegrees, boolean flipHorizontally,
                          int exifDegrees, int exifTranslation);

    @SuppressWarnings("JniMissingFunction")
    native private static boolean
    cropBitmapRegionNative(Bitmap bitmap, String outputPath,
                           int sourceWidth, int sourceHeight,
                           int regionLeft, int regionTop,
                           int left, int top, int width, int height,
                           float angle, float resizeScale,
                           int sourceRotationDegrees, boolean flipHorizontally,
                           int exifDegrees, int exifTranslation,
                           AtomicBoolean cancellationSignal) throws IOException, OutOfMemoryError;

    @SuppressWarnings("JniMissingFunction")
    native public static boolean
    transformCImg(String inputPath, String outputPath,
                  int rotateDegrees, boolean flipHorizontally,
                  int format, int quality,
                  int exifDegrees, int exifTranslation) throws IOException, OutOfMemoryError;

    @NonNull
    private static BitmapFactory.Options bitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inScaled = false;
        return options;
    }

    private float resize() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageInputPath, options);

        boolean swapSides;

        try {
            swapSides = mExifInfo.getExifDegrees() == 90 || mExifInfo.getExifDegrees() == 270;
        } catch (Exception e) {
            swapSides = false;
        }

        float scaleX = (swapSides ? options.outHeight : options.outWidth) / (float) mViewBitmap.getWidth();
        float scaleY = (swapSides ? options.outWidth : options.outHeight) / (float) mViewBitmap.getHeight();

        float resizeScale = Math.min(scaleX, scaleY);

        mCurrentScale /= resizeScale;

        resizeScale = 1;
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            float cropWidth = mCropRect.width() / mCurrentScale;
            float cropHeight = mCropRect.height() / mCurrentScale;

            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {

                scaleX = mMaxResultImageSizeX / cropWidth;
                scaleY = mMaxResultImageSizeY / cropHeight;
                resizeScale = Math.min(scaleX, scaleY);

                mCurrentScale /= resizeScale;
            }
        }
        return resizeScale;
    }

    private static float normalizeAngle(float angle) {
        return ((angle + 180f) % 360f + 360f) % 360f - 180f;
    }

    @NonNull
    private static Bitmap ensureArgb8888(@NonNull Bitmap bitmap) {
        if (bitmap.getConfig() == Bitmap.Config.ARGB_8888) return bitmap;

        try {
            Bitmap converted = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            if (converted == null) {
                throw new OutOfMemoryError("Unable to convert crop region to ARGB_8888");
            }
            return converted;
        } finally {
            bitmap.recycle();
        }
    }

    private static boolean hasPngSignature(@NonNull String path) {
        byte[] signature = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        byte[] header = new byte[signature.length];
        try (FileInputStream input = new FileInputStream(path)) {
            return input.read(header) == header.length && java.util.Arrays.equals(header, signature);
        } catch (IOException ignored) {
            return false;
        }
    }

    @Nullable
    private Throwable doInBackground() {
        if (mCancelled.get()) {
            return new CancellationException("Crop cancelled");
        }
        if (mViewBitmap == null) {
            return new NullPointerException("ViewBitmap is null");
        } else if (mViewBitmap.isRecycled()) {
            return new NullPointerException("ViewBitmap is recycled");
        } else if (mCurrentImageRect.isEmpty()) {
            return new NullPointerException("CurrentImageRect is empty");
        }

        float resizeScale = resize();

        try {
            ensureNotCancelled();
            crop(resizeScale);
            ensureNotCancelled();
            mViewBitmap = null;
        } catch (Throwable throwable) {
            if (mCancelled.get()) {
                new File(mImageOutputPath).delete();
                return new CancellationException("Crop cancelled");
            }
            return throwable;
        }

        return null;
    }

    private void crop(float resizeScale) throws IOException {
        ensureNotCancelled();
        ExifInterface originalExif = null;
        if (mCompressFormat.equals(Bitmap.CompressFormat.JPEG)) {
            try {
                originalExif = new ExifInterface(mImageInputPath);
            } catch (Exception ignored) {
                // EXIF metadata is optional for the JPEG fallback path.
            }
        }

        cropOffsetX = Math.round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale);
        cropOffsetY = Math.round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale);
        int cropRight = Math.round((mCropRect.right - mCurrentImageRect.left) / mCurrentScale);
        int cropBottom = Math.round((mCropRect.bottom - mCurrentImageRect.top) / mCurrentScale);
        mCroppedImageWidth = Math.max(1, cropRight - cropOffsetX);
        mCroppedImageHeight = Math.max(1, cropBottom - cropOffsetY);

        boolean shouldCrop = shouldCropGeometry(mCroppedImageWidth, mCroppedImageHeight);
        Log.i(TAG, "Should crop: " + shouldCrop);

        if (mCompressFormat.equals(Bitmap.CompressFormat.PNG) &&
                !shouldCrop && mSourceRotationDegrees == 0 && !mFlipHorizontally &&
                mExifInfo.getExifDegrees() == 0 && mExifInfo.getExifTranslation() == 1 &&
                hasPngSignature(mImageInputPath)) {
            FileUtils.copyFile(mImageInputPath, mImageOutputPath);
        } else if (mCompressFormat.equals(Bitmap.CompressFormat.PNG)) {
            boolean cropped = cropBitmapRegion(resizeScale);
            if (!cropped) {
                ensureNotCancelled();
                throw new IOException("Native bitmap crop failed");
            }
            ImageHeaderParser.copyIccProfileToPng(
                    mImageInputPath, mImageOutputPath, mCancelled);
        } else if (shouldCrop) {
            boolean cropped = cropCImgTransformed(mImageInputPath, mImageOutputPath,
                    cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight,
                    mFineAngle, resizeScale, mSourceRotationDegrees, mFlipHorizontally,
                    mCompressFormat.ordinal(), mCompressQuality,
                    mExifInfo.getExifDegrees(), mExifInfo.getExifTranslation());
            if (!cropped) {
                throw new IOException("Native crop failed");
            }
            if (cropped && mCompressFormat.equals(Bitmap.CompressFormat.JPEG) &&
                    originalExif != null) {
                ImageHeaderParser.copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, mImageOutputPath);
            } else if (cropped && mCompressFormat.equals(Bitmap.CompressFormat.PNG)) {
                ImageHeaderParser.copyIccProfileToPng(mImageInputPath, mImageOutputPath);
            }
        } else if (mSourceRotationDegrees != 0 || mFlipHorizontally) {
            boolean transformed = transformCImg(
                    mImageInputPath, mImageOutputPath,
                    mSourceRotationDegrees, mFlipHorizontally,
                    mCompressFormat.ordinal(), mCompressQuality,
                    mExifInfo.getExifDegrees(), mExifInfo.getExifTranslation());
            if (!transformed) {
                throw new IOException("Native transform failed");
            }
            if (mCompressFormat.equals(Bitmap.CompressFormat.PNG)) {
                ImageHeaderParser.copyIccProfileToPng(mImageInputPath, mImageOutputPath);
            }
        } else {
            FileUtils.copyFile(mImageInputPath, mImageOutputPath);
        }
        ensureNotCancelled();
        if (!new File(mImageOutputPath).exists()) {
            throw new IOException("Output file was not created: " + mImageOutputPath);
        }
    }

    private boolean cropBitmapRegion(float resizeScale) throws IOException {
        ensureNotCancelled();
        BitmapRegionDecoder decoder = null;
        Bitmap bitmap = null;
        Rect regionRect = null;
        int sourceWidth = 0;
        int sourceHeight = 0;
        try {
            decoder = BitmapRegionDecoder.newInstance(mImageInputPath, false);
            if (decoder != null) {
                sourceWidth = decoder.getWidth();
                sourceHeight = decoder.getHeight();
                int[] region = calculateSourceRegion(
                        sourceWidth, sourceHeight,
                        cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight,
                        mFineAngle, resizeScale, mSourceRotationDegrees, mFlipHorizontally,
                        mExifInfo.getExifDegrees(), mExifInfo.getExifTranslation());
                if (region != null && region.length == 4) {
                    regionRect = new Rect(region[0], region[1], region[2], region[3]);
                    bitmap = decoder.decodeRegion(regionRect, bitmapOptions());
                    if (bitmap != null) {
                        bitmap = ensureArgb8888(bitmap);
                    }
                }
            }
        } catch (Exception exception) {
            Log.w(TAG, "Region decode failed, falling back to a full decode", exception);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = null;
            regionRect = null;
        } finally {
            if (decoder != null && !decoder.isRecycled()) {
                decoder.recycle();
            }
        }

        if (bitmap != null && regionRect != null) {
            try {
                ensureNotCancelled();
                return cropBitmapRegionNative(
                        bitmap, mImageOutputPath,
                        sourceWidth, sourceHeight, regionRect.left, regionRect.top,
                        cropOffsetX, cropOffsetY,
                        mCroppedImageWidth, mCroppedImageHeight,
                        mFineAngle, resizeScale,
                        mSourceRotationDegrees, mFlipHorizontally,
                        mExifInfo.getExifDegrees(), mExifInfo.getExifTranslation(),
                        mCancelled);
            } finally {
                if (!bitmap.isRecycled()) bitmap.recycle();
            }
        }

        try {
            ensureNotCancelled();
            bitmap = BitmapFactory.decodeFile(mImageInputPath, bitmapOptions());
            if (bitmap == null) return false;
            bitmap = ensureArgb8888(bitmap);
        } catch (Exception exception) {
            Log.e(TAG, "Full bitmap decode failed", exception);
            return false;
        }
        try {
            ensureNotCancelled();
            return cropBitmapRegionNative(
                    bitmap, mImageOutputPath,
                    bitmap.getWidth(), bitmap.getHeight(), 0, 0,
                    cropOffsetX, cropOffsetY,
                    mCroppedImageWidth, mCroppedImageHeight,
                    mFineAngle, resizeScale,
                    mSourceRotationDegrees, mFlipHorizontally,
                    mExifInfo.getExifDegrees(), mExifInfo.getExifTranslation(),
                    mCancelled);
        } finally {
            if (!bitmap.isRecycled()) bitmap.recycle();
        }
    }

    private void ensureNotCancelled() {
        if (mCancelled.get()) {
            throw new CancellationException("Crop cancelled");
        }
    }

    /**
     * Check whether an image should be cropped at all or just file can be copied to the destination path.
     * For each 1000 pixels there is one pixel of error due to matrix calculations etc.
     *
     * @param width  - crop area width
     * @param height - crop area height
     * @return - true if image must be cropped, false - if original image fits requirements
     */
    private boolean shouldCropGeometry(int width, int height) {
        int pixelError = 1;
        pixelError += Math.round(Math.max(width, height) / 1000f);
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0)
                || Math.abs(mCropRect.left - mCurrentImageRect.left) > pixelError
                || Math.abs(mCropRect.top - mCurrentImageRect.top) > pixelError
                || Math.abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError
                || Math.abs(mCropRect.right - mCurrentImageRect.right) > pixelError
                || mFineAngle != 0;
    }

    private void onPostExecute(@Nullable Throwable t) {
        if (mCropCallback != null) {
            if (t == null) {
                Uri uri = Uri.fromFile(new File(mImageOutputPath));
                mCropCallback.onBitmapCropped(uri, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight);
            } else {
                mCropCallback.onCropFailure(t);
            }
        }
    }

    public void execute() {
        TaskExecutor.execute(() -> {
            Throwable result = doInBackground();
            TaskExecutor.postToMain(() -> onPostExecute(result));
        });
    }

    public void cancel() {
        mCancelled.set(true);
    }

}
