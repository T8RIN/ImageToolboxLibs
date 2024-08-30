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

import java.io.IOException;

public class LayerMetaDataParser implements LayerAdditionalInformationParser {

    public static final String TAG = "shmd";
    private final LayerMetaDataHandler handler;

    public LayerMetaDataParser(LayerMetaDataHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(PsdInputStream stream, String tag, int size) throws IOException {
        int countOfMetaData = stream.readInt();
        for (int i = 0; i < countOfMetaData; i++) {
            String dataTag = stream.readString(4);
            if (!dataTag.equals("8BIM")) {
                throw new IOException("layer meta data section signature error");
            }
            String key = stream.readString(4);

            @SuppressWarnings("unused")
            int copyOnSheetDuplication = stream.readByte();

            stream.skipBytes(3); // padding
            int len = stream.readInt();
            int pos = stream.getPos();
            if (key.equals("mlst")) {
                parseMlstSection(stream);
            } else {
            }

            stream.skipBytes(len - (stream.getPos() - pos));
        }
    }

    private void parseMlstSection(PsdInputStream stream) throws IOException {
        stream.skipBytes(4); // ???
        PsdDescriptor descriptor = new PsdDescriptor(stream);
        if (handler != null) {
            handler.metaDataMlstSectionParsed(descriptor);
        }
    }

}
