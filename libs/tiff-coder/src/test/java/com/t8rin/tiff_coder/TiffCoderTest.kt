package com.t8rin.tiff_coder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.io.path.createTempFile

class TiffCoderTest {

    @Test
    fun recognizesLittleAndBigEndianClassicTiff() {
        assertTrue(TiffCoder.isTiff(tiff(ByteOrder.LITTLE_ENDIAN, big = false)))
        assertTrue(TiffCoder.isTiff(tiff(ByteOrder.BIG_ENDIAN, big = false)))
    }

    @Test
    fun recognizesLittleAndBigEndianBigTiff() {
        assertTrue(TiffCoder.isTiff(tiff(ByteOrder.LITTLE_ENDIAN, big = true)))
        assertTrue(TiffCoder.isTiff(tiff(ByteOrder.BIG_ENDIAN, big = true)))
    }

    @Test
    fun rejectsTruncatedAndOutOfRangeHeaders() {
        val truncated = createTempFile(suffix = ".tiff").toFile().apply {
            writeBytes(byteArrayOf(0x49, 0x49, 0x2a, 0x00))
            deleteOnExit()
        }
        assertFalse(TiffCoder.isTiff(truncated))

        val invalid = tiff(ByteOrder.LITTLE_ENDIAN, big = false).apply {
            val bytes = readBytes()
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(4, Int.MAX_VALUE)
            writeBytes(bytes)
        }
        assertFalse(TiffCoder.isTiff(invalid))
    }

    private fun tiff(order: ByteOrder, big: Boolean) =
        createTempFile(suffix = ".tiff").toFile().apply {
            val bytes = ByteArray(32)
            val buffer = ByteBuffer.wrap(bytes).order(order)
            if (order == ByteOrder.LITTLE_ENDIAN) {
                bytes[0] = 0x49
                bytes[1] = 0x49
            } else {
                bytes[0] = 0x4d
                bytes[1] = 0x4d
            }
            if (big) {
                buffer.putShort(2, 43.toShort())
                buffer.putShort(4, 8.toShort())
                buffer.putShort(6, 0.toShort())
                buffer.putLong(8, 16L)
            } else {
                buffer.putShort(2, 42.toShort())
                buffer.putInt(4, 8)
            }
            writeBytes(bytes)
            deleteOnExit()
        }
}
