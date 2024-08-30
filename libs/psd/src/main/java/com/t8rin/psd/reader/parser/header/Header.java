package com.t8rin.psd.reader.parser.header;

import com.t8rin.psd.reader.parser.ColorMode;

public class Header {
    int channelsCount;
    int width;
    int height;
    int depth;
    ColorMode colorMode;

    public int getChannelsCount() {
        return channelsCount;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }
}
