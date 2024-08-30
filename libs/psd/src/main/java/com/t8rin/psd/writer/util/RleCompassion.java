/*
 * Copyright 2018 ghbhaha. https://github.com/ghbhaha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.t8rin.psd.writer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RleCompassion {
    final static int MAX_LENGTH = 127;

    public static byte[] compassion(byte[] srcBytes) {

        byte[] dstBytes = new byte[srcBytes.length * 2];

        int index = 0;
        if (srcBytes.length == 0) {
            return null;
        }

        if (srcBytes.length == 1) {
            dstBytes[index++] = 0;
            dstBytes[index++] = srcBytes[0];
            return Arrays.copyOfRange(dstBytes, 0, index);
        }


        int pos = 0;
        List<Byte> buf = new ArrayList();
        int repeatCount = 0;

        // we can safely start with RAW as empty RAW sequences
        // are handled by finish_raw()
        int state = 0; //0 == RAW, 1 == RLE

        while (pos < srcBytes.length - 1) {
            byte currentByte = srcBytes[pos];
            if (srcBytes[pos] == srcBytes[pos + 1]) {
                if (state == 0) {
                    // end of RAW data
                    if (buf.size() != 0) {
                        dstBytes[index++] = (byte) (buf.size() - 1);
                        for (int i = 0; i < buf.size(); i++) {
                            dstBytes[index++] = buf.get(i);
                        }
                        buf.clear();
                    }
                    state = 1;
                    repeatCount = 1;
                } else if (state == 1) {
                    if (repeatCount == MAX_LENGTH) {
                        // restart the encoding
                        dstBytes[index++] = (byte) (256 - (repeatCount - 1));
                        dstBytes[index++] = srcBytes[pos];
                        repeatCount = 0;
                    }
                    // move to next byte
                    repeatCount++;
                }
            } else {
                if (state == 1) {
                    repeatCount++;
                    dstBytes[index++] = (byte) (256 - (repeatCount - 1));
                    dstBytes[index++] = srcBytes[pos];
                    state = 0;
                    repeatCount = 0;
                } else if (state == 0) {
                    if (buf.size() == MAX_LENGTH) {
                        // restart the encoding
                        if (buf.size() != 0) {
                            dstBytes[index++] = (byte) (buf.size() - 1);
                            for (int i = 0; i < buf.size(); i++) {
                                dstBytes[index++] = buf.get(i);
                            }
                            buf.clear();
                        }
                    }
                    buf.add(currentByte);
                }
            }
            pos++;
        }
        if (state == 0) {
            buf.add(srcBytes[pos]);
            if (buf.size() != 0) {
                dstBytes[index++] = (byte) (buf.size() - 1);
                for (int i = 0; i < buf.size(); i++) {
                    dstBytes[index++] = buf.get(i);
                }
                buf.clear();
            }
        } else {
            repeatCount++;
            dstBytes[index++] = (byte) (256 - (repeatCount - 1));
            dstBytes[index++] = srcBytes[pos];
        }


        return Arrays.copyOfRange(dstBytes, 0, index);
    }


}
