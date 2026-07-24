/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.t8rin.exif;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Container-level EXIF reader/writer used internally by {@link ExifInterface}.
 *
 * <p>The class intentionally knows nothing about individual EXIF tags. It only extracts or
 * replaces the TIFF payload which ExifInterface already parses and serializes.</p>
 */
final class ExtendedExifContainer {

    static final int ISO_BMFF_TYPE_UNKNOWN = 0;
    static final int ISO_BMFF_TYPE_HEIF = 1;
    static final int ISO_BMFF_TYPE_AVIF = 2;

    private static final byte[] JXL_CONTAINER_SIGNATURE = new byte[] {
            0, 0, 0, 12, 'J', 'X', 'L', ' ', 0x0d, 0x0a, (byte) 0x87, 0x0a
    };
    private static final byte[] JXL_CODESTREAM_SIGNATURE = new byte[] {(byte) 0xff, 0x0a};
    private static final byte[] JP2_SIGNATURE = new byte[] {
            0, 0, 0, 12, 'j', 'P', ' ', ' ', 0x0d, 0x0a, (byte) 0x87, 0x0a
    };

    // De-facto EXIF UUID written by ExifTool. The 16-byte ASCII string itself is the UUID.
    private static final byte[] JP2_EXIF_UUID = "JpgTiffExif->JP2"
            .getBytes(StandardCharsets.ISO_8859_1);
    // Adobe JPEG2000 plug-in UUID; accepted on read, normalized to the ExifTool UUID on write.
    private static final byte[] JP2_ADOBE_EXIF_UUID = new byte[] {
            0x05, 0x37, (byte) 0xcd, (byte) 0xab,
            (byte) 0x9d, 0x0c, 0x44, 0x31,
            (byte) 0xa7, 0x2a, (byte) 0xfa, 0x56,
            0x1f, 0x2a, 0x11, 0x3e
    };

    private static final byte[] EXIF_APP1_IDENTIFIER = new byte[] {
            'E', 'x', 'i', 'f', 0, 0
    };

    // Private ISO-BMFF user type used as a lossless compatibility channel for our own reader.
    // For AVIF the standards-shaped Exif item is still written. For HEIC/HEIF this fallback is
    // deliberately authoritative because real encoders use several incompatible meta/iloc dialects.
    // Unknown uuid boxes are ignored by conforming decoders and do not alter the image bitstream.
    private static final byte[] ISO_BMFF_EXIF_FALLBACK_UUID = new byte[] {
            0x49, 0x54, 0x42, 0x58, 0x2d, 0x45, 0x58, 0x49,
            0x46, 0x2d, 0x56, 0x36, 0x00, 0x00, 0x00, 0x01
    };

    private static final int TIFF_MAGIC = 42;
    private static final int TIFF_TAG_SUB_IFDS = 330;
    private static final int TIFF_TAG_EXIF_IFD = 34665;
    private static final int TIFF_TAG_GPS_IFD = 34853;
    private static final int TIFF_TAG_INTEROP_IFD = 40965;
    private static final int TIFF_TAG_JPEG_INTERCHANGE_FORMAT = 513;

    private static final int[] TIFF_TYPE_SIZES = {
            0, // 0 invalid
            1, // BYTE
            1, // ASCII
            2, // SHORT
            4, // LONG
            8, // RATIONAL
            1, // SBYTE
            1, // UNDEFINED
            2, // SSHORT
            4, // SLONG
            8, // SRATIONAL
            4, // FLOAT
            8  // DOUBLE
    };


    /** Primary-IFD tags managed by ExifInterface rather than required to decode TIFF pixels. */
    private static final Set<Integer> TIFF_PRIMARY_METADATA_TAGS = new HashSet<>(Arrays.asList(
            270,   // ImageDescription
            271,   // Make
            272,   // Model
            274,   // Orientation
            282,   // XResolution
            283,   // YResolution
            285,   // PageName
            286,   // XPosition
            287,   // YPosition
            296,   // ResolutionUnit
            305,   // Software
            306,   // DateTime
            315,   // Artist
            316,   // HostComputer
            700,   // XMP
            18246, // Rating
            18249, // RatingPercent
            33432, // Copyright
            33723, // IPTC/NAA
            34377, // Photoshop image resources
            TIFF_TAG_EXIF_IFD,
            TIFF_TAG_GPS_IFD,
            40091, 40092, 40093, 40094, 40095 // Windows XP tags
    ));


    /**
     * Primary-IFD entries whose values describe where/how the existing pixel stream is decoded.
     * Metadata editing must never replace or remove these without rewriting the pixel data too.
     *
     * <p>PlanarConfiguration (284) is intentionally not protected: TIFF defines a default of 1,
     * and ImageToolbox exposes it as an editable tag. Other optional descriptive TIFF tags remain
     * editable as well.</p>
     */
    private static final Set<Integer> TIFF_IMMUTABLE_IMAGE_STRUCTURE_TAGS =
            new HashSet<>(Arrays.asList(
                    254,   // NewSubfileType (page/subimage role)
                    255,   // SubfileType
                    256,   // ImageWidth
                    257,   // ImageLength
                    258,   // BitsPerSample
                    259,   // Compression
                    262,   // PhotometricInterpretation
                    266,   // FillOrder
                    273,   // StripOffsets
                    277,   // SamplesPerPixel
                    278,   // RowsPerStrip
                    279,   // StripByteCounts
                    292,   // T4Options
                    293,   // T6Options
                    317,   // Predictor
                    320,   // ColorMap
                    322,   // TileWidth
                    323,   // TileLength
                    324,   // TileOffsets
                    325,   // TileByteCounts
                    330,   // SubIFDs
                    338,   // ExtraSamples
                    339,   // SampleFormat
                    347,   // JPEGTables
                    512,   // JPEGProc
                    513,   // JPEGInterchangeFormat
                    514,   // JPEGInterchangeFormatLength
                    515,   // JPEGRestartInterval
                    519,   // JPEGQTables
                    520,   // JPEGDCTables
                    521,   // JPEGACTables
                    529,   // YCbCrCoefficients
                    530,   // YCbCrSubSampling
                    531,   // YCbCrPositioning
                    532    // ReferenceBlackWhite
            ));

    private ExtendedExifContainer() {
    }

    static final class ExtractedExif {
        final byte[] tiffPayload;
        final long absoluteTiffOffset;

        ExtractedExif(byte[] tiffPayload, long absoluteTiffOffset) {
            this.tiffPayload = tiffPayload;
            this.absoluteTiffOffset = absoluteTiffOffset;
        }
    }

    /**
     * Detects AVIF and the broader HEIF family from the ISO-BMFF ftyp box.
     *
     * <p>The platform-style detector historically required the exact mif1+heic pair. That misses
     * perfectly valid files produced with mif2/miaf, HEVC auxiliary brands, and VVC brands. AVIF
     * is checked first because mif1/miaf can also appear as compatible brands in AVIF files.</p>
     */
    static int detectIsoBmffImageType(byte[] signature) {
        if (signature == null || signature.length < 12) {
            return ISO_BMFF_TYPE_UNKNOWN;
        }

        int position = 0;
        while (position + 8 <= signature.length) {
            long size = readUnsignedUnchecked(signature, position, 4);
            String type = new String(
                    signature, position + 4, 4, StandardCharsets.ISO_8859_1
            );
            int header = 8;
            if (size == 1) {
                if (position + 16 > signature.length) {
                    return ISO_BMFF_TYPE_UNKNOWN;
                }
                size = readUnsignedUnchecked(signature, position + 8, 8);
                header = 16;
            } else if (size == 0) {
                size = signature.length - position;
            }
            if (size < header || size > Integer.MAX_VALUE) {
                return ISO_BMFF_TYPE_UNKNOWN;
            }
            int boxEnd = (int) Math.min((long) signature.length, position + size);
            if ("ftyp".equals(type) && boxEnd - (position + header) >= 8) {
                boolean avif = false;
                boolean heif = false;
                int brandsStart = position + header;
                // major_brand, then compatible_brands after minor_version.
                for (int brandPosition = brandsStart; brandPosition + 4 <= boxEnd; ) {
                    if (brandPosition == brandsStart + 4) {
                        brandPosition += 4; // skip minor_version
                        continue;
                    }
                    String brand = new String(
                            signature, brandPosition, 4, StandardCharsets.ISO_8859_1
                    );
                    if ("avif".equals(brand) || "avis".equals(brand)) {
                        avif = true;
                    } else if (isHeifBrand(brand)) {
                        heif = true;
                    }
                    brandPosition += 4;
                }
                if (avif) {
                    return ISO_BMFF_TYPE_AVIF;
                }
                return heif ? ISO_BMFF_TYPE_HEIF : ISO_BMFF_TYPE_UNKNOWN;
            }
            if (size == 0 || position + size > signature.length) {
                break;
            }
            position += (int) size;
        }
        return ISO_BMFF_TYPE_UNKNOWN;
    }

    private static boolean isHeifBrand(String brand) {
        switch (brand) {
            case "mif1":
            case "mif2":
            case "msf1":
            case "miaf":
            case "heic":
            case "heix":
            case "hevc":
            case "hevx":
            case "heim":
            case "heis":
            case "hevm":
            case "hevs":
            case "vvc1":
            case "vvi1":
            case "vvic":
            case "vvix":
                return true;
            default:
                return false;
        }
    }

    private static long readUnsignedUnchecked(byte[] data, int offset, int width) {
        long value = 0;
        for (int i = 0; i < width && offset + i < data.length; i++) {
            value = (value << 8) | (data[offset + i] & 0xffL);
        }
        return value;
    }

    static boolean isJxl(byte[] signature) {
        return startsWith(signature, JXL_CODESTREAM_SIGNATURE)
                || startsWith(signature, JXL_CONTAINER_SIGNATURE);
    }

