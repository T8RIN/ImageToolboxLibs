package com.t8rin.psd.reader.model;

public interface LayersContainer {
    Layer getLayer(int index);

    int indexOfLayer(Layer layer);

    int getLayersCount();
}
