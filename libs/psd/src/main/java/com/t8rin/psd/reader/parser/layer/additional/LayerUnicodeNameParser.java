package com.t8rin.psd.reader.parser.layer.additional;

import com.t8rin.psd.reader.parser.PsdInputStream;
import com.t8rin.psd.reader.parser.layer.LayerAdditionalInformationParser;

import java.io.IOException;

public class LayerUnicodeNameParser implements LayerAdditionalInformationParser {

    public static final String TAG = "luni";
    private final LayerUnicodeNameHandler handler;

    public LayerUnicodeNameParser(LayerUnicodeNameHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(PsdInputStream stream, String tag, int size) throws IOException {
        int len = stream.readInt();
        StringBuilder name = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            name.append((char) stream.readShort());
        }
        if (handler != null) {
            handler.layerUnicodeNameParsed(name.toString());
        }
    }
}