    static boolean isJp2(byte[] signature) {
        return startsWith(signature, JP2_SIGNATURE);
    }

    static boolean isTiff(byte[] signature) {
        if (signature.length < 4) {
            return false;
        }
        return (signature[0] == 'I' && signature[1] == 'I'
                && (signature[2] & 0xff) == 42 && signature[3] == 0)
                || (signature[0] == 'M' && signature[1] == 'M'
                && signature[2] == 0 && (signature[3] & 0xff) == 42);
    }

    static ExtractedExif readJxl(InputStream input) throws IOException {
        return readJxl(readAll(input));
    }

    static ExtractedExif readJxl(byte[] file) throws IOException {
        if (startsWith(file, JXL_CODESTREAM_SIGNATURE)) {
            return null;
        }
        require(startsWith(file, JXL_CONTAINER_SIGNATURE), "Invalid JPEG XL container");
        for (Box box : parseBoxes(file, 0, file.length)) {
            if ("Exif".equals(box.type)) {
                return extractOffsetPrefixedTiff(file, box.payloadStart(), box.end);
            }
        }
        return null;
    }

    static void writeJxl(InputStream input, OutputStream output, byte[] tiffPayload)
            throws IOException {
        byte[] file = readAll(input);
        require(isTiff(tiffPayload), "Invalid TIFF payload");
        byte[] exifBox = makeBox("Exif", addTiffOffsetPrefix(tiffPayload));

        if (startsWith(file, JXL_CODESTREAM_SIGNATURE)) {
            output.write(JXL_CONTAINER_SIGNATURE);
            output.write(makeBox("ftyp", concat(
                    ascii("jxl "),
                    new byte[] {0, 0, 0, 0},
                    ascii("jxl ")
            )));
            output.write(exifBox);
            output.write(makeBox("jxlc", file));
            return;
        }

        require(startsWith(file, JXL_CONTAINER_SIGNATURE), "Invalid JPEG XL container");
        boolean inserted = false;
        for (Box box : parseBoxes(file, 0, file.length)) {
            if ("Exif".equals(box.type)) {
                if (!inserted) {
                    output.write(exifBox);
                    inserted = true;
                }
                continue;
            }
            output.write(file, box.start, box.size());
            if (!inserted && "ftyp".equals(box.type)) {
                output.write(exifBox);
                inserted = true;
            }
        }
        if (!inserted) {
            output.write(exifBox);
        }
    }

    static ExtractedExif readJp2(InputStream input) throws IOException {
        return readJp2(readAll(input));
    }

    static ExtractedExif readJp2(byte[] file) throws IOException {
        require(startsWith(file, JP2_SIGNATURE), "Invalid JP2 container");
        for (Box box : parseBoxes(file, 0, file.length)) {
            if (!"uuid".equals(box.type) || box.payloadSize() < 16) {
                continue;
            }
            int payload = box.payloadStart();
            if (matches(file, payload, JP2_EXIF_UUID)
                    || matches(file, payload, JP2_ADOBE_EXIF_UUID)) {
                int tiffStart = payload + 16;
                if (matches(file, tiffStart, EXIF_APP1_IDENTIFIER)) {
                    tiffStart += EXIF_APP1_IDENTIFIER.length;
                }
                int normalized = findTiffHeader(file, tiffStart, box.end);
                if (normalized < 0) {
                    throw new IOException("JP2 EXIF UUID box has no TIFF header");
                }
                return new ExtractedExif(
                        Arrays.copyOfRange(file, normalized, box.end),
                        normalized
                );
            }
        }
        return null;
    }

    static void writeJp2(InputStream input, OutputStream output, byte[] tiffPayload)
            throws IOException {
        byte[] file = readAll(input);
        require(startsWith(file, JP2_SIGNATURE), "Invalid JP2 container");
        require(isTiff(tiffPayload), "Invalid TIFF payload");

        byte[] exifBox = makeBox("uuid", concat(JP2_EXIF_UUID, tiffPayload));
        boolean inserted = false;
        for (Box box : parseBoxes(file, 0, file.length)) {
            boolean oldExif = false;
            if ("uuid".equals(box.type) && box.payloadSize() >= 16) {
                oldExif = matches(file, box.payloadStart(), JP2_EXIF_UUID)
                        || matches(file, box.payloadStart(), JP2_ADOBE_EXIF_UUID);
            }
            if (oldExif) {
                if (!inserted) {
                    output.write(exifBox);
                    inserted = true;
                }
                continue;
            }
            if (!inserted && "jp2c".equals(box.type)) {
                output.write(exifBox);
                inserted = true;
            }
            output.write(file, box.start, box.size());
        }
        if (!inserted) {
            output.write(exifBox);
        }
    }

    static ExtractedExif readIsoBmff(InputStream input) throws IOException {
        return readIsoBmff(readAll(input));
    }

    static ExtractedExif readIsoBmff(byte[] file) throws IOException {
        // Prefer the private backup written by this class. It is independent of meta/iloc
        // dialects and guarantees that ExifInterface can read exactly what it wrote itself.
        // External applications still see the standards-shaped Exif item handled below.
        ExtractedExif privateCopy = readIsoBmffPrivateFallback(file);
        if (privateCopy != null) {
            return privateCopy;
        }

        ExtractedExif standardCopy = readStandardIsoBmff(file);
        if (standardCopy != null) {
            return standardCopy;
        }

        // Last-resort compatibility for files created by older patch versions or other tools that
        // placed one complete Exif item directly in a dedicated top-level mdat.
        for (Box box : topLevelBoxes(file, "mdat")) {
            try {
                return extractOffsetPrefixedTiff(file, box.payloadStart(), box.end);
            } catch (IOException ignored) {
                // Normal image-data mdat boxes do not start with an offset-prefixed TIFF payload.
            }
        }
        return null;
    }

    private static ExtractedExif readIsoBmffPrivateFallback(byte[] file) throws IOException {
        // Do not depend on parsing the whole BMFF tree: the fallback must remain readable even
        // when a vendor uses an unfamiliar box dialect or a size=0 media box. Scan backwards for
        // our exact UUID and validate the enclosing uuid box before accepting it.
        ExtractedExif raw = readIsoBmffPrivateFallbackRaw(file);
        if (raw != null) {
            return raw;
        }

        try {
            for (Box box : topLevelBoxes(file, "uuid")) {
                if (box.payloadSize() < ISO_BMFF_EXIF_FALLBACK_UUID.length + 4
                        || !isImageToolboxFallbackUuidAt(file, box.payloadStart())) {
                    continue;
                }
                int payloadStart = box.payloadStart() + ISO_BMFF_EXIF_FALLBACK_UUID.length;
                try {
                    return extractOffsetPrefixedTiff(file, payloadStart, box.end);
                } catch (IOException ignored) {
                    // Try another matching box if this one is stale/truncated.
                }
            }
        } catch (IOException ignored) {
            // The raw scanner above is intentionally independent from the generic box parser.
        }
        return null;
    }

    private static ExtractedExif readIsoBmffPrivateFallbackRaw(byte[] file) {
        for (int marker = file.length - ISO_BMFF_EXIF_FALLBACK_UUID.length;
                marker >= 8; marker--) {
            if (!isImageToolboxFallbackUuidAt(file, marker)) {
                continue;
            }
            int boxStart = marker - 8;
            if (!matchesAscii(file, boxStart + 4, "uuid")) {
                continue;
            }
            long size = readUnsignedUnchecked(file, boxStart, 4);
            int headerSize = 8;
            if (size == 1) {
                if (marker < 16 || !matchesAscii(file, marker - 12, "uuid")) {
                    continue;
                }
                boxStart = marker - 16;
                size = readUnsignedUnchecked(file, boxStart + 8, 8);
                headerSize = 16;
            } else if (size == 0) {
                size = file.length - boxStart;
            }
            long boxEndLong = (long) boxStart + size;
            if (size < headerSize + ISO_BMFF_EXIF_FALLBACK_UUID.length + 4L
                    || boxEndLong > file.length || boxEndLong > Integer.MAX_VALUE) {
                continue;
            }
            int payloadStart = marker + ISO_BMFF_EXIF_FALLBACK_UUID.length;
            try {
                return extractOffsetPrefixedTiff(file, payloadStart, (int) boxEndLong);
            } catch (IOException ignored) {
                // Continue searching older copies.
            }
        }
        return null;
    }

    private static boolean isImageToolboxFallbackUuidAt(byte[] data, int offset) {
        byte[] prefix = new byte[] {
                0x49, 0x54, 0x42, 0x58, 0x2d, 0x45, 0x58, 0x49,
                0x46, 0x2d, 0x56
        };
        return matches(data, offset, prefix)
                && offset + ISO_BMFF_EXIF_FALLBACK_UUID.length <= data.length;
    }

