package com.t8rin.psd.reader.parser.layer;

import com.t8rin.psd.reader.parser.BlendMode;
import com.t8rin.psd.reader.parser.PsdInputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayerParser {

    private final Map<String, LayerAdditionalInformationParser> additionalInformationParsers;
    private List<Channel> channels;
    private LayerHandler handler;
    private LayerAdditionalInformationParser defaultAdditionalInformationParser;

    public LayerParser() {
        handler = null;
        additionalInformationParsers = new HashMap<String, LayerAdditionalInformationParser>();
        defaultAdditionalInformationParser = null;
    }

    public void putAdditionalInformationParser(String tag, LayerAdditionalInformationParser parser) {
        additionalInformationParsers.put(tag, parser);
    }

    public void setDefaultAdditionalInformationParser(LayerAdditionalInformationParser parser) {
        defaultAdditionalInformationParser = parser;
    }

    public void setHandler(LayerHandler handler) {
        this.handler = handler;
    }

    public void parse(PsdInputStream stream) throws IOException {
        parseBounds(stream);
        parseChannelsInfo(stream);

        String tag = stream.readString(4);
        if (!tag.equals("8BIM")) {
            throw new IOException("format error");
        }
        parseBlendMode(stream);
        parseOpacity(stream);
        parseClipping(stream);
        parseFlags(stream);
        int filler = stream.readByte(); // filler. must be zero
        assert filler == 0;
        parseExtraData(stream);
    }

    public void fireBoundsChanged(int left, int top, int right, int bottom) {
        if (handler != null) {
            handler.boundsLoaded(left, top, right, bottom);
        }
    }

    public void fireChannelsLoaded(List<Channel> channels) {
        if (handler != null) {
            handler.channelsLoaded(channels);
        }
    }

    private void parseBounds(PsdInputStream stream) throws IOException {
        int top = stream.readInt();
        int left = stream.readInt();
        int bottom = stream.readInt();
        int right = stream.readInt();
        if (handler != null) {
            handler.boundsLoaded(left, top, right, bottom);
        }
    }

    private void parseChannelsInfo(PsdInputStream stream) throws IOException {
        int channelsCount = stream.readShort();
        channels = new ArrayList<Channel>();
        for (int j = 0; j < channelsCount; j++) {
            channels.add(new Channel(stream));
        }
    }

    private void parseBlendMode(PsdInputStream stream) throws IOException {
        String blendMode = stream.readString(4);
        if (handler != null) {
            handler.blendModeLoaded(BlendMode.getByName(blendMode));
        }
    }

    private void parseOpacity(PsdInputStream stream) throws IOException {
        int opacity = stream.readByte() & 0xff;
        if (handler != null) {
            handler.opacityLoaded(opacity);
        }
    }

    private void parseClipping(PsdInputStream stream) throws IOException {
        boolean clipping = stream.readBoolean();
        if (handler != null) {
            handler.clippingLoaded(clipping);
        }
    }

    private void parseFlags(PsdInputStream stream) throws IOException {
        int flags = stream.readByte();
        boolean transparencyProtected = (flags & 0x01) != 0;
        boolean visible = ((flags >> 1) & 0x01) == 0;
        boolean obsolete = ((flags >> 2) & 0x01) != 0;
        boolean isPixelDataIrrelevantValueUseful = ((flags >> 3) & 0x01) != 0;
        boolean pixelDataIrrelevant = false;
        if (isPixelDataIrrelevantValueUseful) { // tells if bit 4 has useful
            // information
            pixelDataIrrelevant = ((flags >> 4) & 0x01) != 0;
        }

        if (handler != null) {
            handler.flagsLoaded(transparencyProtected, visible, obsolete, isPixelDataIrrelevantValueUseful,
                    pixelDataIrrelevant);
        }
    }

    private void parseExtraData(PsdInputStream stream) throws IOException {
        int extraSize = stream.readInt();
        int extraPos = stream.getPos();

        parseMaskAndAdjustmentData(stream);
        parseBlendingRangesData(stream);
        parseName(stream);
        parseAdditionalSections(stream, extraSize + extraPos);

        int skip = extraSize - (stream.getPos() - extraPos);

        stream.skipBytes(skip);
    }

    private void parseMaskAndAdjustmentData(PsdInputStream stream) throws IOException {
        int size = stream.readInt();
        assert size == 0 || size == 20 || size == 36;
        if (size > 0) {
            Mask mask = new Mask();
            mask.top = stream.readInt();
            mask.left = stream.readInt();
            mask.bottom = stream.readInt();
            mask.right = stream.readInt();
            mask.defaultColor = stream.readByte() & 0xff;
            assert mask.defaultColor == 0 || mask.defaultColor == 255;

            byte flags = stream.readByte();
            mask.relative = (flags & 0x01) != 0;
            mask.disabled = ((flags >> 1) & 0x01) != 0;
            mask.invert = ((flags >> 2) & 0x01) != 0;
            if (size == 20) {
                stream.skipBytes(2);
            } else {
                byte realFlags = stream.readByte();
                mask.relative = (realFlags & 0x01) != 0;
                mask.disabled = ((realFlags >> 1) & 0x01) != 0;
                mask.invert = ((realFlags >> 2) & 0x01) != 0;

                mask.defaultColor = stream.readByte() & 0xff;
                assert mask.defaultColor == 0 || mask.defaultColor == 255;

                mask.top = stream.readInt();
                mask.left = stream.readInt();
                mask.bottom = stream.readInt();
                mask.right = stream.readInt();
            }
            if (handler != null) {
                handler.maskLoaded(mask);
            }
        }
    }

    private void parseBlendingRangesData(PsdInputStream stream) throws IOException {
        int size = stream.readInt();
        int pos = stream.getPos();
        BlendingRanges ranges = new BlendingRanges();

        // Composite gray blend source. Contains 2 black values followed by 2
        // white values. Present but irrelevant for Lab & Grayscale.
        ranges.grayBlackSrc = stream.readShort() & 0xffff;
        ranges.grayWhiteSrc = stream.readShort() & 0xffff;

        // Composite gray blend destination range
        ranges.grayBlackDst = stream.readShort() & 0xffff;
        ranges.grayWhiteDst = stream.readShort() & 0xffff;

        ranges.numberOfBlendingChannels = (size - 8) / 8;
        if (ranges.numberOfBlendingChannels > 0) {
            ranges.channelBlackSrc = new int[ranges.numberOfBlendingChannels];
            ranges.channelWhiteSrc = new int[ranges.numberOfBlendingChannels];
            ranges.channelBlackDst = new int[ranges.numberOfBlendingChannels];
            ranges.channelWhiteDst = new int[ranges.numberOfBlendingChannels];

            for (int i = 0; i < ranges.numberOfBlendingChannels; i++) {
                // channel source range
                ranges.channelBlackSrc[i] = stream.readShort() & 0xffff;
                ranges.channelWhiteSrc[i] = stream.readShort() & 0xffff;

                // channel destination range
                ranges.channelBlackDst[i] = stream.readShort() & 0xffff;
                ranges.channelWhiteDst[i] = stream.readShort() & 0xffff;
            }

            if (handler != null) {
                handler.blendingRangesLoaded(ranges);
            }
        } else {
            // invalid blending channels
            stream.skipBytes(size - (stream.getPos() - pos));
        }
    }

    private void parseName(PsdInputStream stream) throws IOException {
        // Layer name: Pascal string, padded to a multiple of 4 bytes.
        int size = stream.readByte() & 0xFF;
        size = ((size + 1 + 3) & ~0x03) - 1;
        byte[] str = new byte[size];
        int strSize = str.length;
        int readBytesCount = stream.read(str);
        assert readBytesCount == size;
        for (int i = 0; i < str.length; i++) {
            if (str[i] == 0) {
                strSize = i;
                break;
            }
        }
        String name = new String(str, 0, strSize, StandardCharsets.ISO_8859_1);

        if (handler != null) {
            handler.nameLoaded(name);
        }
    }

    private void parseAdditionalSections(PsdInputStream stream, int endPos) throws IOException {
        while (stream.getPos() < endPos) {
            String tag = stream.readString(4);
            if (!tag.equals("8BIM")) {
                throw new IOException("layer information signature error");
            }
            tag = stream.readString(4);

            int size = stream.readInt();
            size = (size + 1) & ~0x01;
            int prevPos = stream.getPos();

            LayerAdditionalInformationParser additionalParser = additionalInformationParsers.get(tag);
            if (additionalParser == null) {
                additionalParser = defaultAdditionalInformationParser;
            }

            if (additionalParser != null) {
                additionalParser.parse(stream, tag, size);
            }

            stream.skipBytes(prevPos + size - stream.getPos());
        }
    }

    public void parseImageSection(PsdInputStream stream) throws IOException {
        for (Channel channel : channels) {
            byte[] data = new byte[channel.getDataLength()];
            stream.readFully(data);
            channel.setCompressedData(data);
        }
        if (handler != null) {
            handler.channelsLoaded(channels);
        }
    }
}
