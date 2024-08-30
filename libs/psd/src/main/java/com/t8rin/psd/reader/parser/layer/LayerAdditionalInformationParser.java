package com.t8rin.psd.reader.parser.layer;

import com.t8rin.psd.reader.parser.PsdInputStream;

import java.io.IOException;

public interface LayerAdditionalInformationParser {
    void parse(PsdInputStream stream, String tag, int size) throws IOException;
}
