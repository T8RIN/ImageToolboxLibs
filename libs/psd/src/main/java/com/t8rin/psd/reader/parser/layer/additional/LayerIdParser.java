package com.t8rin.psd.reader.parser.layer.additional;

import com.t8rin.psd.reader.parser.PsdInputStream;
import com.t8rin.psd.reader.parser.layer.LayerAdditionalInformationParser;

import java.io.IOException;

public class LayerIdParser implements LayerAdditionalInformationParser {

    public static final String TAG = "lyid";
    private final LayerIdHandler handler;

    public LayerIdParser(LayerIdHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(PsdInputStream stream, String tag, int size) throws IOException {
        int layerId = stream.readInt();
        if (handler != null) {
            handler.layerIdParsed(layerId);
        }
    }

}
