package com.t8rin.psd.reader.util;

public class ChannelUncompressor {

    public static final int UNCOMPRESSED = 0;
    public static final int RLE = 1;
    public static final int ZIP_WITHOUT_PREDICTION = 2;
    public static final int ZIP_WITH_PREDICTION = 3;

    public ChannelUncompressor() {
    }

    public byte[] uncompress(byte[] srcData, int width, int height) {
        int compression = ((srcData[0] & 0xff) << 8) | (srcData[1] & 0xff);
        switch (compression) {
            case UNCOMPRESSED:
                return decodeUncompressedData(srcData, 2, width, height);
            case RLE:
                return decodeRleData(srcData, 2, width, height);
            default:
                throw new RuntimeException("Unsupported channel compression: " + compression);
        }
    }

    private byte[] decodeUncompressedData(byte[] data, int offset, int width, int height) {
        int size = width * height;
        byte[] b = new byte[size];
        System.arraycopy(data, offset, b, 0, size);
        return b;
    }

    private byte[] decodeRleData(byte[] data, int offset, int width, int height) {
        short[] lineLengths = new short[height];
        int srcPos = offset;
        for (int i = 0; i < height; i++) {
            lineLengths[i] = (short) (((data[srcPos] & 0xff) << 8) | (data[srcPos + 1] & 0xff));
            srcPos += 2;
        }

        byte[] b = new byte[width * height];
        int dstPos = 0;
        for (int i = 0; i < height; i++) {
            int len = lineLengths[i];
            RleLineUncompressor.decodeRleLine(data, srcPos, len, b, dstPos);
            srcPos += len;
            dstPos += width;
        }
        return b;
    }
}
