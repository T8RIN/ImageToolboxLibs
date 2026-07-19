package com.t8rin.tiff_coder

import android.graphics.Bitmap
import org.beyka.tiffbitmapfactory.TiffBitmapFactory
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object TiffCoder {

    @JvmStatic
    fun isTiff(file: File): Boolean = runCatching {
        if (!file.isFile || file.length() < CLASSIC_HEADER_SIZE) return@runCatching false
        RandomAccessFile(file, "r").use { input ->
            val header = ByteArray(BIG_TIFF_HEADER_SIZE)
            val read = input.read(header)
            if (read < CLASSIC_HEADER_SIZE) return@use false
            val order = when {
                header[0] == 0x49.toByte() && header[1] == 0x49.toByte() -> ByteOrder.LITTLE_ENDIAN
                header[0] == 0x4d.toByte() && header[1] == 0x4d.toByte() -> ByteOrder.BIG_ENDIAN
                else -> return@use false
            }
            val buffer = ByteBuffer.wrap(header).order(order)
            when (buffer.getShort(2).toInt() and 0xffff) {
                CLASSIC_MAGIC -> {
                    val offset = buffer.getInt(4).toLong() and 0xffff_ffffL
                    offset >= CLASSIC_HEADER_SIZE && offset < file.length()
                }

                BIG_TIFF_MAGIC -> {
                    if (read < BIG_TIFF_HEADER_SIZE ||
                        (buffer.getShort(4).toInt() and 0xffff) != 8 ||
                        buffer.getShort(6).toInt() != 0
                    ) return@use false
                    val offset = buffer.getLong(8)
                    offset >= BIG_TIFF_HEADER_SIZE && offset < file.length()
                }

                else -> false
            }
        }
    }.getOrDefault(false)

    @JvmStatic
    fun pageCount(file: File): Int = runCatching {
        RandomAccessFile(file, "r").use { input ->
            val order = readByteOrder(input) ?: return@use 0
            val magic = input.readUnsignedShort(order)
            val bigTiff = magic == BIG_TIFF_MAGIC
            if (magic != CLASSIC_MAGIC && !bigTiff) return@use 0
            val offset = if (bigTiff) {
                if (input.readUnsignedShort(order) != 8 ||
                    input.readUnsignedShort(order) != 0
                ) return@use 0
                input.readLong(order)
            } else {
                input.readUnsignedInt(order)
            }
            countDirectories(input, offset, bigTiff, order)
        }
    }.getOrDefault(0)

    @JvmStatic
    @JvmOverloads
    fun decode(
        file: File,
        page: Int = 0,
        sampleSize: Int = 1,
        applyOrientation: Boolean = true,
        config: TiffBitmapFactory.ImageConfig = TiffBitmapFactory.ImageConfig.ARGB_8888
    ): Bitmap? {
        if (!isTiff(file)) return null
        val safeSampleSize = sampleSize.coerceAtLeast(1).takeHighestOneBit()
        val options = TiffBitmapFactory.Options().apply {
            inDirectoryNumber = page.coerceAtLeast(0)
            inSampleSize = safeSampleSize
            inUseOrientationTag = applyOrientation
            inPreferredConfig = config
            inThrowException = false
            inAvailableMemory = MAX_DECODE_BYTES
        }
        return runCatching { TiffBitmapFactory.decodeFile(file, options) }.getOrNull()
    }

    const val MAX_DECODE_BYTES = 256L * 1024 * 1024

    private fun countDirectories(
        input: RandomAccessFile,
        firstOffset: Long,
        bigTiff: Boolean,
        order: ByteOrder
    ): Int {
        val visited = HashSet<Long>()
        var offset = firstOffset
        var count = 0
        while (offset != 0L && count < MAX_DIRECTORIES) {
            if (offset < 0 || offset >= input.length() || !visited.add(offset)) return 0
            input.seek(offset)
            val entries =
                if (bigTiff) input.readLong(order) else input.readUnsignedShort(order).toLong()
            if (entries !in 0..MAX_IFD_ENTRIES) return 0
            val entrySize = if (bigTiff) 20L else 12L
            val countSize = if (bigTiff) 8L else 2L
            val nextPosition =
                offset.safeAdd(countSize)?.safeAdd(entries.safeMultiply(entrySize) ?: return 0)
                    ?: return 0
            val nextSize = if (bigTiff) 8L else 4L
            if (nextPosition < 0 || nextPosition > input.length() - nextSize) return 0
            input.seek(nextPosition)
            offset = if (bigTiff) input.readLong(order) else input.readUnsignedInt(order)
            count++
        }
        return if (offset == 0L) count else 0
    }

    private fun readByteOrder(input: RandomAccessFile): ByteOrder? {
        if (input.length() < CLASSIC_HEADER_SIZE) return null
        input.seek(0)
        val first = input.read()
        val second = input.read()
        return when {
            first == 0x49 && second == 0x49 -> ByteOrder.LITTLE_ENDIAN
            first == 0x4d && second == 0x4d -> ByteOrder.BIG_ENDIAN
            else -> null
        }
    }

    private fun RandomAccessFile.readUnsignedShort(order: ByteOrder): Int {
        val first = read()
        val second = read()
        if (first < 0 || second < 0) throw java.io.EOFException()
        return if (order == ByteOrder.LITTLE_ENDIAN) first or (second shl 8)
        else (first shl 8) or second
    }

    private fun RandomAccessFile.readUnsignedInt(order: ByteOrder): Long {
        val bytes = ByteArray(4)
        readFully(bytes)
        return ByteBuffer.wrap(bytes).order(order).int.toLong() and 0xffff_ffffL
    }

    private fun RandomAccessFile.readLong(order: ByteOrder): Long {
        val bytes = ByteArray(8)
        readFully(bytes)
        return ByteBuffer.wrap(bytes).order(order).long
    }

    private fun Long.safeAdd(other: Long): Long? =
        if (other > Long.MAX_VALUE - this) null else this + other

    private fun Long.safeMultiply(other: Long): Long? =
        if (this != 0L && other > Long.MAX_VALUE / this) null else this * other

    private const val MAX_DIRECTORIES = 10_000
    private const val MAX_IFD_ENTRIES = 1_000_000L
    private const val CLASSIC_MAGIC = 42
    private const val BIG_TIFF_MAGIC = 43
    private const val CLASSIC_HEADER_SIZE = 8
    private const val BIG_TIFF_HEADER_SIZE = 16
}
