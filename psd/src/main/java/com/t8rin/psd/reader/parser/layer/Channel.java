/*
 * This file is part of java-psd-library.
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.t8rin.psd.reader.parser.layer;

import com.t8rin.psd.reader.parser.PsdInputStream;

import java.io.IOException;

public class Channel {
    public static final int MASK = -2;
    public static final int ALPHA = -1;
    public static final int RED = 0;
    public static final int GREEN = 1;
    public static final int BLUE = 2;

    private final int id;
    private int dataLength;
    private byte[] compressedData;

    public Channel(PsdInputStream stream) throws IOException {
        id = stream.readShort();
        dataLength = stream.readInt();
    }

    public Channel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getDataLength() {
        return dataLength;
    }

    public byte[] getCompressedData() {
        return compressedData;
    }

    public void setCompressedData(byte[] data) {
        this.compressedData = data;
    }

}
