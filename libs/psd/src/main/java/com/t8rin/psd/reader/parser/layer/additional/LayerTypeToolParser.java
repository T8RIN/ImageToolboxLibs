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

package com.t8rin.psd.reader.parser.layer.additional;

import com.t8rin.psd.reader.parser.PsdInputStream;
import com.t8rin.psd.reader.parser.layer.LayerAdditionalInformationParser;
import com.t8rin.psd.reader.parser.object.PsdDescriptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * contains meta data of a text layer like font type, font size, raw text ...)
 */
public class LayerTypeToolParser implements LayerAdditionalInformationParser {

    public static final String TAG = "TySh";

    private final LayerTypeToolHandler handler;

    public LayerTypeToolParser(LayerTypeToolHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(PsdInputStream stream, String tag, int size) throws IOException {
        byte[] data = new byte[size];
        stream.readBytes(data, size);

        PsdInputStream dataStream = new PsdInputStream(new ByteArrayInputStream(data));
        int version = dataStream.readShort();
        assert version == 1;
        Matrix transform = new Matrix(dataStream);
        if (handler != null) {
            handler.typeToolTransformParsed(transform);
        }

        int descriptorVersion = dataStream.readShort();
        if (descriptorVersion == 50) {
            int xTextDescriptorVersion = dataStream.readInt();
            PsdDescriptor descriptor = new PsdDescriptor(new PsdInputStream(dataStream));
            if (handler != null) {
                handler.typeToolDescriptorParsed(xTextDescriptorVersion, descriptor);
            }
        } else {
            // unknown data
            assert false;
        }
    }
}
