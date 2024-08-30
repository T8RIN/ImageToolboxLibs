package com.t8rin.psd.reader.parser.layer;

import com.t8rin.psd.reader.parser.PsdInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LayersSectionParser {

    private LayersSectionHandler handler;

    public LayersSectionParser() {
    }

    public void setHandler(LayersSectionHandler handler) {
        this.handler = handler;
    }

    public void parse(PsdInputStream stream) throws IOException {
        // read layer header info
        int length = stream.readInt();
        int pos = stream.getPos();

        if (length > 0) {
            int size = stream.readInt();
            if ((size & 0x01) != 0) {
                size++;
            }
            if (size > 0) {
                int layersCount = stream.readShort();
                if (layersCount < 0) {
                    layersCount = -layersCount;
                }

                List<LayerParser> parsers = new ArrayList<LayerParser>(layersCount);
                for (int i = 0; i < layersCount; i++) {
                    LayerParser layerParser = new LayerParser();
                    parsers.add(layerParser);
                    if (handler != null) {
                        handler.createLayer(layerParser);
                    }
                    layerParser.parse(stream);
                }

                for (LayerParser layerParser : parsers) {
                    layerParser.parseImageSection(stream);
                }
            }

            int maskSize = length - (stream.getPos() - pos);
            stream.skipBytes(maskSize);
        }
    }

}