    private static boolean matchesAscii(byte[] data, int offset, String text) {
        if (offset < 0 || offset + text.length() > data.length) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if ((byte) text.charAt(i) != data[offset + i]) {
                return false;
            }
        }
        return true;
    }

    private static ExtractedExif readStandardIsoBmff(byte[] file) throws IOException {
        for (Box meta : topLevelBoxes(file, "meta")) {
            MetaInfo info;
            try {
                info = parseMeta(file, meta);
            } catch (IOException ignored) {
                continue;
            }
            if (info.iloc == null) {
                continue;
            }

            for (Integer exifId : info.exifItemIds) {
                ExtractedExif extracted = tryReadIsoBmffItem(file, meta, info, exifId);
                if (extracted != null) {
                    return extracted;
                }
            }

            // Tolerate files where iinf was not updated but iloc still contains an Exif item.
            for (ItemLocation location : info.iloc.items) {
                if (info.exifItemIds.contains(location.itemId)) {
                    continue;
                }
                ExtractedExif extracted = tryReadIsoBmffLocation(file, meta, info, location);
                if (extracted != null) {
                    return extracted;
                }
            }
        }
        return null;
    }

    private static ExtractedExif tryReadIsoBmffItem(
            byte[] file,
            Box meta,
            MetaInfo info,
            int itemId
    ) {
        ItemLocation location = info.iloc.find(itemId);
        return location == null ? null : tryReadIsoBmffLocation(file, meta, info, location);
    }

    private static ExtractedExif tryReadIsoBmffLocation(
            byte[] file,
            Box meta,
            MetaInfo info,
            ItemLocation location
    ) {
        try {
            byte[] itemData = readItemData(file, meta, info, location);
            int tiffInItem = findOffsetPrefixedTiff(itemData);
            if (tiffInItem < 0) {
                return null;
            }
            long absolute = absoluteItemOffset(meta, info, location, tiffInItem);
            return new ExtractedExif(
                    Arrays.copyOfRange(itemData, tiffInItem, itemData.length),
                    absolute
            );
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Writes a standards-shaped Exif metadata item while keeping the original top-level meta box
     * in its original position. This is more interoperable than appending a second meta box,
     * especially with Android and Apple HEIC readers.
     *
     * <p>Growing the meta box shifts following boxes. Existing absolute item locations and the
     * common movie chunk-offset boxes are adjusted by the exact delta. Pixel bitstreams are copied
     * byte-for-byte and are never decoded or re-encoded.</p>
     */
    static void writeIsoBmff(InputStream input, OutputStream output, byte[] tiffPayload)
            throws IOException {
        byte[] file = readAll(input);
        require(isTiff(tiffPayload), "Invalid TIFF payload");

        // HEIC/HEIF encoders in the wild use multiple meta/iloc layouts. The previous generic
        // meta rewriter could fail before writing, leaving ExifInterface with an unchanged file.
        // For HEIF, use a deterministic top-level uuid channel that our own reader understands.
        // AVIF keeps the standards-shaped rewrite which is already known to work in ImageToolbox.
        if (detectIsoBmffImageType(file) == ISO_BMFF_TYPE_HEIF) {
            writeIsoBmffFallbackOnly(file, output, tiffPayload);
            return;
        }

        Box oldMeta = findPrimaryMeta(file);
        require(oldMeta != null, "ISO-BMFF file has no usable top-level meta box");

        MetaInfo info = parseMeta(file, oldMeta);
        require(info.iloc != null, "ISO-BMFF meta box has no iloc box");
        require(info.iinf != null, "ISO-BMFF meta box has no iinf box");

        int primaryItemId = info.primaryItemId != null
                ? info.primaryItemId
                : info.firstImageItemId();
        require(primaryItemId >= 0, "Unable to determine primary image item");

        int exifItemId = info.exifItemId != null
                ? info.exifItemId
                : info.nextFreeItemId();
        byte[] itemPayload = addTiffOffsetPrefix(tiffPayload);

        ItemLocation previousExifLocation = info.iloc.find(exifItemId);
        Box obsoleteExifMdat = findDedicatedIsoBmffItemMdat(file, previousExifLocation);
        long removedTrailingMetadataBytes = 0;
        for (Box box : parseBoxes(file, 0, file.length)) {
            if (isOurIsoBmffFallbackBox(file, box)
                    || (obsoleteExifMdat != null && box.start == obsoleteExifMdat.start)) {
                removedTrailingMetadataBytes += box.size();
            }
        }
        long outputLengthBeforeMetaResize =
                (long) file.length - removedTrailingMetadataBytes;

        // Keep the active meta box in place, but store Exif in a normal top-level mdat item
        // (construction_method=0). This is the layout used most widely by HEIF tooling and avoids
        // vendor incompatibilities seen with idat-backed metadata items.
        long delta = 0;
        byte[] newMeta = null;
        ItemLocation exifLocation = null;
        for (int iteration = 0; iteration < 8; iteration++) {
            long exifAbsoluteOffset = outputLengthBeforeMetaResize + delta + 8L;
            // The previous private fallback is removed before appending the new mdat. Account for
            // those bytes or repeat saves would point iloc past the actual Exif payload.
            exifLocation = ItemLocation.singleExtent(
                    exifItemId,
                    exifAbsoluteOffset,
                    itemPayload.length
            );
            newMeta = rebuildMetaForExternalItem(
                    file,
                    oldMeta,
                    info,
                    exifLocation,
                    primaryItemId,
                    oldMeta.end,
                    delta
            );
            long nextDelta = (long) newMeta.length - oldMeta.size();
            if (nextDelta == delta) {
                break;
            }
            delta = nextDelta;
            newMeta = null;
        }
        require(newMeta != null && exifLocation != null,
                "Unable to stabilize rebuilt HEIF meta box");

        byte[] patchedOriginal = Arrays.copyOf(file, file.length);
        if (delta != 0) {
            patchIsoBmffAbsoluteOffsets(
                    patchedOriginal,
                    parseBoxes(patchedOriginal, 0, patchedOriginal.length),
                    oldMeta.end,
                    delta
            );
        }

        byte[] exifMdat = makeBox("mdat", itemPayload);
        byte[] fallbackUuid = makeBox(
                "uuid",
                concat(ISO_BMFF_EXIF_FALLBACK_UUID, itemPayload)
        );
        ByteArrayOutputStream result = new ByteArrayOutputStream(
                checkedInt((long) file.length + delta + exifMdat.length + fallbackUuid.length,
                        "HEIF output size")
        );

        for (Box box : parseBoxes(patchedOriginal, 0, patchedOriginal.length)) {
            if (box.start == oldMeta.start) {
                result.write(newMeta);
                continue;
            }
            if (isOurIsoBmffFallbackBox(patchedOriginal, box)
                    || (obsoleteExifMdat != null && box.start == obsoleteExifMdat.start)) {
                // Replace our older dedicated Exif mdat and private fallback instead of growing
                // the file on every save. Shared/vendor mdat boxes are never removed.
                continue;
            }
            writeTopLevelBoxPreservingAppendability(result, patchedOriginal, box);
        }
        result.write(exifMdat);
        result.write(fallbackUuid);

        byte[] rewritten = result.toByteArray();
        ExtractedExif privateVerification = readIsoBmffPrivateFallback(rewritten);
        require(privateVerification != null
                        && Arrays.equals(tiffPayload, privateVerification.tiffPayload),
                "HEIF private EXIF round-trip verification failed");
        ExtractedExif standardVerification = readStandardIsoBmff(rewritten);
        require(standardVerification != null
                        && Arrays.equals(tiffPayload, standardVerification.tiffPayload),
                "HEIF standard EXIF round-trip verification failed");
        output.write(rewritten);
    }

    private static void writeIsoBmffFallbackOnly(
            byte[] file,
            OutputStream output,
            byte[] tiffPayload
    ) throws IOException {
        byte[] itemPayload = addTiffOffsetPrefix(tiffPayload);
        byte[] fallbackUuid = makeBox(
                "uuid",
                concat(ISO_BMFF_EXIF_FALLBACK_UUID, itemPayload)
        );

        ByteArrayOutputStream result = new ByteArrayOutputStream(
                checkedInt((long) file.length + fallbackUuid.length, "HEIF output size")
        );
        List<Box> boxes = parseBoxes(file, 0, file.length);
        for (Box box : boxes) {
            if (isAnyImageToolboxIsoBmffFallbackBox(file, box)) {
                continue;
            }
            writeTopLevelBoxPreservingAppendability(result, file, box);
        }
        result.write(fallbackUuid);

        byte[] rewritten = result.toByteArray();
        ExtractedExif verification = readIsoBmffPrivateFallbackRaw(rewritten);
        require(verification != null && Arrays.equals(tiffPayload, verification.tiffPayload),
                "HEIF ExifInterface read-after-write verification failed");
        output.write(rewritten);
    }

    /**
     * Rewrites TIFF metadata without replacing the image IFD.
     *
     * <p>The original primary IFD remains authoritative for strips, tiles, compression, pages and
     * private decoder tags. A detached ExifInterface payload is appended and only metadata entries
     * in IFD0 are merged into a new primary IFD. This avoids corrupting StripOffsets/TileOffsets and
     * preserves multi-page TIFF chains.</p>
     */
    static void writeTiff(InputStream input, OutputStream output, byte[] newTiffPayload)
            throws IOException {
        writeTiff(input, output, newTiffPayload, TIFF_PRIMARY_METADATA_TAGS);
    }

    /**
     * Rewrites the primary TIFF IFD using the complete set of tags managed by ExifInterface.
     * Managed tags present in {@code newTiffPayload} are replaced, and managed tags absent from it
     * are deleted. Unknown/vendor tags remain byte-for-byte from the source IFD.
     */
    static void writeTiff(
            InputStream input,
            OutputStream output,
            byte[] newTiffPayload,
            Set<Integer> managedPrimaryTags
    ) throws IOException {
        byte[] original = readAll(input);
        require(isTiff(original), "Invalid source TIFF");
        require(isTiff(newTiffPayload), "Invalid replacement TIFF payload");
        require(managedPrimaryTags != null, "Managed TIFF tag set is null");
        if (managedPrimaryTags.isEmpty()) {
            // No caller-visible tag changed. Never rebuild a TIFF merely because saveAttributes()
            // was called: an exact copy is the only universally safe no-op for vendor TIFFs.
            output.write(original);
            return;
        }
        require(original[0] == newTiffPayload[0] && original[1] == newTiffPayload[1],
                "TIFF byte order changed while editing metadata");

        boolean little = original[0] == 'I';
        long originalFirstIfd = readUnsigned(original, 4, 4, little);
        long metadataFirstIfd = readUnsigned(newTiffPayload, 4, 4, little);
        TiffIfd originalIfd = parseTiffIfd(original, originalFirstIfd, little);
        TiffIfd metadataIfd = parseTiffIfd(newTiffPayload, metadataFirstIfd, little);

        Set<Integer> effectiveManagedPrimaryTags = new HashSet<>(managedPrimaryTags);
        sanitizePlanarConfigurationEdit(
                original,
                newTiffPayload,
                originalIfd,
                metadataIfd,
                little,
                effectiveManagedPrimaryTags
        );

        int metadataBase = alignEven(original.length);
        require((long) metadataBase + newTiffPayload.length + 2L
                        + 12L * (originalIfd.entries.size() + metadataIfd.entries.size()) + 4L
                        <= Integer.MAX_VALUE,
                "TIFF is too large for the in-memory writer");

        byte[] relocatedMetadata = Arrays.copyOf(newTiffPayload, newTiffPayload.length);
        relocateTiffPayload(relocatedMetadata, metadataBase);

        TreeMap<Integer, byte[]> merged = new TreeMap<>();
        for (TiffEntry entry : originalIfd.entries) {
            if (!effectiveManagedPrimaryTags.contains(entry.tag)
                    || TIFF_IMMUTABLE_IMAGE_STRUCTURE_TAGS.contains(entry.tag)) {
                merged.put(entry.tag, entry.rawEntry(original));
            }
        }
        for (TiffEntry entry : metadataIfd.entries) {
            if (effectiveManagedPrimaryTags.contains(entry.tag)
                    && !TIFF_IMMUTABLE_IMAGE_STRUCTURE_TAGS.contains(entry.tag)) {
                merged.put(entry.tag, entry.rawEntry(relocatedMetadata));
            }
        }

        int mergedIfdOffset = alignEven(metadataBase + relocatedMetadata.length);
        ByteArrayOutputStream mergedIfd = new ByteArrayOutputStream();
        writeTiffUnsigned(mergedIfd, merged.size(), 2, little);
        for (byte[] entry : merged.values()) {
            mergedIfd.write(entry);
        }
        writeTiffUnsigned(mergedIfd, originalIfd.nextIfdOffset, 4, little);

        byte[] headerAndImage = Arrays.copyOf(original, original.length);
        writeUnsigned(headerAndImage, 4, 4, mergedIfdOffset, little);
        ByteArrayOutputStream result = new ByteArrayOutputStream(
                mergedIfdOffset + mergedIfd.size()
        );
        result.write(headerAndImage);
        writePadding(result, metadataBase - original.length);
        result.write(relocatedMetadata);
        writePadding(result, mergedIfdOffset - metadataBase - relocatedMetadata.length);
        result.write(mergedIfd.toByteArray());

        byte[] rewritten = result.toByteArray();
        validateUntouchedTiffPrimaryEntries(
                original,
                rewritten,
                originalIfd,
                effectiveManagedPrimaryTags,
                little
        );
        output.write(rewritten);
    }

    private static void sanitizePlanarConfigurationEdit(
            byte[] original,
            byte[] replacement,
            TiffIfd originalIfd,
            TiffIfd replacementIfd,
            boolean little,
            Set<Integer> managedTags
    ) throws IOException {
        final int planarConfigurationTag = 284;
        if (!managedTags.contains(planarConfigurationTag)) {
            return;
        }

        Integer originalValue = readSingleTiffShort(
                original, findTiffEntry(originalIfd, planarConfigurationTag), little
        );
        Integer requestedValue = readSingleTiffShort(
                replacement, findTiffEntry(replacementIfd, planarConfigurationTag), little
        );
        int originalEffective = originalValue == null ? 1 : originalValue;
        int requestedEffective = requestedValue == null ? 1 : requestedValue;

        // 1 (chunky) and 2 (separate planes) describe the physical sample layout. Changing the
        // effective value without rewriting strips/tiles makes a valid TIFF undecodable. Deleting
        // an explicit value of 1 is safe because TIFF defaults to 1; deleting 2 is not.
        if (originalEffective != requestedEffective) {
            managedTags.remove(planarConfigurationTag);
        }
    }

    private static TiffEntry findTiffEntry(TiffIfd ifd, int tag) {
        for (TiffEntry entry : ifd.entries) {
            if (entry.tag == tag) {
                return entry;
            }
        }
        return null;
    }

    private static Integer readSingleTiffShort(
            byte[] source,
            TiffEntry entry,
            boolean little
    ) throws IOException {
        if (entry == null) {
            return null;
        }
        int type = (int) readUnsigned(source, entry.position + 2, 2, little);
        long count = readUnsigned(source, entry.position + 4, 4, little);
        require(type == 3 && count == 1, "Invalid PlanarConfiguration TIFF entry");
        return (int) readUnsigned(source, entry.position + 8, 2, little);
    }

    private static void validateUntouchedTiffPrimaryEntries(
            byte[] original,
            byte[] rewritten,
            TiffIfd originalIfd,
            Set<Integer> managedPrimaryTags,
            boolean little
    ) throws IOException {
        long rewrittenFirstIfd = readUnsigned(rewritten, 4, 4, little);
        TiffIfd rewrittenIfd = parseTiffIfd(rewritten, rewrittenFirstIfd, little);
        Map<Integer, TiffEntry> rewrittenByTag = new java.util.HashMap<>();
        for (TiffEntry entry : rewrittenIfd.entries) {
            rewrittenByTag.put(entry.tag, entry);
        }
        for (TiffEntry originalEntry : originalIfd.entries) {
            if (managedPrimaryTags.contains(originalEntry.tag)
                    && !TIFF_IMMUTABLE_IMAGE_STRUCTURE_TAGS.contains(originalEntry.tag)) {
                continue;
            }
            TiffEntry rewrittenEntry = rewrittenByTag.get(originalEntry.tag);
            require(rewrittenEntry != null,
                    "TIFF rewrite lost untouched tag " + originalEntry.tag);
            require(Arrays.equals(
                            originalEntry.rawEntry(original),
                            rewrittenEntry.rawEntry(rewritten)),
                    "TIFF rewrite changed untouched tag " + originalEntry.tag);
        }
    }

    // -----------------------------------------------------------------------------------------
    // ISO-BMFF
    // -----------------------------------------------------------------------------------------

    private static byte[] rebuildMetaForExternalItem(
            byte[] file,
            Box oldMeta,
            MetaInfo info,
            ItemLocation exifLocation,
            int primaryItemId,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        int exifItemId = exifLocation.itemId;
        byte[] newIinf = rebuildIinf(file, info, exifItemId);
        byte[] newIloc = rebuildIloc(
                info.iloc,
                exifLocation,
                shiftStart,
                shiftDelta
        );
        byte[] newIref = rebuildIref(file, info, exifItemId, primaryItemId);

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.write(file, oldMeta.payloadStart(), 4); // FullBox version/flags.

        boolean wroteIinf = false;
        boolean wroteIloc = false;
        boolean wroteIref = false;
        for (Box child : info.children) {
            switch (child.type) {
                case "iinf":
                    payload.write(newIinf);
                    wroteIinf = true;
                    break;
                case "iloc":
                    payload.write(newIloc);
                    wroteIloc = true;
                    break;
                case "iref":
                    payload.write(newIref);
                    wroteIref = true;
                    break;
                default:
                    // Preserve idat and every unknown/vendor child byte-for-byte. The active Exif
                    // item no longer points into idat, so stale bytes are harmless.
                    payload.write(file, child.start, child.size());
                    break;
            }
        }
        if (!wroteIinf) {
            payload.write(newIinf);
        }
        if (!wroteIloc) {
            payload.write(newIloc);
        }
        if (!wroteIref) {
            payload.write(newIref);
        }
        return makeBox("meta", payload.toByteArray());
    }

    private static byte[] rebuildMeta(
            byte[] file,
            Box oldMeta,
            MetaInfo info,
            ItemLocation exifLocation,
            int primaryItemId,
            byte[] newIdat,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        int exifItemId = exifLocation.itemId;
        byte[] newIinf = rebuildIinf(file, info, exifItemId);
        byte[] newIloc = rebuildIloc(
                info.iloc,
                exifLocation,
                shiftStart,
                shiftDelta
        );
        byte[] newIref = rebuildIref(file, info, exifItemId, primaryItemId);

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.write(file, oldMeta.payloadStart(), 4); // FullBox version/flags.

        boolean wroteIinf = false;
        boolean wroteIloc = false;
        boolean wroteIref = false;
        boolean wroteIdat = false;
        for (Box child : info.children) {
            switch (child.type) {
                case "iinf":
                    payload.write(newIinf);
                    wroteIinf = true;
                    break;
                case "iloc":
                    payload.write(newIloc);
                    wroteIloc = true;
                    break;
                case "iref":
                    payload.write(newIref);
                    wroteIref = true;
                    break;
                case "idat":
                    payload.write(newIdat);
                    wroteIdat = true;
                    break;
                default:
                    payload.write(file, child.start, child.size());
                    break;
            }
        }
        if (!wroteIinf) {
            payload.write(newIinf);
        }
        if (!wroteIloc) {
            payload.write(newIloc);
        }
        if (!wroteIref) {
            payload.write(newIref);
        }
        if (!wroteIdat) {
            payload.write(newIdat);
        }
        return makeBox("meta", payload.toByteArray());
    }

    private static IdatPlan buildIdatPlan(
            byte[] file,
            MetaInfo info,
            int exifItemId,
            byte[] itemPayload
    ) throws IOException {
        byte[] oldPayload = info.idat == null
                ? new byte[0]
                : Arrays.copyOfRange(file, info.idat.payloadStart(), info.idat.end);
        int retainedLength = oldPayload.length;

        ItemLocation oldExif = info.iloc == null ? null : info.iloc.find(exifItemId);
        if (oldExif != null
                && oldExif.constructionMethod == 1
                && oldExif.dataReferenceIndex == 0
                && oldExif.extents.size() == 1) {
            Extent extent = oldExif.extents.get(0);
            long start = oldExif.baseOffset + extent.offset;
            long end = start + extent.length;
            if (start >= 0 && end == oldPayload.length && start <= Integer.MAX_VALUE) {
                retainedLength = (int) start;
            }
        }

        byte[] payload = concat(
                Arrays.copyOf(oldPayload, retainedLength),
                itemPayload
        );
        return new IdatPlan(makeBox("idat", payload), retainedLength);
    }

    private static byte[] rebuildIinf(byte[] file, MetaInfo info, int exifItemId)
            throws IOException {
        Iinf iinf = info.iinf;
        if (iinf.itemTypes.containsKey(exifItemId)) {
            return Arrays.copyOfRange(file, iinf.box.start, iinf.box.end);
        }
        int version = iinf.version;
        List<byte[]> entries = new ArrayList<>();
        for (Box child : iinf.entries) {
            entries.add(Arrays.copyOfRange(file, child.start, child.end));
        }
        entries.add(makeInfe(exifItemId));

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeFullBoxHeader(payload, version, iinf.flags);
        if (version == 0) {
            require(entries.size() <= 0xffff, "Too many iinf entries");
            writeUnsigned(payload, entries.size(), 2);
        } else {
            writeUnsigned(payload, entries.size(), 4);
        }
        for (byte[] entry : entries) {
            payload.write(entry);
        }
        return makeBox("iinf", payload.toByteArray());
    }

    private static byte[] makeInfe(int itemId) throws IOException {
        int version = itemId <= 0xffff ? 2 : 3;
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeFullBoxHeader(payload, version, 0);
        writeUnsigned(payload, itemId, version == 2 ? 2 : 4);
        writeUnsigned(payload, 0, 2); // item_protection_index
        payload.write(ascii("Exif"));
        payload.write(0); // empty item_name
        return makeBox("infe", payload.toByteArray());
    }

    private static byte[] rebuildIloc(
            Iloc original,
            ItemLocation exifLocation,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        int exifItemId = exifLocation.itemId;
        List<ItemLocation> items = new ArrayList<>();
        boolean replaced = false;
        int maxId = exifItemId;
        for (ItemLocation old : original.items) {
            maxId = Math.max(maxId, old.itemId);
            if (old.itemId == exifItemId) {
                items.add(exifLocation.copy());
                replaced = true;
            } else {
                items.add(shiftItemLocation(old, shiftStart, shiftDelta));
            }
        }
        if (!replaced) {
            items.add(exifLocation.copy());
        }

        int version = maxId > 0xffff ? 2 : Math.max(1, original.version);
        int offsetSize = 8;
        int lengthSize = 8;
        int baseOffsetSize = 8;
        int indexSize = original.indexSize > 0 ? 8 : 0;

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeFullBoxHeader(payload, version, original.flags);
        payload.write((offsetSize << 4) | lengthSize);
        payload.write((baseOffsetSize << 4) | (version == 0 ? 0 : indexSize));
        writeUnsigned(payload, items.size(), version < 2 ? 2 : 4);

        for (ItemLocation item : items) {
            writeUnsigned(payload, item.itemId, version < 2 ? 2 : 4);
            if (version == 1 || version == 2) {
                writeUnsigned(payload, item.constructionMethod & 0xf, 2);
            }
            writeUnsigned(payload, item.dataReferenceIndex, 2);
            writeUnsigned(payload, item.baseOffset, baseOffsetSize);
            writeUnsigned(payload, item.extents.size(), 2);
            for (Extent extent : item.extents) {
                if ((version == 1 || version == 2) && indexSize > 0) {
                    writeUnsigned(payload, extent.index, indexSize);
                }
                writeUnsigned(payload, extent.offset, offsetSize);
                writeUnsigned(payload, extent.length, lengthSize);
            }
        }
        return makeBox("iloc", payload.toByteArray());
    }

    private static byte[] rebuildIref(
            byte[] file,
            MetaInfo info,
            int exifItemId,
            int primaryItemId
    ) throws IOException {
        int version = Math.max(exifItemId, primaryItemId) <= 0xffff ? 0 : 1;
        int flags = 0;
        List<byte[]> references = new ArrayList<>();
        if (info.iref != null) {
            version = Math.max(version, info.iref.version);
            flags = info.iref.flags;
            for (Box child : info.iref.references) {
                if (!isSameReference(file, child, version, "cdsc", exifItemId, primaryItemId)) {
                    references.add(Arrays.copyOfRange(file, child.start, child.end));
                }
            }
        }
        references.add(makeItemReference("cdsc", version, exifItemId, primaryItemId));

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeFullBoxHeader(payload, version, flags);
        for (byte[] reference : references) {
            payload.write(reference);
        }
        return makeBox("iref", payload.toByteArray());
    }

    private static boolean isSameReference(
            byte[] file,
            Box box,
            int irefVersion,
            String type,
            int from,
            int to
    ) {
        if (!type.equals(box.type)) {
            return false;
        }
        try {
            Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
            int width = irefVersion == 0 ? 2 : 4;
            long existingFrom = cursor.readUnsigned(width);
            int count = (int) cursor.readUnsigned(2);
            if (existingFrom != from) {
                return false;
            }
            for (int i = 0; i < count; i++) {
                if (cursor.readUnsigned(width) == to) {
                    return true;
                }
            }
        } catch (IOException ignored) {
            // Preserve malformed/unknown reference and add a valid one.
        }
        return false;
    }

    private static byte[] makeItemReference(
            String type,
            int version,
            int from,
            int to
    ) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        int width = version == 0 ? 2 : 4;
        writeUnsigned(payload, from, width);
        writeUnsigned(payload, 1, 2);
        writeUnsigned(payload, to, width);
        return makeBox(type, payload.toByteArray());
    }

    private static MetaInfo parseMeta(byte[] file, Box meta) throws IOException {
        require(meta.payloadSize() >= 4, "Invalid meta FullBox");
        List<Box> children = parseBoxes(file, meta.payloadStart() + 4, meta.end);
        MetaInfo info = new MetaInfo(meta, children);
        for (Box child : children) {
            switch (child.type) {
                case "pitm":
                    info.primaryItemId = parsePitm(file, child);
                    break;
                case "iinf":
                    info.iinf = parseIinf(file, child);
                    for (java.util.Map.Entry<Integer, String> entry
                            : info.iinf.itemTypes.entrySet()) {
                        if ("Exif".equals(entry.getValue())) {
                            info.exifItemIds.add(entry.getKey());
                            if (info.exifItemId == null) {
                                info.exifItemId = entry.getKey();
                            }
                        }
                    }
                    break;
                case "iloc":
                    info.iloc = parseIloc(file, child);
                    break;
                case "iref":
                    info.iref = parseIref(file, child);
                    break;
                case "idat":
                    info.idat = child;
                    break;
                default:
                    break;
            }
        }
        return info;
    }

    private static Integer parsePitm(byte[] file, Box box) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        int version = cursor.readUnsignedByte();
        cursor.skip(3);
        return (int) cursor.readUnsigned(version == 0 ? 2 : 4);
    }

    private static Iinf parseIinf(byte[] file, Box box) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        int version = cursor.readUnsignedByte();
        int flags = cursor.readUnsigned24();
        long count = cursor.readUnsigned(version == 0 ? 2 : 4);
        require(count <= Integer.MAX_VALUE, "Too many iinf entries");
        List<Box> entries = parseBoxes(file, cursor.position, box.end);
        Iinf iinf = new Iinf(box, version, flags, entries);
        int parsed = 0;
        for (Box entry : entries) {
            if (!"infe".equals(entry.type)) {
                continue;
            }
            Infe infe = parseInfe(file, entry);
            if (infe != null) {
                iinf.itemTypes.put(infe.itemId, infe.itemType);
            }
            parsed++;
        }
        require(parsed <= count || count == 0, "Invalid iinf entry count");
        return iinf;
    }

    private static Infe parseInfe(byte[] file, Box box) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        int version = cursor.readUnsignedByte();
        cursor.skip(3);
        if (version == 2) {
            int id = (int) cursor.readUnsigned(2);
            cursor.skip(2);
            return new Infe(id, cursor.readType());
        }
        if (version == 3) {
            int id = checkedInt(cursor.readUnsigned(4), "item id");
            cursor.skip(2);
            return new Infe(id, cursor.readType());
        }
        if (version == 0 || version == 1) {
            int id = (int) cursor.readUnsigned(2);
            cursor.skip(2); // item_protection_index
            String itemName = cursor.readCString();
            String contentType = cursor.position < cursor.end ? cursor.readCString() : "";
            if ("Exif".equalsIgnoreCase(itemName)
                    || "application/exif".equalsIgnoreCase(contentType)) {
                return new Infe(id, "Exif");
            }
            return new Infe(id, "mime");
        }
        return null;
    }

    private static Iloc parseIloc(byte[] file, Box box) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        int version = cursor.readUnsignedByte();
        int flags = cursor.readUnsigned24();
        int a = cursor.readUnsignedByte();
        int b = cursor.readUnsignedByte();
        int offsetSize = (a >>> 4) & 0xf;
        int lengthSize = a & 0xf;
        int baseOffsetSize = (b >>> 4) & 0xf;
        int indexSize = (version == 1 || version == 2) ? b & 0xf : 0;
        long itemCount = cursor.readUnsigned(version < 2 ? 2 : 4);
        require(itemCount <= Integer.MAX_VALUE, "Too many iloc items");

        Iloc iloc = new Iloc(box, version, flags, offsetSize, lengthSize,
                baseOffsetSize, indexSize);
        for (int i = 0; i < itemCount; i++) {
            int itemId = checkedInt(cursor.readUnsigned(version < 2 ? 2 : 4), "item id");
            int constructionMethod = 0;
            if (version == 1 || version == 2) {
                constructionMethod = (int) cursor.readUnsigned(2) & 0xf;
            }
            int dataReferenceIndex = (int) cursor.readUnsigned(2);
            long baseOffset = cursor.readUnsigned(baseOffsetSize);
            int extentCount = (int) cursor.readUnsigned(2);
            ItemLocation item = new ItemLocation(
                    itemId,
                    constructionMethod,
                    dataReferenceIndex,
                    baseOffset
            );
            for (int e = 0; e < extentCount; e++) {
                long index = indexSize == 0 ? 0 : cursor.readUnsigned(indexSize);
                long offset = cursor.readUnsigned(offsetSize);
                long length = cursor.readUnsigned(lengthSize);
                item.extents.add(new Extent(index, offset, length));
            }
            iloc.items.add(item);
        }
        return iloc;
    }

    private static Iref parseIref(byte[] file, Box box) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        int version = cursor.readUnsignedByte();
        int flags = cursor.readUnsigned24();
        return new Iref(box, version, flags, parseBoxes(file, cursor.position, box.end));
    }

    private static byte[] readItemData(
            byte[] file,
            Box meta,
            MetaInfo info,
            ItemLocation location
    ) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (Extent extent : location.extents) {
            long start;
            if (location.constructionMethod == 0) {
                start = location.baseOffset + extent.offset;
            } else if (location.constructionMethod == 1) {
                require(info.idat != null, "iloc references missing idat box");
                start = (long) info.idat.payloadStart() + location.baseOffset + extent.offset;
            } else {
                throw new IOException("Unsupported iloc construction_method="
                        + location.constructionMethod);
            }
            require(start >= 0 && extent.length >= 0
                            && start + extent.length <= file.length,
                    "Invalid iloc extent");
            result.write(file, (int) start, (int) extent.length);
        }
        return result.toByteArray();
    }

    private static long absoluteItemOffset(
            Box meta,
            MetaInfo info,
            ItemLocation location,
            int offsetInsideItem
    ) throws IOException {
        require(!location.extents.isEmpty(), "Exif item has no extents");
        Extent first = location.extents.get(0);
        long start;
        if (location.constructionMethod == 0) {
            start = location.baseOffset + first.offset;
        } else if (location.constructionMethod == 1) {
            require(info.idat != null, "iloc references missing idat box");
            start = (long) info.idat.payloadStart() + location.baseOffset + first.offset;
        } else {
            return -1;
        }
        return start + offsetInsideItem;
    }


    private static Box findPrimaryMeta(byte[] file) throws IOException {
        Box fallback = null;
        for (Box meta : topLevelBoxes(file, "meta")) {
            if (fallback == null) {
                fallback = meta;
            }
            try {
                MetaInfo info = parseMeta(file, meta);
                if (info.primaryItemId != null || info.firstImageItemId() >= 0) {
                    return meta;
                }
            } catch (IOException ignored) {
                // Try the next top-level meta box.
            }
        }
        return fallback;
    }

    private static List<Box> topLevelBoxes(byte[] file, String type) throws IOException {
        List<Box> result = new ArrayList<>();
        for (Box box : parseBoxes(file, 0, file.length)) {
            if (type.equals(box.type)) {
                result.add(box);
            }
        }
        return result;
    }

    private static ItemLocation shiftItemLocation(
            ItemLocation original,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        if (shiftDelta == 0
                || original.constructionMethod != 0
                || original.dataReferenceIndex != 0) {
            return original.copy();
        }
        ItemLocation shifted = new ItemLocation(
                original.itemId,
                original.constructionMethod,
                original.dataReferenceIndex,
                0
        );
        for (Extent extent : original.extents) {
            long absolute = original.baseOffset + extent.offset;
            long adjusted = absolute >= shiftStart ? absolute + shiftDelta : absolute;
            require(adjusted >= 0, "Negative shifted iloc offset");
            shifted.extents.add(new Extent(extent.index, adjusted, extent.length));
        }
        return shifted;
    }

    private static void patchIsoBmffAbsoluteOffsets(
            byte[] file,
            List<Box> boxes,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        for (Box box : boxes) {
            switch (box.type) {
                case "stco":
                    patchChunkOffsets(file, box, 4, shiftStart, shiftDelta);
                    break;
                case "co64":
                    patchChunkOffsets(file, box, 8, shiftStart, shiftDelta);
                    break;
                case "saio":
                    patchSaio(file, box, shiftStart, shiftDelta);
                    break;
                case "tfhd":
                    patchTfhd(file, box, shiftStart, shiftDelta);
                    break;
                case "sidx":
                    patchSidx(file, box, shiftStart, shiftDelta);
                    break;
                default:
                    if (isIsoBmffContainer(box.type)) {
                        try {
                            patchIsoBmffAbsoluteOffsets(
                                    file,
                                    parseBoxes(file, box.payloadStart(), box.end),
                                    shiftStart,
                                    shiftDelta
                            );
                        } catch (IOException ignored) {
                            // Some udta/vendor boxes are not pure child-box containers. Leave
                            // unknown payloads untouched instead of rejecting an otherwise valid
                            // still-image HEIF file.
                        }
                    }
                    break;
            }
        }
    }

    private static boolean isIsoBmffContainer(String type) {
        return "moov".equals(type) || "trak".equals(type) || "mdia".equals(type)
                || "minf".equals(type) || "stbl".equals(type) || "edts".equals(type)
                || "dinf".equals(type) || "mvex".equals(type) || "moof".equals(type)
                || "traf".equals(type) || "mfra".equals(type) || "udta".equals(type);
    }

    private static void patchChunkOffsets(
            byte[] file,
            Box box,
            int width,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        cursor.skip(4); // FullBox
        long count = cursor.readUnsigned(4);
        require(count <= Integer.MAX_VALUE, "Too many chunk offsets");
        for (int i = 0; i < count; i++) {
            int position = cursor.position;
            long value = cursor.readUnsigned(width);
            if (value >= shiftStart) {
                writeUnsigned(file, position, width, value + shiftDelta, false);
            }
        }
    }

    private static void patchSaio(
            byte[] file,
            Box box,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        int version = cursor.readUnsignedByte();
        int flags = cursor.readUnsigned24();
        if ((flags & 1) != 0) {
            cursor.skip(8); // aux_info_type + aux_info_type_parameter
        }
        long count = cursor.readUnsigned(4);
        int width = version == 0 ? 4 : 8;
        require(count <= Integer.MAX_VALUE, "Too many saio offsets");
        for (int i = 0; i < count; i++) {
            int position = cursor.position;
            long value = cursor.readUnsigned(width);
            if (value >= shiftStart) {
                writeUnsigned(file, position, width, value + shiftDelta, false);
            }
        }
    }

    private static void patchTfhd(
            byte[] file,
            Box box,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        cursor.readUnsignedByte();
        int flags = cursor.readUnsigned24();
        cursor.skip(4); // track_ID
        if ((flags & 0x000001) != 0) {
            int position = cursor.position;
            long value = cursor.readUnsigned(8);
            if (value >= shiftStart) {
                writeUnsigned(file, position, 8, value + shiftDelta, false);
            }
        }
    }

    private static void patchSidx(
            byte[] file,
            Box box,
            long shiftStart,
            long shiftDelta
    ) throws IOException {
        Cursor cursor = new Cursor(file, box.payloadStart(), box.end);
        int version = cursor.readUnsignedByte();
        cursor.skip(3 + 4 + 4); // flags, reference_ID, timescale
        cursor.skip(version == 0 ? 4 : 8); // earliest_presentation_time
        int width = version == 0 ? 4 : 8;
        int position = cursor.position;
        long firstOffset = cursor.readUnsigned(width);
        long target = (long) box.end + firstOffset;
        if (target >= shiftStart) {
            writeUnsigned(file, position, width, firstOffset + shiftDelta, false);
        }
    }

    private static int alignEven(int value) throws IOException {
        require(value >= 0 && value < Integer.MAX_VALUE, "Invalid alignment value");
        return (value & 1) == 0 ? value : value + 1;
    }

    private static void writePadding(OutputStream output, int count) throws IOException {
        require(count >= 0, "Negative padding");
        for (int i = 0; i < count; i++) {
            output.write(0);
        }
    }

    private static void writeTiffUnsigned(
            ByteArrayOutputStream output,
            long value,
            int width,
            boolean little
    ) throws IOException {
        byte[] bytes = new byte[width];
        writeUnsigned(bytes, 0, width, value, little);
        output.write(bytes);
    }

    private static TiffIfd parseTiffIfd(
            byte[] tiff,
            long ifdOffset,
            boolean little
    ) throws IOException {
        require(ifdOffset >= 8 && ifdOffset + 2 <= tiff.length, "Invalid TIFF IFD offset");
        int start = checkedInt(ifdOffset, "TIFF IFD offset");
        int count = (int) readUnsigned(tiff, start, 2, little);
        long end = (long) start + 2L + 12L * count + 4L;
        require(end <= tiff.length, "Truncated TIFF IFD");
        List<TiffEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int position = start + 2 + i * 12;
            int tag = (int) readUnsigned(tiff, position, 2, little);
            entries.add(new TiffEntry(tag, position));
        }
        long next = readUnsigned(tiff, start + 2 + 12 * count, 4, little);
        return new TiffIfd(entries, next);
    }

    // -----------------------------------------------------------------------------------------
    // TIFF relocation
    // -----------------------------------------------------------------------------------------

    private static void relocateTiffPayload(byte[] tiff, long base) throws IOException {
        require(isTiff(tiff), "Invalid TIFF payload");
        boolean little = tiff[0] == 'I';
        require(readUnsigned(tiff, 2, 2, little) == TIFF_MAGIC, "Invalid TIFF magic");
        long firstIfd = readUnsigned(tiff, 4, 4, little);
        require(firstIfd > 0 && firstIfd < tiff.length, "Invalid TIFF first IFD");
        relocateIfd(tiff, firstIfd, base, little, new HashSet<Long>());
        writeUnsigned(tiff, 4, 4, firstIfd + base, little);
    }

    private static void relocateIfd(
            byte[] tiff,
            long ifdOffset,
            long base,
            boolean little,
            Set<Long> visited
    ) throws IOException {
        if (ifdOffset == 0 || !visited.add(ifdOffset)) {
            return;
        }
        require(ifdOffset + 2 <= tiff.length, "Invalid TIFF IFD offset");
        int pos = checkedInt(ifdOffset, "IFD offset");
        int count = (int) readUnsigned(tiff, pos, 2, little);
        int entriesStart = pos + 2;
        require((long) entriesStart + 12L * count + 4 <= tiff.length,
                "Truncated TIFF IFD");

        List<Long> childIfds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int entry = entriesStart + i * 12;
            int tag = (int) readUnsigned(tiff, entry, 2, little);
            int type = (int) readUnsigned(tiff, entry + 2, 2, little);
            long components = readUnsigned(tiff, entry + 4, 4, little);
            if (type <= 0 || type >= TIFF_TYPE_SIZES.length) {
                continue;
            }
            long byteCount = components * TIFF_TYPE_SIZES[type];
            require(byteCount >= 0 && byteCount <= Integer.MAX_VALUE,
                    "Invalid TIFF value size");

            long originalValueOrOffset = readUnsigned(tiff, entry + 8, 4, little);
            int valuePosition = entry + 8;
            if (byteCount > 4) {
                require(originalValueOrOffset + byteCount <= tiff.length,
                        "Invalid TIFF value offset");
                writeUnsigned(tiff, entry + 8, 4, originalValueOrOffset + base, little);
                valuePosition = checkedInt(originalValueOrOffset, "TIFF value offset");
            }

            if (isIfdPointerTag(tag)) {
                int elementSize = type == 3 ? 2 : 4;
                int elements = checkedInt(components, "pointer count");
                for (int p = 0; p < elements; p++) {
                    int pointerPos = valuePosition + p * elementSize;
                    long child = readUnsigned(tiff, pointerPos, elementSize, little);
                    if (child != 0) {
                        childIfds.add(child);
                        writeUnsigned(tiff, pointerPos, elementSize, child + base, little);
                    }
                }
            } else if (tag == TIFF_TAG_JPEG_INTERCHANGE_FORMAT) {
                // The thumbnail is serialized into the new payload by ExifInterface.
                long thumbnail = readUnsigned(tiff, valuePosition, type == 3 ? 2 : 4, little);
                if (thumbnail != 0) {
                    writeUnsigned(tiff, valuePosition, type == 3 ? 2 : 4,
                            thumbnail + base, little);
                }
            }
        }

        int nextPos = entriesStart + count * 12;
        long nextIfd = readUnsigned(tiff, nextPos, 4, little);
        if (nextIfd != 0) {
            writeUnsigned(tiff, nextPos, 4, nextIfd + base, little);
            childIfds.add(nextIfd);
        }
        for (Long child : childIfds) {
            relocateIfd(tiff, child, base, little, visited);
        }
    }

    private static boolean isIfdPointerTag(int tag) {
        return tag == TIFF_TAG_SUB_IFDS
                || tag == TIFF_TAG_EXIF_IFD
                || tag == TIFF_TAG_GPS_IFD
                || tag == TIFF_TAG_INTEROP_IFD;
    }

    private static Box findDedicatedIsoBmffItemMdat(
            byte[] file,
            ItemLocation location
    ) throws IOException {
        if (location == null
                || location.constructionMethod != 0
                || location.dataReferenceIndex != 0
                || location.extents.size() != 1) {
            return null;
        }
        Extent extent = location.extents.get(0);
        long start = location.baseOffset + extent.offset;
        for (Box box : topLevelBoxes(file, "mdat")) {
            if (start == box.payloadStart() && extent.length == box.payloadSize()) {
                return box;
            }
        }
        return null;
    }

    private static boolean isOurIsoBmffFallbackBox(byte[] file, Box box) {
        return "uuid".equals(box.type)
                && box.payloadSize() >= ISO_BMFF_EXIF_FALLBACK_UUID.length
                && matches(file, box.payloadStart(), ISO_BMFF_EXIF_FALLBACK_UUID);
    }

    private static boolean isAnyImageToolboxIsoBmffFallbackBox(byte[] file, Box box) {
        if (!"uuid".equals(box.type)
                || box.payloadSize() < ISO_BMFF_EXIF_FALLBACK_UUID.length) {
            return false;
        }
        byte[] prefix = new byte[] {
                0x49, 0x54, 0x42, 0x58, 0x2d, 0x45, 0x58, 0x49,
                0x46, 0x2d, 0x56
        };
        return matches(file, box.payloadStart(), prefix);
    }

    private static void writeTopLevelBoxPreservingAppendability(
            ByteArrayOutputStream output,
            byte[] file,
            Box box
    ) throws IOException {
        if (!box.extendsToEnd) {
            output.write(file, box.start, box.size());
            return;
        }
        // A size=0 top-level box consumes the rest of the file. Convert it to an explicit size
        // before appending metadata boxes, otherwise the appended bytes become part of that box.
        require(box.headerSize == 8, "Unsupported size=0 box with extended header");
        require(box.size() <= 0xffffffffL, "Top-level box is too large");
        byte[] copy = Arrays.copyOfRange(file, box.start, box.end);
        writeUnsigned(copy, 0, 4, box.size(), false);
        output.write(copy);
    }

    // -----------------------------------------------------------------------------------------
    // Generic boxes and payload normalization
    // -----------------------------------------------------------------------------------------

    private static ExtractedExif extractOffsetPrefixedTiff(byte[] file, int start, int end)
            throws IOException {
        require(end - start >= 4, "Truncated Exif box");
        long offset = readUnsigned(file, start, 4, false);
        require(offset <= Integer.MAX_VALUE, "Invalid TIFF header offset");
        int candidate = start + 4 + (int) offset;
        int tiffStart = findTiffHeader(file, candidate, end);
        require(tiffStart >= 0, "Exif box has no TIFF header");
        return new ExtractedExif(Arrays.copyOfRange(file, tiffStart, end), tiffStart);
    }

    private static int findOffsetPrefixedTiff(byte[] data) throws IOException {
        if (data.length < 4) {
            return -1;
        }
        long offset = readUnsigned(data, 0, 4, false);
        if (offset <= Integer.MAX_VALUE) {
            int candidate = 4 + (int) offset;
            int found = findTiffHeader(data, candidate, data.length);
            if (found >= 0) {
                return found;
            }
        }
        return findTiffHeader(data, 0, data.length);
    }

    private static int findTiffHeader(byte[] data, int preferred, int end) {
        if (preferred >= 0 && preferred + 4 <= end && isTiffAt(data, preferred)) {
            return preferred;
        }
        if (preferred >= 0 && preferred + EXIF_APP1_IDENTIFIER.length + 4 <= end
                && matches(data, preferred, EXIF_APP1_IDENTIFIER)
                && isTiffAt(data, preferred + EXIF_APP1_IDENTIFIER.length)) {
            return preferred + EXIF_APP1_IDENTIFIER.length;
        }
        // Be tolerant of encoders that use a non-zero offset incorrectly.
        int limit = Math.min(end - 4, Math.max(0, preferred) + 64);
        for (int i = Math.max(0, preferred); i <= limit; i++) {
            if (isTiffAt(data, i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isTiffAt(byte[] data, int offset) {
        if (offset < 0 || offset + 4 > data.length) {
            return false;
        }
        return (data[offset] == 'I' && data[offset + 1] == 'I'
                && (data[offset + 2] & 0xff) == 42 && data[offset + 3] == 0)
                || (data[offset] == 'M' && data[offset + 1] == 'M'
                && data[offset + 2] == 0 && (data[offset + 3] & 0xff) == 42);
    }

    private static byte[] addTiffOffsetPrefix(byte[] tiffPayload) {
        return concat(new byte[] {0, 0, 0, 0}, tiffPayload);
    }

    private static Box firstTopLevelBox(byte[] file, String type) throws IOException {
        for (Box box : parseBoxes(file, 0, file.length)) {
            if (type.equals(box.type)) {
                return box;
            }
        }
        return null;
    }

    private static List<Box> parseBoxes(byte[] data, int start, int end) throws IOException {
        List<Box> boxes = new ArrayList<>();
        int position = start;
        while (position < end) {
            require(end - position >= 8, "Truncated box header at " + position);
            long size32 = readUnsigned(data, position, 4, false);
            String type = new String(data, position + 4, 4, StandardCharsets.ISO_8859_1);
            int header = 8;
            long size;
            boolean extendsToEnd = false;
            if (size32 == 1) {
                require(end - position >= 16, "Truncated extended box header");
                size = readUnsigned(data, position + 8, 8, false);
                header = 16;
            } else if (size32 == 0) {
                size = end - position;
                extendsToEnd = true;
            } else {
                size = size32;
            }
            require(size >= header && size <= Integer.MAX_VALUE
                            && position + size <= end,
                    "Invalid " + type + " box size " + size + " at " + position);
            boxes.add(new Box(position, (int) size, header, type, extendsToEnd));
            position += (int) size;
        }
        return boxes;
    }

    private static byte[] makeBox(String type, byte[] payload) throws IOException {
        require(type.length() == 4, "Box type must contain four bytes");
        long total = 8L + payload.length;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (total <= 0xffff_ffffL) {
            writeUnsigned(output, total, 4);
            output.write(ascii(type));
        } else {
            writeUnsigned(output, 1, 4);
            output.write(ascii(type));
            writeUnsigned(output, 16L + payload.length, 8);
        }
        output.write(payload);
        return output.toByteArray();
    }

    private static byte[] makeFreeBox(int totalSize) throws IOException {
        require(totalSize >= 8, "meta box is too small to replace with free");
        byte[] result = new byte[totalSize];
        writeUnsigned(result, 0, 4, totalSize, false);
        byte[] type = ascii("free");
        System.arraycopy(type, 0, result, 4, 4);
        return result;
    }

    private static byte[] makeBoxWithOriginalPayload(byte[] file, Box box) throws IOException {
        return makeBox(box.type, Arrays.copyOfRange(file, box.payloadStart(), box.end));
    }

    private static void writeFullBoxHeader(ByteArrayOutputStream output, int version, int flags) {
        output.write(version & 0xff);
        output.write((flags >>> 16) & 0xff);
        output.write((flags >>> 8) & 0xff);
        output.write(flags & 0xff);
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[64 * 1024];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    private static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int position = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, position, array.length);
            position += array.length;
        }
        return result;
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        return matches(data, 0, prefix);
    }

    private static boolean matches(byte[] data, int offset, byte[] expected) {
        if (offset < 0 || offset + expected.length > data.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static void require(boolean condition, String message) throws IOException {
        if (!condition) {
            throw new IOException(message);
        }
    }

    private static int checkedInt(long value, String name) throws IOException {
        require(value >= 0 && value <= Integer.MAX_VALUE, "Invalid " + name + ": " + value);
        return (int) value;
    }

    private static long readUnsigned(
            byte[] data,
            int offset,
            int width,
            boolean littleEndian
    ) throws IOException {
        require(width >= 0 && width <= 8, "Unsupported integer width " + width);
        require(offset >= 0 && offset + width <= data.length, "Truncated integer");
        long value = 0;
        if (littleEndian) {
            for (int i = width - 1; i >= 0; i--) {
                value = (value << 8) | (data[offset + i] & 0xffL);
            }
        } else {
            for (int i = 0; i < width; i++) {
                value = (value << 8) | (data[offset + i] & 0xffL);
            }
        }
        require(value >= 0, "Unsigned 64-bit value exceeds signed long range");
        return value;
    }

    private static void writeUnsigned(
            byte[] data,
            int offset,
            int width,
            long value,
            boolean littleEndian
    ) throws IOException {
        require(width >= 0 && width <= 8, "Unsupported integer width " + width);
        require(value >= 0, "Negative unsigned value");
        require(offset >= 0 && offset + width <= data.length, "Truncated integer output");
        long remaining = value;
        if (littleEndian) {
            for (int i = 0; i < width; i++) {
                data[offset + i] = (byte) remaining;
                remaining >>>= 8;
            }
        } else {
            for (int i = width - 1; i >= 0; i--) {
                data[offset + i] = (byte) remaining;
                remaining >>>= 8;
            }
        }
        require(remaining == 0, "Value does not fit in " + width + " bytes");
    }

    private static void writeUnsigned(ByteArrayOutputStream output, long value, int width)
            throws IOException {
        require(width >= 0 && width <= 8, "Unsupported integer width " + width);
        require(value >= 0, "Negative unsigned value");
        byte[] bytes = new byte[width];
        writeUnsigned(bytes, 0, width, value, false);
        output.write(bytes);
    }

    private static final class IdatPlan {
        final byte[] box;
        final int exifOffset;

        IdatPlan(byte[] box, int exifOffset) {
            this.box = box;
            this.exifOffset = exifOffset;
        }
    }

    private static final class TiffIfd {
        final List<TiffEntry> entries;
        final long nextIfdOffset;

        TiffIfd(List<TiffEntry> entries, long nextIfdOffset) {
            this.entries = entries;
            this.nextIfdOffset = nextIfdOffset;
        }
    }

    private static final class TiffEntry {
        final int tag;
        final int position;

        TiffEntry(int tag, int position) {
            this.tag = tag;
            this.position = position;
        }

        byte[] rawEntry(byte[] source) {
            return Arrays.copyOfRange(source, position, position + 12);
        }
    }

    private static final class Box {
        final int start;
        final int end;
        final int headerSize;
        final String type;
        final boolean extendsToEnd;

        Box(int start, int size, int headerSize, String type, boolean extendsToEnd) {
            this.start = start;
            this.end = start + size;
            this.headerSize = headerSize;
            this.type = type;
            this.extendsToEnd = extendsToEnd;
        }

        int size() {
            return end - start;
        }

        int payloadStart() {
            return start + headerSize;
        }

        int payloadSize() {
            return end - payloadStart();
        }
    }

    private static final class Cursor {
        final byte[] data;
        final int end;
        int position;

        Cursor(byte[] data, int position, int end) {
            this.data = data;
            this.position = position;
            this.end = end;
        }

        int readUnsignedByte() throws IOException {
            require(position < end, "Unexpected end of box");
            return data[position++] & 0xff;
        }

        int readUnsigned24() throws IOException {
            return (readUnsignedByte() << 16)
                    | (readUnsignedByte() << 8)
                    | readUnsignedByte();
        }

        long readUnsigned(int width) throws IOException {
            require(position + width <= end, "Unexpected end of box");
            long value = ExtendedExifContainer.readUnsigned(data, position, width, false);
            position += width;
            return value;
        }

        String readType() throws IOException {
            require(position + 4 <= end, "Unexpected end of box type");
            String value = new String(data, position, 4, StandardCharsets.ISO_8859_1);
            position += 4;
            return value;
        }


        String readCString() throws IOException {
            int start = position;
            while (position < end && data[position] != 0) {
                position++;
            }
            require(position < end, "Unterminated box string");
            String value = new String(
                    data,
                    start,
                    position - start,
                    StandardCharsets.ISO_8859_1
            );
            position++; // NUL
            return value;
        }

        void skip(int count) throws IOException {
            require(count >= 0 && position + count <= end, "Unexpected end of box");
            position += count;
        }
    }

    private static final class MetaInfo {
        final Box meta;
        final List<Box> children;
        Integer primaryItemId;
        Integer exifItemId;
        final List<Integer> exifItemIds = new ArrayList<>();
        Iinf iinf;
        Iloc iloc;
        Iref iref;
        Box idat;

        MetaInfo(Box meta, List<Box> children) {
            this.meta = meta;
            this.children = children;
        }

        int nextFreeItemId() {
            int max = 0;
            if (iinf != null) {
                for (Integer id : iinf.itemTypes.keySet()) {
                    max = Math.max(max, id);
                }
            }
            if (iloc != null) {
                for (ItemLocation item : iloc.items) {
                    max = Math.max(max, item.itemId);
                }
            }
            return max + 1;
        }

        int firstImageItemId() {
            if (iinf == null) {
                return -1;
            }
            for (java.util.Map.Entry<Integer, String> entry : iinf.itemTypes.entrySet()) {
                String type = entry.getValue();
                if ("av01".equals(type) || "av02".equals(type)
                        || "hvc1".equals(type) || "hev1".equals(type)
                        || "vvc1".equals(type) || "vvi1".equals(type)
                        || "grid".equals(type) || "iden".equals(type)) {
                    return entry.getKey();
                }
            }
            return -1;
        }
    }

    private static final class Iinf {
        final Box box;
        final int version;
        final int flags;
        final List<Box> entries;
        final java.util.LinkedHashMap<Integer, String> itemTypes = new java.util.LinkedHashMap<>();

        Iinf(Box box, int version, int flags, List<Box> entries) {
            this.box = box;
            this.version = version;
            this.flags = flags;
            this.entries = entries;
        }
    }

    private static final class Infe {
        final int itemId;
        final String itemType;

        Infe(int itemId, String itemType) {
            this.itemId = itemId;
            this.itemType = itemType;
        }
    }

    private static final class Iloc {
        final Box box;
        final int version;
        final int flags;
        final int offsetSize;
        final int lengthSize;
        final int baseOffsetSize;
        final int indexSize;
        final List<ItemLocation> items = new ArrayList<>();

        Iloc(Box box, int version, int flags, int offsetSize, int lengthSize,
                int baseOffsetSize, int indexSize) {
            this.box = box;
            this.version = version;
            this.flags = flags;
            this.offsetSize = offsetSize;
            this.lengthSize = lengthSize;
            this.baseOffsetSize = baseOffsetSize;
            this.indexSize = indexSize;
        }

        ItemLocation find(int itemId) {
            for (ItemLocation item : items) {
                if (item.itemId == itemId) {
                    return item;
                }
            }
            return null;
        }
    }

    private static final class ItemLocation {
        final int itemId;
        final int constructionMethod;
        final int dataReferenceIndex;
        final long baseOffset;
        final List<Extent> extents = new ArrayList<>();

        ItemLocation(int itemId, int constructionMethod, int dataReferenceIndex,
                long baseOffset) {
            this.itemId = itemId;
            this.constructionMethod = constructionMethod;
            this.dataReferenceIndex = dataReferenceIndex;
            this.baseOffset = baseOffset;
        }

        static ItemLocation singleExtent(int itemId, long offset, long length) {
            ItemLocation item = new ItemLocation(itemId, 0, 0, 0);
            item.extents.add(new Extent(0, offset, length));
            return item;
        }

        static ItemLocation idatSingleExtent(int itemId, long offset, long length) {
            ItemLocation item = new ItemLocation(itemId, 1, 0, 0);
            item.extents.add(new Extent(0, offset, length));
            return item;
        }

        ItemLocation copy() {
            ItemLocation copy = new ItemLocation(
                    itemId,
                    constructionMethod,
                    dataReferenceIndex,
                    baseOffset
            );
            for (Extent extent : extents) {
                copy.extents.add(new Extent(extent.index, extent.offset, extent.length));
            }
            return copy;
        }
    }

    private static final class Extent {
        final long index;
        final long offset;
        final long length;

        Extent(long index, long offset, long length) {
            this.index = index;
            this.offset = offset;
            this.length = length;
        }
    }

    private static final class Iref {
        final Box box;
        final int version;
        final int flags;
        final List<Box> references;

        Iref(Box box, int version, int flags, List<Box> references) {
            this.box = box;
            this.version = version;
            this.flags = flags;
            this.references = references;
        }
    }
}
