/*
 * Copyright 2015 Google, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY GOOGLE, INC. ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GOOGLE, INC. OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Google, Inc.
 *
 * Adapted for the AdvancedCrop library.
 */

package com.t8rin.crop.advanced.util;

import android.text.TextUtils;
import android.util.Log;

import com.t8rin.exif.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

/**
 * A class for parsing the exif orientation from an image header.
 */
public class ImageHeaderParser {
    /**
     * A constant indicating we were unable to parse the orientation from the image either because
     * no exif segment containing orientation data existed, or because of an I/O error attempting to
     * read the exif segment.
     */
    public static final int UNKNOWN_ORIENTATION = -1;
    private static final String TAG = "ImageHeaderParser";
    private static final int EXIF_MAGIC_NUMBER = 0xFFD8;
    // "MM".
    private static final int MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D;
    // "II".
    private static final int INTEL_TIFF_MAGIC_NUMBER = 0x4949;
    private static final String JPEG_EXIF_SEGMENT_PREAMBLE = "Exif\0\0";
    private static final byte[] JPEG_EXIF_SEGMENT_PREAMBLE_BYTES =
            JPEG_EXIF_SEGMENT_PREAMBLE.getBytes(StandardCharsets.UTF_8);
    private static final byte[] JPEG_ICC_SEGMENT_PREAMBLE_BYTES =
            "ICC_PROFILE\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final int SEGMENT_SOS = 0xDA;
    private static final int MARKER_EOI = 0xD9;
    private static final int SEGMENT_START_ID = 0xFF;
    private static final int EXIF_SEGMENT_TYPE = 0xE1;
    private static final int ICC_SEGMENT_TYPE = 0xE2;
    private static final int ORIENTATION_TAG_TYPE = 0x0112;
    private static final int[] BYTES_PER_FORMAT = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};

    private final Reader reader;

    public ImageHeaderParser(InputStream is) {
        reader = new StreamReader(is);
    }

    private static int parseExifSegment(RandomAccessReader segmentData) {
        final int headerOffsetSize = JPEG_EXIF_SEGMENT_PREAMBLE.length();

        short byteOrderIdentifier = segmentData.getInt16(headerOffsetSize);
        final ByteOrder byteOrder;
        if (byteOrderIdentifier == MOTOROLA_TIFF_MAGIC_NUMBER) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (byteOrderIdentifier == INTEL_TIFF_MAGIC_NUMBER) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unknown endianness = " + byteOrderIdentifier);
            }
            byteOrder = ByteOrder.BIG_ENDIAN;
        }

        segmentData.order(byteOrder);

        int firstIfdOffset = segmentData.getInt32(headerOffsetSize + 4) + headerOffsetSize;
        int tagCount = segmentData.getInt16(firstIfdOffset);

        int tagOffset, tagType, formatCode, componentCount;
        for (int i = 0; i < tagCount; i++) {
            tagOffset = calcTagOffset(firstIfdOffset, i);
            tagType = segmentData.getInt16(tagOffset);

            // We only want orientation.
            if (tagType != ORIENTATION_TAG_TYPE) {
                continue;
            }

            formatCode = segmentData.getInt16(tagOffset + 2);

            // 12 is max format code.
            if (formatCode < 1 || formatCode > 12) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got invalid format code = " + formatCode);
                }
                continue;
            }

            componentCount = segmentData.getInt32(tagOffset + 4);

            if (componentCount < 0) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Negative tiff component count");
                }
                continue;
            }

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got tagIndex=" + i + " tagType=" + tagType + " formatCode=" + formatCode
                        + " componentCount=" + componentCount);
            }

            final int byteCount = componentCount + BYTES_PER_FORMAT[formatCode];

            if (byteCount > 4) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got byte count > 4, not orientation, continuing, formatCode=" + formatCode);
                }
                continue;
            }

            final int tagValueOffset = tagOffset + 8;

            if (tagValueOffset < 0 || tagValueOffset > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal tagValueOffset=" + tagValueOffset + " tagType=" + tagType);
                }
                continue;
            }

            if (byteCount < 0 || tagValueOffset + byteCount > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal number of bytes for TI tag data tagType=" + tagType);
                }
                continue;
            }

            //assume componentCount == 1 && fmtCode == 3
            return segmentData.getInt16(tagValueOffset);
        }

        return -1;
    }

    private static int calcTagOffset(int ifdOffset, int tagIndex) {
        return ifdOffset + 2 + 12 * tagIndex;
    }

    private static boolean handles(int imageMagicNumber) {
        return (imageMagicNumber & EXIF_MAGIC_NUMBER) == EXIF_MAGIC_NUMBER
                || imageMagicNumber == MOTOROLA_TIFF_MAGIC_NUMBER
                || imageMagicNumber == INTEL_TIFF_MAGIC_NUMBER;
    }

    public static void copyExif(ExifInterface originalExif, int width, int height, String imageOutputPath) {
        String[] attributes = new String[]{
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_FLASH,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                ExifInterface.TAG_GPS_PROCESSING_METHOD,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_SUBSEC_TIME,
                ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                ExifInterface.TAG_WHITE_BALANCE
        };

        try {
            ExifInterface newExif = new ExifInterface(imageOutputPath);
            String value;
            for (String attribute : attributes) {
                value = originalExif.getAttribute(attribute);
                if (!TextUtils.isEmpty(value)) {
                    newExif.setAttribute(attribute, value);
                }
            }
            newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(width));
            newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(height));
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, "0");

            newExif.saveAttributes();

        } catch (IOException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public static void copyIccProfileToPng(String sourcePath, String imageOutputPath) {
        try {
            byte[] source = readFile(sourcePath);
            byte[] iccChunkData = extractPngIccChunkData(source);
            if (iccChunkData == null) {
                byte[] jpegIccProfile = extractJpegIccProfile(source);
                if (jpegIccProfile != null) {
                    iccChunkData = createPngIccChunkData(jpegIccProfile);
                }
            }
            if (iccChunkData == null) {
                return;
            }

            byte[] output = readFile(imageOutputPath);
            byte[] outputWithIcc = insertPngIccChunk(output, iccChunkData);
            if (outputWithIcc != null) {
                writeFile(imageOutputPath, outputWithIcc);
            }
        } catch (IOException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private static byte[] extractPngIccChunkData(byte[] png) {
        if (!isPng(png)) {
            return null;
        }

        int offset = PNG_SIGNATURE.length;
        while (offset + 8 <= png.length) {
            int length = readInt(png, offset);
            int dataStart = offset + 8;
            int dataEnd = dataStart + length;
            int chunkEnd = dataEnd + 4;
            if (length < 0 || chunkEnd > png.length) {
                return null;
            }

            String type = new String(png, offset + 4, 4, StandardCharsets.US_ASCII);
            if ("iCCP".equals(type)) {
                return Arrays.copyOfRange(png, dataStart, dataEnd);
            }
            if ("IEND".equals(type)) {
                return null;
            }
            offset = chunkEnd;
        }
        return null;
    }

    private static byte[] extractJpegIccProfile(byte[] jpeg) throws IOException {
        if (jpeg.length < 4 || readUInt16(jpeg, 0) != EXIF_MAGIC_NUMBER) {
            return null;
        }

        int expectedChunkCount = -1;
        TreeMap<Integer, byte[]> chunks = new TreeMap<>();
        int offset = 2;
        while (offset + 4 <= jpeg.length) {
            if ((jpeg[offset] & 0xFF) != SEGMENT_START_ID) {
                return null;
            }
            while (offset < jpeg.length && (jpeg[offset] & 0xFF) == SEGMENT_START_ID) {
                offset++;
            }
            if (offset >= jpeg.length) {
                return null;
            }

            int marker = jpeg[offset++] & 0xFF;
            if (marker == SEGMENT_SOS || marker == MARKER_EOI) {
                break;
            }
            if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD9)) {
                continue;
            }
            if (offset + 2 > jpeg.length) {
                return null;
            }

            int segmentLength = readUInt16(jpeg, offset);
            int dataStart = offset + 2;
            int dataEnd = offset + segmentLength;
            if (segmentLength < 2 || dataEnd > jpeg.length) {
                return null;
            }

            if (marker == ICC_SEGMENT_TYPE
                    && startsWith(jpeg, dataStart, JPEG_ICC_SEGMENT_PREAMBLE_BYTES)
                    && dataStart + JPEG_ICC_SEGMENT_PREAMBLE_BYTES.length + 2 <= dataEnd) {
                int sequenceNumber = jpeg[dataStart + JPEG_ICC_SEGMENT_PREAMBLE_BYTES.length] & 0xFF;
                int chunkCount = jpeg[dataStart + JPEG_ICC_SEGMENT_PREAMBLE_BYTES.length + 1] & 0xFF;
                int profileStart = dataStart + JPEG_ICC_SEGMENT_PREAMBLE_BYTES.length + 2;
                if (sequenceNumber > 0 && chunkCount > 0) {
                    expectedChunkCount = chunkCount;
                    chunks.put(sequenceNumber, Arrays.copyOfRange(jpeg, profileStart, dataEnd));
                }
            }
            offset = dataEnd;
        }

        if (expectedChunkCount <= 0 || chunks.size() != expectedChunkCount) {
            return null;
        }

        ByteArrayOutputStream profile = new ByteArrayOutputStream();
        for (int i = 1; i <= expectedChunkCount; i++) {
            byte[] chunk = chunks.get(i);
            if (chunk == null) {
                return null;
            }
            profile.write(chunk);
        }
        return profile.toByteArray();
    }

    private static byte[] createPngIccChunkData(byte[] iccProfile) throws IOException {
        ByteArrayOutputStream compressedProfile = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressedProfile)) {
            deflater.write(iccProfile);
        }

        ByteArrayOutputStream chunkData = new ByteArrayOutputStream();
        byte[] profileName = "ICC Profile".getBytes(StandardCharsets.ISO_8859_1);
        chunkData.write(profileName);
        chunkData.write(0);
        chunkData.write(0);
        compressedProfile.writeTo(chunkData);
        return chunkData.toByteArray();
    }

    private static byte[] insertPngIccChunk(byte[] png, byte[] iccChunkData) throws IOException {
        if (!isPng(png)) {
            return null;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(png.length + iccChunkData.length + 12);
        output.write(png, 0, PNG_SIGNATURE.length);

        boolean inserted = false;
        int offset = PNG_SIGNATURE.length;
        while (offset + 8 <= png.length) {
            int length = readInt(png, offset);
            int dataStart = offset + 8;
            int dataEnd = dataStart + length;
            int chunkEnd = dataEnd + 4;
            if (length < 0 || chunkEnd > png.length) {
                return null;
            }

            String type = new String(png, offset + 4, 4, StandardCharsets.US_ASCII);
            if (!inserted && "IDAT".equals(type)) {
                writePngChunk(output, "iCCP", iccChunkData);
                inserted = true;
            }
            if (!isColorProfileChunk(type)) {
                output.write(png, offset, chunkEnd - offset);
            }
            offset = chunkEnd;
            if ("IEND".equals(type)) {
                break;
            }
        }

        return inserted ? output.toByteArray() : null;
    }

    private static boolean isColorProfileChunk(String type) {
        return "iCCP".equals(type) || "sRGB".equals(type) || "gAMA".equals(type) || "cHRM".equals(type);
    }

    private static void writePngChunk(ByteArrayOutputStream output, String type, byte[] data) throws IOException {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        writeInt(output, data.length);
        output.write(typeBytes);
        output.write(data);

        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        writeInt(output, (int) crc.getValue());
    }

    private static boolean isPng(byte[] bytes) {
        return startsWith(bytes, 0, PNG_SIGNATURE);
    }

    private static boolean startsWith(byte[] bytes, int offset, byte[] prefix) {
        if (offset < 0 || offset + prefix.length > bytes.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[offset + i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] readFile(String path) throws IOException {
        try (FileInputStream input = new FileInputStream(new File(path));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        }
    }

    private static void writeFile(String path, byte[] bytes) throws IOException {
        try (FileOutputStream output = new FileOutputStream(new File(path))) {
            output.write(bytes);
        }
    }

    private static int readInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    private static int readUInt16(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    private static void writeInt(ByteArrayOutputStream output, int value) {
        output.write((value >> 24) & 0xFF);
        output.write((value >> 16) & 0xFF);
        output.write((value >> 8) & 0xFF);
        output.write(value & 0xFF);
    }

    /**
     * Parse the orientation from the image header. If it doesn't handle this image type (or this is
     * not an image) it will return a default value rather than throwing an exception.
     *
     * @return The exif orientation if present or -1 if the header couldn't be parsed or doesn't
     * contain an orientation
     */
    public int getOrientation() throws IOException {
        final int magicNumber = reader.getUInt16();

        if (!handles(magicNumber)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Parser doesn't handle magic number: " + magicNumber);
            }
            return UNKNOWN_ORIENTATION;
        } else {
            int exifSegmentLength = moveToExifSegmentAndGetLength();
            if (exifSegmentLength == -1) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Failed to parse exif segment length, or exif segment not found");
                }
                return UNKNOWN_ORIENTATION;
            }

            byte[] exifData = new byte[exifSegmentLength];
            return parseExifSegment(exifData, exifSegmentLength);
        }
    }

    private int parseExifSegment(byte[] tempArray, int exifSegmentLength) throws IOException {
        int read = reader.read(tempArray, exifSegmentLength);
        if (read != exifSegmentLength) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unable to read exif segment data"
                        + ", length: " + exifSegmentLength
                        + ", actually read: " + read);
            }
            return UNKNOWN_ORIENTATION;
        }

        boolean hasJpegExifPreamble = hasJpegExifPreamble(tempArray, exifSegmentLength);
        if (hasJpegExifPreamble) {
            return parseExifSegment(new RandomAccessReader(tempArray, exifSegmentLength));
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Missing jpeg exif preamble");
            }
            return UNKNOWN_ORIENTATION;
        }
    }

    private boolean hasJpegExifPreamble(byte[] exifData, int exifSegmentLength) {
        boolean result =
                exifData != null && exifSegmentLength > JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.length;
        if (result) {
            for (int i = 0; i < JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.length; i++) {
                if (exifData[i] != JPEG_EXIF_SEGMENT_PREAMBLE_BYTES[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Moves reader to the start of the exif segment and returns the length of the exif segment or
     * {@code -1} if no exif segment is found.
     */
    private int moveToExifSegmentAndGetLength() throws IOException {
        short segmentId, segmentType;
        int segmentLength;
        while (true) {
            segmentId = reader.getUInt8();
            if (segmentId != SEGMENT_START_ID) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Unknown segmentId=" + segmentId);
                }
                return -1;
            }

            segmentType = reader.getUInt8();

            if (segmentType == SEGMENT_SOS) {
                return -1;
            } else if (segmentType == MARKER_EOI) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Found MARKER_EOI in exif segment");
                }
                return -1;
            }

            // Segment length includes bytes for segment length.
            segmentLength = reader.getUInt16() - 2;

            if (segmentType != EXIF_SEGMENT_TYPE) {
                long skipped = reader.skip(segmentLength);
                if (skipped != segmentLength) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Unable to skip enough data"
                                + ", type: " + segmentType
                                + ", wanted to skip: " + segmentLength
                                + ", but actually skipped: " + skipped);
                    }
                    return -1;
                }
            } else {
                return segmentLength;
            }
        }
    }

    private interface Reader {
        int getUInt16() throws IOException;

        short getUInt8() throws IOException;

        long skip(long total) throws IOException;

        int read(byte[] buffer, int byteCount) throws IOException;
    }

    private static class RandomAccessReader {
        private final ByteBuffer data;

        public RandomAccessReader(byte[] data, int length) {
            this.data = (ByteBuffer) ByteBuffer.wrap(data)
                    .order(ByteOrder.BIG_ENDIAN)
                    .limit(length);
        }

        public void order(ByteOrder byteOrder) {
            this.data.order(byteOrder);
        }

        public int length() {
            return data.remaining();
        }

        public int getInt32(int offset) {
            return data.getInt(offset);
        }

        public short getInt16(int offset) {
            return data.getShort(offset);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class StreamReader implements Reader {
        private final InputStream is;

        StreamReader(InputStream is) {
            this.is = is;
        }

        // Motorola / big endian byte order.

        @Override
        public int getUInt16() throws IOException {
            return (is.read() << 8 & 0xFF00) | (is.read() & 0xFF);
        }

        @Override
        public short getUInt8() throws IOException {
            return (short) (is.read() & 0xFF);
        }

        @Override
        public long skip(long total) throws IOException {
            if (total < 0) {
                return 0;
            }

            long toSkip = total;
            while (toSkip > 0) {
                long skipped = is.skip(toSkip);
                if (skipped > 0) {
                    toSkip -= skipped;
                } else {
                    // Skip has no specific contract as to what happens when you reach the end of
                    // the stream. To differentiate between temporarily not having more data and
                    // having finished the stream, we read a single byte when we fail to skip any
                    // amount of data.
                    int testEofByte = is.read();
                    if (testEofByte == -1) {
                        break;
                    } else {
                        toSkip--;
                    }
                }
            }
            return total - toSkip;
        }

        @Override
        public int read(byte[] buffer, int byteCount) throws IOException {
            int toRead = byteCount;
            int read;
            while (toRead > 0 && ((read = is.read(buffer, byteCount - toRead, toRead)) != -1)) {
                toRead -= read;
            }
            return byteCount - toRead;
        }
    }

}
