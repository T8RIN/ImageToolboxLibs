package com.t8rin.psd.reader.parser;

import com.t8rin.psd.reader.parser.header.HeaderSectionParser;
import com.t8rin.psd.reader.parser.imagedata.ImageDataSectionParser;
import com.t8rin.psd.reader.parser.imageresource.ImageResourceSectionParser;
import com.t8rin.psd.reader.parser.layer.LayersSectionParser;

import java.io.IOException;
import java.io.InputStream;

public class PsdFileParser {
    private final HeaderSectionParser headerParser;
    private final ColorModeSectionParser colorModeSectionParser;
    private final ImageResourceSectionParser imageResourceSectionParser;
    private final LayersSectionParser layersSectionParser;
    private final ImageDataSectionParser imageDataSectionParser;

    public PsdFileParser() {
        headerParser = new HeaderSectionParser();
        colorModeSectionParser = new ColorModeSectionParser();
        imageResourceSectionParser = new ImageResourceSectionParser();
        layersSectionParser = new LayersSectionParser();
        imageDataSectionParser = new ImageDataSectionParser(headerParser.getHeader());
    }

    public HeaderSectionParser getHeaderSectionParser() {
        return headerParser;
    }

    public ImageResourceSectionParser getImageResourceSectionParser() {
        return imageResourceSectionParser;
    }

    public LayersSectionParser getLayersSectionParser() {
        return layersSectionParser;
    }

    public ImageDataSectionParser getImageDataSectionParser() {
        return imageDataSectionParser;
    }

    public void parse(InputStream inputStream) throws IOException {
        PsdInputStream stream = new PsdInputStream(inputStream);
        headerParser.parse(stream);
        colorModeSectionParser.parse(stream);
        imageResourceSectionParser.parse(stream);
        layersSectionParser.parse(stream);
        imageDataSectionParser.parse(stream);
    }
}
