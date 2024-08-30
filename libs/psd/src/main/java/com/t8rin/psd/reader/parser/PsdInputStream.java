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

package com.t8rin.psd.reader.parser;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PsdInputStream extends InputStream {

    private final InputStream in;
    private int pos;
    private int markPos;

    public PsdInputStream(InputStream in) {
        this.in = in;
        pos = 0;
        markPos = 0;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        markPos = pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        pos = markPos;
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = in.read(b, off, len);
        if (res != -1) {
            pos += res;
        }
        return res;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int res = in.read(b);
        if (res != -1) {
            pos += res;
        }
        return res;
    }

    @Override
    public int read() throws IOException {
        int res = in.read();
        if (res != -1) {
            pos++;
        }
        return res;
    }

    @Override
    public long skip(long n) throws IOException {
        long skip = in.skip(n);
        pos += skip;
        return skip;
    }

    public String readString(int len) throws IOException {
        // read string of specified length
        byte[] bytes = new byte[len];
        read(bytes);
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public String readPsdString() throws IOException {
        int size = readInt();
        if (size == 0) {
            size = 4;
        }
        return readString(size);
    }

    public int readBytes(byte[] bytes, int n) throws IOException {
        // read multiple bytes from input
        if (bytes == null)
            return 0;
        int r = 0;
        r = read(bytes, 0, n);
        if (r < n) {
            throw new IOException("format error. readed=" + r + " needed=" + n);
        }
        return r;
    }

    public byte readByte() throws IOException {
        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte) (ch);
    }

    public short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    public int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public boolean readBoolean() throws IOException {
        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (ch != 0);
    }

    public final long readLong() throws IOException {
        int c1 = read();
        int c2 = read();
        int c3 = read();
        int c4 = read();
        int c5 = read();
        int c6 = read();
        int c7 = read();
        int c8 = read();
        return (((long) c1 << 56) + ((long) (c2 & 255) << 48) + ((long) (c3 & 255) << 40) + ((long) (c4 & 255) << 32)
                + ((long) (c5 & 255) << 24) + ((c6 & 255) << 16) + ((c7 & 255) << 8) + (c8 & 255));
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public int skipBytes(int n) throws IOException {
        int total = 0;
        int cur;
        while ((total < n) && ((cur = (int) skip(n - total)) > 0)) {
            total += cur;
        }
        return total;
    }

    public int getPos() {
        return pos;
    }

}
