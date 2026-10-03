package com.t8rin.exif;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.ParcelFileDescriptor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ClearAttributesInstrumentedTest {

    private static final String PRIVATE_DATA = "private metadata to remove";
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final List<File> files = new ArrayList<>();

    private static int[] pixels(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return pixels;
    }

    private static String text(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
    }

    @After
    public void cleanUp() {
        for (File file : files) file.delete();
    }

    @Test
    public void clearsAllWritableFormatsAndAllowsNewAttributesAndSubsequentSaves()
            throws IOException {
        for (File file : images()) {
            ExifInterface exif = new ExifInterface(file);
            exif.setAttribute(ExifInterface.TAG_ARTIST, PRIVATE_DATA);
            exif.setAttribute(ExifInterface.TAG_XMP, "<x:xmpmeta>" + PRIVATE_DATA + "</x:xmpmeta>");
            exif.saveAttributes();
            assertEquals(file.getName(), PRIVATE_DATA,
                    new ExifInterface(file).getAttribute(ExifInterface.TAG_ARTIST));

            exif.clearAttributes();
            assertNull(exif.getAttribute(ExifInterface.TAG_ARTIST));
            assertNull(exif.getAttribute(ExifInterface.TAG_XMP));
            assertFalse(exif.hasThumbnail());
            exif.setAttribute(ExifInterface.TAG_ARTIST, "new artist");
            exif.saveAttributes();
            assertEquals(file.getName(), "new artist",
                    new ExifInterface(file).getAttribute(ExifInterface.TAG_ARTIST));
            assertFalse(file.getName(), text(file).contains(PRIVATE_DATA));

            exif.clearAttributes();
            exif.saveAttributes();
            ExifInterface reopened = new ExifInterface(file);
            assertNull(file.getName(), reopened.getAttribute(ExifInterface.TAG_ARTIST));
            assertNull(file.getName(), reopened.getAttribute(ExifInterface.TAG_XMP));
            assertFalse(file.getName(), text(file).contains("new artist"));
            exif.setAttribute(ExifInterface.TAG_ARTIST, "edited again");
            exif.saveAttributes();
            assertEquals(file.getName(), "edited again",
                    new ExifInterface(file).getAttribute(ExifInterface.TAG_ARTIST));
        }
    }

    @Test
    public void nativeDecodersReturnTheSamePixelsAfterClearing() throws IOException {
        for (File file : images()) {
            if (file.getName().endsWith(".tiff") || file.getName().endsWith(".jxl")
                    || file.getName().endsWith(".jp2")) {
                continue;
            }
            Bitmap before = BitmapFactory.decodeFile(file.getAbsolutePath());
            assertNotNull(file.getName(), before);
            Bitmap after = null;
            try {
                ExifInterface exif = new ExifInterface(file);
                exif.setAttribute(ExifInterface.TAG_ARTIST, PRIVATE_DATA);
                exif.saveAttributes();
                exif.clearAttributes();
                exif.saveAttributes();
                after = BitmapFactory.decodeFile(file.getAbsolutePath());
                assertNotNull(file.getName(), after);
                assertEquals(before.getWidth(), after.getWidth());
                assertEquals(before.getHeight(), after.getHeight());
                assertArrayEquals(file.getName(), pixels(before), pixels(after));
            } finally {
                before.recycle();
                if (after != null) after.recycle();
            }
        }
    }

    @Test
    public void clearsWritableFileDescriptorsAndTruncatesTheirOldMetadataTail() throws IOException {
        for (File file : images()) {
            ExifInterface initial = new ExifInterface(file);
            initial.setAttribute(ExifInterface.TAG_ARTIST, PRIVATE_DATA);
            initial.saveAttributes();
            try (ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_WRITE)) {
                ExifInterface exif = new ExifInterface(descriptor.getFileDescriptor());
                exif.clearAttributes();
                exif.saveAttributes();
                assertTrue(descriptor.getFileDescriptor().valid());
                assertFalse(file.getName(), text(file).contains(PRIVATE_DATA));
                assertNull(new ExifInterface(file).getAttribute(ExifInterface.TAG_ARTIST));
            }
        }
    }

    @Test
    public void removesExifThumbnailAndOrientation() throws IOException {
        File file = bitmapFile("thumbnail.jpg", Bitmap.CompressFormat.JPEG);
        byte[] original = Files.readAllBytes(file.toPath());
        Bitmap thumbnail = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        byte[] thumbnailBytes;
        try {
            thumbnail.eraseColor(Color.MAGENTA);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
            thumbnailBytes = bytes.toByteArray();
        } finally {
            thumbnail.recycle();
        }
        byte[] artist = (PRIVATE_DATA + "\0").getBytes(StandardCharsets.US_ASCII);
        int thumbnailOffset = 80 + artist.length;
        ByteBuffer tiff = ByteBuffer.allocate(thumbnailOffset + thumbnailBytes.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        tiff.put(new byte[]{'I', 'I', 42, 0}).putInt(8).putShort((short) 2);
        tiff.putShort((short) 274).putShort((short) 3).putInt(1).putInt(6);
        tiff.putShort((short) 315).putShort((short) 2).putInt(artist.length).putInt(80);
        tiff.putInt(38).putShort((short) 3);
        tiff.putShort((short) 259).putShort((short) 3).putInt(1).putInt(6);
        tiff.putShort((short) 513).putShort((short) 4).putInt(1).putInt(thumbnailOffset);
        tiff.putShort((short) 514).putShort((short) 4).putInt(1).putInt(thumbnailBytes.length);
        tiff.putInt(0).put(artist).put(thumbnailBytes);
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(original, 0, 2);
            output.write(new byte[]{(byte) 0xff, (byte) 0xe1});
            int length = tiff.capacity() + 8;
            output.write(length >> 8);
            output.write(length);
            output.write(new byte[]{'E', 'x', 'i', 'f', 0, 0});
            output.write(tiff.array());
            output.write(original, 2, original.length - 2);
        }
        ExifInterface exif = new ExifInterface(file);
        assertTrue(exif.hasThumbnail());
        assertNotNull(exif.getThumbnail());
        assertEquals(90, exif.getRotationDegrees());
        exif.clearAttributes();
        assertNull(exif.getThumbnail());
        exif.saveAttributes();
        assertFalse(new ExifInterface(file).hasThumbnail());
        assertEquals(0, new ExifInterface(file).getRotationDegrees());
        assertArrayEquals(original, Files.readAllBytes(file.toPath()));
    }

    @Test
    public void failedCleanupLeavesTheOriginalFileIntact() throws IOException {
        File file = newFile("truncated.jpg");
        byte[] truncated = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe1, 0, 40, 1, 2};
        Files.write(file.toPath(), truncated);
        ExifInterface exif = new ExifInterface(file);
        exif.clearAttributes();
        assertThrows(IOException.class, exif::saveAttributes);
        assertArrayEquals(truncated, Files.readAllBytes(file.toPath()));
    }

    @Test
    public void inputStreamsRetainTheExistingSaveRestriction() throws IOException {
        File file = bitmapFile("stream.png", Bitmap.CompressFormat.PNG);
        ExifInterface exif = new ExifInterface(new ByteArrayInputStream(Files.readAllBytes(file.toPath())));
        exif.clearAttributes();
        assertThrows(IOException.class, exif::saveAttributes);
    }

    private List<File> images() throws IOException {
        List<File> images = new ArrayList<>();
        images.add(bitmapFile("image.jpg", Bitmap.CompressFormat.JPEG));
        images.add(bitmapFile("image.png", Bitmap.CompressFormat.PNG));
        images.add(bitmapFile("image.webp", Bitmap.CompressFormat.WEBP_LOSSLESS));
        // Small 16x12 gradients encoded with libheif, libjxl, Pillow/OpenJPEG and libtiff.
        for (String extension : new String[]{"heic", "avif", "jxl", "tiff", "jp2"}) {
            File file = newFile("image." + extension);
            try (java.io.InputStream input = InstrumentationRegistry.getInstrumentation()
                    .getContext().getAssets().open(file.getName());
                 FileOutputStream output = new FileOutputStream(file)) {
                byte[] bytes = new byte[1024];
                int count;
                while ((count = input.read(bytes)) != -1) output.write(bytes, 0, count);
            }
            images.add(file);
        }
        return images;
    }

    private File bitmapFile(String name, Bitmap.CompressFormat format) throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(16, 12, Bitmap.Config.ARGB_8888);
        try {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    bitmap.setPixel(x, y, Color.rgb(x * 16, y * 20, (x + y) * 9));
                }
            }
            File file = newFile(name);
            try (FileOutputStream output = new FileOutputStream(file)) {
                assertTrue(bitmap.compress(format, 95, output));
            }
            return file;
        } finally {
            bitmap.recycle();
        }
    }

    private File newFile(String name) {
        File file = new File(context.getCacheDir(), name);
        files.add(file);
        return file;
    }
}
