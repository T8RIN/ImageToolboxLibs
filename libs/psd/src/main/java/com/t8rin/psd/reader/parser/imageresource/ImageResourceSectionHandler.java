package com.t8rin.psd.reader.parser.imageresource;

import com.t8rin.psd.reader.parser.object.PsdDescriptor;


public interface ImageResourceSectionHandler {
    void imageResourceManiSectionParsed(PsdDescriptor descriptor);
}
