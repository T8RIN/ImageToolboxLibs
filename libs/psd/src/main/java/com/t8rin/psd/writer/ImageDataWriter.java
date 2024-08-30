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

package com.t8rin.psd.writer;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.t8rin.psd.writer.util.RleCompassion;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageDataWriter implements WriterFace {


    private final Bitmap bitmap;

    public ImageDataWriter(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    @Override
    public byte[] toByte() {
        int allSize = 0;
        List<byte[]> bytesList = new ArrayList<>();
        Bitmap bimg = bitmap;

        int[] rgbas = new int[bimg.getWidth() * bimg.getHeight()];
        for (int i = 0; i < bimg.getHeight(); i++) {
            for (int j = 0; j < bimg.getWidth(); j++) {
                rgbas[i * bimg.getWidth() + j] = bimg.getPixel(j, i);
            }
        }


        ByteBuffer lengthBuffer = ByteBuffer.allocate(2 * 4 * bimg.getHeight());
        int compassion = 1;
        for (int j = 0; j < 4; j++) {
            byte[] chanelData = new byte[rgbas.length];
            for (int index = 0; index < rgbas.length; index++) {
                Color color = Color.valueOf(rgbas[index]);
                if (j == 0) {
                    chanelData[index] = (byte) color.red();
                } else if (j == 1) {
                    chanelData[index] = (byte) color.green();
                } else if (j == 2) {
                    chanelData[index] = (byte) color.blue();
                } else if (j == 3) {
                    chanelData[index] = (byte) color.alpha();
                }
                //  chanelData[index] = chanelData[index] == 0? (byte) 255 : chanelData[index];
            }
            if (compassion == 1) {
                for (int i = 0; i < bimg.getHeight(); i++) {
                    byte[] colors = RleCompassion.compassion(Arrays.copyOfRange(chanelData, i * bimg.getWidth(), (i + 1) * bimg.getWidth()));
                    lengthBuffer.putShort((short) colors.length);
                    bytesList.add(colors);
                }
            } else {
                bytesList.add(chanelData);
            }
        }

        if (compassion == 1) {
            bytesList.add(0, lengthBuffer.array());
        }

        for (byte[] bytes : bytesList) {
            allSize += bytes.length;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + allSize);
        byteBuffer.putShort((short) compassion);

        for (byte[] bytes : bytesList) {
            byteBuffer.put(bytes);
        }

        bytesList.clear();
        return byteBuffer.array();
    }


    @Override
    public void writeBytes(BufferedOutputStream fileOutputStream) throws IOException {
        fileOutputStream.write(toByte());
    }
}
