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

import com.t8rin.psd.reader.parser.ColorMode;
import com.t8rin.psd.reader.parser.PsdFileParser;
import com.t8rin.psd.reader.parser.header.Header;
import com.t8rin.psd.reader.parser.header.HeaderSectionHandler;
import com.t8rin.psd.reader.parser.imagedata.ImageDataSectionHandler;
import com.t8rin.psd.reader.parser.layer.LayerParser;
import com.t8rin.psd.reader.parser.layer.LayersSectionHandler;
import com.t8rin.psd.reader.util.BitmapBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Psd implements LayersContainer {
    private final Bitmap image;
    private final String name;
    private Header header;
    private List<Layer> layers = new ArrayList<Layer>();

    public Psd(File psdFile) throws IOException {
        name = psdFile.getName();
        final byte[][] channels = new byte[4][];

        PsdFileParser parser = new PsdFileParser();
        parser.getHeaderSectionParser().setHandler(new HeaderSectionHandler() {
            @Override
            public void headerLoaded(Header header) {
                Psd.this.header = header;
            }
        });

        final List<Layer> fullLayersList = new ArrayList<Layer>();
        parser.getLayersSectionParser().setHandler(new LayersSectionHandler() {
            @Override
            public void createLayer(LayerParser parser) {
                fullLayersList.add(new Layer(parser));
            }
        });

        parser.getImageDataSectionParser().setHandler(new ImageDataSectionHandler() {
            @Override
            public void channelLoaded(int channelId, byte[] channelData) {
                if (channelId >= 0 && channelId < 3) {
                    channels[channelId] = channelData;
                } else if (channelId == -1) {
                    channels[3] = channelData;
                }
            }
        });

        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(psdFile));
        parser.parse(stream);
        stream.close();

        layers = makeLayersHierarchy(fullLayersList);

        if (parser.getHeaderSectionParser().getHeader().getColorMode() == ColorMode.GRAYSCALE) {
            channels[1] = channels[2] = channels[0];
        }
        image = new BitmapBuilder(channels, header.getWidth(), header.getHeight()).makeImage();
    }

    private List<Layer> makeLayersHierarchy(List<Layer> layers) {
        LinkedList<LinkedList<Layer>> layersStack = new LinkedList<LinkedList<Layer>>();
        ArrayList<Layer> rootLayers = new ArrayList<Layer>();
        for (Layer layer : layers) {
            switch (layer.getType()) {
                case HIDDEN: {
                    layersStack.addFirst(new LinkedList<Layer>());
                    break;
                }
                case FOLDER: {
                    assert !layersStack.isEmpty();
                    LinkedList<Layer> folderLayers = layersStack.removeFirst();
                    for (Layer l : folderLayers) {
                        layer.addLayer(l);
                    }
                }
                // break isn't needed
                case NORMAL: {
                    if (layersStack.isEmpty()) {
                        rootLayers.add(layer);
                    } else {
                        layersStack.getFirst().add(layer);
                    }
                    break;
                }
                default:
                    assert false;
            }
        }
        return rootLayers;
    }

    public int getWidth() {
        return header.getWidth();
    }

    public int getHeight() {
        return header.getHeight();
    }

    public Bitmap getImage() {
        return image;
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

    @Override
    public String toString() {
        return name;
    }

    public List<Layer> getLayers() {
        return layers;
    }
}
