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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final int MAX_ICC_PROFILE_BYTES = 8 * 1024 * 1024;
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

    public static void copyIccProfileToPng(
            String sourcePath,
            String imageOutputPath
    ) throws IOException {
        copyIccProfileToPng(sourcePath, imageOutputPath, null);
    }

    public static void copyIccProfileToPng(
            String sourcePath,
            String imageOutputPath,
            AtomicBoolean cancellationSignal
    ) throws IOException {
        throwIfCancelled(cancellationSignal);
        File source = new File(sourcePath);
        byte[] iccChunkData = extractPngIccChunkData(source, cancellationSignal);
        if (iccChunkData == null) {
            byte[] iccProfile = extractJpegIccProfile(source, cancellationSignal);
            if (iccProfile == null) {
                iccProfile = extractWebpIccProfile(source, cancellationSignal);
            }
            if (iccProfile != null) {
                iccChunkData = createPngIccChunkData(iccProfile);
            }
        }
        if (iccChunkData == null) {
            return;
        }

        insertPngIccChunk(
                new File(imageOutputPath), iccChunkData, cancellationSignal);
    }

    private static byte[] extractPngIccChunkData(
            File png,
            AtomicBoolean cancellationSignal
    ) throws IOException {
        try (InputStream input = new BufferedInputStream(new FileInputStream(png))) {
            byte[] signature = new byte[PNG_SIGNATURE.length];
            if (!readFully(input, signature) || !Arrays.equals(signature, PNG_SIGNATURE)) {
                return null;
            }

            byte[] header = new byte[8];
            byte[] copyBuffer = new byte[64 * 1024];
            while (readFully(input, header)) {
                throwIfCancelled(cancellationSignal);
                int length = readInt(header, 0);
                if (length < 0) return null;

                String type = new String(header, 4, 4, StandardCharsets.US_ASCII);
                if ("iCCP".equals(type)) {
                    if (length > MAX_ICC_PROFILE_BYTES) return null;
                    byte[] data = new byte[length];
                    return readFully(input, data) ? data : null;
                }
                if ("IDAT".equals(type)) return null;
                if (!copyExactly(
                        input, null, (long) length + 4, copyBuffer, cancellationSignal)) {
                    return null;
                }
                if ("IEND".equals(type)) return null;
            }
            return null;
        }
    }

    private static byte[] extractJpegIccProfile(
            File jpeg,
            AtomicBoolean cancellationSignal
    ) throws IOException {
        int expectedChunkCount = -1;
        TreeMap<Integer, byte[]> chunks = new TreeMap<>();
        byte[] copyBuffer = new byte[64 * 1024];
        try (InputStream input = new BufferedInputStream(new FileInputStream(jpeg))) {
            if (input.read() != SEGMENT_START_ID || input.read() != 0xD8) {
                return null;
            }

            while (true) {
                throwIfCancelled(cancellationSignal);
                if (input.read() != SEGMENT_START_ID) return null;
                int marker;
                do {
                    marker = input.read();
                } while (marker == SEGMENT_START_ID);
                if (marker < 0 || marker == SEGMENT_SOS || marker == MARKER_EOI) break;
                if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD9)) continue;

                int high = input.read();
                int low = input.read();
                if (high < 0 || low < 0) return null;
                int segmentLength = (high << 8) | low;
                if (segmentLength < 2) return null;
                int dataLength = segmentLength - 2;

                if (marker == ICC_SEGMENT_TYPE) {
                    byte[] data = new byte[dataLength];
                    if (!readFully(input, data)) return null;
                    int metadataLength = JPEG_ICC_SEGMENT_PREAMBLE_BYTES.length + 2;
                    if (dataLength >= metadataLength &&
                            startsWith(data, 0, JPEG_ICC_SEGMENT_PREAMBLE_BYTES)) {
                        int sequenceNumber = data[JPEG_ICC_SEGMENT_PREAMBLE_BYTES.length] & 0xFF;
                        int chunkCount = data[JPEG_ICC_SEGMENT_PREAMBLE_BYTES.length + 1] & 0xFF;
                        if (sequenceNumber > 0 && chunkCount > 0) {
                            expectedChunkCount = chunkCount;
                            chunks.put(
                                    sequenceNumber,
                                    Arrays.copyOfRange(data, metadataLength, data.length));
                        }
                    }
                } else if (!copyExactly(
                        input, null, dataLength, copyBuffer, cancellationSignal)) {
                    return null;
                }
            }
        }

        if (expectedChunkCount <= 0 || chunks.size() != expectedChunkCount) {
            return null;
        }

        long profileSize = 0;
        for (byte[] chunk : chunks.values()) {
            profileSize += chunk.length;
            if (profileSize > MAX_ICC_PROFILE_BYTES) return null;
        }
        byte[] profile = new byte[(int) profileSize];
        int profileOffset = 0;
        for (int i = 1; i <= expectedChunkCount; i++) {
            byte[] chunk = chunks.get(i);
            if (chunk == null) {
                return null;
            }
            System.arraycopy(chunk, 0, profile, profileOffset, chunk.length);
            profileOffset += chunk.length;
        }
        return profile;
    }

    private static byte[] extractWebpIccProfile(
            File webp,
            AtomicBoolean cancellationSignal
    ) throws IOException {
        try (InputStream input = new BufferedInputStream(new FileInputStream(webp))) {
            byte[] riffHeader = new byte[12];
            if (!readFully(input, riffHeader) ||
                    !matchesAscii(riffHeader, 0, "RIFF") ||
                    !matchesAscii(riffHeader, 8, "WEBP")) {
                return null;
            }

            byte[] chunkHeader = new byte[8];
            byte[] copyBuffer = new byte[64 * 1024];
            while (readFully(input, chunkHeader)) {
                throwIfCancelled(cancellationSignal);
                long length = readLittleEndianUInt32(chunkHeader, 4);
                if (length > Integer.MAX_VALUE) return null;
                if (matchesAscii(chunkHeader, 0, "ICCP")) {
                    if (length > MAX_ICC_PROFILE_BYTES) return null;
                    byte[] profile = new byte[(int) length];
                    return readFully(input, profile) ? profile : null;
                }
                if (matchesAscii(chunkHeader, 0, "VP8 ") ||
                        matchesAscii(chunkHeader, 0, "VP8L") ||
                        matchesAscii(chunkHeader, 0, "ANIM") ||
                        matchesAscii(chunkHeader, 0, "ANMF")) {
                    return null;
                }
                long paddedLength = length + (length & 1);
                if (!copyExactly(
                        input, null, paddedLength, copyBuffer, cancellationSignal)) return null;
            }
            return null;
        }
    }

    private static byte[] createPngIccChunkData(byte[] iccProfile) throws IOException {
        ByteArrayOutputStream chunkData = new ByteArrayOutputStream();
        byte[] profileName = "ICC Profile".getBytes(StandardCharsets.ISO_8859_1);
        chunkData.write(profileName);
        chunkData.write(0);
        chunkData.write(0);
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(chunkData)) {
            deflater.write(iccProfile);
        }
        return chunkData.toByteArray();
    }

    private static void insertPngIccChunk(
            File png,
            byte[] iccChunkData,
            AtomicBoolean cancellationSignal
    ) throws IOException {
        File parent = png.getParentFile();
        File temporary = File.createTempFile("icc_", ".png", parent);
        boolean inserted = false;
        try {
            try (InputStream input = new BufferedInputStream(new FileInputStream(png));
                 OutputStream output = new BufferedOutputStream(new FileOutputStream(temporary))) {
                byte[] signature = new byte[PNG_SIGNATURE.length];
                if (!readFully(input, signature) || !Arrays.equals(signature, PNG_SIGNATURE)) {
                    return;
                }
                output.write(signature);

                byte[] header = new byte[8];
                byte[] copyBuffer = new byte[64 * 1024];
                while (readFully(input, header)) {
                    throwIfCancelled(cancellationSignal);
                    int length = readInt(header, 0);
                    if (length < 0) {
                        return;
                    }
                    String type = new String(header, 4, 4, StandardCharsets.US_ASCII);
                    if (!inserted && "IDAT".equals(type)) {
                        writePngChunk(output, "iCCP", iccChunkData);
                        inserted = true;
                    }

                    boolean copyChunk = !isColorProfileChunk(type);
                    if (copyChunk) {
                        output.write(header);
                    }
                    if (!copyExactly(
                            input, copyChunk ? output : null, (long) length + 4, copyBuffer,
                            cancellationSignal)) {
                        return;
                    }
                    if ("IEND".equals(type)) {
                        break;
                    }
                }
            }

            if (!inserted) {
                return;
            }
            throwIfCancelled(cancellationSignal);
            if (!temporary.renameTo(png)) {
                throw new IOException("Unable to replace PNG after copying its ICC profile");
            }
        } finally {
            temporary.delete();
        }
    }

    private static boolean isColorProfileChunk(String type) {
        return "iCCP".equals(type) || "sRGB".equals(type) || "gAMA".equals(type) || "cHRM".equals(type);
    }

    private static void writePngChunk(OutputStream output, String type, byte[] data) throws IOException {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        writeInt(output, data.length);
        output.write(typeBytes);
        output.write(data);

        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        writeInt(output, (int) crc.getValue());
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

    private static boolean matchesAscii(byte[] bytes, int offset, String value) {
        if (offset < 0 || offset + value.length() > bytes.length) return false;
        for (int i = 0; i < value.length(); i++) {
            if (bytes[offset + i] != (byte) value.charAt(i)) return false;
        }
        return true;
    }

    private static boolean readFully(InputStream input, byte[] bytes) throws IOException {
        int offset = 0;
        while (offset < bytes.length) {
            int count = input.read(bytes, offset, bytes.length - offset);
            if (count < 0) return false;
            offset += count;
        }
        return true;
    }

    private static boolean copyExactly(
            InputStream input,
            OutputStream output,
            long byteCount,
            byte[] buffer,
            AtomicBoolean cancellationSignal
    ) throws IOException {
        long remaining = byteCount;
        while (remaining > 0) {
            throwIfCancelled(cancellationSignal);
            int count = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (count < 0) return false;
            if (output != null) {
                output.write(buffer, 0, count);
            }
            remaining -= count;
        }
        return true;
    }

    private static void throwIfCancelled(AtomicBoolean cancellationSignal) throws IOException {
        if (cancellationSignal != null && cancellationSignal.get()) {
            throw new InterruptedIOException("ICC copy cancelled");
        }
    }

    private static int readInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    private static long readLittleEndianUInt32(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFFL)
                | ((bytes[offset + 1] & 0xFFL) << 8)
                | ((bytes[offset + 2] & 0xFFL) << 16)
                | ((bytes[offset + 3] & 0xFFL) << 24);
    }

    private static void writeInt(OutputStream output, int value) throws IOException {
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
