package com.t8rin.psd.reader.parser.imagedata;

public interface ImageDataSectionHandler {
    void channelLoaded(int channelId, byte[] channelData);
}
