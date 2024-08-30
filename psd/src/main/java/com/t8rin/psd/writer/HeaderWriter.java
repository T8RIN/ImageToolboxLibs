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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HeaderWriter implements WriterFace {


    int width = 1080;
    int height = 1920;

    public HeaderWriter() {

    }

    public HeaderWriter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static void main(String[] args) {
        HeaderWriter headerWriter = new HeaderWriter();
        byte[] bytes = headerWriter.toByte();
        System.out.println(bytes);
    }

    @Override
    public byte[] toByte() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(26);
        //signature
        byteBuffer.put("8BPS".getBytes());
        //version
        byteBuffer.putShort((short) 1);
        //Reserved
        byteBuffer.put(new byte[6]);
        //Channels
        byteBuffer.putShort((short) 4);
        //Rows
        byteBuffer.putInt(height);
        //Columns
        byteBuffer.putInt(width);
        //Depth 1,8,16
        byteBuffer.putShort((short) 8);
        //Mode Bitmap=0; Grayscale=1; Indexed=2; RGB=3; CMYK=4; Multichannel=7; Duotone=8; Lab=9.
        byteBuffer.putShort((short) 3);
        return byteBuffer.array();
    }

    @Override
    public void writeBytes(BufferedOutputStream fileOutputStream) throws IOException {
        fileOutputStream.write(toByte());
    }
}
