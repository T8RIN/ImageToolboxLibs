package com.t8rin.psd.reader.util;

public class RleLineUncompressor {

    public static void decodeRleLine(byte[] src, int srcIndex, int slen, byte[] dst, int dstIndex) {
        int sIndex = srcIndex;
        int dIndex = dstIndex;
        int max = sIndex + slen;
        while (sIndex < max) {
            byte b = src[sIndex++];
            int n = b;
            if (n < 0) {
                n = 1 - n;
                b = src[sIndex++];
                for (int i = 0; i < n; i++) {
                    dst[dIndex++] = b;
                }
            } else {
                n = n + 1;
                System.arraycopy(src, sIndex, dst, dIndex, n);
                dIndex += n;
                sIndex += n;
            }
        }
    }
}
