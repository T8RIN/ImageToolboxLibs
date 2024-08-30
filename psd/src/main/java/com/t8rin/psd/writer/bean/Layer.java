/*
 * Copyright 2018 ghbhaha. https://github.com/ghbhaha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.t8rin.psd.writer.bean;

import android.graphics.Bitmap;

import com.t8rin.psd.reader.parser.layer.LayerType;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Layer {
    boolean transparencyProtected = true;
    boolean visible = true;
    boolean obsolete = false;
    boolean isPixelDataIrrelevantValueUseful = false;
    boolean pixelDataIrrelevant = false;
    ByteBuffer chanelInfo;
    private int top = 0;
    private int left = 0;
    private int bottom = 1920;
    private int right = 1080;
    private int alpha = 255;
    private String name;
    private Bitmap image;
    private LayerType type = LayerType.NORMAL;

    public Layer(Bitmap image) {
        this.image = image;
        this.name = UUID.randomUUID().toString();
    }

    public Layer() {

    }

    public int getWidth() {
        return getRight() - getLeft();
    }

    public int getHeight() {
        return getBottom() - getTop();
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public LayerType getType() {
        return type;
    }

    public void setType(LayerType type) {
        this.type = type;
    }

    public ByteBuffer getChanelInfo() {
        return chanelInfo;
    }

    public void setChanelInfo(ByteBuffer chanelInfo) {
        this.chanelInfo = chanelInfo;
    }

    public boolean isTransparencyProtected() {
        return transparencyProtected;
    }

    public void setTransparencyProtected(boolean transparencyProtected) {
        this.transparencyProtected = transparencyProtected;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public boolean isPixelDataIrrelevantValueUseful() {
        return isPixelDataIrrelevantValueUseful;
    }

    public void setPixelDataIrrelevantValueUseful(boolean pixelDataIrrelevantValueUseful) {
        isPixelDataIrrelevantValueUseful = pixelDataIrrelevantValueUseful;
    }

    public boolean isPixelDataIrrelevant() {
        return pixelDataIrrelevant;
    }

    public void setPixelDataIrrelevant(boolean pixelDataIrrelevant) {
        this.pixelDataIrrelevant = pixelDataIrrelevant;
    }
}
