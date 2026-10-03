package com.t8rin.exif;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;

public class ExtendedMetadataCleanerTest {

    private static byte[] tiffPixels(byte[] bytes) throws IOException {
        ByteBuffer input = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int ifd = input.getInt(4);
        int pages = 0;
        ByteArrayOutputStream pixels = new ByteArrayOutputStream();
        while (ifd != 0) {
            int count = input.getShort(ifd) & 0xffff;
            int offset = 0;
            int length = 0;
            for (int i = 0; i < count; i++) {
                int position = ifd + 2 + 12 * i;
                int tag = input.getShort(position) & 0xffff;
                if (tag == 273) offset = input.getInt(position + 8);
                if (tag == 279) length = input.getInt(position + 8);
            }
            assertTrue(offset > 0 && length > 0);
            pixels.write(bytes, offset, length);
            ifd = input.getInt(ifd + 2 + 12 * count);
            pages++;
        }
        assertEquals(2, pages);
        return pixels.toByteArray();
    }

    private static byte[] bmff(String brand, int storage) throws IOException {
        byte[] ftyp = box("ftyp", join(ascii(brand), new byte[4], ascii("mif1"), ascii(brand)));
        byte[] data = join(ascii("compressed image"), metadataTiff(), ascii("private XMP"));
        byte[] meta = meta(brand, storage, 0, data);
        int offset = storage == 0 ? ftyp.length + meta.length + 8
                : storage == 1 ? 0 : ftyp.length + find(meta, "idat") + 4;
        meta = meta(brand, storage, offset, data);
        return join(ftyp, meta, storage == 0 ? box("mdat", data) : new byte[0],
                box("uuid", join(new byte[16], ascii("private C2PA"))));
    }

    private static byte[] meta(String brand, int storage, int offset, byte[] data)
            throws IOException {
        byte[] iinf = box("iinf", join(new byte[4], shorts(3),
                infe(1, "avif".equals(brand) ? "av01" : "hvc1", "image", ""),
                infe(2, "Exif", "Exif", ""),
                infe(3, "mime", "XMP", "application/rdf+xml")));
        ByteArrayOutputStream locations = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(locations);
        output.write(new byte[]{1, 0, 0, 0, 0x44, 0});
        output.writeShort(3);
        int position = offset;
        int[] lengths = {ascii("compressed image").length, metadataTiff().length,
                ascii("private XMP").length};
        for (int i = 0; i < 3; i++) {
            output.writeShort(i + 1);
            output.writeShort(storage == 1 ? 1 : 0);
            output.writeShort(0);
            output.writeShort(1);
            output.writeInt(position);
            output.writeInt(lengths[i]);
            position += lengths[i];
        }
        byte[] iref = box("iref", join(new byte[4],
                box("cdsc", shorts(2, 1, 1)), box("cdsc", shorts(3, 1, 1))));
        byte[] ipma = box("ipma", join(new byte[4], ints(3), shorts(1), new byte[]{1, 1},
                shorts(2), new byte[]{1, 1}, shorts(3), new byte[]{1, 1}));
        byte[] iprp = box("iprp", join(box("ipco", box("colr", ascii("nclxcolor profile"))), ipma));
        return box("meta", join(new byte[4], box("pitm", join(new byte[4], shorts(1))),
                iinf, box("iloc", locations.toByteArray()), iref, iprp,
                storage == 0 ? new byte[0] : box("idat", data)));
    }

    private static byte[] imageItem(byte[] bytes) {
        ByteBuffer input = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        int iloc = find(bytes, "iloc");
        assertEquals(1, input.getShort(iloc + 10)); // Only the image item remains.
        assertEquals(1, input.getShort(iloc + 12));
        int method = input.getShort(iloc + 14);
        int offset = input.getInt(iloc + 20);
        int length = input.getInt(iloc + 24);
        if (method == 1) {
            offset += find(bytes, "idat") + 4;
        }
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    private static byte[] infe(int id, String type, String name, String contentType)
            throws IOException {
        return box("infe", join(new byte[]{2, 0, 0, 0}, shorts(id, 0), ascii(type),
                ascii(name + "\0"), "mime".equals(type) ? ascii(contentType + "\0") : new byte[0]));
    }

    private static byte[] metadataTiff() {
        byte[] privateData = ascii("private metadata\0");
        ByteBuffer bytes = ByteBuffer.allocate(64 + privateData.length).order(ByteOrder.LITTLE_ENDIAN);
        bytes.put(new byte[]{'I', 'I', 42, 0}).putInt(8).putShort((short) 3);
        for (int tag : new int[]{315, 700, 52545}) {
            bytes.putShort((short) tag).putShort((short) 1).putInt(privateData.length).putInt(64);
        }
        bytes.putInt(0);
        bytes.position(64);
        bytes.put(privateData);
        return bytes.array();
    }

    private static byte[] clean(byte[] bytes) throws IOException {
        File source = temporary(bytes);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MetadataCleaner.remove(source, output);
            return output.toByteArray();
        } finally {
            source.delete();
        }
    }

