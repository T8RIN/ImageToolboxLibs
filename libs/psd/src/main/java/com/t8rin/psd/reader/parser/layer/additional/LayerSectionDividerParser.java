package com.t8rin.psd.reader.parser.layer.additional;

import com.t8rin.psd.reader.parser.PsdInputStream;
import com.t8rin.psd.reader.parser.layer.LayerAdditionalInformationParser;
import com.t8rin.psd.reader.parser.layer.LayerType;

import java.io.IOException;

public class LayerSectionDividerParser implements LayerAdditionalInformationParser {

    public static final String TAG = "lsct";

    private final LayerSectionDividerHandler handler;

    public LayerSectionDividerParser(LayerSectionDividerHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(PsdInputStream stream, String tag, int size) throws IOException {
        int dividerType = stream.readInt();
        LayerType type = LayerType.NORMAL;
        switch (dividerType) {
            case 1:
            case 2:
                type = LayerType.FOLDER;
                break;
            case 3:
                type = LayerType.HIDDEN;
                break;
        }
        handler.sectionDividerParsed(type);
    }

}
