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

public class ImageResWriter implements WriterFace {
    @Override
    public byte[] toByte() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(0);
        return byteBuffer.array();
    }


    @Override
    public void writeBytes(BufferedOutputStream fileOutputStream) throws IOException {
        fileOutputStream.write(toByte());
    }
}
