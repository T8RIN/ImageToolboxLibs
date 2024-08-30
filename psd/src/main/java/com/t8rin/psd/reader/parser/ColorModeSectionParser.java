package com.t8rin.psd.reader.parser;

import java.io.IOException;

public class ColorModeSectionParser {

    public void parse(PsdInputStream stream) throws IOException {
        int colorMapLength = stream.readInt();
        stream.skipBytes(colorMapLength);
    }

}
