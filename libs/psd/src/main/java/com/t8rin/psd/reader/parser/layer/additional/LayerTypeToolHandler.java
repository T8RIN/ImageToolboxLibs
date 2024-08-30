package com.t8rin.psd.reader.parser.layer.additional;

import com.t8rin.psd.reader.parser.object.PsdDescriptor;

public interface LayerTypeToolHandler {

    void typeToolTransformParsed(Matrix transform);

    void typeToolDescriptorParsed(int version, PsdDescriptor descriptor);

}
