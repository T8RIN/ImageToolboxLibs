package com.t8rin.exif;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

public class MetadataCleanerTest {

    private static byte[] clean(byte[] input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MetadataCleaner.remove(new ByteArrayInputStream(input), output);
        return output.toByteArray();
    }

    private static byte[] jpegSegment(int marker, byte[] payload) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeByte(0xff);
        output.writeByte(marker);
        output.writeShort(payload.length + 2);
        output.write(payload);
        return bytes.toByteArray();
    }

    private static byte[] jpegBox(String type, int sequence) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeBytes("JP");
        output.writeShort(1);
        output.writeInt(sequence);
        output.writeInt(24);
        output.writeBytes(type);
        output.writeBytes("box data");
        return bytes.toByteArray();
    }

    private static byte[] pngSignature() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
    }

    private static byte[] pngChunk(String type, byte[] payload) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeInt(payload.length);
        output.writeBytes(type);
        output.write(payload);
        CRC32 crc = new CRC32();
        crc.update(ascii(type));
        crc.update(payload);
        output.writeInt((int) crc.getValue());
        return bytes.toByteArray();
    }

    private static byte[] webp(byte[] chunks) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(ascii("RIFF"));
        writeLittleEndianInt(output, chunks.length + 4);
        output.write(ascii("WEBP"));
        output.write(chunks);
        return output.toByteArray();
    }

    private static byte[] riffChunk(String type, byte[] payload) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(ascii(type));
        writeLittleEndianInt(output, payload.length);
        output.write(payload);
        if ((payload.length & 1) != 0) {
            output.write(0);
        }
        return output.toByteArray();
    }

    private static void writeLittleEndianInt(ByteArrayOutputStream output, int value) {
        for (int i = 0; i < 4; i++) {
            output.write((value >> (8 * i)) & 0xff);
        }
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] join(byte[]... parts) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            output.write(part);
        }
        return output.toByteArray();
    }

    @Test
    public void jpegRemovesDuplicateAndExtendedXmpAndAllJumbfFragments() throws IOException {
        byte[] image = jpegSegment(0xdb, new byte[]{1, 2, 3});
        byte[] profile = jpegSegment(0xe2, ascii("ICC_PROFILE\0profile"));
        byte[] unknownApp1 = jpegSegment(0xe1, ascii("unrelated application data"));
        byte[] imageExtension = jpegSegment(0xeb, jpegBox("jhdr", 1));
        byte[] input = join(new byte[]{(byte) 0xff, (byte) 0xd8},
                jpegSegment(0xe1, ascii("Exif\0\0private EXIF and thumbnail")),
                jpegSegment(0xe1, ascii("http://ns.adobe.com/xap/1.0/\0first XMP")),
                jpegSegment(0xe1, ascii("http://ns.adobe.com/xap/1.0/\0second XMP")),
                jpegSegment(0xe1, ascii("http://ns.adobe.com/xmp/extension/\0extended XMP")),
                jpegSegment(0xeb, jpegBox("jumb", 1)),
                jpegSegment(0xeb, jpegBox("jumb", 2)),
                jpegSegment(0xed, ascii("Photoshop 3.0\0IPTC")),
                jpegSegment(0xfe, ascii("private comment")),
                profile, unknownApp1, imageExtension, image,
                new byte[]{(byte) 0xff, (byte) 0xd9}, ascii("trailing metadata"));

        assertArrayEquals(join(new byte[]{(byte) 0xff, (byte) 0xd8}, profile, unknownApp1,
                imageExtension, image, new byte[]{(byte) 0xff, (byte) 0xd9}), clean(input));
    }

    @Test
    public void jpegCleansBetweenProgressiveScansAndPreservesStuffingAndRestartMarkers()
            throws IOException {
        byte[] scanHeader = jpegSegment(0xda, new byte[]{1, 1, 0, 0, 63, 0});
        byte[] scan = {1, (byte) 0xff, 0, 2, (byte) 0xff, (byte) 0xd0, 3};
        byte[] start = {(byte) 0xff, (byte) 0xd8};
        byte[] end = {(byte) 0xff, (byte) 0xd9};
        byte[] input = join(start, scanHeader, scan,
                jpegSegment(0xe1, ascii("http://ns.adobe.com/xmp/extension/\0late XMP")),
                jpegSegment(0xeb, jpegBox("jumb", 1)),
                jpegSegment(0xfe, ascii("late comment")), scanHeader, scan, end);

        assertArrayEquals(join(start, scanHeader, scan, scanHeader, scan, end), clean(input));
    }

    @Test
    public void jpegPreservesScanDataAfterDefineNumberOfLines() throws IOException {
        byte[] input = join(new byte[]{(byte) 0xff, (byte) 0xd8},
                jpegSegment(0xda, new byte[]{1, 1, 0, 0, 63, 0}), new byte[]{1, 2, 3},
                jpegSegment(0xdc, new byte[]{0, 1}), new byte[]{4, 5, 6},
                new byte[]{(byte) 0xff, (byte) 0xd9});
        assertArrayEquals(input, clean(input));
    }

    @Test
    public void pngRemovesCompressedAndInternationalTextAndC2paAfterImageData()
            throws IOException {
        byte[] header = pngChunk("IHDR", new byte[13]);
        byte[] profile = pngChunk("iCCP", ascii("profile\0compressed ICC"));
        byte[] animation = pngChunk("acTL", new byte[8]);
        byte[] frame = pngChunk("fcTL", new byte[26]);
        byte[] pixels = pngChunk("IDAT", new byte[100_000]);
        byte[] framePixels = pngChunk("fdAT", new byte[20]);
        byte[] end = pngChunk("IEND", new byte[0]);
        byte[] input = join(pngSignature(), header,
                pngChunk("eXIf", ascii("private EXIF")),
                pngChunk("iTXt", ascii("XML:com.adobe.xmp\0\0\0\0\0first XMP")),
                profile, animation, frame, pixels,
                pngChunk("iTXt", ascii("XML:com.adobe.xmp\0\1\0en\0description\0compressed XMP")),
                pngChunk("tEXt", ascii("parameters\0AI generation parameters")),
                pngChunk("zTXt", ascii("Raw profile type xmp\0\0compressed XMP")),
                pngChunk("caBX", ascii("C2PA manifest")),
                pngChunk("tIME", new byte[7]), framePixels, end, ascii("trailing metadata"));

        assertArrayEquals(join(pngSignature(), header, profile, animation, frame, pixels,
                framePixels, end), clean(input));
    }

    @Test
    public void webpRemovesAllMetadataAndUpdatesSizeAndFlagsWithoutChangingAnimation()
            throws IOException {
        byte[] extendedHeader = {0x3e, 0, 0, 0, 1, 0, 0, 1, 0, 0};
        byte[] profile = riffChunk("ICCP", ascii("profile"));
        byte[] animation = riffChunk("ANIM", new byte[6]);
        byte[] pixels = riffChunk("ANMF", new byte[100_001]);
        byte[] unknown = riffChunk("test", ascii("unknown chunk"));
        byte[] input = webp(join(riffChunk("VP8X", extendedHeader), profile, animation, pixels,
                riffChunk("EXIF", ascii("Exif\0\0private data")),
                riffChunk("EXIF", ascii("duplicate EXIF")),
                riffChunk("XMP ", ascii("first XMP")),
                riffChunk("XMP ", ascii("second XMP")),
                riffChunk("C2PA", ascii("manifest")), unknown));
        extendedHeader[0] = 0x32;
        byte[] expected = webp(join(riffChunk("VP8X", extendedHeader), profile, animation,
                pixels, unknown));

        assertArrayEquals(expected, clean(join(input, ascii("trailing metadata"))));
    }

    @Test
    public void webpPreservesSimpleLosslessImageAndOddChunkPadding() throws IOException {
        byte[] chunk = riffChunk("VP8L", new byte[]{1, 2, 3});
        chunk[chunk.length - 1] = 17;
        byte[] input = webp(chunk);
        assertArrayEquals(input, clean(input));
    }

    @Test
    public void supportsShortReadsWithoutClosingOrFlushingCallerStreams() throws IOException {
        byte[] input = join(pngSignature(), pngChunk("IHDR", new byte[13]),
                pngChunk("IDAT", new byte[100_000]), pngChunk("IEND", new byte[0]));
        boolean[] closed = {false, false, false};
        InputStream source = new ByteArrayInputStream(input) {
            @Override
            public synchronized int read(byte[] bytes, int offset, int length) {
                return super.read(bytes, offset, Math.min(3, length));
            }

            @Override
            public void close() {
                closed[0] = true;
            }
        };
        ByteArrayOutputStream target = new ByteArrayOutputStream() {
            @Override
            public void close() {
                closed[1] = true;
            }

            @Override
            public void flush() {
                closed[2] = true;
            }
        };
        MetadataCleaner.remove(source, target);

        assertArrayEquals(input, target.toByteArray());
        assertFalse(closed[0]);
        assertFalse(closed[1]);
        assertFalse(closed[2]);
    }

    @Test
    public void unsupportedFormatDoesNotWriteOutput() {
        for (byte[] input : new byte[][]{
                ascii("GIF89a"), ascii("II*\0"), ascii("RIFF\0\0\0\0WAVE"),
                new byte[0], new byte[]{1}
        }) {
            ByteArrayOutputStream target = new ByteArrayOutputStream();
            assertThrows(IOException.class,
                    () -> MetadataCleaner.remove(new ByteArrayInputStream(input), target));
            assertEquals(0, target.size());
        }
    }

    @Test
    public void truncatedImagesAndInvalidLengthsFail() throws IOException {
        byte[] png = join(pngSignature(), pngChunk("IHDR", new byte[13]),
                pngChunk("IDAT", new byte[10]), pngChunk("IEND", new byte[0]));
        byte[] webp = webp(riffChunk("VP8L", new byte[10]));
        byte[] invalidRiffLength = webp.clone();
        invalidRiffLength[16] = (byte) 0xff;
        invalidRiffLength[17] = (byte) 0xff;
        invalidRiffLength[18] = (byte) 0xff;
        invalidRiffLength[19] = (byte) 0xff;
        for (byte[] input : new byte[][]{
                new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe1, 0, 1},
                new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe1, 0, 20},
                join(new byte[]{(byte) 0xff, (byte) 0xd8},
                        jpegSegment(0xda, new byte[6]), new byte[]{1, 2, 3}),
                Arrays.copyOf(png, png.length - 1),
                join(pngSignature(), new byte[]{(byte) 0x80, 0, 0, 0}),
                join(pngSignature(), pngChunk("IDAT", new byte[0])),
                Arrays.copyOf(webp, webp.length - 1), invalidRiffLength,
                webp(riffChunk("VP8X", new byte[9]))
        }) {
            assertThrows(IOException.class, () -> clean(input));
        }
    }

    @Test
    public void cleaningIsIdempotentForAllSupportedFormats() throws IOException {
        for (byte[] input : new byte[][]{
                join(new byte[]{(byte) 0xff, (byte) 0xd8},
                        jpegSegment(0xfe, ascii("comment")),
                        new byte[]{(byte) 0xff, (byte) 0xd9}),
                join(pngSignature(), pngChunk("IHDR", new byte[13]),
                        pngChunk("tEXt", ascii("private text")),
                        pngChunk("IDAT", new byte[10]), pngChunk("IEND", new byte[0])),
                webp(join(riffChunk("VP8L", new byte[10]),
                        riffChunk("C2PA", ascii("manifest"))))
        }) {
            byte[] cleaned = clean(input);
            assertArrayEquals(cleaned, clean(cleaned));
        }
    }
}