    private static File temporary(byte[] bytes) throws IOException {
        File file = File.createTempFile("metadata-test", ".tmp");
        Files.write(file.toPath(), bytes);
        return file;
    }

    private static byte[] asset(String name) throws IOException {
        return Files.readAllBytes(new File("src/androidTest/assets", name).toPath());
    }

    private static int find(byte[] bytes, String value) {
        int position = text(bytes).indexOf(value);
        assertTrue(value, position >= 0);
        return position;
    }

    private static byte[] box(String type, byte[] payload) throws IOException {
        return join(ints(payload.length + 8), ascii(type), payload);
    }

    private static byte[] ints(int... values) {
        ByteBuffer bytes = ByteBuffer.allocate(values.length * 4);
        for (int value : values) bytes.putInt(value);
        return bytes.array();
    }

    private static byte[] shorts(int... values) {
        ByteBuffer bytes = ByteBuffer.allocate(values.length * 2);
        for (int value : values) bytes.putShort((short) value);
        return bytes.array();
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String text(byte[] value) {
        return new String(value, StandardCharsets.ISO_8859_1);
    }

    private static byte[] join(byte[]... parts) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] part : parts) output.write(part);
        return output.toByteArray();
    }

    @Test
    public void jxlRemovesExifXmlJumbfCompressedMetadataAndJpegReconstruction() throws IOException {
        byte[] original = asset("image.jxl");
        byte[] metadata = box("Exif", metadataTiff());
        byte[] input = join(original, metadata, box("xml ", ascii("private XMP")),
                box("jumb", ascii("private C2PA")),
                box("brob", ascii("Exifcompressed EXIF")),
                box("brob", ascii("xml compressed XMP")),
                box("jbrd", ascii("original JPEG metadata")));
        assertArrayEquals(original, clean(input));
        byte[] rawCodestream = {(byte) 0xff, 0x0a, 1, 2, 3, 4};
        assertArrayEquals(rawCodestream, clean(rawCodestream));
    }

    @Test
    public void jp2RemovesUuidXmlAssociationsAndIntellectualPropertyMetadata() throws IOException {
        byte[] original = asset("image.jp2");
        byte[] input = join(original,
                box("uuid", join(ascii("JpgTiffExif->JP2"), metadataTiff())),
                box("uuid", join(new byte[16], ascii("private XMP"))),
                box("xml ", ascii("private XMP")),
                box("asoc", box("jumb", ascii("private C2PA"))),
                box("jp2i", ascii("private copyright")));
        assertArrayEquals(original, clean(input));
    }

    @Test
    public void tiffRebuildPreservesBothPagesAndRemovesDetachedOldMetadata() throws IOException {
        File source = temporary(asset("image.tiff"));
        File edited = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ExtendedExifContainer.writeTiff(source, bytes, metadataTiff(), Set.of(700, 315, 52545));
            edited = temporary(bytes.toByteArray());
            bytes.reset();
            ExtendedExifContainer.writeTiff(edited, bytes, metadataTiff(), Set.of(700, 315, 52545));
            byte[] cleaned = clean(bytes.toByteArray());
            assertFalse(text(cleaned).contains("private metadata"));
            assertTrue(cleaned.length < bytes.size());
            assertArrayEquals(tiffPixels(asset("image.tiff")), tiffPixels(cleaned));
        } finally {
            source.delete();
            if (edited != null) edited.delete();
        }
    }

    @Test
    public void heifAndAvifEraseMetadataItemsAndKeepMdatAndIdatImageExtents() throws IOException {
        for (String brand : new String[]{"heic", "avif"}) {
            for (int storage = 0; storage < 3; storage++) {
                byte[] input = bmff(brand, storage);
                byte[] cleaned = clean(input);
                assertEquals(input.length, cleaned.length);
                assertFalse(text(cleaned).contains("private metadata"));
                assertFalse(text(cleaned).contains("private XMP"));
                assertFalse(text(cleaned).contains("private C2PA"));
                assertNull(ExtendedExifContainer.readIsoBmff(cleaned));
                assertArrayEquals(ascii("compressed image"), imageItem(cleaned));
                assertArrayEquals(cleaned, clean(cleaned));
            }
        }
    }
}
