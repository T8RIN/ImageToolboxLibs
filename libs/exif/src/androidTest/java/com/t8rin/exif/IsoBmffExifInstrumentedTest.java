package com.t8rin.exif;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.ParcelFileDescriptor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;

@RunWith(AndroidJUnit4.class)
public class IsoBmffExifInstrumentedTest {

    @Test
    public void androidReadsCaptureDateAndLocationAfterSaving() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        for (String name : new String[] {"image.heic", "image.avif"}) {
            File file = File.createTempFile("exif-compatibility-", name, context.getCacheDir());
            try {
                try (InputStream input = testContext.getAssets().open(name)) {
                    Files.copy(input, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                Bitmap original = BitmapFactory.decodeFile(file.getAbsolutePath());
                assertNotNull(name, original);
                try {
                    for (int save = 0; save < 2; save++) {
                        String date = save == 0 ? "2026:11:11 12:47:17" : "2026:11:12 13:48:18";
                        try (ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file,
                                ParcelFileDescriptor.MODE_READ_WRITE)) {
                            ExifInterface exif = new ExifInterface(descriptor.getFileDescriptor());
                            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, date);
                            exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, "+03:00");
                            exif.setLatLong(51.5, -0.125);
                            exif.saveAttributes();
                        }

                        // Read through Android's item table, bypassing our private UUID fallback.
                        byte[] bytes;
                        try (MediaMetadataRetriever reader = new MediaMetadataRetriever()) {
                            reader.setDataSource(file.getAbsolutePath());
                            String offset = reader.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_EXIF_OFFSET);
                            String length = reader.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_EXIF_LENGTH);
                            assertNotNull(name + "/EXIF offset", offset);
                            assertNotNull(name + "/EXIF length", length);
                            bytes = new byte[Integer.parseInt(length)];
                            try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
                                input.seek(Long.parseLong(offset));
                                input.readFully(bytes);
                            }
                        }
                        assertArrayEquals(new byte[] {'E', 'x', 'i', 'f', 0, 0},
                                java.util.Arrays.copyOf(bytes, 6));
                        ExifInterface extracted = new ExifInterface(new ByteArrayInputStream(bytes),
                                ExifInterface.STREAM_TYPE_EXIF_DATA_ONLY);
                        assertEquals(name, date,
                                extracted.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL));
                        assertEquals("+03:00",
                                extracted.getAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL));
                        assertArrayEquals(new double[] {51.5, -0.125}, extracted.getLatLong(), 0.000001);

                        Bitmap decoded = BitmapFactory.decodeFile(file.getAbsolutePath());
                        assertNotNull(name, decoded);
                        try {
                            assertEquals(original.getWidth(), decoded.getWidth());
                            assertEquals(original.getHeight(), decoded.getHeight());
                            assertArrayEquals(pixels(original), pixels(decoded));
                        } finally {
                            decoded.recycle();
                        }
                    }
                } finally {
                    original.recycle();
                }
            } finally {
                file.delete();
            }
        }
    }

    private static int[] pixels(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return pixels;
    }
}