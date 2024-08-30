/*
 * This file is part of java-psd-library.
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.t8rin.psd.reader.model;

import android.graphics.Bitmap;

import com.t8rin.psd.reader.parser.BlendMode;
import com.t8rin.psd.reader.parser.layer.BlendingRanges;
import com.t8rin.psd.reader.parser.layer.Channel;
import com.t8rin.psd.reader.parser.layer.LayerHandler;
import com.t8rin.psd.reader.parser.layer.LayerParser;
import com.t8rin.psd.reader.parser.layer.LayerType;
import com.t8rin.psd.reader.parser.layer.Mask;
import com.t8rin.psd.reader.parser.layer.additional.LayerSectionDividerHandler;
import com.t8rin.psd.reader.parser.layer.additional.LayerSectionDividerParser;
import com.t8rin.psd.reader.parser.layer.additional.LayerUnicodeNameHandler;
import com.t8rin.psd.reader.parser.layer.additional.LayerUnicodeNameParser;
import com.t8rin.psd.reader.util.BitmapBuilder;

import java.util.ArrayList;
import java.util.List;

public class Layer implements LayersContainer {
    private final ArrayList<Layer> layers = new ArrayList<Layer>();
    private int top = 0;
    private int left = 0;
    private int bottom = 0;
    private int right = 0;
    private int alpha = 255;
    private boolean visible = true;
    private String name;
    private Bitmap image;
    private LayerType type = LayerType.NORMAL;

    public Layer(LayerParser parser) {
        parser.setHandler(new LayerHandler() {
            @Override
            public void boundsLoaded(int left, int top, int right, int bottom) {
                Layer.this.left = left;
                Layer.this.top = top;
                Layer.this.right = right;
                Layer.this.bottom = bottom;
            }

            @Override
            public void blendModeLoaded(BlendMode blendMode) {
            }

            @Override
            public void blendingRangesLoaded(BlendingRanges ranges) {
            }

            @Override
            public void opacityLoaded(int opacity) {
                Layer.this.alpha = opacity;
            }

            @Override
            public void clippingLoaded(boolean clipping) {
            }

            @Override
            public void flagsLoaded(boolean transparencyProtected, boolean visible, boolean obsolete, boolean isPixelDataIrrelevantValueUseful, boolean pixelDataIrrelevant) {
                Layer.this.visible = visible;
            }

            @Override
            public void nameLoaded(String name) {
                Layer.this.name = name;
            }

            @Override
            public void channelsLoaded(List<Channel> channels) {
                BitmapBuilder imageBuilder = new BitmapBuilder(channels, getWidth(), getHeight());
                image = imageBuilder.makeImage();
            }

            @Override
            public void maskLoaded(Mask mask) {
            }

        });

        parser.putAdditionalInformationParser(LayerSectionDividerParser.TAG, new LayerSectionDividerParser(new LayerSectionDividerHandler() {
            @Override
            public void sectionDividerParsed(LayerType type) {
                Layer.this.type = type;
            }
        }));

        parser.putAdditionalInformationParser(LayerUnicodeNameParser.TAG, new LayerUnicodeNameParser(new LayerUnicodeNameHandler() {
            @Override
            public void layerUnicodeNameParsed(String unicodeName) {
                name = unicodeName;
            }
        }));
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
    }

    @Override
    public Layer getLayer(int index) {
        return layers.get(index);
    }

    @Override
    public int indexOfLayer(Layer layer) {
        return layers.indexOf(layer);
    }

    @Override
    public int getLayersCount() {
        return layers.size();
    }

    public Bitmap getImage() {
        return image;
    }

    public int getX() {
        return left;
    }

    public int getY() {
        return top;
    }

    public int getWidth() {
        return right - left;
    }

    public int getHeight() {
        return bottom - top;
    }

    public LayerType getType() {
        return type;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getAlpha() {
        return alpha;
    }


}
