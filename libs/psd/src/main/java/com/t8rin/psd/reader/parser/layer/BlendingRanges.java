package com.t8rin.psd.reader.parser.layer;

public class BlendingRanges {
    int grayBlackSrc;    // Composite gray blend source. Contains 2 black values followed by 2 white values. Present but irrelevant for Lab & Grayscale.
    int grayWhiteSrc;
    int grayBlackDst;    // Composite gray blend destination range
    int grayWhiteDst;
    int numberOfBlendingChannels;
    int[] channelBlackSrc;// channel source range
    int[] channelWhiteSrc;
    int[] channelBlackDst;// First channel destination range
    int[] channelWhiteDst;
}
