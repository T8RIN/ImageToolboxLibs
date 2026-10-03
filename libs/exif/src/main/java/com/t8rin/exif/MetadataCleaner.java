/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.t8rin.exif;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Container metadata removal; compressed image data is copied verbatim.
 */
final class MetadataCleaner {

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a
    };
    private static final byte[] EXIF_IDENTIFIER = "Exif\0\0"
            .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] XMP_IDENTIFIER = "http://ns.adobe.com/xap/1.0/\0"
            .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] EXTENDED_XMP_IDENTIFIER = "http://ns.adobe.com/xmp/extension/\0"
            .getBytes(StandardCharsets.US_ASCII);
    private static final int BUFFER_SIZE = 64 * 1024;

    private MetadataCleaner() {
    }

    static void remove(File source, OutputStream output) throws IOException {
        byte[] signature = new byte[512];
        try (DataInputStream input = new DataInputStream(new FileInputStream(source))) {
            int length = 0;
            int count;
            while (length < signature.length
                    && (count = input.read(signature, length, signature.length - length)) > 0) {
                length += count;
            }
            signature = Arrays.copyOf(signature, length);
        }
        if (ExtendedExifContainer.isTiff(signature)) {
            ExtendedExifContainer.clearTiffMetadata(source, output);
        } else if (ExtendedExifContainer.isJxl(signature)) {
            ExtendedExifContainer.clearBoxMetadata(source, output, true);
        } else if (ExtendedExifContainer.isJp2(signature)) {
            ExtendedExifContainer.clearBoxMetadata(source, output, false);
        } else if (ExtendedExifContainer.detectIsoBmffImageType(signature)
                != ExtendedExifContainer.ISO_BMFF_TYPE_UNKNOWN) {
            ExtendedExifContainer.clearIsoBmffMetadata(source, output);
        } else {
            try (InputStream input = new FileInputStream(source)) {
                remove(input, output);
            }
        }
    }

    static void remove(InputStream input, OutputStream output) throws IOException {
        DataInputStream data = new DataInputStream(new BufferedInputStream(input, BUFFER_SIZE));
        int signature = data.readUnsignedShort();
        if (signature == 0xffd8) {
            removeJpeg(data, output);
        } else if (signature == 0x8950) {
            byte[] rest = new byte[6];
            data.readFully(rest);
            require(Arrays.equals(rest, Arrays.copyOfRange(PNG_SIGNATURE, 2, 8)),
                    "Invalid PNG signature");
            removePng(data, output);
        } else if (signature == 0x5249) {
            require(data.readUnsignedShort() == 0x4646, "Invalid RIFF signature");
            long size = readLittleEndianInt(data);
            require("WEBP".equals(readType(data)), "Unsupported RIFF format");
            removeWebp(data, output, size);
        } else {
            throw new IOException("Metadata removal supports JPEG, PNG and WebP only");
        }
    }

    private static void removeJpeg(DataInputStream input, OutputStream output) throws IOException {
        output.write(0xff);
        output.write(0xd8);
        int marker = readJpegMarker(input, output);
        while (true) {
            if (marker == 0xd9) {
                output.write(0xff);
                output.write(marker);
                return;
            }
            require(marker != 0xd8 && marker != 0, "Invalid JPEG marker");
            if (marker == 0x01 || (marker >= 0xd0 && marker <= 0xd7)) {
                output.write(0xff);
                output.write(marker);
                marker = readJpegMarker(input, output);
                continue;
            }
            int length = input.readUnsignedShort();
            require(length >= 2, "Invalid JPEG segment length");
            byte[] payload = new byte[length - 2];
            input.readFully(payload);
            boolean metadata = marker == 0xfe || marker == 0xed
                    || (marker == 0xe1 && (startsWith(payload, EXIF_IDENTIFIER)
                    || startsWith(payload, XMP_IDENTIFIER)
                    || startsWith(payload, EXTENDED_XMP_IDENTIFIER)))
                    // JPEG XT repeats the box header in each APP11 fragment. Only JUMBF boxes
                    // are metadata; other APP11 boxes can contain JPEG XT image data.
                    || (marker == 0xeb && payload.length >= 16
                    && payload[0] == 'J' && payload[1] == 'P'
                    && payload[12] == 'j' && payload[13] == 'u'
                    && payload[14] == 'm' && payload[15] == 'b');
            if (!metadata) {
                output.write(0xff);
                output.write(marker);
                output.write(length >> 8);
                output.write(length);
                output.write(payload);
            }
            // Walk all scans, including markers between progressive scans. Copying everything
            // after the first SOS would leave metadata placed later in the JPEG behind.
            marker = marker == 0xda || marker == 0xdc
                    ? copyJpegScan(input, output) : readJpegMarker(input, output);
        }
    }

    private static int readJpegMarker(DataInputStream input, OutputStream output)
            throws IOException {
        require(input.readUnsignedByte() == 0xff, "Invalid JPEG marker");
        int marker = input.readUnsignedByte();
        while (marker == 0xff) {
            output.write(0xff);
            marker = input.readUnsignedByte();
        }
        return marker;
    }

    private static int copyJpegScan(DataInputStream input, OutputStream output) throws IOException {
        while (true) {
            int value = input.readUnsignedByte();
            if (value != 0xff) {
                output.write(value);
                continue;
            }
            int marker = input.readUnsignedByte();
            while (marker == 0xff) {
                output.write(0xff);
                marker = input.readUnsignedByte();
            }
            if (marker == 0 || marker == 0x01 || (marker >= 0xd0 && marker <= 0xd7)) {
                output.write(0xff);
                output.write(marker);
            } else {
                return marker;
            }
        }
    }

    private static void removePng(DataInputStream input, OutputStream output) throws IOException {
        output.write(PNG_SIGNATURE);
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean firstChunk = true;
        boolean hasImageData = false;
        while (true) {
            int length = input.readInt();
            require(length >= 0, "Invalid PNG chunk length");
            String type = readType(input);
            if (firstChunk) {
                require("IHDR".equals(type) && length == 13, "Invalid PNG header");
                firstChunk = false;
            }
            boolean metadata = "eXIf".equals(type) || "caBX".equals(type)
                    || "tEXt".equals(type) || "zTXt".equals(type) || "iTXt".equals(type)
                    || "tIME".equals(type);
            if (!metadata) {
                writeBigEndianInt(output, length);
                writeType(output, type);
            }
            transfer(input, metadata ? null : output, (long) length + 4, buffer);
            hasImageData |= "IDAT".equals(type);
            if ("IEND".equals(type)) {
                require(length == 0 && hasImageData, "Invalid PNG end chunk");
                return;
            }
        }
    }

    private static void removeWebp(DataInputStream input, OutputStream output, long riffSize)
            throws IOException {
        require(riffSize >= 4, "Invalid WebP RIFF size");
        File chunks = File.createTempFile("webp-metadata", ".tmp");
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            try (OutputStream body = new BufferedOutputStream(new FileOutputStream(chunks))) {
                long remaining = riffSize - 4;
                while (remaining > 0) {
                    require(remaining >= 8, "Truncated WebP chunk header");
                    String type = readType(input);
                    long length = readLittleEndianInt(input);
                    long paddedLength = length + (length & 1);
                    require(paddedLength <= remaining - 8, "Invalid WebP chunk length");
                    boolean metadata = "EXIF".equals(type) || "XMP ".equals(type)
                            || "C2PA".equals(type);
                    if (!metadata) {
                        writeType(body, type);
                        writeLittleEndianInt(body, length);
                    }
                    if ("VP8X".equals(type)) {
                        require(length == 10, "Invalid WebP VP8X length");
                        body.write(input.readUnsignedByte() & ~0x0c); // Clear EXIF and XMP flags.
                        transfer(input, body, paddedLength - 1, buffer);
                    } else {
                        transfer(input, metadata ? null : body, paddedLength, buffer);
                    }
                    remaining -= 8 + paddedLength;
                }
            }
            writeType(output, "RIFF");
            writeLittleEndianInt(output, chunks.length() + 4);
            writeType(output, "WEBP");
            try (InputStream body = new FileInputStream(chunks)) {
                transfer(body, output, chunks.length(), buffer);
            }
        } finally {
            chunks.delete();
        }
    }

    private static void transfer(InputStream input, OutputStream output, long length, byte[] buffer)
            throws IOException {
        while (length > 0) {
            int count = input.read(buffer, 0, (int) Math.min(length, buffer.length));
            if (count < 0) {
                throw new EOFException("Truncated image data");
            }
            if (count == 0) {
                int value = input.read();
                if (value < 0) {
                    throw new EOFException("Truncated image data");
                }
                buffer[0] = (byte) value;
                count = 1;
            }
            if (output != null) {
                output.write(buffer, 0, count);
            }
            length -= count;
        }
    }

    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static String readType(DataInputStream input) throws IOException {
        byte[] bytes = new byte[4];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private static void writeType(OutputStream output, String type) throws IOException {
        output.write(type.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static long readLittleEndianInt(DataInputStream input) throws IOException {
        return Integer.reverseBytes(input.readInt()) & 0xffffffffL;
    }

    private static void writeLittleEndianInt(OutputStream output, long value) throws IOException {
        for (int i = 0; i < 4; i++) {
            output.write((int) (value >> (8 * i)) & 0xff);
        }
    }

    private static void writeBigEndianInt(OutputStream output, int value) throws IOException {
        for (int i = 3; i >= 0; i--) {
            output.write((value >> (8 * i)) & 0xff);
        }
    }

    private static void require(boolean condition, String message) throws IOException {
        if (!condition) {
            throw new IOException(message);
        }
    }
}
