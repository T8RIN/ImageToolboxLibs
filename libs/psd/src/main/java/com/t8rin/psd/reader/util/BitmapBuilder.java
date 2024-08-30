package com.t8rin.psd.reader.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.t8rin.psd.reader.parser.layer.Channel;

import java.util.List;

public class BitmapBuilder {

    private final List<Channel> channels;
    private final byte[][] uncompressedChannels;
    private final int width;
    private final int height;
    private int opacity = -1;

    public BitmapBuilder(List<Channel> channels, int width, int height) {
        this.uncompressedChannels = null;
        this.channels = channels;
        this.width = width;
        this.height = height;
    }

    public BitmapBuilder(byte[][] channels, int width, int height) {
        this.uncompressedChannels = channels;
        this.channels = null;
        this.width = width;
        this.height = height;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public Bitmap makeImage() {
        if (width == 0 || height == 0) {
            return null;
        }
        byte[] rChannel = getChannelData(Channel.RED);
        byte[] gChannel = getChannelData(Channel.GREEN);
        byte[] bChannel = getChannelData(Channel.BLUE);
        byte[] aChannel = getChannelData(Channel.ALPHA);

        applyOpacity(aChannel);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] data = new int[width * height];
        int n = width * height - 1;
        while (n >= 0) {
            int a;

            if (aChannel == null) a = 255;
            else a = aChannel[n] & 0xff;

            int r = rChannel[n] & 0xff;
            int g = gChannel[n] & 0xff;
            int b = bChannel[n] & 0xff;
            data[n] = Color.argb(a, r, g, b);
            n--;
        }
        bitmap.setPixels(data, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private void applyOpacity(byte[] a) {
        if (opacity != -1 && a != null) {
            double o = (opacity & 0xff) / 256.0;
            for (int i = 0; i < a.length; i++) {
                a[i] = (byte) ((a[i] & 0xff) * o);
            }
        }
    }

    private byte[] getChannelData(int channelId) {
        if (uncompressedChannels == null) {
            for (Channel c : channels) {
                if (channelId == c.getId() && c.getCompressedData() != null) {
                    ChannelUncompressor uncompressor = new ChannelUncompressor();
                    byte[] uncompressedChannel = uncompressor.uncompress(c.getCompressedData(), width, height);
                    if (uncompressedChannel != null) {
                        return uncompressedChannel;
                    }
                }
            }
        } else {
            if (channelId >= 0 && uncompressedChannels[channelId] != null) {
                return uncompressedChannels[channelId];
            } else if (channelId == -1) {
                return uncompressedChannels[3];
            }
        }
        return fillBytes(width * height, (byte) (channelId == Channel.ALPHA ? 255 : 0));
    }

    private byte[] fillBytes(int size, byte value) {
        byte[] result = new byte[size];
        if (value != 0) {
            for (int i = 0; i < size; i++) {
                result[i] = value;
            }
        }
        return result;
    }

}
