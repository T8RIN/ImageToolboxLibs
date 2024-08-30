package com.t8rin.psd.reader.parser.layer;

import com.t8rin.psd.reader.parser.BlendMode;

import java.util.List;

public interface LayerHandler {
    void boundsLoaded(int left, int top, int right, int bottom);

    void blendModeLoaded(BlendMode blendMode);

    void opacityLoaded(int opacity);

    void clippingLoaded(boolean clipping);

    void flagsLoaded(boolean transparencyProtected, boolean visible, boolean obsolete,
                     boolean isPixelDataIrrelevantValueUseful, boolean pixelDataIrrelevant);

    void nameLoaded(String name);

    void channelsLoaded(List<Channel> channels);

    void maskLoaded(Mask mask);

    void blendingRangesLoaded(BlendingRanges ranges);
}
