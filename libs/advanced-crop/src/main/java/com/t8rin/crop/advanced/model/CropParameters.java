package com.t8rin.crop.advanced.model;

import android.graphics.Bitmap;

public class CropParameters {

    private final int mMaxResultImageSizeX;
    private final int mMaxResultImageSizeY;

    private final Bitmap.CompressFormat mCompressFormat;
    private final int mCompressQuality;
    private final String mImageInputPath;
    private final String mImageOutputPath;
    private final ExifInfo mExifInfo;
    private final int mSourceRotationDegrees;
    private final boolean mFlipHorizontally;

    public CropParameters(int maxResultImageSizeX, int maxResultImageSizeY,
                          Bitmap.CompressFormat compressFormat, int compressQuality,
                          String imageInputPath, String imageOutputPath, ExifInfo exifInfo) {
        this(maxResultImageSizeX, maxResultImageSizeY, compressFormat, compressQuality,
                imageInputPath, imageOutputPath, exifInfo, 0, false);
    }

    public CropParameters(int maxResultImageSizeX, int maxResultImageSizeY,
                          Bitmap.CompressFormat compressFormat, int compressQuality,
                          String imageInputPath, String imageOutputPath, ExifInfo exifInfo,
                          int sourceRotationDegrees, boolean flipHorizontally) {
        mMaxResultImageSizeX = maxResultImageSizeX;
        mMaxResultImageSizeY = maxResultImageSizeY;
        mCompressFormat = compressFormat;
        mCompressQuality = compressQuality;
        mImageInputPath = imageInputPath;
        mImageOutputPath = imageOutputPath;
        mExifInfo = exifInfo;
        mSourceRotationDegrees = sourceRotationDegrees;
        mFlipHorizontally = flipHorizontally;
    }

    public int getMaxResultImageSizeX() {
        return mMaxResultImageSizeX;
    }

    public int getMaxResultImageSizeY() {
        return mMaxResultImageSizeY;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return mCompressFormat;
    }

    public int getCompressQuality() {
        return mCompressQuality;
    }

    public String getImageInputPath() {
        return mImageInputPath;
    }

    public String getImageOutputPath() {
        return mImageOutputPath;
    }

    public ExifInfo getExifInfo() {
        return mExifInfo;
    }

    public int getSourceRotationDegrees() {
        return mSourceRotationDegrees;
    }

    public boolean isFlipHorizontally() {
        return mFlipHorizontally;
    }

}
