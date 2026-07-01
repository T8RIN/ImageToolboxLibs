package com.jhlabs;

public class LightFilter extends WholeImageFilter {
    public static final int COLORS_FROM_IMAGE = 0, COLORS_CONSTANT = 1;
    public static final int BUMPS_FROM_IMAGE = 0, BUMPS_FROM_IMAGE_ALPHA = 1, BUMPS_FROM_MAP = 2, BUMPS_FROM_BEVEL = 3;
    private float bumpHeight = 1.0f, bumpSoftness = 5.0f, viewDistance = 10000.0f;
    private int colorSource, bumpSource, diffuseColor = 0xffffffff;
    private Light light = new DistantLight();

    public float getBumpHeight() {
        return bumpHeight;
    }

    public void setBumpHeight(float v) {
        bumpHeight = v;
    }

    public float getBumpSoftness() {
        return bumpSoftness;
    }

    public void setBumpSoftness(float v) {
        bumpSoftness = v;
    }

    public float getViewDistance() {
        return viewDistance;
    }

    public void setViewDistance(float v) {
        viewDistance = v;
    }

    public int getColorSource() {
        return colorSource;
    }

    public void setColorSource(int v) {
        colorSource = v;
    }

    public int getBumpSource() {
        return bumpSource;
    }

    public void setBumpSource(int v) {
        bumpSource = v;
    }

    public int getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(int v) {
        diffuseColor = v;
    }

    public Light getLight() {
        return light;
    }

    public void setLight(Light v) {
        light = v;
    }

    public String toString() {
        return "Stylize/Light Effects...";
    }

    public static class Light {
        private float azimuth = 4.712389f, elevation = 0.523599f, distance = 100.0f, intensity = 1.0f, coneAngle = 0.523599f, focus = 0.5f, centreX = 0.5f, centreY = 0.5f;
        private int color = 0xffffffff;

        public float getAzimuth() {
            return azimuth;
        }

        public void setAzimuth(float v) {
            azimuth = v;
        }

        public float getElevation() {
            return elevation;
        }

        public void setElevation(float v) {
            elevation = v;
        }

        public float getDistance() {
            return distance;
        }

        public void setDistance(float v) {
            distance = v;
        }

        public float getIntensity() {
            return intensity;
        }

        public void setIntensity(float v) {
            intensity = v;
        }

        public float getConeAngle() {
            return coneAngle;
        }

        public void setConeAngle(float v) {
            coneAngle = v;
        }

        public float getFocus() {
            return focus;
        }

        public void setFocus(float v) {
            focus = v;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int v) {
            color = v;
        }

        public float getCentreX() {
            return centreX;
        }

        public void setCentreX(float v) {
            centreX = v;
        }

        public float getCentreY() {
            return centreY;
        }

        public void setCentreY(float v) {
            centreY = v;
        }
    }

    public static class DistantLight extends Light {
    }

    public static class PointLight extends Light {
    }

    public static class SpotLight extends Light {
    }
}
